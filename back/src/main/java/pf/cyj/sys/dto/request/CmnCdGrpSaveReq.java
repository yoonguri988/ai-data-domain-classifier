package pf.cyj.sys.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** 공통코드 그룹(COMMON_CODE_GROUP) 등록/수정(upsert) 요청 - codeGroup 이 이미 있으면 groupName 만 갱신된다 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CmnCdGrpSaveReq {

    @Schema(description = "코드 그룹 (PK)", example = "JOB_STAT_CD")
    @NotBlank(message = "코드 그룹은 필수입니다.")
    @Size(max = 30)
    private String codeGroup;

    @Schema(description = "코드 그룹명", example = "배치작업 상태")
    @NotBlank(message = "그룹명은 필수입니다.")
    @Size(max = 100)
    private String groupName;
}
