package pf.cyj.sys.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** COMMON_CODE_GROUP - 공통코드 그룹 */
@Entity
@Table(name = "COMMON_CODE_GROUP")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CmnCdGrp {

    @Id
    @Column(name = "CODE_GROUP", length = 30)
    private String codeGroup;

    @Column(name = "GROUP_NAME", nullable = false, length = 100)
    private String groupName;
}
