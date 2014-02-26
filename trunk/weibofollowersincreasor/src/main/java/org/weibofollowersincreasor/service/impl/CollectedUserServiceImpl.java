package org.weibofollowersincreasor.service.impl;

import java.util.List;

import org.weibofollowersincreasor.dao.CollectedUserDao;
import org.weibofollowersincreasor.dao.exception.DaoException;
import org.weibofollowersincreasor.entity.CollectedUser;
import org.weibofollowersincreasor.service.CollectedUserService;
import org.weibofollowersincreasor.service.exception.ServiceException;

public class CollectedUserServiceImpl implements CollectedUserService {

	private CollectedUserDao collectedUserDao;

	public void setCollectedUserDao(CollectedUserDao collectedUserDao) {
		this.collectedUserDao = collectedUserDao;
	}

	@Override
	public List<CollectedUser> getCollectedUserList(int categoryId, int typeId)
			throws ServiceException {
		try {
			return collectedUserDao.getCollectedUserList(categoryId, typeId);
		} catch (DaoException e) {
			throw new ServiceException(e);
		}
	}

}
