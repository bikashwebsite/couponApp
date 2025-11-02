package com.ecommerce.cms.couponApp.model;

import java.util.List;

public class CartRequest {
	
	private Cart cart;
	
	public Cart getCart() {
		return cart;
	}


	public void setCart(Cart cart) {
		this.cart = cart;
	}


	public static class Cart{
		
		private List<Item> items;

		public List<Item> getItems() {
			return items;
		}

		public void setItems(List<Item> items) {
			this.items = items;
		}
		
	}
	
	
	public static class Item{
		
		private long product_id;
		private int quantity;
		private Double price;
		
		public long getProduct_id() {
			return product_id;
		}
		public void setProduct_id(long product_id) {
			this.product_id = product_id;
		}
		public int getQuantity() {
			return quantity;
		}
		public void setQuantity(int quantity) {
			this.quantity = quantity;
		}
		public Double getPrice() {
			return price;
		}
		public void setPrice(Double price) {
			this.price = price;
		}
		
		
	}
	
	

}
