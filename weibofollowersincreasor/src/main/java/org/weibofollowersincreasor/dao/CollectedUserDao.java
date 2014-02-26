package org.weibofollowersincreasor.dao;

import java.util.List;

import org.weibofollowersincreasor.dao.exception.DaoException;
import org.weibofollowersincreasor.entity.CollectedUser;

public interface CollectedUserDao {

	List<CollectedUser> getCollectedUserList(int categoryId, int typeId)
			throws DaoException;

}
