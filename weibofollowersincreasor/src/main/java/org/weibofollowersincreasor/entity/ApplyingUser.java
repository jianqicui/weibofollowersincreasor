package org.weibofollowersincreasor.entity;

public class ApplyingUser {

	private int id;
	private byte[] cookies;
	private int followingIndex;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public byte[] getCookies() {
		return cookies;
	}

	public void setCookies(byte[] cookies) {
		this.cookies = cookies;
	}

	public int getFollowingIndex() {
		return followingIndex;
	}

	public void setFollowingIndex(int followingIndex) {
		this.followingIndex = followingIndex;
	}

}
