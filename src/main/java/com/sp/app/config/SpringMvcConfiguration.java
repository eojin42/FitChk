package com.sp.app.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/*
- CORS(Cross-Origin Resource Sharing)
  브라우저에서 다른 출처(Origin) 의 서버로 요청을 보낼 때 발생하는 보안 정책
- Origin = 프로토콜 + 도메인 + 포트
- 브라우저는 Same-Origin Policy (동일 출처 정책) 를 기본
- 특정 컨트롤러에서 설정
  @CrossOrigin(origins = "http://localhost:5173")
  @RestController
  @RequestMapping("/api")
  public class TestController {
      :
*/

@Configuration
public class SpringMvcConfiguration implements WebMvcConfigurer {
	@Value("${file.upload-root}")
	private String uploadRoot;
	
	@Override
	public void addResourceHandlers(ResourceHandlerRegistry registry) {
		// 브라우저에서 /uploads/로 시작하는 요청이 오면 로컬의 uploadRoot 경로에서 파일을 찾도록 설정 
		registry.addResourceHandler("/uploads/**")
			.addResourceLocations("file:///" + uploadRoot);
	}
	
	@Override
	public void addCorsMappings(CorsRegistry registry) {
		registry.addMapping("/api/**")
			.allowedOrigins("http://localhost:5173", "http://localhost:3000", "http://localhost") // Vue, React 기본 포트
			.allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE");
	}	
}
