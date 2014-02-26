package org.weibofollowersincreasor.service.impl;

import java.util.List;

import org.weibofollowersincreasor.dao.CategoryDao;
import org.weibofollowersincreasor.dao.exception.DaoException;
import org.weibofollowersincreasor.entity.Category;
import org.weibofollowersincreasor.service.CategoryService;
import org.weibofollowersincreasor.service.exception.ServiceException;

public class CategoryServiceImpl implements CategoryService {

	private CategoryDao categoryDao;

	public void setCategoryDao(CategoryDao categoryDao) {
		this.categoryDao = categoryDao;
	}

	@Override
	public List<Category> getCategoryList() throws ServiceException {
		try {
			return categoryDao.getCategoryList();
		} catch (DaoException e) {
			throw new ServiceException(e);
		}
	}

}
