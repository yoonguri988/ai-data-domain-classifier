package pf.cyj.sys.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import pf.cyj.sys.entity.StdDmn;

/** 확정된 표준 도메인(확정본, STANDARD_DOMAIN) 응답 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class StdDmnRsp {

    @Schema(description = "컬럼 ID (확정본은 컬럼과 PK 를 공유한다)", example = "1")
    private Long columnId;

    @Schema(description = "컬럼명", example = "PHONE_NO")
    private String columnName;

    @Schema(description = "확정된 표준 도메인 코드", example = "CONTACT")
    private String domainCode;

    @Schema(description = "확정된 표준 도메인 한글명", example = "연락처")
    private String domainNameKo;

    @Schema(description = "확정 버전 번호 (재확정될 때마다 증가)", example = "1")
    private Integer versionNo;

    @Schema(description = "확정 처리자 이름", example = "관리자")
    private String confirmedByName;

    @Schema(description = "확정 일시", example = "2026-08-26T10:00:00")
    private LocalDateTime confirmedAt;

    public static StdDmnRsp from(StdDmn std) {
        return new StdDmnRsp(
                std.getColumnId(),
                std.getAnlCol() != null ? std.getAnlCol().getColumnName() : null,
                std.getDmnCd() != null ? std.getDmnCd().getDomainCode() : null,
                std.getDmnCd() != null ? std.getDmnCd().getDomainNameKo() : null,
                std.getVersionNo(),
                std.getConfirmedBy() != null ? std.getConfirmedBy().getUserName() : null,
                std.getConfirmedAt()
        );
    }
}
