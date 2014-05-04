package org.weibofollowersincreasor.service.impl;

import java.util.List;

import org.weibofollowersincreasor.dao.CollectingUserDao;
import org.weibofollowersincreasor.dao.exception.DaoException;
import org.weibofollowersincreasor.entity.CollectingUser;
import org.weibofollowersincreasor.service.CollectingUserService;
import org.weibofollowersincreasor.service.exception.ServiceException;

public class CollectingUserServiceImpl implements CollectingUserService {

	private CollectingUserDao collectingUserDao;

	public void setCollectingUserDao(CollectingUserDao collectingUserDao) {
		this.collectingUserDao = collectingUserDao;
	}

	@Override
	public List<CollectingUser> getCollectingUserList() throws ServiceException {
		try {
			return collectingUserDao.getCollectingUserList();
		} catch (DaoException e) {
			throw new ServiceException(e);
		}
	}

}
