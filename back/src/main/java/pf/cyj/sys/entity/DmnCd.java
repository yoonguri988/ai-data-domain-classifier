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

/** DOMAIN_CODE - 표준 도메인 후보 마스터 (AI 분류 클래스 집합: ID/AMOUNT/DATE/CONTENT/NAME/NUMBER/COUNT/CONTACT/RATE/CODE/FLAG) */
@Entity
@Table(name = "DOMAIN_CODE")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DmnCd {

    @Id
    @Column(name = "DOMAIN_CODE", length = 20)
    private String domainCode;

    @Column(name = "DOMAIN_NAME_KO", nullable = false, length = 50)
    private String domainNameKo;

    @Column(name = "SORT_ORDER")
    @Builder.Default
    private Integer sortOrder = 0;
}
