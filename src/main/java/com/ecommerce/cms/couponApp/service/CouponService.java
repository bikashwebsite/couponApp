package com.ecommerce.cms.couponApp.service;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.ecommerce.cms.couponApp.model.CartRequest;
import com.ecommerce.cms.couponApp.model.Coupon;

public interface CouponService {

	Coupon createCoupon(Coupon coupon);
	List<Coupon> getAllCoupons();
	Optional<Coupon> findCouponById(Long id);
	void deleteCoupon(Long id);
	
	List<Map<String, Object>> getAllApplicableCoupons(CartRequest cartRequest);
	
	Map<String, Object> applyCoupon(Long id, CartRequest cartRequest);
}
