package com.planetbiru;
import org.springframework.beans.BeansException;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

@SpringBootApplication
public class SmsApplication implements ApplicationContextAware{
	public static void main(String[] args) {
		SpringApplication.run(SmsApplication.class);
	}

	private ApplicationContext appContentx;

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		appContentx = applicationContext;
		
	}

}
