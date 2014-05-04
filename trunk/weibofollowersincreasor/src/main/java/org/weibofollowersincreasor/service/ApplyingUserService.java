package org.weibofollowersincreasor.service;

import java.util.List;

import org.weibofollowersincreasor.entity.ApplyingUser;
import org.weibofollowersincreasor.service.exception.ServiceException;

public interface ApplyingUserService {

	List<ApplyingUser> getApplyingUserList(int categoryId, int typeId)
			throws ServiceException;

	void updateApplyingUserList(int categoryId, int typeId,
			List<ApplyingUser> applyingUserList) throws ServiceException;

}
