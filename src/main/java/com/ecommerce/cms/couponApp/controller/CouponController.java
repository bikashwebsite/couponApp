package com.ecommerce.cms.couponApp.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ecommerce.cms.couponApp.model.CartRequest;
import com.ecommerce.cms.couponApp.model.Coupon;
import com.ecommerce.cms.couponApp.service.CouponService;

@RestController
@RequestMapping("/cms")
public class CouponController {
	
	@Autowired
	private CouponService couponService;
	
	@GetMapping("/coupons")
	public ResponseEntity<List<Coupon>> getCoupons(){
		return ResponseEntity.ok(couponService.getAllCoupons());
	}
	
	@GetMapping("/coupons/{id}")
	public ResponseEntity<Coupon> getCouponById(@PathVariable Long id){
		Optional<Coupon> couponById = couponService.findCouponById(id);
		if(couponById.isPresent()) {
			return ResponseEntity.ok(couponById.get());
		}
		else {
			return ResponseEntity.notFound().build();
		}
	}
	
	@PostMapping("/addCoupon")
	public ResponseEntity<Coupon> createCoupon(@RequestBody Coupon cpn){
		
		Coupon savedcoupon = couponService.createCoupon(cpn);
		return new ResponseEntity<>(savedcoupon, HttpStatus.CREATED);
	}
	
	@DeleteMapping("/deletCoupon/{id}")
	public ResponseEntity<Void> deleteCouponById(@PathVariable Long id){
		couponService.deleteCoupon(id);
		return ResponseEntity.noContent().build();
	}
	
	@PostMapping("/applicable-coupons")
	public ResponseEntity<Map<String, Object>> getApplicableCoupons(@RequestBody CartRequest cartRequest){
		List<Map<String, Object>> applicableCoupons = couponService.getAllApplicableCoupons(cartRequest);
		
		Map<String, Object> response = new HashMap<String, Object>();
		response.put("applicable_coupons", applicableCoupons);
		return ResponseEntity.ok(response);
	}
	
	@PostMapping("/apply-coupon/{id}")
	public ResponseEntity<Map<String, Object>> applyCouponOnCart(@PathVariable Long id, @RequestBody CartRequest cartRequest){
		Map<String, Object> result = couponService.applyCoupon(id, cartRequest);
		if(result.containsKey("error")) {
			return ResponseEntity.badRequest().body(result);
		}
		return ResponseEntity.ok(result);
		
	}
	

}
