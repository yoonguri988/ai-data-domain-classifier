package pf.cyj.sys.dto.response;

import java.time.LocalDateTime;
import pf.cyj.sys.entity.StdDmn;

/** 확정된 표준 도메인(확정본) 응답 */
public record StdDmnRsp(
        Long columnId,
        String columnName,
        String domainCode,
        String domainNameKo,
        Integer versionNo,
        String confirmedByName,
        LocalDateTime confirmedAt
) {
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
