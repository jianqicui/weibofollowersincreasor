package org.weibofollowersincreasor.service;

import org.weibofollowersincreasor.entity.QueryingUser;
import org.weibofollowersincreasor.service.exception.ServiceException;

public interface QueryingUserService {

	QueryingUser getQueryingUser() throws ServiceException;

	void updateQueryingUser(QueryingUser queryingUser) throws ServiceException;

}
