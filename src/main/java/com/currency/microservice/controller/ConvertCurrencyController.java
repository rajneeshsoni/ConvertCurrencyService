package com.currency.microservice.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.currency.microservice.bean.ResponseObjectBean;
import com.currency.microservice.delegate.ConvertCurrencyService;

@RestController
@RequestMapping(path="/currency/conversion")
public class ConvertCurrencyController {
	
	private static Logger log = LoggerFactory.getLogger(ConvertCurrencyController.class);
	
	@Autowired
	ConvertCurrencyService convertCurrencyservice;
	
	@RequestMapping(value = "/{amount}/{fromcurrencycode}/{tocurrencycode}", method = RequestMethod.GET)
	public ResponseEntity<ResponseObjectBean> getConvertedAmount(@PathVariable int amount, @PathVariable String fromcurrencycode,@PathVariable String tocurrencycode) {
		
		log.info("------- Start getConvertedAmount - Info LOG ");
		return convertCurrencyservice.getConvertedAmount(amount, fromcurrencycode, tocurrencycode);
	}
	
	
	//--------------------------------

	@RequestMapping(path = "/feign/{amount}/{fromcurrencycode}/{tocurrencycode}", method = RequestMethod.GET)
	public ResponseEntity<ResponseObjectBean> getConvertedAmountWithFeign(@PathVariable int amount, @PathVariable String fromcurrencycode ,@PathVariable String tocurrencycode) {
		log.info("Start getConvertedAmountWithFeign ");
		return convertCurrencyservice.getConvertedAmountWithFeign(amount, fromcurrencycode, tocurrencycode);
	}
	
	
	
	//-------------------------------------
	
	@RequestMapping(path = "/feignandhystrix/{amount}/{fromcurrencycode}/{tocurrencycode}", method = RequestMethod.GET)
	public ResponseEntity<ResponseObjectBean> getConversionFactorWithFeignAndHystrix(@PathVariable int amount, @PathVariable String fromcurrencycode ,@PathVariable String tocurrencycode) {

		log.info("--getConversionFactorWithFeignAndHystrix : fromcurrencycode = " + fromcurrencycode + ", tocurrencycode = " + tocurrencycode + ", amount = " + amount  );
		return convertCurrencyservice.getConversionWithFeignAndHystrix(amount, fromcurrencycode, tocurrencycode);
	}
	
	//---------------------
	
	@RequestMapping(path = "/ribbon/{amount}/{fromcurrencycode}/{tocurrencycode}", method = RequestMethod.GET)
	public ResponseEntity<ResponseObjectBean> getConvertedAmountWithRibbon(@PathVariable int amount, @PathVariable String fromcurrencycode ,@PathVariable String tocurrencycode) 
	{
		return convertCurrencyservice.getConvertedWithRibbon(amount, fromcurrencycode, tocurrencycode);
	}

}
