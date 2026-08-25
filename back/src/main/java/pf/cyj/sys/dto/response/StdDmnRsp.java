package pf.cyj.sys.dto.response;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import pf.cyj.sys.entity.StdDmn;

/** 확정된 표준 도메인(확정본) 응답 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class StdDmnRsp {

    private Long columnId;
    private String columnName;
    private String domainCode;
    private String domainNameKo;
    private Integer versionNo;
    private String confirmedByName;
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
