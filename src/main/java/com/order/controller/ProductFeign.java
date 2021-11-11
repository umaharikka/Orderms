package com.order.controller;


import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

import com.order.dto.productDTO;
@FeignClient(name="productms",url="http://localhost:8000")
public interface ProductFeign {
	
	@GetMapping(value="product/{prodId}",  produces = MediaType.APPLICATION_JSON_VALUE)
	productDTO getProduct(@PathVariable("prodId") String prodId) throws Exception;
	@PutMapping(value = "product/{prodId}",  produces = MediaType.APPLICATION_JSON_VALUE)
	String updatestock(@PathVariable String prodId,@RequestBody Integer quantity ) throws Exception;
}
