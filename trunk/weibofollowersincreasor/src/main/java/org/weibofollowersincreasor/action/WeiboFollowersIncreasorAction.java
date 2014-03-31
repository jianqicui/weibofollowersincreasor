package org.weibofollowersincreasor.action;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
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
import org.apache.http.impl.cookie.BasicClientCookie2;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.CoreConnectionPNames;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.weibofollowersincreasor.action.exception.ActionException;
import org.weibofollowersincreasor.entity.ActiveUser;
import org.weibofollowersincreasor.entity.ActiveUserPhase;
import org.weibofollowersincreasor.entity.Category;
import org.weibofollowersincreasor.entity.CollectedUser;
import org.weibofollowersincreasor.entity.Follower;
import org.weibofollowersincreasor.entity.FollowerPhase;
import org.weibofollowersincreasor.entity.Followers;
import org.weibofollowersincreasor.entity.Type;
import org.weibofollowersincreasor.handler.SaeAppBatchhelperHandler;
import org.weibofollowersincreasor.handler.WeiboHandler;
import org.weibofollowersincreasor.handler.exception.HandlerException;
import org.weibofollowersincreasor.service.ActiveUserService;
import org.weibofollowersincreasor.service.CategoryService;
import org.weibofollowersincreasor.service.CollectedUserService;
import org.weibofollowersincreasor.service.FollowerService;
import org.weibofollowersincreasor.service.TypeService;
import org.weibofollowersincreasor.service.exception.ServiceException;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class WeiboFollowersIncreasorAction {

	private static final Logger logger = LoggerFactory
			.getLogger(WeiboFollowersIncreasorAction.class);

	private ActiveUserService activeUserService;

	private CategoryService categoryService;

	private TypeService typeService;

	private CollectedUserService collectedUserService;

	private FollowerService followerService;

	private WeiboHandler weiboHandler;

	private SaeAppBatchhelperHandler saeAppBatchhelperHandler;

	private int filteredFollowerSize;

	private int followedFollowerSize;

	private int followedDays;

	private int unfollowedFollowerSize;

	private ObjectMapper objectMapper;

	private DefaultHttpClient collectingDefaultHttpClient;

	private DefaultHttpClient followingDefaultHttpClient;

	private DefaultHttpClient unfollowingDefaultHttpClient;

	public void setActiveUserService(ActiveUserService activeUserService) {
		this.activeUserService = activeUserService;
	}

	public void setCategoryService(CategoryService categoryService) {
		this.categoryService = categoryService;
	}

	public void setTypeService(TypeService typeService) {
		this.typeService = typeService;
	}

	public void setCollectedUserService(
			CollectedUserService collectedUserService) {
		this.collectedUserService = collectedUserService;
	}

	public void setFollowerService(FollowerService followerService) {
		this.followerService = followerService;
	}

	public void setWeiboHandler(WeiboHandler weiboHandler) {
		this.weiboHandler = weiboHandler;
	}

	public void setSaeAppBatchhelperHandler(
			SaeAppBatchhelperHandler saeAppBatchhelperHandler) {
		this.saeAppBatchhelperHandler = saeAppBatchhelperHandler;
	}

	public void setFilteredFollowerSize(int filteredFollowerSize) {
		this.filteredFollowerSize = filteredFollowerSize;
	}

	public void setFollowedFollowerSize(int followedFollowerSize) {
		this.followedFollowerSize = followedFollowerSize;
	}

	public void setFollowedDays(int followedDays) {
		this.followedDays = followedDays;
	}

	public void setUnfollowedFollowerSize(int unfollowedFollowerSize) {
		this.unfollowedFollowerSize = unfollowedFollowerSize;
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

			String commentURL = (String) map.get("commentURL");

			int[] ports = null;
			if (map.get("ports") != null) {
				String[] portsStrings = ((String) map.get("ports")).split(",");

				ports = new int[portsStrings.length];

				for (int i = 0; i < portsStrings.length; i++) {
					String portString = portsStrings[i];

					ports[i] = Integer.parseInt(portString);
				}
			}

			boolean persistent = (boolean) map.get("persistent");

			BasicClientCookie2 basicClientCookie2 = new BasicClientCookie2(
					name, value);

			basicClientCookie2.setComment(comment);
			basicClientCookie2.setDomain(domain);
			basicClientCookie2.setExpiryDate(expiryDate);
			basicClientCookie2.setPath(path);
			basicClientCookie2.setSecure(secure);
			basicClientCookie2.setVersion(version);

			basicClientCookie2.setCommentURL(commentURL);
			basicClientCookie2.setPorts(ports);
			basicClientCookie2.setDiscard(persistent);

			basicCookieStore.addCookie(basicClientCookie2);
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

	private List<Follower> getCollectedFollowerListByUserId(
			HttpClient httpClient,
			SaeAppBatchhelperHandler saeAppBatchhelperHandler, int categoryId,
			int typeId, String userId, String userName, int times) {
		int cursor = 0;
		int size = 100;

		List<Follower> collectedFollowerList = new ArrayList<Follower>();

		while (true) {
			List<Follower> vFollowerList;
			int nextCursor;

			try {
				Followers followers = saeAppBatchhelperHandler
						.getFollowersByUserName(httpClient, userName, cursor,
								size);

				vFollowerList = followers.getFollowerList();
				nextCursor = followers.getNextCursor();
			} catch (HandlerException e) {
				vFollowerList = new ArrayList<Follower>();
				nextCursor = cursor + size;
			}

			times--;

			collectedFollowerList.addAll(vFollowerList);

			if (times == 0) {
				break;
			}

			if (nextCursor == 0) {
				break;
			} else {
				cursor = nextCursor;
			}

			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				logger.error("Exception", e);

				throw new ActionException(e);
			}
		}

		return collectedFollowerList;
	}

	private void deduplicateCollectedUserList(List<Follower> followerList) {
		List<Follower> vFollowerList = new ArrayList<Follower>();

		for (Iterator<Follower> it = followerList.iterator(); it.hasNext();) {
			Follower follower = it.next();

			boolean existing = false;

			for (Follower vFollower : vFollowerList) {
				if (follower.getUserId().equals(vFollower.getUserId())) {
					existing = true;

					break;
				}
			}

			if (existing) {
				it.remove();
			} else {
				vFollowerList.add(follower);
			}
		}
	}

	public void collectFollowers() {
		ActiveUser activeUser;

		ActiveUserPhase activeUserPhase = ActiveUserPhase.querying;

		try {
			activeUser = activeUserService.getActiveUser(activeUserPhase);
		} catch (ServiceException e) {
			logger.error("Exception", e);

			throw new ActionException(e);
		}

		setCookies(collectingDefaultHttpClient, activeUser.getCookies());

		try {
			weiboHandler.refresh(collectingDefaultHttpClient);
		} catch (HandlerException e) {
			return;
		}

		activeUser.setCookies(getCookies(collectingDefaultHttpClient));

		try {
			activeUserService.updateActiveUser(activeUserPhase, activeUser);
		} catch (ServiceException e) {
			logger.error("Exception", e);

			throw new ActionException(e);
		}

		try {
			saeAppBatchhelperHandler.authorize(collectingDefaultHttpClient);
		} catch (HandlerException e) {
			return;
		}

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

				List<Follower> followerList = new ArrayList<Follower>();

				int followerSize = 0;

				logger.debug(String
						.format("Begin to collect followers, categoryId = %s, typeId = %s, followerSize = %s",
								categoryId, typeId, followerSize));

				List<CollectedUser> collectedUserList;

				try {
					collectedUserList = collectedUserService
							.getCollectedUserList(categoryId, typeId);
				} catch (ServiceException e) {
					logger.error("Exception", e);

					throw new ActionException(e);
				}

				for (CollectedUser collectedUser : collectedUserList) {
					String userId = collectedUser.getUserId();
					String userName = collectedUser.getUserName();

					List<Follower> collectedFollowerList = getCollectedFollowerListByUserId(
							collectingDefaultHttpClient,
							saeAppBatchhelperHandler, categoryId, typeId,
							userId, userName, 50);

					followerList.addAll(collectedFollowerList);
				}

				deduplicateCollectedUserList(followerList);

				followerSize = 0;

				for (Follower follower : followerList) {
					try {
						followerService.addFollower(categoryId, typeId,
								FollowerPhase.collected, follower);

						followerSize++;
					} catch (ServiceException e) {
						logger.error("Exception", e);

						throw new ActionException(e);
					}
				}

				logger.debug(String
						.format("End to collect followers, categoryId = %s, typeId = %s, followerSize = %s",
								categoryId, typeId, followerSize));
			}
		}
	}

	private boolean isSameFollowerExisting(int categoryId, int typeId,
			Follower follower) {
		boolean sameFollowerExisting = false;

		try {
			sameFollowerExisting = followerService.isSameFollowerExisting(
					categoryId, typeId, FollowerPhase.filtered, follower);
		} catch (ServiceException e) {
			logger.error("Exception", e);

			throw new ActionException(e);
		}

		if (!sameFollowerExisting) {
			try {
				sameFollowerExisting = followerService.isSameFollowerExisting(
						categoryId, typeId, FollowerPhase.followed, follower);
			} catch (ServiceException e) {
				logger.error("Exception", e);

				throw new ActionException(e);
			}

			if (!sameFollowerExisting) {
				try {
					sameFollowerExisting = followerService
							.isSameFollowerExisting(categoryId, typeId,
									FollowerPhase.unfollowed, follower);
				} catch (ServiceException e) {
					logger.error("Exception", e);

					throw new ActionException(e);
				}
			}
		}

		return sameFollowerExisting;
	}

	public void filterFollowers() {
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

				List<Follower> followerList;

				try {
					followerList = followerService.getFollowerList(categoryId,
							typeId, FollowerPhase.collected, 0,
							filteredFollowerSize);
				} catch (ServiceException e) {
					logger.error("Exception", e);

					throw new ActionException(e);
				}

				int followerSize = followerList.size();

				logger.debug(String
						.format("Begin to filter followers, categoryId = %s, typeId = %s, followerSize = %s",
								categoryId, typeId, followerSize));

				followerSize = 0;

				List<String> userIdList = new ArrayList<String>();

				for (Follower follower : followerList) {
					userIdList.add(follower.getUserId());
				}

				for (Follower follower : followerList) {
					boolean sameFollowerExisting = isSameFollowerExisting(
							categoryId, typeId, follower);

					if (!sameFollowerExisting) {
						try {
							followerService.moveFollower(categoryId, typeId,
									FollowerPhase.collected,
									FollowerPhase.filtered, follower);

							followerSize++;
						} catch (ServiceException e) {
							logger.error("Exception", e);

							throw new ActionException(e);
						}
					} else {
						try {
							followerService.deleteFollower(categoryId, typeId,
									FollowerPhase.collected, follower.getId());
						} catch (ServiceException e) {
							logger.error("Exception", e);

							throw new ActionException(e);
						}
					}
				}

				logger.debug(String
						.format("End to filter followers, categoryId = %s, typeId = %s, followerSize = %s",
								categoryId, typeId, followerSize));
			}
		}
	}

	public void followFollowers() {
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

				ActiveUserPhase activeUserPhase = ActiveUserPhase.applying;

				List<ActiveUser> activeUserList;

				try {
					activeUserList = activeUserService.getActiveUserList(
							categoryId, typeId, activeUserPhase);
				} catch (ServiceException e) {
					logger.error("Exception", e);

					throw new ActionException(e);
				}

				for (ActiveUser activeUser : activeUserList) {
					setCookies(followingDefaultHttpClient,
							activeUser.getCookies());

					try {
						weiboHandler.refresh(followingDefaultHttpClient);
					} catch (HandlerException e) {
						continue;
					}

					activeUser
							.setCookies(getCookies(followingDefaultHttpClient));

					List<Follower> followerList;

					try {
						followerList = followerService.getFollowerList(
								categoryId, typeId, FollowerPhase.filtered, 0,
								followedFollowerSize);
					} catch (ServiceException e) {
						logger.error("Exception", e);

						throw new ActionException(e);
					}

					int successfulFollowedSize = 0;
					int failedFollowedSize = 0;

					logger.debug(String
							.format("Begin to follow followers, categoryId = %s, typeId = %s, successfulFollowedSize = %s, failedFollowedSize = %s",
									categoryId, typeId, successfulFollowedSize,
									failedFollowedSize));

					for (Follower follower : followerList) {
						try {
							weiboHandler.follow(followingDefaultHttpClient,
									follower.getUserId());

							try {
								followerService.moveFollower(categoryId,
										typeId, FollowerPhase.filtered,
										FollowerPhase.followed, follower);

								successfulFollowedSize++;
							} catch (ServiceException e) {
								logger.error("Exception", e);

								throw new ActionException(e);
							}
						} catch (HandlerException e) {
							try {
								followerService.deleteFollower(categoryId,
										typeId, FollowerPhase.filtered,
										follower.getId());

								failedFollowedSize++;
							} catch (ServiceException ex) {
								logger.error("Exception", ex);

								throw new ActionException(ex);
							}
						}

						try {
							Thread.sleep(10000);
						} catch (InterruptedException e) {
							logger.error("Exception", e);

							throw new ActionException(e);
						}
					}

					logger.debug(String
							.format("End to follow followers, categoryId = %s, typeId = %s, successfulFollowedSize = %s, failedFollowedSize = %s",
									categoryId, typeId, successfulFollowedSize,
									failedFollowedSize));
				}

				try {
					activeUserService.updateActiveUserList(categoryId, typeId,
							activeUserPhase, activeUserList);
				} catch (ServiceException e) {
					logger.error("Exception", e);

					throw new ActionException(e);
				}
			}
		}
	}

	public void unfollowFollowers() {
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

				ActiveUserPhase activeUserPhase = ActiveUserPhase.applying;

				List<ActiveUser> activeUserList;

				try {
					activeUserList = activeUserService.getActiveUserList(
							categoryId, typeId, activeUserPhase);
				} catch (ServiceException e) {
					logger.error("Exception", e);

					throw new ActionException(e);
				}

				for (ActiveUser activeUser : activeUserList) {
					setCookies(unfollowingDefaultHttpClient,
							activeUser.getCookies());

					try {
						weiboHandler.refresh(unfollowingDefaultHttpClient);
					} catch (HandlerException e) {
						continue;
					}

					activeUser
							.setCookies(getCookies(unfollowingDefaultHttpClient));

					List<Follower> followerList;

					try {
						followerList = followerService
								.getFollowerListBeforeDays(categoryId, typeId,
										FollowerPhase.followed, followedDays,
										0, unfollowedFollowerSize);
					} catch (ServiceException e) {
						logger.error("Exception", e);

						throw new ActionException(e);
					}

					int successfulUnfollowedSize = 0;
					int failedUnfollowedSize = 0;

					logger.debug(String
							.format("Begin to unfollow followers, categoryId = %s, typeId = %s, successfulUnfollowedSize = %s, failedUnfollowedSize = %s",
									categoryId, typeId,
									successfulUnfollowedSize,
									failedUnfollowedSize));

					for (Follower follower : followerList) {
						try {
							weiboHandler.unfollow(unfollowingDefaultHttpClient,
									follower.getUserId());

							try {
								followerService.moveFollower(categoryId,
										typeId, FollowerPhase.followed,
										FollowerPhase.unfollowed, follower);

								successfulUnfollowedSize++;
							} catch (ServiceException e) {
								logger.error("Exception", e);

								throw new ActionException(e);
							}
						} catch (HandlerException e) {
							try {
								followerService.deleteFollower(categoryId,
										typeId, FollowerPhase.followed,
										follower.getId());

								failedUnfollowedSize++;
							} catch (ServiceException ex) {
								logger.error("Exception", ex);

								throw new ActionException(ex);
							}
						}

						try {
							Thread.sleep(10000);
						} catch (InterruptedException e) {
							logger.error("Exception", e);

							throw new ActionException(e);
						}
					}

					logger.debug(String
							.format("End to unfollow followers, categoryId = %s, typeId = %s, successfulUnfollowedSize = %s, failedUnfollowedSize = %s",
									categoryId, typeId,
									successfulUnfollowedSize,
									failedUnfollowedSize));
				}

				try {
					activeUserService.updateActiveUserList(categoryId, typeId,
							activeUserPhase, activeUserList);
				} catch (ServiceException e) {
					logger.error("Exception", e);

					throw new ActionException(e);
				}
			}
		}
	}

}