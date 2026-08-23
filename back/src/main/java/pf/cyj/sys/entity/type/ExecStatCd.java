package pf.cyj.sys.entity.type;

/**
 * ANALYSIS_JOB_LOG.EXEC_STATUS
 * 원본 솔루션의 5종 배치 상태(실행전/진행중/성공/작업내부에러/작업호출에러) 체계를 그대로 유지한다.
 * INTERNAL_ERROR(내부 로직 오류)와 CALL_ERROR(외부 AI API 호출 실패)를 분리해 장애 원인을 즉시 구분한다.
 */
public enum ExecStatCd {
    READY, RUNNING, SUCCESS, INTERNAL_ERROR, CALL_ERROR
}
