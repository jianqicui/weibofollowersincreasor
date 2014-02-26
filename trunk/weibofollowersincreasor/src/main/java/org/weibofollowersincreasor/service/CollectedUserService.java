package org.weibofollowersincreasor.service;

import java.util.List;

import org.weibofollowersincreasor.entity.CollectedUser;
import org.weibofollowersincreasor.service.exception.ServiceException;

public interface CollectedUserService {

	List<CollectedUser> getCollectedUserList(int categoryId, int typeId)
			throws ServiceException;

}
