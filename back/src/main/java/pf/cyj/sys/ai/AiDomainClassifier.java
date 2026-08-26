package pf.cyj.sys.ai;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import pf.cyj.sys.entity.AnlCol;
import pf.cyj.sys.entity.DmnCd;
import pf.cyj.sys.exception.ExternalApiException;

/**
 * 컬럼 메타데이터를 외부 AI API(OpenAI Chat Completions 형식)에 보내 표준 도메인 후보 Top-N 을 판별한다.
 *
 * <p>원본 솔루션은 사내망에 상시 기동된 Python gRPC 분류 모델을 호출했지만, 이 포트폴리오에서는
 * 별도로 학습된 분류 모델을 둘 형편이 안 되므로 범용 LLM 에게 "컬럼 메타 + 후보 도메인 목록"을
 * 프롬프트로 주고 JSON 형식으로만 답하도록 강제하는 방식으로 같은 역할을 재현한다.
 * AI가 후보 목록 밖의 코드를 만들어내는(hallucination) 상황을 막기 위해 프롬프트에 후보 목록을
 * 명시하고, 응답을 파싱한 뒤에도 DOMAIN_CODE 마스터에 실제로 존재하는 코드만 남긴다(방어적 필터링).
 *
 * <p>호출 실패(네트워크 오류, 4xx/5xx, 응답 파싱 실패, 유효한 후보 0개)는 전부
 * {@link ExternalApiException} 으로 변환한다 - "우리 로직 문제"가 아니라 "외부 API 문제"임을
 * 호출부(DmnPdtService)와 로그에서 바로 구분하기 위함이다(ANALYSIS_JOB_LOG 의 CALL_ERROR와 같은 맥락).
 */
@Component
public class AiDomainClassifier {

    /** 화면/DB 부담을 고려해 한 번의 판별 요청당 저장할 최대 후보 수 */
    private static final int MAX_CANDIDATES = 3;

    private static final String SYSTEM_PROMPT =
            "너는 데이터 거버넌스팀의 표준 도메인 분류기다. "
                    + "주어진 컬럼의 메타데이터(물리명/한글명/영문명/데이터타입/특성)를 보고, "
                    + "반드시 주어진 [표준 도메인 후보 목록] 안에서만 골라 가장 유력한 순서대로 최대 "
                    + MAX_CANDIDATES + "개까지 추천하라. "
                    + "다른 설명 없이 반드시 아래 JSON 형식으로만 답하라: "
                    + "{\"candidates\":[{\"domainCode\":\"<후보 목록에 있는 코드>\",\"probability\":<0과 1 사이 소수>}]}. "
                    + "probability 는 확률(0~1)이며 candidates 는 확률이 높은 순으로 정렬한다. "
                    + "후보 목록에 없는 domainCode 는 절대 반환하지 마라.";

    private final RestClient restClient;
    private final String modelName;

    public AiDomainClassifier(RestClient.Builder restClientBuilder,
                               @Value("${ai.api-base-url}") String apiBaseUrl,
                               @Value("${ai.api-key}") String apiKey,
                               @Value("${ai.model-name}") String modelName) {
        this.restClient = restClientBuilder
                .baseUrl(apiBaseUrl)
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();
        this.modelName = modelName;
    }

    /** 컬럼 1개를 판별해 확률 내림차순으로 정렬된 후보 목록(최대 {@value #MAX_CANDIDATES}개)을 반환한다. */
    public List<AiPredictionCandidate> classify(AnlCol col, List<DmnCd> candidateDomains) {
        String requestBody = buildRequestBody(col, candidateDomains);

        String rawResponse;
        long start = System.currentTimeMillis();
        try {
            rawResponse = restClient.post()
                    .uri("/chat/completions")
                    .body(requestBody)
                    .retrieve()
                    .body(String.class);
        } catch (RestClientException e) {
            // 4xx/5xx 응답, 타임아웃, 커넥션 실패 등을 전부 여기서 포착한다.
            throw new ExternalApiException("AI_CALL_ERROR", "외부 AI API 호출에 실패했습니다: " + e.getMessage());
        }
        int responseMs = (int) (System.currentTimeMillis() - start);

        return parseCandidates(rawResponse, responseMs, candidateDomains);
    }

