package com.livenation.mobile.android.na.models;

import java.io.Serializable;

import com.livenation.mobile.android.platform.util.Logger;

public class User implements Serializable {
	private static final long serialVersionUID = 1L;
	private final String id;	
	private String name;
	private String email;
	private String pictureUrl;
	
	public User(String id) {
		this.id = id;
	}
	
	public void setName(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getEmail() {
		return email;
	}
	
	public String getId() {
		return id;
	}
	
	public void setPictureUrl(String pictureUrl) {
		Logger.log("pic", pictureUrl);
		this.pictureUrl = pictureUrl;
	}
	
	public String getPictureUrl() {
		return pictureUrl;
	}

}
