package org.weibofollowersincreasor.dao.jdbc;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.weibofollowersincreasor.dao.CollectedUserDao;
import org.weibofollowersincreasor.dao.exception.DaoException;
import org.weibofollowersincreasor.entity.CollectedUser;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:applicationContext.xml" })
public class CollectedUserJdbcDaoTest {

	@Autowired
	private CollectedUserDao collectedUserDao;

	@Test
	public void testGetCollectedUserList() throws DaoException {
		int categoryId = 1;
		int typeId = 1;

		List<CollectedUser> collectedUserList = collectedUserDao
				.getCollectedUserList(categoryId, typeId);

		Assert.assertNotNull(collectedUserList);
	}

}
