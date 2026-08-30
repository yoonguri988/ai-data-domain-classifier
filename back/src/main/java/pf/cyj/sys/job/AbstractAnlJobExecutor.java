package pf.cyj.sys.job;

import lombok.RequiredArgsConstructor;
import pf.cyj.sys.dto.response.AnlJobLogRsp;
import pf.cyj.sys.entity.type.ExecStatCd;
import pf.cyj.sys.exception.ExternalApiException;
import pf.cyj.sys.service.AnlJobService;

/**
 * 배치작업 실행 템플릿 메서드 - "실행 시작 로그 기록 → 실제 실행 → 성공/실패 기록"을 여기서 공통으로
 * 처리하고, 하위 클래스는 실제 작업 내용({@link #doExecute})만 구현하면 된다. 원본 솔루션이 Quartz +
 * 쉘 스크립트로 하던 걸, 별도 프로세스 없이 애플리케이션 내장 스케줄러(AnlJobScheduler)가 이 클래스를
 * 통해 실행하는 구조로 재구현했다.
 *
 * <p>핵심은 예외 종류에 따라 실행 이력의 상태를 자동으로 구분해서 남기는 것이다 -
 * {@link ExternalApiException}(외부 AI API 호출 실패)은 CALL_ERROR로, 그 외 모든 예외는
 * INTERNAL_ERROR(우리 로직 문제)로 기록된다. 장애가 나면 ANALYSIS_JOB_LOG.EXEC_STATUS 만 보고
 * "우리 코드 문제인지 외부 API 문제인지"를 바로 구분할 수 있다.
 */
@RequiredArgsConstructor
public abstract class AbstractAnlJobExecutor {

    protected final AnlJobService anlJobService;

    /**
     * jobId 배치작업을 실행한다.
     * @param executorId 사람이 Swagger 등으로 수동 실행한 경우 그 사용자 ID, 스케줄러가 자동 실행한 경우 null
     * @return 이번 실행의 최종 이력(성공 시 SUCCESS, 실패 시 CALL_ERROR/INTERNAL_ERROR 상태를 담고 있다)
     */
    public final AnlJobLogRsp execute(Long jobId, Long executorId) {
        AnlJobLogRsp startedLog = anlJobService.startExecution(jobId, executorId);
        try {
            JobResult result = doExecute(jobId);
            return anlJobService.completeExecution(startedLog.getLogId(), result.getSuccessCount(), result.getFailCount());
        } catch (ExternalApiException e) {
            return anlJobService.failExecution(startedLog.getLogId(), ExecStatCd.CALL_ERROR, e.getMessage());
        } catch (Exception e) {
            return anlJobService.failExecution(startedLog.getLogId(), ExecStatCd.INTERNAL_ERROR, e.getMessage());
        }
    }

    /**
     * 실제 배치 로직을 구현한다. 여기서 던진 예외 종류에 따라 위 {@link #execute}가 CALL_ERROR/INTERNAL_ERROR를
     * 자동으로 구분해 기록하므로, 외부 API 호출 실패는 그대로 {@link ExternalApiException}을 던지면 된다.
     */
    protected abstract JobResult doExecute(Long jobId);
}
