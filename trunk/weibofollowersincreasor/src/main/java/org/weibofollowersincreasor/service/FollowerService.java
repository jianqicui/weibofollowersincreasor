package org.weibofollowersincreasor.service;

import java.util.List;

import org.weibofollowersincreasor.entity.Follower;
import org.weibofollowersincreasor.entity.FollowerPhase;
import org.weibofollowersincreasor.service.exception.ServiceException;

public interface FollowerService {

	void addFollower(int categoryId, int typeId, FollowerPhase followerPhase,
			Follower follower) throws ServiceException;

	List<Follower> getFollowerList(int categoryId, int typeId,
			FollowerPhase followerPhase, int index, int size)
			throws ServiceException;

	boolean isSameFollowerExisting(int categoryId, int typeId,
			FollowerPhase followerPhase, Follower follower)
			throws ServiceException;

	void deleteFollower(int categoryId, int typeId,
			FollowerPhase followerPhase, int id) throws ServiceException;

	void moveFollower(int categoryId, int typeId,
			FollowerPhase fromFollowerPhase, FollowerPhase toFollowerPhase,
			Follower follower) throws ServiceException;

}
