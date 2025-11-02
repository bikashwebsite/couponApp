package com.ecommerce.cms.couponApp.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ecommerce.cms.couponApp.model.Coupon;

public interface CouponRepository extends JpaRepository<Coupon, Long>{
	
	

}
