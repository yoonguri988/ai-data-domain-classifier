package pf.cyj.sys.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** 공통코드 상세(COMMON_CODE) 등록/수정(upsert) 요청 - (codeGroup, codeValue) 가 이미 있으면 나머지 값만 갱신된다 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CmnCdSaveReq {

    @Schema(description = "코드 그룹 (미리 등록된 그룹이어야 함)", example = "JOB_STAT_CD")
    @NotBlank(message = "코드 그룹은 필수입니다.")
    @Size(max = 30)
    private String codeGroup;

    @Schema(description = "코드값", example = "RUNNING")
    @NotBlank(message = "코드값은 필수입니다.")
    @Size(max = 30)
    private String codeValue;

    @Schema(description = "코드명", example = "실행중")
    @NotBlank(message = "코드명은 필수입니다.")
    @Size(max = 100)
    private String codeName;

    @Schema(description = "정렬 순서 (오름차순)", example = "1")
    @NotNull
    private Integer sortOrder;

    @Schema(description = "사용 여부", example = "true")
    @NotNull
    private Boolean useYn;
}
