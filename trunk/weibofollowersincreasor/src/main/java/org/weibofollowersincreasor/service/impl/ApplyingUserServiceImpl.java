package org.weibofollowersincreasor.service.impl;

import java.util.List;

import org.weibofollowersincreasor.dao.ApplyingUserDao;
import org.weibofollowersincreasor.dao.exception.DaoException;
import org.weibofollowersincreasor.entity.ApplyingUser;
import org.weibofollowersincreasor.service.ApplyingUserService;
import org.weibofollowersincreasor.service.exception.ServiceException;

public class ApplyingUserServiceImpl implements ApplyingUserService {

	private ApplyingUserDao applyingUserDao;

	public void setApplyingUserDao(ApplyingUserDao applyingUserDao) {
		this.applyingUserDao = applyingUserDao;
	}

	@Override
	public List<ApplyingUser> getApplyingUserList(int categoryId, int typeId)
			throws ServiceException {
		try {
			return applyingUserDao.getApplyingUserList(categoryId, typeId);
		} catch (DaoException e) {
			throw new ServiceException(e);
		}
	}

	@Override
	public void updateApplyingUserList(int categoryId, int typeId,
			List<ApplyingUser> applyingUserList) throws ServiceException {
		try {
			applyingUserDao.updateApplyingUserList(categoryId, typeId,
					applyingUserList);
		} catch (DaoException e) {
			throw new ServiceException(e);
		}
	}

}
