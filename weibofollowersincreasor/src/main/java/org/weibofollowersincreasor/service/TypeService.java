package org.weibofollowersincreasor.service;

import java.util.List;

import org.weibofollowersincreasor.entity.Type;
import org.weibofollowersincreasor.service.exception.ServiceException;

public interface TypeService {

	List<Type> getTypeList(int categoryId) throws ServiceException;

}
