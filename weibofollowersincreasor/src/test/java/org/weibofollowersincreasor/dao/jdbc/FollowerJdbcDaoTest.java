package org.weibofollowersincreasor.dao.jdbc;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.weibofollowersincreasor.dao.FollowerDao;
import org.weibofollowersincreasor.dao.exception.DaoException;
import org.weibofollowersincreasor.entity.Follower;
import org.weibofollowersincreasor.entity.FollowerPhase;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:applicationContext.xml" })
public class FollowerJdbcDaoTest {

	@Autowired
	private FollowerDao followerDao;

	@Test
	public void testGetFollowerList() throws DaoException {
		int categoryId = 1;
		int typeId = 1;
		FollowerPhase followerPhase = FollowerPhase.collected;
		int index = 0;
		int size = 10;

		List<Follower> followerList = followerDao.getFollowerList(categoryId,
				typeId, followerPhase, index, size);

		Assert.assertNotNull(followerList);
	}

}
