package com.order.service;

import java.util.ArrayList;    
import java.util.Date;
import java.util.List;
import java.util.Optional;
import javax.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

import com.order.controller.ProductFeign;
import com.order.controller.UserFeign;
import com.order.dto.BuyerDTO;
import com.order.dto.CartDTO;
import com.order.dto.OrderDTO;
import com.order.dto.orderstatus;
import com.order.dto.productDTO;
import com.order.dto.reorderDTO;
import com.order.entity.Order;
import com.order.entity.productordered;
import com.order.repository.OrderRepository;
import com.order.repository.ProductsOrderedRepository;
  

@Service
@Transactional
public class OrderService {
	Logger logger = LoggerFactory.getLogger(this.getClass());
	@Autowired
	OrderRepository oRepo;
	@Autowired
	ProductFeign productFeign;
	@Autowired
	ProductsOrderedRepository productsOrderedRepository;
	@Autowired
	UserFeign userFeign;
	
	public void placeOrder(String buyer,String Address) throws Exception
	{	List<CartDTO> list=null;
	try
	{
		list=userFeign.CartList(buyer);
	}
	catch(Exception e)
	{
		if(list==null)
			throw new Exception("no item in the cart to order");
	}
		for(CartDTO item :list)
			QuantityCheck(item);
		for(CartDTO item :list)
			ProceedOrder(item,Address);
		userFeign.DeleteAll(buyer);
		userFeign.updateAddress(buyer,Address);
	}
	
	public void ProductOrdered(CartDTO cartdto,String sellerId)
	{		
	productordered product=new productordered();
		product.setBuyerId(cartdto.getBuyerId());
		product.setProdId(cartdto.getProdId());
		product.setQuantity(cartdto.getQuantity());
		product.setSellerId(sellerId);	
		productsOrderedRepository.save(product);
	}
	public void ProceedOrder(CartDTO cartDTO,String Address) throws Exception {
		int rewards=0;
		productDTO ProductDTO=productFeign.getProduct(cartDTO.getProdId());
		productFeign.updatestock(cartDTO.getProdId(),ProductDTO.getStock()-cartDTO.getQuantity());
			Order ord=oRepo.findTopByOrderByOrdersIdDesc();
			String orderId=ord.getOrderId();
			String id =orderId.substring(1);
			int num = Integer.parseInt(id);
			num=num+1;
			id="O"+num;
			Order order=new Order();
			order.setOrderId(id);
			order.setAddress(Address);
			int amount=ProductDTO.getPrice()*cartDTO.getQuantity();
			rewards=amount/25;
			order.setAmount(amount);
			order.setBuyerId(cartDTO.getBuyerId());
			userFeign.addrewards(rewards,cartDTO.getBuyerId());
			 Date  date = new Date(); 
			order.setDate(date);
			order.setStatus(orderstatus.OrderPlaced);
			oRepo.save(order);
			ProductOrdered(cartDTO,ProductDTO.getSellerId());
				
	}
	 public OrderDTO viewOrder(String orderId) throws Exception {
			Optional<Order> optional = oRepo.findById(orderId);
			Order order = optional.orElseThrow(()->new Exception("Service.ORDERID_NOT_FOUND"));
			OrderDTO orderDTO= OrderEntity_to_DTO(order);
			return orderDTO;
		}
	public static OrderDTO OrderEntity_to_DTO(Order order)
	{
		OrderDTO orderDTO = new OrderDTO();
		orderDTO.setOrderId(order.getOrderId());
		orderDTO.setBuyerId(order.getBuyerId());                                                                           
		orderDTO.setAmount(order.getAmount());
		orderDTO.setAddress(order.getAddress());
		orderDTO.setDate(order.getDate());
		orderDTO.setStatus(order.getStatus());		
		return orderDTO;
	}
			
	

		public void QuantityCheck(CartDTO cartDTO) throws Exception
		{
			productDTO ProductDTO=productFeign.getProduct(cartDTO.getProdId());
			if(ProductDTO.getStock()<cartDTO.getQuantity())
				throw new Exception("order failed due to unavaliabity of required stock"
						+ " for this product :"+ProductDTO.getProdName()+"and its product is    "+ProductDTO.getProdId());
		}
		
