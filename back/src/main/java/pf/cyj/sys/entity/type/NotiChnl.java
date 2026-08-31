package pf.cyj.sys.entity.type;

/**
 * NOTIFICATION_LOG.CHANNEL - 현재는 이메일만 지원한다. 나중에 다른 채널(SMS, 카카오톡 알림톡 등)을
 * 붙일 일이 생기면 이 enum 에 값만 추가하면 되도록, 컬럼 자체는 이번에도 그대로 두었다.
 */
public enum NotiChnl {
    EMAIL
}
