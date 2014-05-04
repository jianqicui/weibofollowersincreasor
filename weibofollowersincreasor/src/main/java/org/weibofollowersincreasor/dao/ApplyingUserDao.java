package org.weibofollowersincreasor.dao;

import java.util.List;

import org.weibofollowersincreasor.dao.exception.DaoException;
import org.weibofollowersincreasor.entity.ApplyingUser;

public interface ApplyingUserDao {

	List<ApplyingUser> getApplyingUserList(int categoryId, int typeId)
			throws DaoException;

	void updateApplyingUserList(int categoryId, int typeId,
			List<ApplyingUser> applyingUserList) throws DaoException;

}
