package org.weibofollowersincreasor.dao.jdbc;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.weibofollowersincreasor.dao.UserDao;
import org.weibofollowersincreasor.dao.exception.DaoException;
import org.weibofollowersincreasor.entity.GlobalUserPhase;
import org.weibofollowersincreasor.entity.User;
import org.weibofollowersincreasor.entity.UserPhase;

public class UserJdbcDao implements UserDao {

	private JdbcTemplate jdbcTemplate;

	public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	private String getTableName(GlobalUserPhase globalUserPhase) {
		String tableName = "user_" + globalUserPhase;

		return tableName;
	}

	private String getTableName(int categoryId, int typeId, int applyingUserId,
			UserPhase userPhase) {
		String tableName = "category" + categoryId + "_type" + typeId + "_user"
				+ applyingUserId + "_" + userPhase;

		return tableName;
	}

	private RowMapper<User> rowMapper = new UserRowMapper();

	private class UserRowMapper implements RowMapper<User> {

		@Override
		public User mapRow(ResultSet rs, int rowNum) throws SQLException {
			User user = new User();

			try {
				user.setId(rs.getInt("id"));
				user.setUserId(rs.getString("user_id"));
			} catch (SQLException e) {
				throw e;
			}

			return user;
		}

	}

	@Override
	public void addUser(GlobalUserPhase globalUserPhase, User user)
			throws DaoException {
		String sql = "insert into " + getTableName(globalUserPhase)
				+ " (user_id) values (?)";

		try {
			jdbcTemplate.update(sql, user.getUserId());
		} catch (Exception e) {
			throw new DaoException(e);
		}
	}

	@Override
	public List<User> getUserList(GlobalUserPhase globalUserPhase, int index,
			int size) throws DaoException {
		String sql = "select id, user_id from " + getTableName(globalUserPhase)
				+ " order by id limit ?, ?";

		try {
			return jdbcTemplate.query(sql, rowMapper, new Object[] { index,
					size });
		} catch (Exception e) {
			throw new DaoException(e);
		}
	}

	@Override
	public boolean isSameUserExisting(GlobalUserPhase globalUserPhase, User user)
			throws DaoException {
		String sql = "select count(*) from " + getTableName(globalUserPhase)
				+ " where user_id = ?";

		int size;

		try {
			size = jdbcTemplate.queryForObject(sql,
					new Object[] { user.getUserId() }, Integer.class);
		} catch (Exception e) {
			throw new DaoException(e);
		}

		return size > 0;
	}

	@Override
	public void deleteUser(GlobalUserPhase globalUserPhase, int id)
			throws DaoException {
		String sql = "delete from " + getTableName(globalUserPhase)
				+ " where id = ?";

		try {
			jdbcTemplate.update(sql, id);
		} catch (Exception e) {
			throw new DaoException(e);
		}
	}

	@Override
	public void addUser(int categoryId, int typeId, int applyingUserId,
			UserPhase userPhase, User user) throws DaoException {
		String sql = "insert into "
				+ getTableName(categoryId, typeId, applyingUserId, userPhase)
				+ " (user_id) values (?)";

		try {
			jdbcTemplate.update(sql, user.getUserId());
		} catch (Exception e) {
			throw new DaoException(e);
		}
	}

	@Override
	public List<User> getUserListBeforeDays(int categoryId, int typeId,
			int applyingUserId, UserPhase userPhase, int days, int index,
			int size) throws DaoException {
		String sql = "select id, user_id from "
				+ getTableName(categoryId, typeId, applyingUserId, userPhase)
				+ " where datediff(now(), created_timestamp) >= ?  order by id limit ?, ?";

		try {
			return jdbcTemplate.query(sql, rowMapper, new Object[] { days,
					index, size });
		} catch (Exception e) {
			throw new DaoException(e);
		}
	}

	@Override
	public void deleteUser(int categoryId, int typeId, int applyingUserId,
			UserPhase userPhase, int id) throws DaoException {
		String sql = "delete from "
				+ getTableName(categoryId, typeId, applyingUserId, userPhase)
				+ " where id = ?";

		try {
			jdbcTemplate.update(sql, id);
		} catch (Exception e) {
			throw new DaoException(e);
		}
	}

}
