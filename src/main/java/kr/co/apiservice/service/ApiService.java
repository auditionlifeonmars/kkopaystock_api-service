package kr.co.apiservice.service;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import brave.Tracer;
import kr.co.apiservice.exception.ServiceException;
import kr.co.apiservice.util.CommonUtil;
import kr.co.apiservice.util.LogUtil;

@Service
public class ApiService extends CommonUtil {

	private static final Logger log = LoggerFactory.getLogger(ApiService.class);
	Gson gson = new GsonBuilder().disableHtmlEscaping().setPrettyPrinting().create();

	@Autowired
	RedisTemplate<String, String> redisTemplate;

	@Autowired
	LogUtil logUtil;

	@Autowired
	Tracer tracer;

	private String x_user_id = null;
	private String x_room_id = null;
	private String time = null;
	private String send_call_time = null;
	private int amount = 0;
	private int count = 0;
	private String token = null;
	private int received_amount = 0;
	private int total_received_amount = 0;

	/**
	 * īī�� ���� �Ѹ��� ����
	 */
	public String PaySendService(HttpServletRequest request, @RequestBody String jsonStr, long called_time) {

		try {
			// ���
			x_user_id = request.getHeader("X-USER-ID");
			x_room_id = request.getHeader("X-ROOM-ID");
			time = convertMillisToDate(called_time);

			// �����ͺ�
			JsonParser Parser = new JsonParser();
			JsonObject jsonObject = (JsonObject) Parser.parse(jsonStr).getAsJsonObject();
			amount = jsonObject.get("amount").getAsInt();
			count = jsonObject.get("person_count").getAsInt();

			// �� ������
			int[] div_amount = dutchPay(amount, count);

			// token �߱�(3�ڸ� ���ڿ�)
			token = tracer.currentSpan().context().traceIdString();
			token = token.substring(0, 3);

			// Redis key : token_���ȣ
			StringBuffer key = new StringBuffer();
			key.append(token);
			key.append("_");
			key.append(x_room_id);
			log.info("key :: " + key.toString());

			// Redis Value
			JsonObject jsonvalue = new JsonObject();
			jsonvalue.addProperty("send_user_id", x_user_id);
			jsonvalue.addProperty("send_call_time", called_time);
			jsonvalue.addProperty("time", time);
			jsonvalue.addProperty("person_count", count);
			jsonvalue.addProperty("amount", amount);
			jsonvalue.addProperty("total_received_amount", 0);

			JsonArray infoArray = new JsonArray();
			for (int i = 0; i < count; i++) {
				JsonObject received_info = new JsonObject();
				received_info.addProperty("received_amount", div_amount[i]);
				received_info.addProperty("received_id", "");
				infoArray.add(received_info);
			}
			jsonvalue.add("received_info", infoArray);

			// redis set
			redisTemplate.setValueSerializer(new StringRedisSerializer());
			redisTemplate.opsForValue().set(key.toString(), jsonvalue.toString());

			// ���䵥���� ���� ( ���� �ʵ� - token��)
			JsonObject json_result = new JsonObject();
			json_result.addProperty("token", token);
			return json_result.toString();

		} catch (Throwable throwable) {
			if (throwable instanceof ServiceException) {
				return handleException((ServiceException) throwable);
			} 
			log.error("API_PROCESSING_ERROR");
			return handleException(new ServiceException(ServiceException.SVC_INTERNAL_ERROR));
		} finally {
			log.info("----------------------------------------");
			log.info("token >>>  [{}] ", token);
			log.info("x_user_id >>> [{}] ", x_user_id);
			log.info("x_room_id >>> [{}]", x_room_id);
			log.info("amount >>> [{}]", amount);
			log.info("person_count >>> [{}]", count);
			log.info("time >>>  [{}]", time);
			log.info("----------------------------------------");
			logUtil.recordForLog(request.getRequestURI().toString(), token, x_user_id, x_room_id, amount, count, time);
		}
	}

