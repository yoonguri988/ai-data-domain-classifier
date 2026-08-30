package pf.cyj.sys;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

// @EnableScheduling - AnlJobScheduler(@Scheduled)가 READY 상태 배치작업을 자동으로 훑어 실행하려면 필요하다.
@EnableScheduling
@SpringBootApplication
public class BackApplication {

	public static void main(String[] args) {
		SpringApplication.run(BackApplication.class, args);
	}

}
