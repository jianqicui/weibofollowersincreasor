package org.weibofollowersincreasor.dao;

import java.util.List;

import org.weibofollowersincreasor.dao.exception.DaoException;
import org.weibofollowersincreasor.entity.ActiveUser;
import org.weibofollowersincreasor.entity.ActiveUserPhase;

public interface ActiveUserDao {

	ActiveUser getActiveUser(ActiveUserPhase activeUserPhase)
			throws DaoException;

	void updateActiveUser(ActiveUserPhase activeUserPhase, ActiveUser activeUser)
			throws DaoException;

	List<ActiveUser> getActiveUserList(int categoryId, int typeId,
			ActiveUserPhase activeUserPhase) throws DaoException;

	void updateActiveUserList(int categoryId, int typeId,
			ActiveUserPhase activeUserPhase, List<ActiveUser> activeUserList)
			throws DaoException;

}
