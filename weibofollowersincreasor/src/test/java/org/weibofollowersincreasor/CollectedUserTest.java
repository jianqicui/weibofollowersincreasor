package org.weibofollowersincreasor;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import org.apache.commons.lang.StringUtils;
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
		private List<Integer> followerSizeList = new ArrayList<Integer>();
		private List<Integer> increasedFollowerSizeList = new ArrayList<Integer>();
		private int totalIncreasedFollowerSize;

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

		public List<Integer> getFollowerSizeList() {
			return followerSizeList;
		}

		public void addFollowerSize(int followerSize) {
			this.followerSizeList.add(followerSize);
		}

		public List<Integer> getIncreasedFollowerSizeList() {
			return increasedFollowerSizeList;
		}

		public void addIncreasedFollowerSize(int increasedFollowerSize) {
			this.increasedFollowerSizeList.add(increasedFollowerSize);
		}

		public int getTotalIncreasedFollowerSize() {
			return totalIncreasedFollowerSize;
		}

		public void setTotalIncreasedFollowerSize(int totalIncreasedFollowerSize) {
			this.totalIncreasedFollowerSize = totalIncreasedFollowerSize;
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

	private List<String> getUserIdList(List<User> userList) {
		List<String> userIdList = new ArrayList<String>();

		for (User user : userList) {
			userIdList.add(user.getUserId());
		}

		return userIdList;
	}

	private JsonNode getJsonNode(String result) {
		int beginIndex = result.indexOf("(");
		int endIndex = result.lastIndexOf(")");

		JsonNode jsonNode = null;

		try {
			jsonNode = objectMapper.readTree(result.substring(beginIndex + 1,
					endIndex));
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return jsonNode;
	}

	private String get(String url) {
		String result = null;

		HttpGet get = new HttpGet(url);

		try {
			HttpResponse response = defaultHttpClient.execute(get);

			int statusCode = response.getStatusLine().getStatusCode();

			if (statusCode == HttpStatus.SC_OK) {
				result = EntityUtils.toString(response.getEntity(), "UTF-8");
			}
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			get.releaseConnection();
		}

		return result;
	}

	private String crossDomain() {
		StringBuilder url = new StringBuilder();

		url.append("http://login.sina.com.cn/sso/crossdomain.php");
		url.append("?");
		url.append("scriptId");
		url.append("=");
		url.append("ssoCrossDomainScriptId");
		url.append("&");
		url.append("callback");
		url.append("=");
		url.append("sinaSSOController.crossDomainCallBack");
		url.append("&");
		url.append("action");
		url.append("=");
		url.append("login");
		url.append("&");
		url.append("domain");
		url.append("=");
		url.append("sina.com.cn");
		url.append("&");
		url.append("sr");
		url.append("=");
		url.append("1440*900");
		url.append("&");
		url.append("client");
		url.append("=");
		url.append("ssologin.js(v1.4.13)");

		String result = get(url.toString());

		return result;
	}

	private String loginWeibo(String query) {
		StringBuilder url = new StringBuilder();

		url.append("http://www.weibo.com/sso/login.php");
		url.append("?");
		url.append(query);
		url.append("&");
		url.append("callback");
		url.append("=");
		url.append("sinaSSOController.doCrossDomainCallBack");
		url.append("&");
		url.append("scriptId");
		url.append("=");
		url.append("ssoscript0");
		url.append("&");
		url.append("client");
		url.append("=");
		url.append("ssologin.js(v1.4.13)");

		String result = get(url.toString());

		return result;
	}

	private void refresh() {
		// crossDomain
		String result = crossDomain();

		JsonNode jsonNode = getJsonNode(result);

		String arrUrl = ((ArrayNode) jsonNode.get("arrURL")).get(0).asText();

		String query = null;

		try {
			URI uri = new URI(arrUrl);

			query = uri.getQuery();
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}

		// loginWeibo
		loginWeibo(query);
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

				User friend = new User();
				friend.setUserId(id);
				friend.setUserName(screenName);

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

	private List<List<String>> getUserIdListList(List<String> userIdList) {
		List<List<String>> userIdListList = new ArrayList<List<String>>();

		int count = 100;
		int size = (userIdList.size() / count) + (userIdList.size() % count == 0 ? 0 : 1);

		int fromIndex;
		int toIndex;

		for (int i = 0; i < size; i++) {
			fromIndex = i * count;

			if (i != size - 1) {
				toIndex = (i + 1) * count;
			} else {
				toIndex = userIdList.size();
			}

			userIdListList.add(userIdList.subList(fromIndex, toIndex));
		}

		return userIdListList;
	}

	private Map<String, Integer> getFollowerSizeMap(List<String> userIdList) {
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
		url.append(StringUtils.join(userIdList, ","));

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

		Map<String, Integer> followerSizeMap = new HashMap<String, Integer>();

		for (JsonNode jsonNode : arrayNode) {
			JsonNode idJsonNode = jsonNode.get("id");
			JsonNode followerSizeJsonNode = jsonNode.get("followers_count");

			if (idJsonNode != null && followerSizeJsonNode != null) {
				String userId = idJsonNode.asText();
				int followerSize = followerSizeJsonNode.asInt();

				followerSizeMap.put(userId, followerSize);
			}
		}

		return followerSizeMap;
	}

	private void increaseSamplesForCollectedUserList(
			List<User> collectedUserList) {
		List<String> collectedUserIdList = getUserIdList(collectedUserList);
		List<List<String>> collectedUserIdListList = getUserIdListList(collectedUserIdList);

		for (int i = 0; i < 25; i++) {
			System.out.println(String.format(
					"Begin increaseSamplesForCollectedUserList, i = %s, %s", i,
					new Date()));

			refresh();

			Map<String, Integer> followerSizeMap = new HashMap<String, Integer>();

			for (List<String> userIdList : collectedUserIdListList) {
				followerSizeMap.putAll(getFollowerSizeMap(userIdList));
			}

			for (User collectedUser : collectedUserList) {
				int followerSize = followerSizeMap.get(collectedUser
						.getUserId());
				collectedUser.addFollowerSize(followerSize);
			}

			System.out.println(String.format(
					"End increaseSamplesForCollectedUserList, i = %s, %s", i,
					new Date()));

			try {
				Thread.sleep(60 * 60 * 1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	private void calculateIncreasedFollowerSizeList(List<User> collectedUserList) {
		for (User collectedUser : collectedUserList) {
			List<Integer> followerSizeList = collectedUser
					.getFollowerSizeList();

			int totalIncreasedFollowerSize = 0;

			for (int i = 0; i < followerSizeList.size() - 1; i++) {
				int beginFollowerSize = followerSizeList.get(i);
				int endFollowerSize = followerSizeList.get(i + 1);

				int increasedFollowerSize = endFollowerSize - beginFollowerSize;
				totalIncreasedFollowerSize = totalIncreasedFollowerSize
						+ increasedFollowerSize;

				collectedUser.addIncreasedFollowerSize(increasedFollowerSize);
			}

			collectedUser
					.setTotalIncreasedFollowerSize(totalIncreasedFollowerSize);
		}
	}

	private class CollectedUserIncreasedFollowerSizeComparator implements
			Comparator<User> {

		@Override
		public int compare(User user1, User user2) {
			return user2.getTotalIncreasedFollowerSize()
					- user1.getTotalIncreasedFollowerSize();
		}

	}

	private void sortCollectedUserList(List<User> collectedUserList) {
		Collections.sort(collectedUserList,
				new CollectedUserIncreasedFollowerSizeComparator());
	}

	private List<User> getCandidateCollectedUserList(String userIdNames) {
		List<User> userList = getUserList(userIdNames);

		List<User> collectedUserList = getCollectedUserList(userList);

		System.out.println(String.format("GetCollectedUserListSize = %s",
				collectedUserList.size()));

		deduplicateCollectedUserList(collectedUserList);

		System.out.println(String.format(
				"DeduplicateCollectedUserListSize = %s",
				collectedUserList.size()));

		increaseSamplesForCollectedUserList(collectedUserList);

		System.out.println(String.format(
				"IncreaseSamplesForCollectedUserListSize = %s",
				collectedUserList.size()));

		calculateIncreasedFollowerSizeList(collectedUserList);

		System.out.println(String.format(
				"CalculateIncreasedFollowerSizeListSize = %s",
				collectedUserList.size()));

		sortCollectedUserList(collectedUserList);

		System.out.println(String.format("SortCollectedUserListSize = %s",
				collectedUserList.size()));

		return collectedUserList;
	}

	private void saveCollectedUserList(List<User> collectedUserList,
			String collectedUsersFile) {
		Writer writer = null;

		try {
			writer = new OutputStreamWriter(new FileOutputStream(
					collectedUsersFile), "UTF-8");

			for (User collectedUser : collectedUserList) {
				StringBuilder content = new StringBuilder();

				content.append(collectedUser.getUserId());
				content.append(",");
				content.append(collectedUser.getUserName());
				content.append(",");
				content.append(collectedUser.getTotalIncreasedFollowerSize());
				content.append(",");
				content.append(StringUtils.join(
						collectedUser.getIncreasedFollowerSizeList(), ","));

				writer.write(content.toString());
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

		List<User> collectedUserList = collectedUserTest
				.getCandidateCollectedUserList(userIdNames);

		collectedUserTest.saveCollectedUserList(collectedUserList,
				collectedUsersFile);
	}

}
