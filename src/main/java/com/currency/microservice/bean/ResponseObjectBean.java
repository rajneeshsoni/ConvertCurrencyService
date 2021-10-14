package com.currency.microservice.bean;

public class ResponseObjectBean {
	
     private String responseText;
	
	private double convertedAmount;

	
	
	public ResponseObjectBean() {
		super();
	}

	public ResponseObjectBean(String responseText, double convertedAmount) {
		super();
		this.responseText = responseText;
		this.convertedAmount = convertedAmount;
	}

	public String getResponseText() {
		return responseText;
	}

	public void setResponseText(String responseText) {
		this.responseText = responseText;
	}

	public double getConvertedAmount() {
		return convertedAmount;
	}

	public void setConvertedAmount(double convertedAmount) {
		this.convertedAmount = convertedAmount;
	}
	

}
