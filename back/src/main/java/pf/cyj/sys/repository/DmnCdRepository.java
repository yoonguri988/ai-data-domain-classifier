package pf.cyj.sys.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import pf.cyj.sys.entity.DmnCd;

public interface DmnCdRepository extends JpaRepository<DmnCd, String> {

    List<DmnCd> findAllByOrderBySortOrderAsc();
}
