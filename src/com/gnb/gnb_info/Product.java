package com.gnb.gnb_info;

import java.math.BigDecimal;

public class Product {
	public String sku;
	public Double amount;
	public String currency;
	public BigDecimal total;
	
	public Product(String sku, Double amount, String currency)
	{
		this.sku = sku;
		this.amount = amount;
		this.currency = currency;		
		this.total = new BigDecimal(0);
	}
	
	public Product(String sku, BigDecimal total, String currency)
	{
		this.sku = sku;
		this.total = total;
		this.currency = currency;	
		this.total = total;
	}
}
