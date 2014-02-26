package org.weibofollowersincreasor.entity;

import java.util.List;

public class Followers {

	private int previousCursor;
	private int nextCursor;
	private List<Follower> followerList;

	public int getPreviousCursor() {
		return previousCursor;
	}

	public void setPreviousCursor(int previousCursor) {
		this.previousCursor = previousCursor;
	}

	public int getNextCursor() {
		return nextCursor;
	}

	public void setNextCursor(int nextCursor) {
		this.nextCursor = nextCursor;
	}

	public List<Follower> getFollowerList() {
		return followerList;
	}

	public void setFollowerList(List<Follower> followerList) {
		this.followerList = followerList;
	}

}
