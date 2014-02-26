package org.weibofollowersincreasor.service;

import java.util.List;

import org.weibofollowersincreasor.entity.Category;
import org.weibofollowersincreasor.service.exception.ServiceException;

public interface CategoryService {

	List<Category> getCategoryList() throws ServiceException;

}