    private String buildRequestBody(AnlCol col, List<DmnCd> candidateDomains) {
        JsonObject root = new JsonObject();
        root.addProperty("model", modelName);
        root.addProperty("temperature", 0);

        JsonArray messages = new JsonArray();
        messages.add(chatMessage("system", SYSTEM_PROMPT));
        messages.add(chatMessage("user", buildUserPrompt(col, candidateDomains)));
        root.add("messages", messages);

        // OpenAI Chat Completions 의 JSON 강제 출력 옵션 - 응답이 항상 파싱 가능한 JSON 문자열이 되도록 한다.
        JsonObject responseFormat = new JsonObject();
        responseFormat.addProperty("type", "json_object");
        root.add("response_format", responseFormat);

        return root.toString();
    }

    private JsonObject chatMessage(String role, String content) {
        JsonObject message = new JsonObject();
        message.addProperty("role", role);
        message.addProperty("content", content);
        return message;
    }

    private String buildUserPrompt(AnlCol col, List<DmnCd> candidateDomains) {
        StringBuilder sb = new StringBuilder();
        sb.append("[분석 대상 컬럼]\n");
        sb.append("물리 컬럼명: ").append(col.getColumnName()).append('\n');
        sb.append("한글 논리명: ").append(orDash(col.getColumnNameKo())).append('\n');
        sb.append("영문 논리명: ").append(orDash(col.getColumnNameEn())).append('\n');
        sb.append("데이터 타입: ").append(orDash(col.getDataType()));
        if (col.getDataLength() != null) {
            sb.append('(').append(col.getDataLength());
            if (col.getDataScale() != null) {
                sb.append(',').append(col.getDataScale());
            }
            sb.append(')');
        }
        sb.append('\n');
        sb.append("숫자 컬럼 여부: ").append(col.isNumericYn() ? "Y" : "N").append('\n');
        sb.append("날짜 컬럼 여부: ").append(col.isDateYn() ? "Y" : "N").append('\n');
        sb.append("유일값 컬럼 여부: ").append(col.isUniqueYn() ? "Y" : "N").append("\n\n");

        sb.append("[표준 도메인 후보 목록 - 이 중에서만 선택할 것]\n");
        for (DmnCd domain : candidateDomains) {
            sb.append("- ").append(domain.getDomainCode()).append(": ").append(domain.getDomainNameKo()).append('\n');
        }
        return sb.toString();
    }

    private String orDash(String value) {
        return (value == null || value.isBlank()) ? "-" : value;
    }

    private List<AiPredictionCandidate> parseCandidates(String rawResponse, int responseMs, List<DmnCd> candidateDomains) {
        Set<String> validCodes = candidateDomains.stream().map(DmnCd::getDomainCode).collect(Collectors.toSet());

        try {
            JsonObject responseJson = JsonParser.parseString(rawResponse).getAsJsonObject();
            String modelVersion = responseJson.has("model") ? responseJson.get("model").getAsString() : modelName;
            String content = responseJson
                    .getAsJsonArray("choices").get(0).getAsJsonObject()
                    .getAsJsonObject("message").get("content").getAsString();

            JsonObject contentJson = JsonParser.parseString(content).getAsJsonObject();
            JsonArray candidatesJson = contentJson.getAsJsonArray("candidates");

            List<AiPredictionCandidate> candidates = new ArrayList<>();
            for (JsonElement element : candidatesJson) {
                JsonObject c = element.getAsJsonObject();
                String domainCode = c.get("domainCode").getAsString();
                if (!validCodes.contains(domainCode)) {
                    continue; // AI가 후보 목록 밖의 코드를 반환한 경우 방어적으로 제외(hallucination 대비)
                }
                BigDecimal probability = c.get("probability").getAsBigDecimal();
                candidates.add(new AiPredictionCandidate(domainCode, probability, modelName, modelVersion, responseMs));
            }

            candidates.sort(Comparator.comparing(AiPredictionCandidate::getProbability).reversed());

            if (candidates.isEmpty()) {
                throw new ExternalApiException("AI_INVALID_RESPONSE", "AI가 유효한 표준 도메인 후보를 반환하지 않았습니다.");
            }
            return candidates.size() > MAX_CANDIDATES ? candidates.subList(0, MAX_CANDIDATES) : candidates;

        } catch (ExternalApiException e) {
            throw e;
        } catch (RuntimeException e) {
            // JsonSyntaxException, IllegalStateException(getAsJsonObject 등), NullPointerException 등
            // "AI 응답이 우리가 기대한 형식이 아닌" 모든 경우를 여기서 포착한다.
            throw new ExternalApiException("AI_INVALID_RESPONSE", "AI 응답을 해석할 수 없습니다: " + e.getMessage());
        }
    }
}
