package org.weibofollowersincreasor;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.cookie.BasicClientCookie;
import org.apache.http.util.EntityUtils;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;

public class CollectedUserTest {

	private ObjectMapper objectMapper;

	private DefaultHttpClient defaultHttpClient;

	@SuppressWarnings("unchecked")
	public CollectedUserTest(String cookiesFile) {
		objectMapper = new ObjectMapper();

		defaultHttpClient = new DefaultHttpClient();

		List<Map<String, Object>> list = null;

		try {
			list = objectMapper.readValue(new File(cookiesFile), List.class);
		} catch (JsonParseException e) {
			e.printStackTrace();
		} catch (JsonMappingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		BasicCookieStore basicCookieStore = new BasicCookieStore();

		for (Map<String, Object> map : list) {
			String name = (String) map.get("name");
			String value = (String) map.get("value");
			String path = (String) map.get("path");
			String comment = (String) map.get("comment");
			int version = (int) map.get("version");
			String domain = (String) map.get("domain");
			boolean secure = (boolean) map.get("secure");

			Date expiryDate = null;

			if (map.get("expiryDate") != null) {
				expiryDate = new Date((long) map.get("expiryDate"));
			}

			BasicClientCookie basicClientCookie = new BasicClientCookie(name,
					value);
			basicClientCookie.setPath(path);
			basicClientCookie.setComment(comment);
			basicClientCookie.setVersion(version);
			basicClientCookie.setDomain(domain);
			basicClientCookie.setSecure(secure);
			basicClientCookie.setExpiryDate(expiryDate);

			basicCookieStore.addCookie(basicClientCookie);
		}

		defaultHttpClient.setCookieStore(basicCookieStore);
	}

	private void authorize() {
		String url = "https://api.weibo.com/oauth2/authorize?client_id=3144078080&redirect_uri=http%3A%2F%2Fbatchhelper.sinaapp.com%2F&response_type=code";

		HttpGet get = new HttpGet(url);

		try {
			defaultHttpClient.execute(get);
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			get.releaseConnection();
		}
	}

	private class User {

		private String userId;
		private String userName;
		private int followerSize;

		public String getUserId() {
			return userId;
		}

		public void setUserId(String userId) {
			this.userId = userId;
		}

		public String getUserName() {
			return userName;
		}

		public void setUserName(String userName) {
			this.userName = userName;
		}

		public int getFollowerSize() {
			return followerSize;
		}

		public void setFollowerSize(int followerSize) {
			this.followerSize = followerSize;
		}

	}

	private class Users {

		private int nextCursor;
		private List<User> userList;

		public int getNextCursor() {
			return nextCursor;
		}

		public void setNextCursor(int nextCursor) {
			this.nextCursor = nextCursor;
		}

		public List<User> getUserList() {
			return userList;
		}

		public void setUserList(List<User> userList) {
			this.userList = userList;
		}

	}

	private List<User> getUserList(String userIdNames) {
		List<User> userList = new ArrayList<User>();

		for (String userIdName : userIdNames.split(",")) {
			String[] userIdNameArray = userIdName.split(":");
			String userId = userIdNameArray[0];
			String userName = userIdNameArray[1];

			User user = new User();

			user.setUserId(userId);
			user.setUserName(userName);

			userList.add(user);
		}

		return userList;
	}

	private void setUserFollowerSizeByUserId(User user) {
		byte[] result = null;

		StringBuilder url = new StringBuilder();
		url.append("http://batchhelper.sinaapp.com/action.php");
		url.append("?");
		url.append("action");
		url.append("=");
		url.append("queryUsersCounts");
		url.append("&");
		url.append("userIds");
		url.append("=");
		url.append(user.getUserId());

		HttpGet get = new HttpGet(url.toString());

		try {
			HttpResponse response = defaultHttpClient.execute(get);

			int statusCode = response.getStatusLine().getStatusCode();

			if (statusCode == HttpStatus.SC_OK) {
				result = EntityUtils.toByteArray(response.getEntity());
			}
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			get.releaseConnection();
		}

		ArrayNode arrayNode = null;

		try {
			arrayNode = (ArrayNode) objectMapper.readTree(result);
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		JsonNode jsonNode = arrayNode.get(0);

		JsonNode followerSizeJsonNode = jsonNode.get("followers_count");

		if (followerSizeJsonNode != null) {
			int followerSize = followerSizeJsonNode.asInt();

			user.setFollowerSize(followerSize);
		}
	}

	private Users getFriendsByUserName(String userName, int cursor, int size) {
		byte[] result = null;

		StringBuilder url = new StringBuilder();
		url.append("http://batchhelper.sinaapp.com/action.php");
		url.append("?");
		url.append("action");
		url.append("=");
		url.append("queryFriends");
		url.append("&");
		url.append("userName");
		url.append("=");
		url.append(userName);
		url.append("&");
		url.append("cursor");
		url.append("=");
		url.append(cursor);
		url.append("&");
		url.append("count");
		url.append("=");
		url.append(size);

		HttpGet get = new HttpGet(url.toString());

		try {
			HttpResponse response = defaultHttpClient.execute(get);

			int statusCode = response.getStatusLine().getStatusCode();

			if (statusCode == HttpStatus.SC_OK) {
				result = EntityUtils.toByteArray(response.getEntity());
			}
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			get.releaseConnection();
		}

		JsonNode jsonNode = null;

		try {
			jsonNode = objectMapper.readTree(result);
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		Users friends = new Users();

		JsonNode nextCursorJsonNode = jsonNode.get("nextCursor");

		if (nextCursorJsonNode != null) {
			int nextCursor = nextCursorJsonNode.asInt();

			friends.setNextCursor(nextCursor);
		}

		ArrayNode usersArrayNode = (ArrayNode) jsonNode.get("users");

		if (usersArrayNode != null) {
			List<User> friendList = new ArrayList<User>();

			for (int i = 0; i < usersArrayNode.size(); i++) {
				JsonNode userNode = usersArrayNode.get(i);

				String id = userNode.get("id").asText();
				String screenName = userNode.get("screenName").asText();
				int followersCount = userNode.get("followersCount").asInt();

				User friend = new User();
				friend.setUserId(id);
				friend.setUserName(screenName);
				friend.setFollowerSize(followersCount);

				friendList.add(friend);
			}

			friends.setUserList(friendList);
		}

		return friends;
	}

	private List<User> getFriendListByUserName(String userName) {
		int cursor = 0;
		int size = 100;

		List<User> friendList = new ArrayList<User>();

		while (true) {
			Users friends = getFriendsByUserName(userName, cursor, size);

			List<User> vFollowerList = friends.getUserList();
			int nextCursor = friends.getNextCursor();

			friendList.addAll(vFollowerList);

			if (nextCursor == 0) {
				break;
			} else {
				cursor = nextCursor;
			}
		}

		return friendList;
	}

	private List<User> getCollectedUserList(List<User> userList) {
		List<User> collectedUserList = new ArrayList<User>();

		for (User user : userList) {
			setUserFollowerSizeByUserId(user);
			collectedUserList.add(user);

			List<User> friendList = getFriendListByUserName(user.getUserName());

			collectedUserList.addAll(friendList);
		}

		return collectedUserList;
	}

	private void deduplicateCollectedUserList(List<User> collectedUserList) {
		List<User> vCollectedUserList = new ArrayList<User>();

		for (Iterator<User> it = collectedUserList.iterator(); it.hasNext();) {
			User collectedUser = it.next();

			boolean existing = false;

			for (User vCollectedUser : vCollectedUserList) {
				if (collectedUser.getUserId()
						.equals(vCollectedUser.getUserId())) {
					existing = true;

					break;
				}
			}

			if (existing) {
				it.remove();
			} else {
				vCollectedUserList.add(collectedUser);
			}
		}
	}

	private class CollectedUserComparator implements Comparator<User> {

		@Override
		public int compare(User user1, User user2) {
			return user2.getFollowerSize() - user1.getFollowerSize();
		}

	}

	private void sortCollectedUserList(List<User> collectedUserList) {
		Collections.sort(collectedUserList, new CollectedUserComparator());
	}

	private void saveCollectedUserList(List<User> collectedUserList,
			String collectedUsersFile) {
		Writer writer = null;

		try {
			writer = new OutputStreamWriter(new FileOutputStream(
					collectedUsersFile), "UTF-8");

			for (User collectedUser : collectedUserList) {
				String content = collectedUser.getFollowerSize() + ","
						+ collectedUser.getUserId() + ","
						+ collectedUser.getUserName();

				writer.write(content);
				writer.write("\n");
			}

			writer.flush();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (writer != null) {
				try {
					writer.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	public static void main(String[] args) {
		Scanner scanner = new Scanner(System.in);

		System.out.println("Input cookies file: ");
		String cookiesFile = scanner.nextLine();

		System.out.println("Input user [id:name]s (separated by commas): ");
		String userIdNames = scanner.nextLine();

		System.out.println("Input collected user file: ");
		String collectedUsersFile = scanner.nextLine();

		CollectedUserTest collectedUserTest = new CollectedUserTest(cookiesFile);
		collectedUserTest.authorize();

		List<User> userList = collectedUserTest.getUserList(userIdNames);

		List<User> collectedUserList = collectedUserTest
				.getCollectedUserList(userList);

		System.out.println(String.format("getCollectedUserList size = %s",
				collectedUserList.size()));

		collectedUserTest.deduplicateCollectedUserList(collectedUserList);

		System.out.println(String.format(
				"deduplicateCollectedUserList size = %s",
				collectedUserList.size()));

		collectedUserTest.sortCollectedUserList(collectedUserList);

		System.out.println(String.format("sortCollectedUserList size = %s",
				collectedUserList.size()));

		collectedUserTest.saveCollectedUserList(collectedUserList,
				collectedUsersFile);

		System.out.println(String.format("saveCollectedUserList size = %s",
				collectedUserList.size()));
	}

}
