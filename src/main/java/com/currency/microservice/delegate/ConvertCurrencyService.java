package com.currency.microservice.delegate;

import java.math.RoundingMode;
import java.text.DecimalFormat;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import com.currency.microservice.bean.ResponseObjectBean;
import com.currency.microservice.service.ConvertCurrencyServiceProxy;
import com.currency.microservice.util.ConversionServiceConstant;
import com.netflix.appinfo.InstanceInfo;
import com.netflix.discovery.EurekaClient;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixProperty;

@Service
public class ConvertCurrencyService {
	
	private static Logger log = LoggerFactory.getLogger(ConvertCurrencyService.class);
	
	@LoadBalanced
	@Bean
	RestTemplate getRestTemplate() {
		return new RestTemplate();
	}

	@Autowired
	RestTemplate restTemplate;
	
	@Autowired
	@Lazy
	private EurekaClient eurekaClient;
	
	@Autowired
	ConvertCurrencyServiceProxy convertCurrencyServiceProxy;
	private static DecimalFormat df2 = new DecimalFormat("0.00");
	
	
	
	public ResponseEntity<ResponseObjectBean> getConvertedAmount(int amount, String fromCurrencyCode, String toCurrencyCode) {

		log.info("Start  getConvertedAmount ");
		try
		{
			InstanceInfo info = eurekaClient.getNextServerFromEureka("MANAGECURRCONVFACTOR", false);
			String url = "http://" + info.getHostName() + ":" + info.getPort() + "/conversionfactor/{fromcurrencycode}/{tocurrencycode}";
			ResponseEntity<Double> conversionFactorEntity = new RestTemplate().getForEntity(url, Double.class, fromCurrencyCode, toCurrencyCode);
			if(conversionFactorEntity!=null && conversionFactorEntity.getBody()!=null && !conversionFactorEntity.getBody().isNaN())
			{
				double conversionFactor = conversionFactorEntity.getBody();
				log.info("Conversion factor from currency " + fromCurrencyCode + " to currency " + toCurrencyCode + " is : " + conversionFactor);
				
				if (conversionFactor == 0.0)
				{
					log.info("Start sending response back ");
					return createResponseBean(ConversionServiceConstant.CURRENCY_NOT_EXIST,0.0, HttpStatus.BAD_REQUEST);
				}
				else
				{
					log.info("Start sending response back ");
					df2.setRoundingMode(RoundingMode.DOWN);
					String convertedVal = df2.format(amount * conversionFactor);
					double convertedAmount =  Double.parseDouble(convertedVal);
					log.info("convertedAmount>>>>>"+convertedAmount);
					
					
					return createResponseBean(ConversionServiceConstant.SUCCESS_MESSAGE,convertedAmount, HttpStatus.OK);
				}
				
			}
		}
		catch (HttpClientErrorException httpClientErrorException) {
		    log.info("getConversionFactor - " + httpClientErrorException.getStatusCode().toString());
		    log.info("getConversionFactor - " + httpClientErrorException.getResponseBodyAsString());
		}
		catch (Exception e) {
			log.info(e.getMessage());
		}
		
		log.info("Start - sending response back ");
		return createResponseBean(ConversionServiceConstant.ERROR_MESSAGE,0.0, HttpStatus.INTERNAL_SERVER_ERROR);
		

	}

