package org.weibofollowersincreasor.dao;

import java.util.List;

import org.weibofollowersincreasor.dao.exception.DaoException;
import org.weibofollowersincreasor.entity.Category;

public interface CategoryDao {

	List<Category> getCategoryList() throws DaoException;

}
