package kr.co.apiservice.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * ·Î±ë
 */
@Component
public class LogUtil {
	private static final Logger log = LoggerFactory.getLogger(LogUtil.class);
	Gson gson = new GsonBuilder().disableHtmlEscaping().setPrettyPrinting().create();

	/**
	 * ·Î±×
	 * @param uri
	 * @param token
	 * @param send_user_id
	 * @param x_room_id
	 * @param amount
	 * @param person_count
	 * @param time
	 */
	public void recordForLog(String uri, String token, String x_user_id, String x_room_id, int amount, int count, String time) {
		String result = String.format("%s|%s|%s|%s|%s|%d|%s",
				uri,
				token,
				x_user_id,
				x_room_id,
				amount,
				count,
				time);
		log.info(result);
	}

}
