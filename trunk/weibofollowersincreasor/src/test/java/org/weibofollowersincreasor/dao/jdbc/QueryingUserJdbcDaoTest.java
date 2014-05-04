package org.weibofollowersincreasor.dao.jdbc;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.weibofollowersincreasor.dao.QueryingUserDao;
import org.weibofollowersincreasor.dao.exception.DaoException;
import org.weibofollowersincreasor.entity.QueryingUser;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:applicationContext.xml" })
public class QueryingUserJdbcDaoTest {

	@Autowired
	private QueryingUserDao queryingUserDao;

	@Test
	public void testGetQueryingUser() throws DaoException {
		QueryingUser queryingUser = queryingUserDao.getQueryingUser();

		Assert.assertNotNull(queryingUser);
	}

}
