package org.weibofollowersincreasor.dao.jdbc;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.weibofollowersincreasor.dao.UserDao;
import org.weibofollowersincreasor.dao.exception.DaoException;
import org.weibofollowersincreasor.entity.GlobalUserPhase;
import org.weibofollowersincreasor.entity.User;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:applicationContext.xml" })
public class UserJdbcDaoTest {

	@Autowired
	private UserDao userDaoDao;

	@Test
	public void testGetUserList() throws DaoException {
		GlobalUserPhase globalUserPhase = GlobalUserPhase.collected;
		int index = 0;
		int size = 10;

		List<User> userList = userDaoDao.getUserList(globalUserPhase, index,
				size);

		Assert.assertNotNull(userList);
	}

}
