package org.weibofollowersincreasor.dao;

import java.util.List;

import org.weibofollowersincreasor.dao.exception.DaoException;
import org.weibofollowersincreasor.entity.Type;

public interface TypeDao {

	List<Type> getTypeList(int categoryId) throws DaoException;

}
