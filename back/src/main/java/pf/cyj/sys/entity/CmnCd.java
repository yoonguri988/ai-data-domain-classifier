package pf.cyj.sys.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import pf.cyj.sys.entity.converter.YnConverter;

/** COMMON_CODE - 공통코드 상세 */
@Entity
@Table(name = "COMMON_CODE")
@IdClass(CmnCdId.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CmnCd {

    @Id
    @Column(name = "CODE_GROUP", length = 30)
    private String codeGroup;

    @Id
    @Column(name = "CODE_VALUE", length = 30)
    private String codeValue;

    @Column(name = "CODE_NAME", nullable = false, length = 100)
    private String codeName;

    @Column(name = "SORT_ORDER")
    @Builder.Default
    private Integer sortOrder = 0;

    @Convert(converter = YnConverter.class)
    @Column(name = "USE_YN", nullable = false, length = 1, columnDefinition = "char(1)")
    @Builder.Default
    private boolean useYn = true;
}
