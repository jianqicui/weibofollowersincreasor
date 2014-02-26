package org.weibofollowersincreasor.dao;

import java.util.List;

import org.weibofollowersincreasor.dao.exception.DaoException;
import org.weibofollowersincreasor.entity.Follower;
import org.weibofollowersincreasor.entity.FollowerPhase;

public interface FollowerDao {

	void addFollower(int categoryId, int typeId, FollowerPhase followerPhase,
			Follower follower) throws DaoException;

	List<Follower> getFollowerList(int categoryId, int typeId,
			FollowerPhase followerPhase, int index, int size)
			throws DaoException;

	boolean isSameFollowerExisting(int categoryId, int typeId,
			FollowerPhase followerPhase, Follower follower) throws DaoException;

	void deleteFollower(int categoryId, int typeId,
			FollowerPhase followerPhase, int id) throws DaoException;

}
