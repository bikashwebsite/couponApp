package com.ecommerce.cms.couponApp.model;


import java.time.LocalDateTime;


import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;

@Entity
@Table(name= "coupons")
public class Coupon {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private long id;
	
	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private CouponType type; 
	
	@Lob
	@Column(name="detail_json", columnDefinition = "TEXT")
	private String detailsJson;
	
	@Column(name="Code", unique = true)
	private String code;
	
	@Column(name="active", nullable = false)
	private boolean active=true;
	
	@Column(name="Expire_At")
	private LocalDateTime expiresAt;
	
	@Column(name="Created_At", nullable = false)
	private LocalDateTime createdAt = LocalDateTime.now();

	public Coupon() {
		super();
		// TODO Auto-generated constructor stub
	}

	public Coupon(long id, CouponType type, String detailsJson, String code, boolean active, LocalDateTime expiresAt,
			LocalDateTime createdAt) {
		super();
		this.id = id;
		this.type = type;
		this.detailsJson = detailsJson;
		this.code = code;
		this.active = active;
		this.expiresAt = expiresAt;
		this.createdAt = LocalDateTime.now();
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public CouponType getType() {
		return type;
	}

	public void setType(CouponType type) {
		this.type = type;
	}

	public String getDetailsJson() {
		return detailsJson;
	}

	public void setDetailsJson(String detailsJson) {
		this.detailsJson = detailsJson;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public boolean isActive() {
		return active;
	}

	public void setActive(boolean active) {
		this.active = active;
	}

	public LocalDateTime getExpiresAt() {
		return expiresAt;
	}

	public void setExpiresAt(LocalDateTime expiresAt) {
		this.expiresAt = expiresAt;
	}

	public LocalDateTime getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(LocalDateTime createdAt) {
		this.createdAt = createdAt;
	}

	@Override
	public String toString() {
		return "Coupon [id=" + id + ", type=" + type + ", detailsJson=" + detailsJson + ", code=" + code + ", active="
				+ active + ", expiresAt=" + expiresAt + ", createdAt=" + createdAt + "]";
	}
	
	
	
}
