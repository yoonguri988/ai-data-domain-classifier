package pf.cyj.sys.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import pf.cyj.sys.entity.AnlDset;
import pf.cyj.sys.entity.type.DsetStatCd;

public interface AnlDsetRepository extends JpaRepository<AnlDset, String> {

    boolean existsByRequestNo(String requestNo);

    List<AnlDset> findByDatasetStatus(DsetStatCd datasetStatus);

    List<AnlDset> findByRequestedBy_UserId(Long userId);
}
