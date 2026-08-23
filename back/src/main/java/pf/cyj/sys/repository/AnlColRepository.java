package pf.cyj.sys.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import pf.cyj.sys.entity.AnlCol;

public interface AnlColRepository extends JpaRepository<AnlCol, Long> {

    List<AnlCol> findByAnlDset_DatasetId(String datasetId);
}
