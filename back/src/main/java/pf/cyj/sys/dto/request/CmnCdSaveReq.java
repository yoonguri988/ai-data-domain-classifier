package pf.cyj.sys.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** 공통코드 상세 등록/수정 요청 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CmnCdSaveReq {

    @NotBlank(message = "코드 그룹은 필수입니다.")
    @Size(max = 30)
    private String codeGroup;

    @NotBlank(message = "코드값은 필수입니다.")
    @Size(max = 30)
    private String codeValue;

    @NotBlank(message = "코드명은 필수입니다.")
    @Size(max = 100)
    private String codeName;

    @NotNull
    private Integer sortOrder;

    @NotNull
    private Boolean useYn;
}
