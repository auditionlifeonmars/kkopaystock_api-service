package kr.co.apiservice.model;

/**
 * 오류응답 
 */
public class ResponseTemplate<T> {

	public String resCd;
	public String resMsg;
	
	
	public ResponseTemplate(){
		
	}
	
	public ResponseTemplate(String rspCd, String resMsg){
		
		this.resCd = rspCd;
		this.resMsg = resMsg;
		
	}
	
}
