package pf.cyj.sys.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

/**
 * RestClient.Builder Bean.
 * 외부 AI API 연동(AiDomainClassifier)이 붙는 다음 단계에서, 이 Builder 를 주입받아
 * baseUrl/헤더가 세팅된 전용 RestClient 를 구성할 예정이다.
 */
@Configuration
public class RestClientConfig {

    @Bean
    public RestClient.Builder restClientBuilder() {
        return RestClient.builder();
    }
}
