package pf.cyj.sys.entity;

import java.io.Serializable;
import java.util.Objects;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** COMMON_CODE 복합키 (CODE_GROUP + CODE_VALUE) - CmnCd 의 @IdClass 로 사용 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CmnCdId implements Serializable {

    private String codeGroup;
    private String codeValue;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CmnCdId that)) return false;
        return Objects.equals(codeGroup, that.codeGroup) && Objects.equals(codeValue, that.codeValue);
    }

    @Override
    public int hashCode() {
        return Objects.hash(codeGroup, codeValue);
    }
}
