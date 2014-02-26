package org.weibofollowersincreasor.service.impl;

import java.util.List;

import org.weibofollowersincreasor.dao.TypeDao;
import org.weibofollowersincreasor.dao.exception.DaoException;
import org.weibofollowersincreasor.entity.Type;
import org.weibofollowersincreasor.service.TypeService;
import org.weibofollowersincreasor.service.exception.ServiceException;

public class TypeServiceImpl implements TypeService {

	private TypeDao typeDao;

	public void setTypeDao(TypeDao typeDao) {
		this.typeDao = typeDao;
	}

	@Override
	public List<Type> getTypeList(int categoryId) throws ServiceException {
		try {
			return typeDao.getTypeList(categoryId);
		} catch (DaoException e) {
			throw new ServiceException(e);
		}
	}

}