		public void reorder(reorderDTO Reorder) throws Exception
		{	
			productordered ProductOrder=null;

		ProductOrder= productsOrderedRepository.findByBuyerIdAndProdId(Reorder.getBuyerId(), Reorder.getProdId());
		int rewards=0;
			if(ProductOrder==null)
			throw new Exception("This product not Ordered before plz order the product in different way");
			productDTO ProductDTO=productFeign.getProduct(Reorder.getProdId());
			if(ProductDTO.getStock()<Reorder.getQuantity())
				throw new Exception("order failed due to unavaliabity of required stock"
						+ " for this product :"+ProductDTO.getProdName()+"and its product is    "+ProductDTO.getProdId());
				ProductOrder.setQuantity(Reorder.getQuantity()+ProductOrder.getQuantity());
				productDTO ProductDTO1=productFeign.getProduct(Reorder.getProdId());
				productFeign.updatestock(Reorder.getProdId(),ProductDTO1.getStock()-Reorder.getQuantity());
				Order ord=oRepo.findTopByOrderByOrdersIdDesc();
				String ordersId=ord.getOrderId();
				String id =ordersId.substring(1);
				int num = Integer.parseInt(id);
				num=num+1;
				id="O"+num;
				int amount=ProductDTO.getPrice()*Reorder.getQuantity();
				rewards=amount/25;
				userFeign.addrewards(rewards,Reorder.getBuyerId());
				BuyerDTO bb= userFeign.findBuyer(Reorder.getBuyerId());
				Order order=new Order();
				order.setOrderId(id);
				order.setAddress(bb.getAddress());
				order.setAmount(ProductDTO1.getPrice()*Reorder.getQuantity());
				order.setBuyerId(Reorder.getBuyerId());
				 Date  date = new Date(); 
				order.setDate(date);
				order.setStatus(orderstatus.OrderPlaced);
				oRepo.save(order);
				
				
			}
		
		 public void updateStatus(orderstatus orderstatus,String orderId) throws Exception{
			 Optional<Order> od=oRepo.findById(orderId);
			 Order o=od.orElseThrow(()->new Exception("Service.ORDERID_NOT_FOUND"));
			 o.setStatus(orderstatus);
		 }
		public List<OrderDTO> viewOrdersByBuyer(String buyerId) throws Exception {
			List<Order> orders =  oRepo.findAllByBuyerId(buyerId);
			if(orders.isEmpty())
				throw new Exception("SERVICE.NO_ORDERS_FOUND_ON_BUYERID");
			List<OrderDTO> dtoList = new ArrayList<>();
			for(Order or :orders) 
			{
					OrderDTO odto = new OrderDTO();
					odto.setOrderId(or.getOrderId());
					odto.setBuyerId(or.getBuyerId());
					odto.setAmount(or.getAmount());
					odto.setAddress(or.getAddress());
					odto.setDate(or.getDate());
					odto.setStatus(or.getStatus());
					dtoList.add(odto);
				
			}
			
			return dtoList;
		}
		

		
/*
		@KafkaListener(topics = "kafka-cartItemss", groupId = "group_id")
	    public void consume(String message)  {
			try {
				logger.info("Consumed message: "+message);
				CartDTO cartDTO =new CartDTO();
				String[] data=message.split(" ");
				cartDTO.setBuyerId(data[0]);
				cartDTO.setProdId(data[1]);
				int quantity=Integer.parseInt(data[2]);
				cartDTO.setQuantity(quantity);
				QuantityCheck(cartDTO);
				ProceedOrder(cartDTO);
			}
	*/	
		@KafkaListener(topics = "kafka-cartItemss", groupId = "group_id")
	    public void consume(String message)
		{
			try {
				logger.info("Consumed message: "+message);
			CartDTO cartDTO =new CartDTO();
			String[] data=message.split(" ");
			cartDTO.setBuyerId(data[0]);
			cartDTO.setProdId(data[1]);
			int quantity=Integer.parseInt(data[2]);
			cartDTO.setQuantity(quantity);
			QuantityCheck(cartDTO);
			int rewards=0; 
				productDTO ProductDTO=productFeign.getProduct(cartDTO.getProdId());
				productFeign.updatestock(cartDTO.getProdId(),ProductDTO.getStock()-cartDTO.getQuantity());
				Order ord=oRepo.findTopByOrderByOrdersIdDesc();
				String orderId=ord.getOrderId();
				String id =orderId.substring(1);
				int num = Integer.parseInt(id);
				num=num+1;
				id="O"+num;
				BuyerDTO bb= userFeign.findBuyer(cartDTO.getBuyerId());
				Order order=new Order();
				order.setOrderId(id);
				order.setAddress(bb.getAddress());
				int amount=ProductDTO.getPrice()*cartDTO.getQuantity();
				rewards=amount/25;
				order.setAmount(amount);
				order.setBuyerId(cartDTO.getBuyerId());
				userFeign.addrewards(rewards,cartDTO.getBuyerId());
				Date  date = new Date(); 
				order.setDate(date);
				order.setStatus(orderstatus.OrderPlaced);
				oRepo.save(order);
				productordered ProductOrder=null;
				ProductOrder= productsOrderedRepository.findByBuyerIdAndProdId(cartDTO.getBuyerId(), cartDTO.getProdId());
					if(ProductOrder==null)
						ProductOrdered(cartDTO,ProductDTO.getSellerId());
					else
						ProductOrder.setQuantity(ProductOrder.getQuantity()+cartDTO.getQuantity());
					userFeign.Delete(cartDTO.getBuyerId(), cartDTO.getProdId());
			}
			catch(Exception exception)
			{
				logger.warn(exception.getMessage());
			}
	    }
	
}
	
