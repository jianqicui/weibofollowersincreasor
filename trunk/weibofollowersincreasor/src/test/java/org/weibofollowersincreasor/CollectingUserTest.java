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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import org.apache.commons.lang3.StringUtils;
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

public class CollectingUserTest {

	private ObjectMapper objectMapper;

	private DefaultHttpClient defaultHttpClient;

	@SuppressWarnings("unchecked")
	public CollectingUserTest(String cookiesFile) {
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

	private List<List<String>> getUserIdListList(List<String> userIdList) {
		List<List<String>> userIdListList = new ArrayList<List<String>>();

		int count = 100;
		int size = (userIdList.size() / count)
				+ (userIdList.size() % count == 0 ? 0 : 1);

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

	private void increaseSamplesForUserList(List<User> userList) {
		List<String> userIdList = getUserIdList(userList);
		List<List<String>> userIdListList = getUserIdListList(userIdList);

		for (int i = 0; i < 2; i++) {
			Map<String, Integer> followerSizeMap = new HashMap<String, Integer>();

			for (List<String> vUserIdList : userIdListList) {
				followerSizeMap.putAll(getFollowerSizeMap(vUserIdList));
			}

			for (User user : userList) {
				int followerSize = followerSizeMap.get(user.getUserId());
				user.addFollowerSize(followerSize);
			}

			try {
				Thread.sleep(60 * 60 * 1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	private void calculateIncreasedFollowerSizeList(List<User> userList) {
		for (User user : userList) {
			List<Integer> followerSizeList = user.getFollowerSizeList();

			int totalIncreasedFollowerSize = 0;

			for (int i = 0; i < followerSizeList.size() - 1; i++) {
				int beginFollowerSize = followerSizeList.get(i);
				int endFollowerSize = followerSizeList.get(i + 1);

				int increasedFollowerSize = endFollowerSize - beginFollowerSize;
				totalIncreasedFollowerSize = totalIncreasedFollowerSize
						+ increasedFollowerSize;

				user.addIncreasedFollowerSize(increasedFollowerSize);
			}

			user.setTotalIncreasedFollowerSize(totalIncreasedFollowerSize);
		}
	}

	private class UserIncreasedFollowerSizeComparator implements
			Comparator<User> {

		@Override
		public int compare(User user1, User user2) {
			return user2.getTotalIncreasedFollowerSize()
					- user1.getTotalIncreasedFollowerSize();
		}

	}

	private void sortUserList(List<User> userList) {
		Collections.sort(userList, new UserIncreasedFollowerSizeComparator());
	}

	private List<User> getCollectingUserList(String userIdNames) {
		List<User> collectingUserList = getUserList(userIdNames);

		increaseSamplesForUserList(collectingUserList);

		calculateIncreasedFollowerSizeList(collectingUserList);

		sortUserList(collectingUserList);

		return collectingUserList;
	}

	private void saveCollectingUserList(List<User> userList, String usersFile) {
		Writer writer = null;

		try {
			writer = new OutputStreamWriter(new FileOutputStream(usersFile),
					"UTF-8");

			for (User user : userList) {
				StringBuilder content = new StringBuilder();

				content.append(user.getUserId());
				content.append(",");
				content.append(user.getUserName());
				content.append(",");
				content.append(user.getTotalIncreasedFollowerSize());
				content.append(",");
				content.append(StringUtils.join(
						user.getIncreasedFollowerSizeList(), ","));

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
		String collectingUsersFile = scanner.nextLine();

		CollectingUserTest collectingUserTest = new CollectingUserTest(
				cookiesFile);
		collectingUserTest.authorize();

		List<User> collectingUserList = collectingUserTest
				.getCollectingUserList(userIdNames);

		collectingUserTest.saveCollectingUserList(collectingUserList,
				collectingUsersFile);
	}

}
