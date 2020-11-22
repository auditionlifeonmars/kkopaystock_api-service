package kr.co.apiservice.util;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import kr.co.apiservice.config.MessageConfig;
import kr.co.apiservice.exception.ServiceException;
import kr.co.apiservice.model.ResponseTemplate;

/**
 * 공통메소드
 *
 */
@Component
public class CommonUtil {
	private static final Logger log = LoggerFactory.getLogger(CommonUtil.class);
	Gson gson = new GsonBuilder().disableHtmlEscaping().setPrettyPrinting().create();

	private SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	
	@Autowired
	private MessageConfig message;
	
	/**
	 * 오류를 처리한다.
	 */
	public String handleException(ServiceException e) {
		ResponseTemplate<?> response = new ResponseTemplate<Object>();
		response.resCd = e.getExceptionCode();
		response.resMsg = message.get(response.resCd, new String[] { e.getMessageParam() });

		log.info("resCd : {} ", response.resCd);
		log.info("resMsg : {} ", response.resMsg);
		log.error("", e);

		return gson.toJson(response);
	}

	/**
	 * 밀리세컨드를 yyyy-MM-dd hh:mm:ss 포멧의 문자열로 변환하여 리턴한다.
	 */
	public String convertMillisToDate(long millis) {
		return dateFormatter.format(new Date(millis));
	}

	/**
	 * 머니 나누기
	 */
	public int[] dutchPay(int amount, int count) {
		int[] div_amount = new int[count];
		int peopleAmount = amount / count;
		int reminder = amount % count;
		for (int i = 0; i < count; i++) {
			if (i == 0) {
				div_amount[i] = peopleAmount + reminder;
				log.info("div_amount [{}] :: {} ", i, div_amount[i]);
			} else {
				div_amount[i] = peopleAmount;
				log.info("div_amount [{}] :: {} ", i, div_amount[i]);
			}
		}
		return div_amount;
	}

	/**
	 * 시간 체크
	 */
	public boolean checkTimestamp(String timeStamp, int expire_duration) {

		Date currentDate = new Date();
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String currentTime = dateFormat.format(currentDate);
		Date reqDate = new Date(Long.parseLong(timeStamp));
		String reqTime = dateFormat.format(reqDate);
		log.info("reqDate: " + reqDate);
		log.info("expire_duration: " + expire_duration);
		Calendar cal = Calendar.getInstance();
		cal.setTime(reqDate);
		cal.add(Calendar.SECOND, expire_duration);
		Date expiredDate = cal.getTime();
		String expiredTime = dateFormat.format(expiredDate);

		boolean checkTimeout = DateTimeCompare(dateFormat, reqTime, currentTime, expiredTime);
		log.info("checkTimeout: " + checkTimeout);
		if (checkTimeout) {
			return true;
		} else {
			return false;
		}
	}

	private boolean DateTimeCompare(SimpleDateFormat format, String reqTime, String currentTime, String expiredTime) {

		try {
			Date reqDate = format.parse(reqTime);
			Date currentDate = format.parse(currentTime);
			Date expiredDate = format.parse(expiredTime);
			log.info("currentDate: {}", currentDate);
			log.info("expiredDate: {}", expiredDate);
			int fCompare = reqDate.compareTo(currentDate);
			if (fCompare > 0) {
				log.info("요청일이 현재일보다 미래");
				return false;
			} else if (fCompare < 0) {
				log.info("요청일이 현재일보다 과거");
			} else {
				log.info("요청일=현재일");
			}

			int sCompare = currentDate.compareTo(expiredDate);
			if (sCompare > 0) {
				log.info("현재일이 만료일보다 미래");
				return false;
			} else if (sCompare < 0) {
				log.info("현재일이 만료일보다 과거");
			} else {
				log.info("현재일 = 만료일");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return true;
	}

}
