package pf.cyj.sys.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import pf.cyj.sys.entity.converter.YnConverter;

/** ANALYSIS_COLUMN - 분석 대상 컬럼 메타데이터 (AI 판별 입력 피처) */
@Entity
@Table(name = "ANALYSIS_COLUMN")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AnlCol {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "COLUMN_ID")
    private Long columnId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "DATASET_ID", nullable = false)
    private AnlDset anlDset;

    /** 물리 컬럼명 */
    @Column(name = "COLUMN_NAME", nullable = false, length = 100)
    private String columnName;

    /** 한글 논리명 (형태소분석 입력) */
    @Column(name = "COLUMN_NAME_KO", length = 200)
    private String columnNameKo;

    /** 영문 논리명 (토큰분리 입력) */
    @Column(name = "COLUMN_NAME_EN", length = 200)
    private String columnNameEn;

    @Column(name = "DATA_TYPE", length = 30)
    private String dataType;

    @Column(name = "DATA_LENGTH")
    private Integer dataLength;

    @Column(name = "DATA_SCALE")
    private Integer dataScale;

    @Convert(converter = YnConverter.class)
    @Column(name = "IS_NUMERIC_YN", nullable = false, length = 1, columnDefinition = "char(1)")
    @Builder.Default
    private boolean numericYn = false;

    @Convert(converter = YnConverter.class)
    @Column(name = "IS_DATE_YN", nullable = false, length = 1, columnDefinition = "char(1)")
    @Builder.Default
    private boolean dateYn = false;

    @Convert(converter = YnConverter.class)
    @Column(name = "IS_UNIQUE_YN", nullable = false, length = 1, columnDefinition = "char(1)")
    @Builder.Default
    private boolean uniqueYn = false;

    @CreationTimestamp
    @Column(name = "CREATED_AT", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
