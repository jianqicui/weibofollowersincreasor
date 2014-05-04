package org.weibofollowersincreasor.service;

import java.util.List;

import org.weibofollowersincreasor.entity.CollectingUser;
import org.weibofollowersincreasor.service.exception.ServiceException;

public interface CollectingUserService {

	List<CollectingUser> getCollectingUserList() throws ServiceException;

}
