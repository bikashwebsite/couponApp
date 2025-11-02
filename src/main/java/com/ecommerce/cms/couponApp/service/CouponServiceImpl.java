package com.ecommerce.cms.couponApp.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ecommerce.cms.couponApp.model.CartRequest;
import com.ecommerce.cms.couponApp.model.CartRequest.Item;
import com.ecommerce.cms.couponApp.model.Coupon;
import com.ecommerce.cms.couponApp.repository.CouponRepository;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class CouponServiceImpl implements CouponService{
	
	//@Autowired
	//private CouponRepository repository;
	
	private final CouponRepository repository;

    @Autowired
    public CouponServiceImpl(CouponRepository repository) {
        this.repository = repository;
    }
	
	private final ObjectMapper objectMapper = new ObjectMapper();
	
	@Override
	public List<Coupon> getAllCoupons(){
		return repository.findAll();
	}
	
	@Override
	public Optional<Coupon> findCouponById(Long id) {
		return repository.findById(id);
	}
	
	@Override
	public Coupon createCoupon(Coupon coupon) {
		return repository.save(coupon);
	}
	
	@Override
	public void deleteCoupon(Long id) {
	     repository.deleteById(id);
	}
	
	@Override
	public List<Map<String, Object>> getAllApplicableCoupons(CartRequest cartrequest){
		
		List<Map<String, Object>> applicableCoupons = new ArrayList<>();
	 try {
		 if (cartrequest == null || cartrequest.getCart() == null || cartrequest.getCart().getItems().isEmpty()) {
	            Map<String, Object> error = new LinkedHashMap<>();
	            error.put("error", true);
	            error.put("message", "Cart is empty or invalid");
	            applicableCoupons.add(error);
	            return applicableCoupons;
	        }
		 
		List<Coupon> coupons = repository.findAll();
		
		if (coupons == null || coupons.isEmpty()) {
            Map<String, Object> error = new LinkedHashMap<>();
            error.put("error", true);
            error.put("message", "No coupons available");
            applicableCoupons.add(error);
            return applicableCoupons;
        }
		
		double totalCartValue = cartrequest.getCart().getItems()
									.stream().mapToDouble(item -> item.getPrice() * item.getQuantity()).sum();
		
		for(Coupon coupon : coupons) {
			try {
				Map<String, Object> details = objectMapper.readValue(coupon.getDetailsJson(), Map.class);
				Map<String, Object> result = new LinkedHashMap<String, Object>();
				
				switch(coupon.getType()) {
				   case CART_WISE ->{
					   double threshold = ((Number)details.get("threshold")).doubleValue();
					   double discount = ((Number)details.get("discount")).doubleValue();
					   
					   if(totalCartValue >= threshold) {
						   double discountAmt = (totalCartValue * discount)/100;
						   result.put("coupon_id", coupon.getId());
						   result.put("type", coupon.getType());
						   result.put("discount", discountAmt);
						   
						   applicableCoupons.add(result);
						   
					   }
				   }
				   
				   case PRODUCT_WISE ->{
					   int productid = (int)details.get("product_id");
					   double discount = ((Number)details.get("discount")).doubleValue();
					   
					   cartrequest.getCart().getItems()
					   	.stream().forEach(item ->{
					   		if(item.getProduct_id() == productid) {
					   			double discountAmt = (item.getPrice() * item.getQuantity() * discount)/100;
					   		    result.put("coupon_id", coupon.getId());
							    result.put("type", coupon.getType());
							    result.put("discount", discountAmt);
							   
							    applicableCoupons.add(result);
					   		}
					   	});
				   }
				   
				   case BXGY ->{
					   try {
						 List<Map<String, Object>> buyProducts = (List<Map<String, Object>>) details.get("buy_products");
						 List<Map<String, Object>> getProducts = (List<Map<String, Object>>) details.get("get_products");
						 int repitationLimit = (int)details.get("repition_limit");
						 
						 int totalCouponApplicableTime = 0;
						 
						 for(Map<String, Object> buyItem : buyProducts) {
							 int buyProductId = (int) buyItem.get("product_id");
							 int requiredQty = (int) buyItem.get("quantity");
							 
							 Optional<CartRequest.Item> ItemOpt = cartrequest.getCart().getItems().stream()
							 			.filter(i -> i.getProduct_id() == buyProductId).findFirst();
							 
							 if(ItemOpt.isPresent()) {
								 int cartQty = ItemOpt.get().getQuantity();
								 totalCouponApplicableTime += cartQty/requiredQty;
							 }
						 }
						 
						 totalCouponApplicableTime = Math.min(repitationLimit, totalCouponApplicableTime);
						 
						 if(totalCouponApplicableTime > 0) {
							 double discountAmt = 0;
							 for(Map<String, Object> getItem : getProducts) {
								 int getProductId = (int)getItem.get("product_id");
								 int freeQtyPerApply = (int)getItem.get("quantity");
								 
								 Optional<CartRequest.Item> getOptItem = cartrequest.getCart().getItems().stream()
								 				.filter(i -> i.getProduct_id() == getProductId).findFirst();
								 if(getOptItem.isPresent()) {
									 double price = getOptItem.get().getPrice();
									 discountAmt += freeQtyPerApply * price * totalCouponApplicableTime;
								 }
							 }
							 
							 if(discountAmt > 0) {
								    result.put("coupon_id", coupon.getId());
								    result.put("type", coupon.getType());
								    result.put("discount", discountAmt);
								   
								    applicableCoupons.add(result);
							 }
						 }
						 
						 
						 
					} catch (Exception e) {
						e.printStackTrace();
					}
				   }
				   
				}
				
			} catch (Exception e) {
				e.printStackTrace();
			} 
		}
	  }catch (Exception e) {
	        Map<String, Object> error = new LinkedHashMap<>();
	        error.put("error", true);
	        error.put("message", "Unexpected error occurred while fetching applicable coupons");
	        applicableCoupons.clear();
	        applicableCoupons.add(error);
	    }
		return applicableCoupons;
	}
	
	@Override
	public Map<String, Object> applyCoupon(Long id, CartRequest cartRequest){
		
		Map<String, Object> response = new HashMap<>();
		
	try {
		Optional<Coupon> ApplyingCoupon = repository.findById(id);
		
		if(ApplyingCoupon.isEmpty()) {
			response.put("error", true);
            response.put("message", "Coupon not found with id: " + id);
			return response;
		}
		
		Coupon coupon = ApplyingCoupon.get();
		
		if(!coupon.isActive()) {
			response.put("error", true);
            response.put("message", "Coupon is not active");
			return response;
		}
		
		if(coupon.getExpiresAt() != null && coupon.getExpiresAt().isBefore(LocalDateTime.now())) {
			response.put("error", true);
            response.put("message", "Coupon has expired");
			return response;
		}
		
		if (cartRequest == null || cartRequest.getCart() == null || cartRequest.getCart().getItems().isEmpty()) {
            response.put("error", true);
            response.put("message", "Cart is empty or invalid");
            return response;
        }
		
		List<CartRequest.Item> originalItems = cartRequest.getCart().getItems();
		List<Map<String, Object>> updatedItems = new ArrayList<>();
		
		double OriginaltotalAmt = originalItems.stream()
										.mapToDouble(i -> i.getQuantity() * i.getPrice()).sum();
		double totalDiscount = 0.0;
		
		try {
			Map<String, Object> details = objectMapper.readValue(coupon.getDetailsJson(), Map.class);
			
			switch(coupon.getType()) {
				case CART_WISE ->{
					double threshold = ((Number)details.getOrDefault("threshold", 0.0)).doubleValue();
					double discountpct = ((Number)details.getOrDefault("discount", 0.0)).doubleValue();
					
					if(OriginaltotalAmt > threshold) {
						totalDiscount = (OriginaltotalAmt * discountpct) /100.0;
						
						for(CartRequest.Item items : originalItems) {
							Map<String, Object> m = new LinkedHashMap<String, Object>();
							double lineTotal = items.getPrice() * items.getQuantity();
							double lineDiscount = (lineTotal * totalDiscount) / OriginaltotalAmt;
							
							m.put("product_id", items.getProduct_id());
							m.put("quantity", items.getQuantity());
							m.put("price", items.getPrice());
							m.put("total_discount", round(lineDiscount));
							
							updatedItems.add(m);
						}
					}
					else {
						for(CartRequest.Item items : originalItems) {
							Map<String, Object> m = new LinkedHashMap<String, Object>();
							
							m.put("product_id", items.getProduct_id());
							m.put("quantity", items.getQuantity());
							m.put("price", items.getPrice());
							m.put("total_discount", 0.0);
							
							updatedItems.add(m);
						}
					}
				}
				
				case PRODUCT_WISE ->{
					int targetproductId = ((Number)details.getOrDefault("product_id", 0)).intValue();
					double discountPct = ((Number)details.getOrDefault("discount", 0.0)).doubleValue();
					
					for(CartRequest.Item items : originalItems) {
						Map<String, Object> m = new LinkedHashMap<String, Object>();
						m.put("product_id", items.getProduct_id());
						m.put("quantity", items.getQuantity());
						m.put("price", items.getPrice());
						if (items.getProduct_id() == targetproductId) {
							double lineTotal = items.getPrice() * items.getQuantity();
							double lineDiscount = (lineTotal * discountPct) / 100.0; 
							m.put("total_discount", round(lineDiscount));
							totalDiscount +=lineDiscount;
						}
						else {
							m.put("total_discount", 0.0);
						}
						updatedItems.add(m);
					}
					
				}
				
				case BXGY ->{
					List<Map<String, Object>> buyProducts = (List<Map<String, Object>>)details.get("buy_products");
					List<Map<String, Object>> getProducts = (List<Map<String, Object>>)details.get("get_products");
					int repetationLimit = (int)details.getOrDefault("repition_limit", 1);
					
					if (buyProducts == null || getProducts == null) {
	                    response.put("error", true);
	                    response.put("message", "Invalid BXGY coupon configuration");
	                    return response;
	                }
					
					int totalgroups =0;
					
					for(Map<String, Object> buyItem : buyProducts) {
						long buyId = ((Number)buyItem.get("product_id")).longValue();
						int reqQty = ((Number)buyItem.get("quantity")).intValue();
						
						int cartQty = originalItems.stream()
										.mapToInt(CartRequest.Item :: getQuantity)
										.findFirst().orElse(0);
						if(cartQty >= reqQty) {
							totalgroups += (cartQty/reqQty);
						}
					}
					
					int repeat = Math.min(repetationLimit, totalgroups);
					
					if(repeat <= 0) {
						for(CartRequest.Item items : originalItems) {
							Map<String, Object> m = new LinkedHashMap<String, Object>();
							
							m.put("product_id", items.getProduct_id());
							m.put("quantity", items.getQuantity());
							m.put("price", items.getPrice());
							m.put("total_discount", 0.0);
							
							updatedItems.add(m);
						}
						break;
					}
					
					Map<Long, Integer> updatedQty = new HashMap<Long, Integer>();
					for(CartRequest.Item items : originalItems) {
						updatedQty.put(items.getProduct_id(), items.getQuantity());
					}
					
					double freeValue =0.0;
					for(Map<String, Object> getItem : getProducts) {
						long freePid = ((Number)getItem.get("product_id")).longValue();
						int freePerGroup = ((Number)getItem.get("quantity")).intValue();
						int totalFreeQty = freePerGroup * repeat;
						
						Optional<CartRequest.Item> freeItem = originalItems.stream()
							.filter(i -> i.getProduct_id() == freePid).findFirst();
						
						if(freeItem.isPresent()) {
							CartRequest.Item fi = freeItem.get();
							updatedQty.put(freePid, updatedQty.getOrDefault(freePid, 0) + totalFreeQty);
							freeValue += fi.getPrice() * totalFreeQty;	
						}
					}
					
					for(CartRequest.Item items : originalItems) {
						Map<String, Object> m = new LinkedHashMap<String, Object>();
						long pid = items.getProduct_id();
						int newQty = updatedQty.getOrDefault(pid, items.getQuantity());
						
						m.put("product_id", pid);
						m.put("quantity", newQty);
						m.put("price", items.getPrice());
						
						int addedQty = newQty - items.getQuantity();
						if(addedQty > 0) {
							double lineDiscount = addedQty * items.getPrice();
							m.put("total_discount", round(lineDiscount));
						} else {
				            m.put("total_discount", 0.0); 
				        }
						updatedItems.add(m);
					}
					
					totalDiscount = freeValue;
				}
				
				default ->{
					throw new RuntimeException("Unsupported Coupon type");
				}
			}
			
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		double totalPrice = updatedItems.stream()
					.mapToDouble(i -> ((Number)i.get("quantity")).doubleValue() * ((Number)i.get("price")).doubleValue()).sum();
				
		double finalprice = totalPrice - totalDiscount;
		
		Map<String, Object> updatedCart = new LinkedHashMap<String, Object>();
		updatedCart.put("items", updatedItems);
		updatedCart.put("total_price", totalPrice);
		updatedCart.put("total_discount", round(totalDiscount));
		updatedCart.put("final_price", finalprice);
		
		
		response.put("updated_cart", updatedCart);
	}catch (Exception e) {
        response.put("error", true);
        response.put("message", "An unexpected error occurred while applying the coupon");
    }
		return response;
	}
	
	
	private double round(double value) {
		
		return Math.round(value * 100)/100.0;
	}

}
