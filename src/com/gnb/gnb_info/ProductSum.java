package com.gnb.gnb_info;

import java.math.BigDecimal;
import java.util.ArrayList;

public class ProductSum {
	public ArrayList<Product> product;
	public Double sum;
	
	public ProductSum() 
	{
		this.product = new ArrayList<Product>();
		sum = 0.0;
	}
	
	public ProductSum(ArrayList<Product> product, Double sum)
	{
		this.product = product;
		this.sum = sum;
	}
	
	public BigDecimal getSumRoudedAsBankers()
	{
		BigDecimal dd = new BigDecimal(sum).setScale(2, BigDecimal.ROUND_HALF_UP);
		return dd;
	}
}
