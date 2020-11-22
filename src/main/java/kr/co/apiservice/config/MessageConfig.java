package kr.co.apiservice.config;


import java.util.Locale;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.support.MessageSourceAccessor;
import org.springframework.stereotype.Component;

@Component
public class MessageConfig {
	
	@Autowired
    private MessageSource messageSource;
	
	private MessageSourceAccessor accessor;
	
	@PostConstruct
	private void init() {
		accessor = new MessageSourceAccessor(messageSource, Locale.ENGLISH);
	}
	
	public String get(String code) {
        return accessor.getMessage(code);
    }
	
	public String get(String code, Locale locale) {
		return accessor.getMessage(code, null, get(code), locale);
	}
	
	public String get(String code, Object[] objs) {
		return accessor.getMessage(code, objs, Locale.getDefault());
	}
	
}
