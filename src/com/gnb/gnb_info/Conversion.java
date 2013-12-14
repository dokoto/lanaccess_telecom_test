package com.gnb.gnb_info;

public class Conversion {
	public String from;
	public String to;
	public Double rate;
	
	Conversion(){}
	Conversion(String from, String to, Double rate)
	{
		this.from = from;
		this.to = to;
		this.rate = rate;
	}
}
