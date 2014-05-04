package org.weibofollowersincreasor.service.impl;

import org.weibofollowersincreasor.dao.QueryingUserDao;
import org.weibofollowersincreasor.dao.exception.DaoException;
import org.weibofollowersincreasor.entity.QueryingUser;
import org.weibofollowersincreasor.service.QueryingUserService;
import org.weibofollowersincreasor.service.exception.ServiceException;

public class QueryingUserServiceImpl implements QueryingUserService {

	private QueryingUserDao queryingUserDao;

	public void setQueryingUserDao(QueryingUserDao queryingUserDao) {
		this.queryingUserDao = queryingUserDao;
	}

	@Override
	public QueryingUser getQueryingUser() throws ServiceException {
		try {
			return queryingUserDao.getQueryingUser();
		} catch (DaoException e) {
			throw new ServiceException(e);
		}
	}

	@Override
	public void updateQueryingUser(QueryingUser queryingUser)
			throws ServiceException {
		try {
			queryingUserDao.updateQueryingUser(queryingUser);
		} catch (DaoException e) {
			throw new ServiceException(e);
		}
	}

}
