package org.weibofollowersincreasor.service.impl;

import java.util.List;

import org.weibofollowersincreasor.dao.FollowerDao;
import org.weibofollowersincreasor.dao.exception.DaoException;
import org.weibofollowersincreasor.entity.Follower;
import org.weibofollowersincreasor.entity.FollowerPhase;
import org.weibofollowersincreasor.service.FollowerService;
import org.weibofollowersincreasor.service.exception.ServiceException;

public class FollowerServiceImpl implements FollowerService {

	private FollowerDao followerDao;

	public void setFollowerDao(FollowerDao followerDao) {
		this.followerDao = followerDao;
	}

	@Override
	public void addFollower(int categoryId, int typeId,
			FollowerPhase followerPhase, Follower follower)
			throws ServiceException {
		try {
			followerDao
					.addFollower(categoryId, typeId, followerPhase, follower);
		} catch (DaoException e) {
			throw new ServiceException(e);
		}

	}

	@Override
	public List<Follower> getFollowerList(int categoryId, int typeId,
			FollowerPhase followerPhase, int index, int size)
			throws ServiceException {
		try {
			return followerDao.getFollowerList(categoryId, typeId,
					followerPhase, index, size);
		} catch (DaoException e) {
			throw new ServiceException(e);
		}
	}

	@Override
	public boolean isSameFollowerExisting(int categoryId, int typeId,
			FollowerPhase followerPhase, Follower follower)
			throws ServiceException {
		try {
			return followerDao.isSameFollowerExisting(categoryId, typeId,
					followerPhase, follower);
		} catch (DaoException e) {
			throw new ServiceException(e);
		}
	}

	@Override
	public void deleteFollower(int categoryId, int typeId,
			FollowerPhase followerPhase, int id) throws ServiceException {
		try {
			followerDao.deleteFollower(categoryId, typeId, followerPhase, id);
		} catch (DaoException e) {
			throw new ServiceException(e);
		}
	}

	@Override
	public void moveFollower(int categoryId, int typeId,
			FollowerPhase fromFollowerPhase, FollowerPhase toFollowerPhase,
			Follower follower) throws ServiceException {
		try {
			followerDao.addFollower(categoryId, typeId, toFollowerPhase,
					follower);

			followerDao.deleteFollower(categoryId, typeId, fromFollowerPhase,
					follower.getId());
		} catch (DaoException e) {
			throw new ServiceException(e);
		}
	}

	@Override
	public List<Follower> getFollowerListBeforeDays(int categoryId, int typeId,
			FollowerPhase followerPhase, int days, int index, int size)
			throws ServiceException {
		try {
			return followerDao.getFollowerListBeforeDays(categoryId, typeId,
					followerPhase, days, index, size);
		} catch (DaoException e) {
			throw new ServiceException(e);
		}
	}

}
