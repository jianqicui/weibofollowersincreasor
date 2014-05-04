package org.weibofollowersincreasor.dao;

import java.util.List;

import org.weibofollowersincreasor.dao.exception.DaoException;
import org.weibofollowersincreasor.entity.CollectingUser;

public interface CollectingUserDao {

	List<CollectingUser> getCollectingUserList() throws DaoException;

}