	private ResponseEntity<ResponseObjectBean> createResponseBean(String responseText, double convertedAmount, HttpStatus status) {

		ResponseObjectBean responseObjectBean = new ResponseObjectBean(responseText,convertedAmount);
		return new ResponseEntity<ResponseObjectBean> (responseObjectBean, status);
		
	}
	
	
	public ResponseEntity<ResponseObjectBean> getConvertedAmountWithFeign(int amount, String fromCurrencyCode, String toCurrencyCode) {

		try
		{
			double conversionFactor =  convertCurrencyServiceProxy.getConversionFactorForCurrencies(fromCurrencyCode, toCurrencyCode);
			log.info("Conversion factor from currency " + fromCurrencyCode + " to currency " + toCurrencyCode + " is : " + conversionFactor);
			
			if (conversionFactor == 0.0)
			{
				return createResponseBean(ConversionServiceConstant.CURRENCY_NOT_EXIST,0.0, HttpStatus.BAD_REQUEST);
			}
			else
			{
				df2.setRoundingMode(RoundingMode.DOWN);
				String convertedVal = df2.format(amount * conversionFactor);
				double convertedAmount =  Double.parseDouble(convertedVal);
				return createResponseBean(ConversionServiceConstant.SUCCESS_MESSAGE,convertedAmount, HttpStatus.OK);
			}
			
		}
		catch (Exception e) {
			log.info(e.getMessage());
			return createResponseBean(ConversionServiceConstant.ERROR_MESSAGE,0.0, HttpStatus.INTERNAL_SERVER_ERROR);

		}	
		
	}
	
	
	//------------------------------------------
	
	@HystrixCommand(commandKey = "GET-CURRENCY", fallbackMethod = "getConversionFactor_Fallback", threadPoolKey = "getConversionFactor_Fallback", commandProperties = {
			@HystrixProperty(name = "execution.isolation.thread.timeoutInMilliseconds", value = "3000"),
			@HystrixProperty(name = "circuitBreaker.errorThresholdPercentage", value = "60") })
	public ResponseEntity<ResponseObjectBean> getConversionWithFeignAndHystrix(int amount, String fromCurrencyCode, String toCurrencyCode) {

		Double conversionFactor = convertCurrencyServiceProxy.getConversionFactorForCurrencies(fromCurrencyCode, toCurrencyCode);
		
		if (conversionFactor == 0.0)
		{
			return createResponseBean(ConversionServiceConstant.CURRENCY_NOT_EXIST,0.0, HttpStatus.BAD_REQUEST);
		}
		else
		{
			df2.setRoundingMode(RoundingMode.DOWN);
			String convertedVal = df2.format(amount * conversionFactor);
			double convertedAmount =  Double.parseDouble(convertedVal);
			return createResponseBean(ConversionServiceConstant.SUCCESS_MESSAGE,convertedAmount, HttpStatus.OK);
		}

	
	}

	public ResponseEntity<ResponseObjectBean> getConversionFactor_Fallback(int amount, String fromCurrencyCode, String toCurrencyCode) {

		return createResponseBean(ConversionServiceConstant.ERROR_MESSAGE,0.0, HttpStatus.SERVICE_UNAVAILABLE);
	
	}
	
	
	//----------------------------
	public ResponseEntity<ResponseObjectBean> getConvertedWithRibbon(int amount, String fromCurrencyCode, String toCurrencyCode) {

		try
		{
			Double conversionFactor = this.restTemplate
					.getForObject("http://managecurrconvfactor/conversionfactor/{fromcurrencycode}/{tocurrencycode}", Double.class, fromCurrencyCode, toCurrencyCode );
			log.info("Conversion factor from currency " + fromCurrencyCode + " to currency " + fromCurrencyCode + " is : " + conversionFactor);
			
			if (conversionFactor == 0.0)
			{
				return createResponseBean(ConversionServiceConstant.CURRENCY_NOT_EXIST,0.0, HttpStatus.BAD_REQUEST);
			}
			else
			{
				df2.setRoundingMode(RoundingMode.DOWN);
				String convertedVal = df2.format(amount * conversionFactor);
				double convertedAmount =  Double.parseDouble(convertedVal);
				return createResponseBean(ConversionServiceConstant.SUCCESS_MESSAGE,convertedAmount, HttpStatus.OK);
			}
		
		}
		catch (Exception e) {
			log.info(e.getMessage());
		}
		
		return createResponseBean(ConversionServiceConstant.ERROR_MESSAGE,0.0, HttpStatus.INTERNAL_SERVER_ERROR);

	}

}
