package pf.cyj.sys.job;

import java.util.List;
import org.springframework.stereotype.Component;
import pf.cyj.sys.entity.AnlCol;
import pf.cyj.sys.entity.AnlJob;
import pf.cyj.sys.exception.ExternalApiException;
import pf.cyj.sys.exception.ResourceNotFoundException;
import pf.cyj.sys.repository.AnlColRepository;
import pf.cyj.sys.repository.AnlJobRepository;
import pf.cyj.sys.service.AnlJobService;
import pf.cyj.sys.service.DmnPdtService;

/**
 * jobType="DOMAIN_PREDICT" 배치작업의 실제 실행 로직 - 작업에 연결된 데이터셋의 컬럼을 전부 순회하며
 * DmnPdtService.predict()(v4.3에서 만든 외부 AI API 연동)를 호출해 재판별한다.
 *
 * <p>컬럼 하나가 외부 API 호출 실패({@link ExternalApiException})로 실패해도 배치 전체를 중단하지
 * 않고 실패 건수만 세어 다음 컬럼을 계속 진행한다 - 배치는 "가능한 만큼 처리하고 실패한 건 기록해서
 * 나중에 확인"하는 게 "한 컬럼 때문에 전체를 멈춘다"보다 실무적으로 낫기 때문이다. 데이터셋 자체를
 * 찾을 수 없는 등 작업 전체가 성립하지 않는 경우에는 예외를 그대로 던져 AbstractAnlJobExecutor 가
 * INTERNAL_ERROR로 기록하게 한다.
 */
@Component
public class DomainPredictJobExecutor extends AbstractAnlJobExecutor {

    private final AnlJobRepository anlJobRepository;
    private final AnlColRepository anlColRepository;
    private final DmnPdtService dmnPdtService;

    public DomainPredictJobExecutor(AnlJobService anlJobService,
                                     AnlJobRepository anlJobRepository,
                                     AnlColRepository anlColRepository,
                                     DmnPdtService dmnPdtService) {
        super(anlJobService);
        this.anlJobRepository = anlJobRepository;
        this.anlColRepository = anlColRepository;
        this.dmnPdtService = dmnPdtService;
    }

    @Override
    protected JobResult doExecute(Long jobId) {
        AnlJob job = anlJobRepository.findById(jobId)
                .orElseThrow(() -> new ResourceNotFoundException("존재하지 않는 배치작업입니다: " + jobId));
        if (job.getAnlDset() == null) {
            throw new IllegalStateException("데이터셋이 지정되지 않은 배치작업입니다(jobId=" + jobId + ")");
        }

        List<AnlCol> columns = anlColRepository.findByAnlDset_DatasetId(job.getAnlDset().getDatasetId());

        int success = 0;
        int fail = 0;
        for (AnlCol col : columns) {
            try {
                dmnPdtService.predict(col.getColumnId());
                success++;
            } catch (ExternalApiException e) {
                fail++; // 컬럼 하나의 외부 API 실패는 CALL_ERROR 로 개별 처리하지 않고 실패 건수로만 집계한다.
            }
        }

        return new JobResult(success, fail);
    }
}
