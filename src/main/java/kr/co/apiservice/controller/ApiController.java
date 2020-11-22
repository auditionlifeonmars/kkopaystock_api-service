package kr.co.apiservice.controller;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import kr.co.apiservice.service.ApiService;

/**
 * Api 호출을 처리하는 ApiController
 */
@RestController
public class ApiController {

	private static final Logger log = LoggerFactory.getLogger(ApiController.class);
	Gson gson = new GsonBuilder().disableHtmlEscaping().setPrettyPrinting().create();

	@Autowired
	private ApiService apiService;

	private long calledTime;

	// 머니 뿌리기 API
	@PostMapping("/v1/send")
	public String PaySendService(HttpServletRequest request, @RequestBody String jsonStr) {
		log.info("***ApiController kakao pay send");
		// 호출 시간
		calledTime = System.currentTimeMillis();
		return apiService.PaySendService(request, jsonStr, calledTime);
	}

	// 머니 받기 API
	@GetMapping("/v1/receive")
	public String PayReceiveService(HttpServletRequest request, @RequestParam String token) {
		log.info("***ApiController kakap pay receive");
		// 호출 시간
		calledTime = System.currentTimeMillis();	
		return apiService.PayReceiveService(request, token, calledTime);
	}

	// 조회 API
	@GetMapping("/v1/search")
	public String PaySearchService(HttpServletRequest request, @RequestParam String token) {
		log.info("***ApiController kakao pay search");
		// 호출 시간
		calledTime = System.currentTimeMillis();
		return apiService.PaySearchService(request, token, calledTime);
	}

}
