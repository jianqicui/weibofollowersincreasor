package org.weibofollowersincreasor.service;

import java.util.List;

import org.weibofollowersincreasor.entity.GlobalUserPhase;
import org.weibofollowersincreasor.entity.User;
import org.weibofollowersincreasor.entity.UserPhase;
import org.weibofollowersincreasor.service.exception.ServiceException;

public interface UserService {

	void addUser(GlobalUserPhase globalUserPhase, User user)
			throws ServiceException;

	List<User> getUserList(GlobalUserPhase globalUserPhase, int index, int size)
			throws ServiceException;

	boolean isSameUserExisting(GlobalUserPhase globalUserPhase, User user)
			throws ServiceException;

	void moveUser(GlobalUserPhase fromGlobalUserPhase,
			GlobalUserPhase toGlobalUserPhase, User user)
			throws ServiceException;

	void deleteUser(GlobalUserPhase globalUserPhase, int id)
			throws ServiceException;

	void addUser(int categoryId, int typeId, int applyingUserId,
			UserPhase userPhase, User user) throws ServiceException;

	List<User> getUserListBeforeDays(int categoryId, int typeId,
			int applyingUserId, UserPhase userPhase, int days, int index,
			int size) throws ServiceException;

	void deleteUser(int categoryId, int typeId, int applyingUserId,
			UserPhase userPhase, int id) throws ServiceException;

}
