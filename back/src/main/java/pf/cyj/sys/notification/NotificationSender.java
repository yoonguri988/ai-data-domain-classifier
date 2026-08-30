package pf.cyj.sys.notification;

import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

/**
 * 알림 실제 발송 - 이메일(spring-boot-starter-mail)을 담당한다.
 * NotiRptService.sendAndRecord() 가 이력을 남기기 직전에 이 클래스를 호출해 실제로 내보낸다.
 *
 * <p>SMS(CoolSMS 등)는 이번 프로젝트 범위에서 제외했다 - 이메일만으로 승인/반려 통보 요구사항을
 * 충분히 충족하고, SMS 발신번호 사전등록 같은 외부 서비스 심사 절차까지 포트폴리오에 끌고 올 필요는
 * 없다고 판단했다. NOTIFICATION_LOG.CHANNEL 컬럼 자체는 나중에 다른 채널(SMS, 카카오톡 알림톡 등)을
 * 붙일 여지를 남겨두려고 그대로 두었다(NotiChnl 참고).
 *
 * <p>발송 실패를 예외로 던지지 않고 boolean 으로 돌려준다 - 발송이 실패해도 "실패했다"는 사실 자체는
 * NOTIFICATION_LOG(SEND_STATUS=FAIL)에 반드시 남아야 하는데, 여기서 예외를 던지면 그 이력 저장까지
 * 막혀버리기 때문이다(호출부는 GlobalExceptionHandler 로 500을 띄우는 대신 "발송 실패"라는 정상적인
 * 비즈니스 결과로 처리한다).
 */
@Component
public class NotificationSender {

    private static final Logger log = LoggerFactory.getLogger(NotificationSender.class);

    private final JavaMailSender mailSender;

    public NotificationSender(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    /** toEmail 로 메일을 보낸다. 성공하면 true, 발송 자체가 실패하면(SMTP 오류 등) false 를 반환한다. */
    public boolean sendEmail(String toEmail, String title, String content) {
        if (toEmail == null || toEmail.isBlank()) {
            log.warn("[NotificationSender] 수신 이메일 주소가 없어 발송을 건너뜁니다.");
            return false;
        }
        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, "UTF-8");
            helper.setTo(toEmail);
            helper.setSubject(title != null ? title : "알림");
            helper.setText(content != null ? content : "", false);
            mailSender.send(mimeMessage);
            return true;
        } catch (Exception e) {
            log.warn("[NotificationSender] 이메일 발송 실패. to={}, error={}", toEmail, e.getMessage());
            return false;
        }
    }
}
