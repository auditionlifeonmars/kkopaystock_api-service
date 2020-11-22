package kr.co.apiservice.exception;

import java.util.List;
import java.util.Set;

import javax.validation.ConstraintViolation;

import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;

/**
 *  예외 클래스
 */
public class ServiceException extends RuntimeException {

	private static final long serialVersionUID = 1L;
	
	
    /**
     * receive(받기API) 이미 받은 사람이 보낸 요청에 오류 처리
     */
    public static final String   RECEIVE_ERROR_ALREADY_RECEIVE                                     = "E2100";
	
    /**
     * receive(받기API) 더 이상 받을 금액이 남아있지 않을 때 오류처리
     */
    public static final String   RECEIVE_ERROR_NO_AMOUNT_RECEIVE                               = "E2200";
    
    /**
     * receive(받기API) 뿌리기 요청한 사람이 보낸 받기 요청에 오류 처리
     */
    public static final String   RECEIVE_ERROR_SEND_ID_CANNOT_BE_RECEIVED           = "E2300";

    /**
     * receive(받기API) 뿌리기 요청 후 10분 지난 거래에 대해 오류처리(timeout)
     */
    public static final String   RECEIVE_ERROR_TIMEOUT                                                       = "E2400";

    /**
     * search(조회API) 조회ID 오류(뿌린 사람만 조회 가능)
     */
    public static final String   SEARCH_ERROR_SEARCH_ID_ERROR                                    = "E3100";
    
    /**
     * search(조회API) 조회ID timeout (7일간만 조회 가능)
     */
    public static final String   SEARCH_ERROR_TIMEOUT                                                       = "E3200";

    /**
     * Redis에 키가 존재하지 않는 경우
     */
    public static final String   ERROR_NOT_REDIS_KEY_EXIST                                              = "E4100";

    /**
     * 내부 시스템 오류
     */
    public static final String   SVC_INTERNAL_ERROR                                                             = "E4200";
    
    
    private String               exceptionCode;
    private String            messageParam;

    public ServiceException(String exceptionCode) {
        this.exceptionCode = exceptionCode;
    }

    public ServiceException(String exceptionCode, String messageParam) {
        this.exceptionCode = exceptionCode;
        this.messageParam = messageParam;
    }

    public ServiceException(String exceptionCode, BindingResult result) {
        this.exceptionCode = exceptionCode;
        List<FieldError> errList = result.getFieldErrors();
        for (FieldError err : errList) {
            if (messageParam == null)
                messageParam = err.getField();
            else
                messageParam += ", " + err.getField();
        }
    }

    public <T> ServiceException(String exceptionCode, Set<ConstraintViolation<T>> violations) {
        this.exceptionCode = exceptionCode;
        for (ConstraintViolation<T> violation : violations) {
            if (messageParam == null)
                messageParam = violation.getPropertyPath().toString();
            else
                messageParam += ", " + violation.getPropertyPath().toString();
        }
    }

    public String getExceptionCode() {
        return exceptionCode;
    }

    public String getMessageParam() {
        return messageParam;
    }
}
