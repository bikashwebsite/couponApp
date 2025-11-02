package com.ecommerce.cms.couponApp.service;

import com.ecommerce.cms.couponApp.model.*;
import com.ecommerce.cms.couponApp.repository.CouponRepository;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

public class CouponServiceTest {

    @Test
    public void testApplyCartWiseCoupon_Basic() {
        // mock repository
        CouponRepository mockRepo = Mockito.mock(CouponRepository.class);

        // service object
        CouponServiceImpl service = new CouponServiceImpl(mockRepo);

        // create coupon
        Coupon coupon = new Coupon();
        coupon.setId(1L);
        coupon.setType(CouponType.CART_WISE);
        coupon.setActive(true);
        coupon.setExpiresAt(LocalDateTime.now().plusDays(2));
        coupon.setDetailsJson("{\"threshold\":100.0,\"discount\":10.0}");

        // mock repo call
        Mockito.when(mockRepo.findById(1L)).thenReturn(Optional.of(coupon));

        // create a simple cart
        CartRequest.Item item = new CartRequest.Item();
        item.setProduct_id(1L);
        item.setQuantity(3);
        item.setPrice(50.0);

        CartRequest.Cart cart = new CartRequest.Cart();
        cart.setItems(List.of(item));

        CartRequest cartRequest = new CartRequest();
        cartRequest.setCart(cart);

        // call the service
        Map<String, Object> response = service.applyCoupon(1L, cartRequest);

        // basic validations
        assertNotNull(response.get("updated_cart"));

        Map<String, Object> updatedCart = (Map<String, Object>) response.get("updated_cart");

        double totalPrice = ((Number) updatedCart.get("total_price")).doubleValue();
        double totalDiscount = ((Number) updatedCart.get("total_discount")).doubleValue();
        double finalPrice = ((Number) updatedCart.get("final_price")).doubleValue();

        // print the values to see output
        System.out.println("Total Price: " + totalPrice);
        System.out.println("Discount: " + totalDiscount);
        System.out.println("Final Price: " + finalPrice);

        // quick assertions
        assertEquals(150.0, totalPrice);
        assertEquals(15.0, totalDiscount);
        assertEquals(135.0, finalPrice);
    }
}
