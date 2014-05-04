package org.weibofollowersincreasor.service.impl;

import java.util.List;

import org.weibofollowersincreasor.dao.UserDao;
import org.weibofollowersincreasor.dao.exception.DaoException;
import org.weibofollowersincreasor.entity.GlobalUserPhase;
import org.weibofollowersincreasor.entity.User;
import org.weibofollowersincreasor.entity.UserPhase;
import org.weibofollowersincreasor.service.UserService;
import org.weibofollowersincreasor.service.exception.ServiceException;

public class UserServiceImpl implements UserService {

	private UserDao userDao;

	public void setUserDao(UserDao userDao) {
		this.userDao = userDao;
	}

	@Override
	public void addUser(GlobalUserPhase globalUserPhase, User user)
			throws ServiceException {
		try {
			userDao.addUser(globalUserPhase, user);
		} catch (DaoException e) {
			throw new ServiceException(e);
		}
	}

	@Override
	public List<User> getUserList(GlobalUserPhase globalUserPhase, int index,
			int size) throws ServiceException {
		try {
			return userDao.getUserList(globalUserPhase, index, size);
		} catch (DaoException e) {
			throw new ServiceException(e);
		}
	}

	@Override
	public boolean isSameUserExisting(GlobalUserPhase globalUserPhase, User user)
			throws ServiceException {
		try {
			return userDao.isSameUserExisting(globalUserPhase, user);
		} catch (DaoException e) {
			throw new ServiceException(e);
		}
	}

	@Override
	public void moveUser(GlobalUserPhase fromGlobalUserPhase,
			GlobalUserPhase toGlobalUserPhase, User user)
			throws ServiceException {
		try {
			userDao.addUser(toGlobalUserPhase, user);

			userDao.deleteUser(fromGlobalUserPhase, user.getId());
		} catch (DaoException e) {
			throw new ServiceException(e);
		}
	}

	@Override
	public void deleteUser(GlobalUserPhase globalUserPhase, int id)
			throws ServiceException {
		try {
			userDao.deleteUser(globalUserPhase, id);
		} catch (DaoException e) {
			throw new ServiceException(e);
		}
	}

	@Override
	public void addUser(int categoryId, int typeId, int applyingUserId,
			UserPhase userPhase, User user) throws ServiceException {
		try {
			userDao.addUser(categoryId, typeId, applyingUserId, userPhase, user);
		} catch (DaoException e) {
			throw new ServiceException(e);
		}

	}

	@Override
	public List<User> getUserListBeforeDays(int categoryId, int typeId,
			int applyingUserId, UserPhase userPhase, int days, int index,
			int size) throws ServiceException {
		try {
			return userDao.getUserListBeforeDays(categoryId, typeId,
					applyingUserId, userPhase, days, index, size);
		} catch (DaoException e) {
			throw new ServiceException(e);
		}
	}

	@Override
	public void deleteUser(int categoryId, int typeId, int applyingUserId,
			UserPhase userPhase, int id) throws ServiceException {
		try {
			userDao.deleteUser(categoryId, typeId, applyingUserId, userPhase,
					id);
		} catch (DaoException e) {
			throw new ServiceException(e);
		}
	}

}
