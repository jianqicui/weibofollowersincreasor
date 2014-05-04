package org.weibofollowersincreasor.dao;

import java.util.List;

import org.weibofollowersincreasor.dao.exception.DaoException;
import org.weibofollowersincreasor.entity.GlobalUserPhase;
import org.weibofollowersincreasor.entity.User;
import org.weibofollowersincreasor.entity.UserPhase;

public interface UserDao {

	void addUser(GlobalUserPhase globalUserPhase, User user)
			throws DaoException;

	List<User> getUserList(GlobalUserPhase globalUserPhase, int index, int size)
			throws DaoException;

	boolean isSameUserExisting(GlobalUserPhase globalUserPhase, User user)
			throws DaoException;

	void deleteUser(GlobalUserPhase globalUserPhase, int id)
			throws DaoException;

	void addUser(int categoryId, int typeId, int applyingUserId,
			UserPhase userPhase, User user) throws DaoException;

	List<User> getUserListBeforeDays(int categoryId, int typeId,
			int applyingUserId, UserPhase userPhase, int days, int index,
			int size) throws DaoException;

	void deleteUser(int categoryId, int typeId, int applyingUserId,
			UserPhase userPhase, int id) throws DaoException;

}
