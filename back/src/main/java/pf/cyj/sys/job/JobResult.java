package pf.cyj.sys.job;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * AbstractAnlJobExecutor.doExecute() 가 배치 실행 결과를 돌려줄 때 쓰는 값 - 성공/실패 건수만 담는다.
 * AnlJobService.completeExecution(logId, successCount, failCount) 로 그대로 전달된다.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class JobResult {

    private int successCount;

    private int failCount;
}