	/**
	 * īī�� ���� �ޱ� ����
	 */
	public String PayReceiveService(HttpServletRequest request, @RequestParam String token, long called_time) {
		
		try {
			// ���
			x_user_id = request.getHeader("X-USER-ID");
			x_room_id = request.getHeader("X-ROOM-ID");

			// Redis key : token_���ȣ
			StringBuffer key = new StringBuffer();
			key.append(token);
			key.append("_");
			key.append(x_room_id);

			// Redis Key ��ȸ Ű�� ������ ����
			if (redisTemplate.opsForValue().get(key.toString()) == null) {
				throw new ServiceException(ServiceException.ERROR_NOT_REDIS_KEY_EXIST);
			}

			// value �Ľ�
			JsonParser Parser = new JsonParser();
			String jsonStr = redisTemplate.opsForValue().get(key.toString());
			JsonObject jsonObj = (JsonObject) Parser.parse(jsonStr).getAsJsonObject();

			// �Ѹ���(send) ��û�� ����� �ޱ�(receive) ��û�� ���� �� �����ϴ�.
			String send_user_id = jsonObj.get("send_user_id").getAsString();
			if (send_user_id.equals(x_user_id)) {
				throw new ServiceException(ServiceException.RECEIVE_ERROR_SEND_ID_CANNOT_BE_RECEIVED);
			}

			time = jsonObj.get("time").getAsString();
			send_call_time = jsonObj.get("send_call_time").getAsString();
			// 10�� 600��
			boolean check = checkTimestamp(send_call_time, 600);
			if (!check) {
				throw new ServiceException(ServiceException.RECEIVE_ERROR_TIMEOUT);
			}

			amount = jsonObj.get("amount").getAsInt();
			count = jsonObj.get("person_count").getAsInt();
			JsonArray jsonArray = jsonObj.getAsJsonArray("received_info");

			total_received_amount = jsonObj.get("total_received_amount").getAsInt();
			if (total_received_amount >= amount) {
				log.info("** total_received_amount >> {}", total_received_amount);
				throw new ServiceException(ServiceException.RECEIVE_ERROR_NO_AMOUNT_RECEIVE);		
			} else {
				for (int i = 0; i < count; i++) {
					JsonElement jsonElement = jsonArray.get(i);
					String received_id = jsonElement.getAsJsonObject().get("received_id").getAsString();
					if (received_id.equals(x_user_id)) {
						throw new ServiceException(ServiceException.RECEIVE_ERROR_ALREADY_RECEIVE);
					}
					received_amount = jsonElement.getAsJsonObject().get("received_amount").getAsInt();
					if (StringUtils.isEmpty(received_id)) {
						jsonElement.getAsJsonObject().addProperty("received_id", x_user_id);
						log.info(jsonArray.toString());
						// �ݺ��� ��������
						break;
					}
				}
			} 
			// redis set
			total_received_amount += received_amount;
			jsonObj.addProperty("total_received_amount", total_received_amount);
			redisTemplate.setValueSerializer(new StringRedisSerializer());
			redisTemplate.opsForValue().set(key.toString(), jsonObj.toString());

			// ���䵥���� ���� ( ���� �ʵ� - received_amount��)
			JsonObject json_result = new JsonObject();
			json_result.addProperty("received_amount", received_amount);
			return json_result.toString();

		} catch (Throwable throwable){
			if (throwable instanceof ServiceException) {
				return handleException((ServiceException) throwable);
			}
			log.error("API_PROCESSING_ERROR");
			return handleException(new ServiceException(ServiceException.SVC_INTERNAL_ERROR));
		} finally{
			log.info("----------------------------------------");
			log.info("token >>>  [{}] ", token);
			log.info("x_user_id >>> [{}] ", x_user_id);
			log.info("x_room_id >>> [{}]", x_room_id);
			log.info("amount >>> [{}]", amount);
			log.info("person_count >>> [{}]", count);
			log.info("time >>>  [{}]", time);
			log.info("----------------------------------------");
			logUtil.recordForLog(request.getRequestURI().toString(), token, x_user_id, x_room_id, amount, count, time);
		}

	}

	/**
	 * īī������ ��ȸ
	 */
	public String PaySearchService(HttpServletRequest request, @RequestParam String token, long called_time) {

		try {
			// ���
			x_user_id = request.getHeader("X-USER-ID");
			x_room_id = request.getHeader("X-ROOM-ID");

			// Redis key : token_���ȣ
			StringBuffer key = new StringBuffer();
			key.append(token);
			key.append("_");
			key.append(x_room_id);

			// Redis Key ��ȸ Ű�� ������ ����
			if (redisTemplate.opsForValue().get(key.toString()) == null) {
				throw new ServiceException(ServiceException.ERROR_NOT_REDIS_KEY_EXIST);
			}

			// value �Ľ�
			JsonParser Parser = new JsonParser();
			String jsonStr = redisTemplate.opsForValue().get(key.toString());
			JsonObject jsonObj = (JsonObject) Parser.parse(jsonStr).getAsJsonObject();

			// �Ѹ��� ���� ����� �ƴ� ��� ��ȸ ����ó��
			String send_user_id = jsonObj.get("send_user_id").getAsString();
			if (!send_user_id.equals(x_user_id)) {
				return handleException(new ServiceException(ServiceException.SEARCH_ERROR_SEARCH_ID_ERROR));
			}

			// ��ȸ timeout (7�� (86400��*7��))
			send_call_time = jsonObj.get("send_call_time").getAsString();
			boolean check = checkTimestamp(send_call_time, 86400 * 7);
			if (!check) {
				throw new ServiceException(ServiceException.SEARCH_ERROR_TIMEOUT);
			}

			return jsonObj.toString();

		} catch (Throwable throwable) {
			if (throwable instanceof ServiceException) {
				return handleException((ServiceException) throwable);
			}
			log.error("API_PROCESSING_ERROR");
			return handleException(new ServiceException(ServiceException.SVC_INTERNAL_ERROR));
		} finally {
			log.info("----------------------------------------");
			log.info("token >>>  [{}] ", token);
			log.info("x_user_id >>> [{}] ", x_user_id);
			log.info("x_room_id >>> [{}]", x_room_id);
			log.info("time >>>  [{}]", time);
			log.info("----------------------------------------");
			logUtil.recordForLog(request.getRequestURI().toString(), token, x_user_id, x_room_id, amount, count, time);
		}
	}
}
