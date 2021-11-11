package com.order.repository;



import org.springframework.data.jpa.repository.JpaRepository;

import com.order.entity.productordered;

public interface ProductsOrderedRepository extends JpaRepository<productordered, String> {
	
	productordered findByBuyerIdAndProdId(String BuyerId,String prodId);

}
