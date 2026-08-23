package pf.cyj.sys.entity;

import jakarta.persistence.Column;
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

/** REPORT_EXPORT_LOG - PDF 판별결과 리포트 출력이력 (Apache PDFBox) */
@Entity
@Table(name = "REPORT_EXPORT_LOG")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RptExpLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "EXPORT_ID")
    private Long exportId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "DATASET_ID", nullable = false)
    private AnlDset anlDset;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "EXPORTED_BY", nullable = false)
    private AppUsr exportedBy;

    @Column(name = "FILE_NAME", nullable = false, length = 200)
    private String fileName;

    @CreationTimestamp
    @Column(name = "EXPORTED_AT", nullable = false, updatable = false)
    private LocalDateTime exportedAt;
}
