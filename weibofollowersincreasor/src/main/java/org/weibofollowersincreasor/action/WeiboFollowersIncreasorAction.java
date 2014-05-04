package org.weibofollowersincreasor.action;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.http.client.CookieStore;
import org.apache.http.client.HttpClient;
import org.apache.http.client.params.ClientPNames;
import org.apache.http.client.params.CookiePolicy;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.PoolingClientConnectionManager;
import org.apache.http.impl.conn.SchemeRegistryFactory;
import org.apache.http.impl.cookie.BasicClientCookie;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.CoreConnectionPNames;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.weibofollowersincreasor.action.exception.ActionException;
import org.weibofollowersincreasor.entity.ApplyingUser;
import org.weibofollowersincreasor.entity.Category;
import org.weibofollowersincreasor.entity.CollectingUser;
import org.weibofollowersincreasor.entity.GlobalUserPhase;
import org.weibofollowersincreasor.entity.User;
import org.weibofollowersincreasor.entity.UserPhase;
import org.weibofollowersincreasor.entity.QueryingUser;
import org.weibofollowersincreasor.entity.Type;
import org.weibofollowersincreasor.handler.WeiboApiHandler;
import org.weibofollowersincreasor.handler.WeiboHandler;
import org.weibofollowersincreasor.handler.exception.HandlerException;
import org.weibofollowersincreasor.service.ApplyingUserService;
import org.weibofollowersincreasor.service.CategoryService;
import org.weibofollowersincreasor.service.CollectingUserService;
import org.weibofollowersincreasor.service.UserService;
import org.weibofollowersincreasor.service.QueryingUserService;
import org.weibofollowersincreasor.service.TypeService;
import org.weibofollowersincreasor.service.exception.ServiceException;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class WeiboFollowersIncreasorAction {

	private static final Logger logger = LoggerFactory
			.getLogger(WeiboFollowersIncreasorAction.class);

	private QueryingUserService queryingUserService;

	private ApplyingUserService applyingUserService;

	private CategoryService categoryService;

	private TypeService typeService;

	private CollectingUserService collectingUserService;

	private UserService userService;

	private WeiboHandler weiboHandler;

	private WeiboApiHandler weiboApiHandler;

	private int filteringUserSize;

	private int followingUserSize;

	private int reservingDays;

	private int unfollowingUserSize;

	private ObjectMapper objectMapper;

	private DefaultHttpClient collectingDefaultHttpClient;

	private DefaultHttpClient followingDefaultHttpClient;

	private DefaultHttpClient unfollowingDefaultHttpClient;

	public void setQueryingUserService(QueryingUserService queryingUserService) {
		this.queryingUserService = queryingUserService;
	}

	public void setApplyingUserService(ApplyingUserService applyingUserService) {
		this.applyingUserService = applyingUserService;
	}

	public void setCategoryService(CategoryService categoryService) {
		this.categoryService = categoryService;
	}

	public void setTypeService(TypeService typeService) {
		this.typeService = typeService;
	}

	public void setCollectingUserService(
			CollectingUserService collectingUserService) {
		this.collectingUserService = collectingUserService;
	}

	public void setUserService(UserService userService) {
		this.userService = userService;
	}

	public void setWeiboHandler(WeiboHandler weiboHandler) {
		this.weiboHandler = weiboHandler;
	}

	public void setWeiboApiHandler(WeiboApiHandler weiboApiHandler) {
		this.weiboApiHandler = weiboApiHandler;
	}

	public void setFilteringUserSize(int filteringUserSize) {
		this.filteringUserSize = filteringUserSize;
	}

	public void setFollowingUserSize(int followingUserSize) {
		this.followingUserSize = followingUserSize;
	}

	public void setReservingDays(int reservingDays) {
		this.reservingDays = reservingDays;
	}

	public void setUnfollowingUserSize(int unfollowingUserSize) {
		this.unfollowingUserSize = unfollowingUserSize;
	}

	public void initialize() {
		objectMapper = new ObjectMapper();

		collectingDefaultHttpClient = getDefaultHttpClient();
		followingDefaultHttpClient = getDefaultHttpClient();
		unfollowingDefaultHttpClient = getDefaultHttpClient();
	}

	public void destroy() {
		collectingDefaultHttpClient.getConnectionManager().shutdown();
		followingDefaultHttpClient.getConnectionManager().shutdown();
		unfollowingDefaultHttpClient.getConnectionManager().shutdown();
	}

	private DefaultHttpClient getDefaultHttpClient() {
		X509TrustManager tm = new X509TrustManager() {
			public X509Certificate[] getAcceptedIssuers() {
				return new java.security.cert.X509Certificate[] {};
			}

			public void checkClientTrusted(
					java.security.cert.X509Certificate[] certs, String authType)
					throws CertificateException {
			}

			public void checkServerTrusted(
					java.security.cert.X509Certificate[] certs, String authType)
					throws CertificateException {
			}
		};

		SSLContext sslContext;

		try {
			sslContext = SSLContext.getInstance("TLS");

			sslContext.init(null, new TrustManager[] { tm }, null);
		} catch (NoSuchAlgorithmException e) {
			logger.error("Exception", e);

			throw new ActionException(e);
		} catch (KeyManagementException e) {
			logger.error("Exception", e);

			throw new ActionException(e);
		}

		SSLSocketFactory ssf = new SSLSocketFactory(sslContext);
		Scheme scheme = new Scheme("https", 443, ssf);

		SchemeRegistry registry = SchemeRegistryFactory.createDefault();
		registry.register(scheme);

		PoolingClientConnectionManager poolingClientConnectionManager = new PoolingClientConnectionManager(
				registry, 60000, TimeUnit.MILLISECONDS);
		poolingClientConnectionManager.setDefaultMaxPerRoute(10);
		poolingClientConnectionManager.setMaxTotal(100);

		BasicHttpParams basicHttpParams = new BasicHttpParams();

		basicHttpParams.setParameter(ClientPNames.COOKIE_POLICY,
				CookiePolicy.BROWSER_COMPATIBILITY);
		basicHttpParams.setParameter(CoreConnectionPNames.CONNECTION_TIMEOUT,
				600000);
		basicHttpParams.setParameter(CoreConnectionPNames.SO_TIMEOUT, 600000);

		return new DefaultHttpClient(poolingClientConnectionManager,
				basicHttpParams);
	}

	@SuppressWarnings("unchecked")
	private void setCookies(DefaultHttpClient defaultHttpClient, byte[] cookies) {
		List<Map<String, Object>> list;

		try {
			list = objectMapper.readValue(cookies, List.class);
		} catch (JsonParseException e) {
			logger.error("Exception", e);

			throw new ActionException(e);
		} catch (JsonMappingException e) {
			logger.error("Exception", e);

			throw new ActionException(e);
		} catch (IOException e) {
			logger.error("Exception", e);

			throw new ActionException(e);
		}

		BasicCookieStore basicCookieStore = new BasicCookieStore();

		for (Map<String, Object> map : list) {
			String name = (String) map.get("name");
			String value = (String) map.get("value");

			String comment = (String) map.get("comment");
			String domain = (String) map.get("domain");

			Date expiryDate = null;
			if (map.get("expiryDate") != null) {
				expiryDate = new Date((long) map.get("expiryDate"));
			}

			String path = (String) map.get("path");
			boolean secure = (boolean) map.get("secure");
			int version = (int) map.get("version");

			BasicClientCookie basicClientCookie = new BasicClientCookie(name,
					value);

			basicClientCookie.setComment(comment);
			basicClientCookie.setDomain(domain);
			basicClientCookie.setExpiryDate(expiryDate);
			basicClientCookie.setPath(path);
			basicClientCookie.setSecure(secure);
			basicClientCookie.setVersion(version);

			basicCookieStore.addCookie(basicClientCookie);
		}

		defaultHttpClient.setCookieStore(basicCookieStore);
	}

	private byte[] getCookies(DefaultHttpClient defaultHttpClient) {
		byte[] cookies;

		CookieStore cookieStore = defaultHttpClient.getCookieStore();

		List<Cookie> cookieList = cookieStore.getCookies();

		try {
			cookies = objectMapper.writeValueAsBytes(cookieList);
		} catch (JsonProcessingException e) {
			logger.error("Exception", e);

			throw new ActionException(e);
		}

		return cookies;
	}

	private List<User> getUserListByUserId(HttpClient httpClient,
			String accessToken, String userId) {
		List<User> userList = new ArrayList<User>();

		int cursor = 0;
		int size = 100;

		for (int i = 0; i < 50; i++) {
			List<User> vUserList;

			try {
				vUserList = weiboApiHandler.getUserListByUserId(httpClient,
						accessToken, userId, cursor, size);
			} catch (HandlerException e) {
				vUserList = new ArrayList<User>();
			}

			userList.addAll(vUserList);

			cursor = cursor + size;
		}

		return userList;
	}

	private void deduplicateUserList(List<User> userList) {
		Set<User> userSet = new HashSet<User>();

		userSet.addAll(userList);

		userList.clear();

		userList.addAll(userSet);
	}

	private void collectUsers() {
		QueryingUser queryingUser;

		try {
			queryingUser = queryingUserService.getQueryingUser();
		} catch (ServiceException e) {
			logger.error("Exception", e);

			throw new ActionException(e);
		}

		setCookies(collectingDefaultHttpClient, queryingUser.getCookies());

		try {
			weiboHandler.refresh(collectingDefaultHttpClient);
		} catch (HandlerException e) {
			return;
		}

		queryingUser.setCookies(getCookies(collectingDefaultHttpClient));

		try {
			queryingUserService.updateQueryingUser(queryingUser);
		} catch (ServiceException e) {
			logger.error("Exception", e);

			throw new ActionException(e);
		}

		List<User> userList = new ArrayList<User>();

		int userSize = 0;

		logger.debug(String.format("Begin to collect users, userSize = %s",
				userSize));

		List<CollectingUser> collectingUserList;

		try {
			collectingUserList = collectingUserService.getCollectingUserList();
		} catch (ServiceException e) {
			logger.error("Exception", e);

			throw new ActionException(e);
		}

		String accessToken;

		try {
			accessToken = weiboApiHandler
					.getAccessToken(collectingDefaultHttpClient);
		} catch (HandlerException e) {
			return;
		}

		for (CollectingUser collectingUser : collectingUserList) {
			String userId = collectingUser.getUserId();

			List<User> vUserList = getUserListByUserId(
					collectingDefaultHttpClient, accessToken, userId);

			userList.addAll(vUserList);
		}

		deduplicateUserList(userList);

		userSize = 0;

		for (User user : userList) {
			try {
				userService.addUser(GlobalUserPhase.collected, user);

				userSize++;
			} catch (ServiceException e) {
				logger.error("Exception", e);

				throw new ActionException(e);
			}
		}

		logger.debug(String.format("End to collect users, userSize = %s",
				userSize));
	}

	private void filterUsers() {
		List<User> userList;

		try {
			userList = userService.getUserList(GlobalUserPhase.collected, 0,
					filteringUserSize);
		} catch (ServiceException e) {
			logger.error("Exception", e);

			throw new ActionException(e);
		}

		int userSize = userList.size();

		if (userSize < filteringUserSize) {
			return;
		}

		logger.debug(String.format("Begin to filter users, userSize = %s",
				userSize));

		userSize = 0;

		for (User user : userList) {
			boolean sameUserExisting = false;

			try {
				sameUserExisting = userService.isSameUserExisting(
						GlobalUserPhase.filtered, user);
			} catch (ServiceException e) {
				logger.error("Exception", e);

				throw new ActionException(e);
			}

			if (!sameUserExisting) {
				try {
					userService.moveUser(GlobalUserPhase.collected,
							GlobalUserPhase.filtered, user);

					userSize++;
				} catch (ServiceException e) {
					logger.error("Exception", e);

					throw new ActionException(e);
				}
			} else {
				try {
					userService.deleteUser(GlobalUserPhase.collected,
							user.getId());
				} catch (ServiceException e) {
					logger.error("Exception", e);

					throw new ActionException(e);
				}
			}
		}

		logger.debug(String.format("End to filter users, userSize = %s",
				userSize));
	}

	public void collectAndFilterUsers() {
		collectUsers();

		filterUsers();
	}

	private void followUsers() {
		List<Category> categoryList;

		try {
			categoryList = categoryService.getCategoryList();
		} catch (ServiceException e) {
			logger.error("Exception", e);

			throw new ActionException(e);
		}

		for (Category category : categoryList) {
			int categoryId = category.getCategoryId();

			List<Type> typeList;

			try {
				typeList = typeService.getTypeList(categoryId);
			} catch (ServiceException e) {
				logger.error("Exception", e);

				throw new ActionException(e);
			}

			for (Type type : typeList) {
				int typeId = type.getTypeId();

				List<ApplyingUser> applyingUserList;

				try {
					applyingUserList = applyingUserService.getApplyingUserList(
							categoryId, typeId);
				} catch (ServiceException e) {
					logger.error("Exception", e);

					throw new ActionException(e);
				}

				for (ApplyingUser applyingUser : applyingUserList) {
					int applyingUserId = applyingUser.getId();
					int followingIndex = applyingUser.getFollowingIndex();

					List<User> userList;

					try {
						userList = userService.getUserList(
								GlobalUserPhase.filtered, followingIndex,
								followingUserSize);
					} catch (ServiceException e) {
						logger.error("Exception", e);

						throw new ActionException(e);
					}

					int userSize = userList.size();

					if (userSize < followingUserSize) {
						return;
					}

					followingIndex = followingIndex + followingUserSize;

					applyingUser.setFollowingIndex(followingIndex);

					setCookies(followingDefaultHttpClient,
							applyingUser.getCookies());

					try {
						weiboHandler.refresh(followingDefaultHttpClient);
					} catch (HandlerException e) {
						continue;
					}

					applyingUser
							.setCookies(getCookies(followingDefaultHttpClient));

					logger.debug(String
							.format("Begin to follow users, categoryId = %s, typeId = %s, userSize = %s",
									categoryId, typeId, userSize));

					userSize = 0;

					for (User user : userList) {
						String userId = user.getUserId();

						try {
							boolean normal = weiboHandler.isNormal(
									followingDefaultHttpClient, userId);

							if (normal) {
								weiboHandler.follow(followingDefaultHttpClient,
										userId);

								userSize++;

								try {
									userService.addUser(categoryId, typeId,
											applyingUserId, UserPhase.followed,
											user);
								} catch (ServiceException e) {
									logger.error("Exception", e);

									throw new ActionException(e);
								}
							}
						} catch (HandlerException e) {

						}

						try {
							Thread.sleep(10000);
						} catch (InterruptedException e) {
							logger.error("Exception", e);

							throw new ActionException(e);
						}
					}

					logger.debug(String
							.format("End to follow users, categoryId = %s, typeId = %s, userSize = %s",
									categoryId, typeId, userSize));
				}

				try {
					applyingUserService.updateApplyingUserList(categoryId,
							typeId, applyingUserList);
				} catch (ServiceException e) {
					logger.error("Exception", e);

					throw new ActionException(e);
				}
			}
		}
	}

	private void unfollowUsers() {
		List<Category> categoryList;

		try {
			categoryList = categoryService.getCategoryList();
		} catch (ServiceException e) {
			logger.error("Exception", e);

			throw new ActionException(e);
		}

		for (Category category : categoryList) {
			int categoryId = category.getCategoryId();

			List<Type> typeList;

			try {
				typeList = typeService.getTypeList(categoryId);
			} catch (ServiceException e) {
				logger.error("Exception", e);

				throw new ActionException(e);
			}

			for (Type type : typeList) {
				int typeId = type.getTypeId();

				List<ApplyingUser> applyingUserList;

				try {
					applyingUserList = applyingUserService.getApplyingUserList(
							categoryId, typeId);
				} catch (ServiceException e) {
					logger.error("Exception", e);

					throw new ActionException(e);
				}

				for (ApplyingUser applyingUser : applyingUserList) {
					int applyingUserId = applyingUser.getId();

					List<User> userList;

					try {
						userList = userService.getUserListBeforeDays(
								categoryId, typeId, applyingUserId,
								UserPhase.followed, reservingDays, 0,
								unfollowingUserSize);
					} catch (ServiceException e) {
						logger.error("Exception", e);

						throw new ActionException(e);
					}

					int userSize = userList.size();

					if (userSize < unfollowingUserSize) {
						return;
					}

					setCookies(unfollowingDefaultHttpClient,
							applyingUser.getCookies());

					try {
						weiboHandler.refresh(unfollowingDefaultHttpClient);
					} catch (HandlerException e) {
						continue;
					}

					applyingUser
							.setCookies(getCookies(unfollowingDefaultHttpClient));

					logger.debug(String
							.format("Begin to unfollow users, categoryId = %s, typeId = %s, userSize = %s",
									categoryId, typeId, userSize));

					userSize = 0;

					for (User user : userList) {
						String userId = user.getUserId();

						try {
							weiboHandler.unfollow(unfollowingDefaultHttpClient,
									userId);

							userSize++;
						} catch (HandlerException e) {

						}

						try {
							userService.deleteUser(categoryId, typeId,
									applyingUserId, UserPhase.followed,
									user.getId());
						} catch (ServiceException ex) {
							logger.error("Exception", ex);

							throw new ActionException(ex);
						}

						try {
							Thread.sleep(10000);
						} catch (InterruptedException e) {
							logger.error("Exception", e);

							throw new ActionException(e);
						}
					}

					logger.debug(String
							.format("End to unfollow users, categoryId = %s, typeId = %s, userSize = %s",
									categoryId, typeId, userSize));
				}

				try {
					applyingUserService.updateApplyingUserList(categoryId,
							typeId, applyingUserList);
				} catch (ServiceException e) {
					logger.error("Exception", e);

					throw new ActionException(e);
				}
			}
		}
	}

	public void followAndUnfollowUsers() {
		followUsers();

		unfollowUsers();
	}

}
