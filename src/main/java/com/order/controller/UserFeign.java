package com.order.controller;


import java.util.List; 
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import com.order.dto.BuyerDTO;
import com.order.dto.CartDTO;
@FeignClient(name="UserMS",url="http://localhost:9002")
public interface UserFeign{
	@DeleteMapping(value="/e-commerce-app/{buyerId}/cart/{prodId}/delete")
	String Delete(@PathVariable String buyerId,@PathVariable String prodId) throws Exception;
	@GetMapping(value="/e-commerce-app/{buyerId}/cart")
	List<CartDTO> CartList(@PathVariable String buyerId) throws Exception;
	@DeleteMapping(value="/e-commerce-app/{buyerId}/cart/deleteAll")
	String DeleteAll(@PathVariable String buyerId) throws Exception;
	@PostMapping(value = "/e-commerce-app/buyers/rewards/{buyerId}",  consumes = MediaType.APPLICATION_JSON_VALUE)
	String addrewards(@RequestBody Integer rewards,@PathVariable String buyerId) throws Exception;
	@GetMapping(value = "/e-commerce-app/buyer/{buyerId}",  produces = MediaType.APPLICATION_JSON_VALUE)
	BuyerDTO findBuyer(@PathVariable String buyerId) throws Exception;
	@PostMapping(value = "/e-commerce-app/update/address/{BuyerId}",  consumes = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<String> updateAddress(@PathVariable String BuyerId,@RequestBody String Address) throws Exception;
}
