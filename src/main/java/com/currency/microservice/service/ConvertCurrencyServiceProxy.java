package com.currency.microservice.service;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name="MANAGECURRCONVFACTOR")
public interface ConvertCurrencyServiceProxy{
	
	@GetMapping (path="/conversionfactor/{fromcurrencycode}/{tocurrencycode}")
	  public Double getConversionFactorForCurrencies(@PathVariable String fromcurrencycode, @PathVariable String tocurrencycode);


}
