package org.weibofollowersincreasor.dao;

import org.weibofollowersincreasor.dao.exception.DaoException;
import org.weibofollowersincreasor.entity.QueryingUser;

public interface QueryingUserDao {

	QueryingUser getQueryingUser() throws DaoException;

	void updateQueryingUser(QueryingUser queryingUser) throws DaoException;

}
