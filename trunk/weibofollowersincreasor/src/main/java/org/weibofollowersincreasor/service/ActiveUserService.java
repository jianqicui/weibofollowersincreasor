package org.weibofollowersincreasor.service;

import java.util.List;

import org.weibofollowersincreasor.entity.ActiveUser;
import org.weibofollowersincreasor.entity.ActiveUserPhase;
import org.weibofollowersincreasor.service.exception.ServiceException;

public interface ActiveUserService {

	ActiveUser getActiveUser(ActiveUserPhase activeUserPhase)
			throws ServiceException;

	void updateActiveUser(ActiveUserPhase activeUserPhase, ActiveUser activeUser)
			throws ServiceException;

	List<ActiveUser> getActiveUserList(int categoryId, int typeId,
			ActiveUserPhase activeUserPhase) throws ServiceException;

	void updateActiveUserList(int categoryId, int typeId,
			ActiveUserPhase activeUserPhase, List<ActiveUser> activeUserList)
			throws ServiceException;

}
