package pf.cyj.sys.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer{
	//
	@Value("${file.upload-dir}")
	private String uploadDir;
	
	// 이미지 리소스
	@Override
	public void addResourceHandlers(ResourceHandlerRegistry registry) {
		registry.addResourceHandler("/uploads/**")
		.addResourceLocations("file:"+uploadDir+"/");
	}
	
	// Cor - 외부에서 접근 가능하게 설정
//	@Override
//    public void addCorsMappings(CorsRegistry registry) { 
//        registry.addMapping("/**")
//                .allowedOrigins("http://localhost:3000")  // 프론트 엔드 주소
//                .allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
//                .allowedHeaders("*")
//                .allowCredentials(true)  
//                .maxAge(3600);
//    }
}
