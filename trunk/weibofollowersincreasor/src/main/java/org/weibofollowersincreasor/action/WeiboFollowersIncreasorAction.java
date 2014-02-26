package org.weibofollowersincreasor.action;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.http.client.CookieStore;
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

	private int unfollowedFollowerSize;

	private ObjectMapper objectMapper;

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

	public void setUnfollowedFollowerSize(int unfollowedFollowerSize) {
		this.unfollowedFollowerSize = unfollowedFollowerSize;
	}

	public void setObjectMapper(ObjectMapper objectMapper) {
		this.objectMapper = objectMapper;
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

	private void collectExistingFollowers() {
		DefaultHttpClient defaultHttpClient = getDefaultHttpClient();

		ActiveUser activeUser;

		ActiveUserPhase activeUserPhase = ActiveUserPhase.collecting;

		try {
			activeUser = activeUserService.getActiveUser(activeUserPhase);
		} catch (ServiceException e) {
			logger.error("Exception", e);

			throw new ActionException(e);
		}

		setCookies(defaultHttpClient, activeUser.getCookies());

		boolean successful = false;

		for (int i = 0; i < 10; i++) {
			try {
				weiboHandler.refresh(defaultHttpClient);

				successful = true;

				break;
			} catch (HandlerException e) {
				continue;
			}
		}

		if (!successful) {
			return;
		}

		activeUser.setCookies(getCookies(defaultHttpClient));

		try {
			activeUserService.updateActiveUser(activeUserPhase, activeUser);
		} catch (ServiceException e) {
			logger.error("Exception", e);

			throw new ActionException(e);
		}

		try {
			saeAppBatchhelperHandler.authorize(defaultHttpClient);
		} catch (HandlerException e) {
			logger.error("Exception", e);

			throw new ActionException(e);
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

					List<Follower> collectedFollowerList = new ArrayList<Follower>();

					int cursor = 0;
					int count = 100;

					followerSize = 0;

					logger.debug(String
							.format("Begin to collect followers, categoryId = %s, typeId = %s, userId = %s, followerSize = %s",
									categoryId, typeId, userId, followerSize));

					while (true) {
						List<Follower> vFollowerList;
						int nextCursor;

						followerSize = 0;

						logger.debug(String
								.format("Begin to collect followers, categoryId = %s, typeId = %s, userId = %s, cursor = %s, followerSize = %s",
										categoryId, typeId, userId, cursor,
										followerSize));

						try {
							Followers followers = saeAppBatchhelperHandler
									.getFollowerIdsByUserName(
											defaultHttpClient, userName,
											cursor, count);

							vFollowerList = followers.getFollowerList();
							nextCursor = followers.getNextCursor();
						} catch (HandlerException e) {
							vFollowerList = new ArrayList<Follower>();
							nextCursor = cursor + count;
						}

						followerSize = vFollowerList.size();

						logger.debug(String
								.format("End to collect followers, categoryId = %s, typeId = %s, userId = %s, cursor = %s, followerSize = %s",
										categoryId, typeId, userId, cursor,
										followerSize));

						collectedFollowerList.addAll(vFollowerList);

						if (nextCursor == 0) {
							break;
						} else {
							cursor = nextCursor;
						}
					}

					followerSize = collectedFollowerList.size();

					logger.debug(String
							.format("End to collect followers, categoryId = %s, typeId = %s, userId = %s, followerSize = %s",
									categoryId, typeId, userId, followerSize));

					followerList.addAll(collectedFollowerList);
				}

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

		defaultHttpClient.getConnectionManager().shutdown();
	}

	public void initialize() {
		objectMapper = new ObjectMapper();

		collectExistingFollowers();
	}

	public void collectFollowers() {
		DefaultHttpClient defaultHttpClient = getDefaultHttpClient();

		ActiveUser activeUser;

		ActiveUserPhase activeUserPhase = ActiveUserPhase.collecting;

		try {
			activeUser = activeUserService.getActiveUser(activeUserPhase);
		} catch (ServiceException e) {
			logger.error("Exception", e);

			throw new ActionException(e);
		}

		setCookies(defaultHttpClient, activeUser.getCookies());

		boolean successful = false;

		for (int i = 0; i < 10; i++) {
			try {
				weiboHandler.refresh(defaultHttpClient);

				successful = true;

				break;
			} catch (HandlerException e) {
				continue;
			}
		}

		if (!successful) {
			return;
		}

		activeUser.setCookies(getCookies(defaultHttpClient));

		try {
			activeUserService.updateActiveUser(activeUserPhase, activeUser);
		} catch (ServiceException e) {
			logger.error("Exception", e);

			throw new ActionException(e);
		}

		try {
			saeAppBatchhelperHandler.authorize(defaultHttpClient);
		} catch (HandlerException e) {
			logger.error("Exception", e);

			throw new ActionException(e);
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

					List<Follower> collectedFollowerList = new ArrayList<Follower>();

					int cursor = 0;
					int count = 100;

					followerSize = 0;

					logger.debug(String
							.format("Begin to collect followers, categoryId = %s, typeId = %s, userId = %s, followerSize = %s",
									categoryId, typeId, userId, followerSize));

					int times = 5;

					while (true) {
						List<Follower> vFollowerList;
						int nextCursor;

						followerSize = 0;

						logger.debug(String
								.format("Begin to collect followers, categoryId = %s, typeId = %s, userId = %s, cursor = %s, followerSize = %s",
										categoryId, typeId, userId, cursor,
										followerSize));

						try {
							Followers followers = saeAppBatchhelperHandler
									.getFollowerIdsByUserName(
											defaultHttpClient, userName,
											cursor, count);

							vFollowerList = followers.getFollowerList();
							nextCursor = followers.getNextCursor();

							times--;
						} catch (HandlerException e) {
							vFollowerList = new ArrayList<Follower>();
							nextCursor = cursor + count;
						}

						followerSize = vFollowerList.size();

						logger.debug(String
								.format("End to collect followers, categoryId = %s, typeId = %s, userId = %s, cursor = %s, followerSize = %s",
										categoryId, typeId, userId, cursor,
										followerSize));

						collectedFollowerList.addAll(vFollowerList);

						if (times == 0) {
							break;
						}

						if (nextCursor == 0) {
							break;
						} else {
							cursor = nextCursor;
						}
					}

					followerSize = collectedFollowerList.size();

					logger.debug(String
							.format("End to collect followers, categoryId = %s, typeId = %s, userId = %s, followerSize = %s",
									categoryId, typeId, userId, followerSize));

					followerList.addAll(collectedFollowerList);
				}

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

		defaultHttpClient.getConnectionManager().shutdown();
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
		DefaultHttpClient defaultHttpClient = getDefaultHttpClient();

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
					setCookies(defaultHttpClient, activeUser.getCookies());

					boolean successful = false;

					for (int i = 0; i < 10; i++) {
						try {
							weiboHandler.refresh(defaultHttpClient);

							successful = true;

							break;
						} catch (HandlerException e) {
							continue;
						}
					}

					if (!successful) {
						continue;
					}

					activeUser.setCookies(getCookies(defaultHttpClient));

					List<Follower> followerList;

					try {
						followerList = followerService.getFollowerList(
								categoryId, typeId, FollowerPhase.filtered, 0,
								followedFollowerSize);
					} catch (ServiceException e) {
						logger.error("Exception", e);

						throw new ActionException(e);
					}

					int followerSize = followerList.size();

					logger.debug(String
							.format("Begin to follow followers, categoryId = %s, typeId = %s, followerSize = %s",
									categoryId, typeId, followerSize));

					followerSize = 0;

					for (Follower follower : followerList) {
						try {
							weiboHandler.follow(defaultHttpClient,
									follower.getUserId());

							try {
								Thread.sleep(10000);
							} catch (InterruptedException e) {
								logger.error("Exception", e);

								throw new ActionException(e);
							}

							try {
								followerService.moveFollower(categoryId,
										typeId, FollowerPhase.filtered,
										FollowerPhase.followed, follower);

								followerSize++;
							} catch (ServiceException e) {
								logger.error("Exception", e);

								throw new ActionException(e);
							}

							logger.debug(String
									.format("Follow follower successfully, followerUserId = %s",
											follower.getUserId()));
						} catch (HandlerException e) {
							try {
								followerService.deleteFollower(categoryId,
										typeId, FollowerPhase.filtered,
										follower.getId());
							} catch (ServiceException ex) {
								logger.error("Exception", ex);

								throw new ActionException(ex);
							}

							logger.debug(String
									.format("Follow follower failed, followerUserId = %s",
											follower.getUserId()));
						}
					}

					logger.debug(String
							.format("End to follow followers, categoryId = %s, typeId = %s, followerSize = %s",
									categoryId, typeId, followerSize));
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

		defaultHttpClient.getConnectionManager().shutdown();
	}

	public void unfollowFollowers() {
		DefaultHttpClient defaultHttpClient = getDefaultHttpClient();

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
					setCookies(defaultHttpClient, activeUser.getCookies());

					boolean successful = false;

					for (int i = 0; i < 10; i++) {
						try {
							weiboHandler.refresh(defaultHttpClient);

							successful = true;

							break;
						} catch (HandlerException e) {
							continue;
						}
					}

					if (!successful) {
						continue;
					}

					activeUser.setCookies(getCookies(defaultHttpClient));

					List<Follower> followerList;

					try {
						followerList = followerService.getFollowerList(
								categoryId, typeId, FollowerPhase.followed, 0,
								unfollowedFollowerSize);
					} catch (ServiceException e) {
						logger.error("Exception", e);

						throw new ActionException(e);
					}

					int followerSize = followerList.size();

					logger.debug(String
							.format("Begin to unfollow followers, categoryId = %s, typeId = %s, followerSize = %s",
									categoryId, typeId, followerSize));

					followerSize = 0;

					for (Follower follower : followerList) {
						try {
							weiboHandler.unfollow(defaultHttpClient,
									follower.getUserId());

							try {
								Thread.sleep(10000);
							} catch (InterruptedException e) {
								logger.error("Exception", e);

								throw new ActionException(e);
							}

							try {
								followerService.moveFollower(categoryId,
										typeId, FollowerPhase.followed,
										FollowerPhase.unfollowed, follower);

								followerSize++;
							} catch (ServiceException e) {
								logger.error("Exception", e);

								throw new ActionException(e);
							}

							logger.debug(String
									.format("Unfollow follower successfully, followerUserId = %s",
											follower.getUserId()));
						} catch (HandlerException e) {
							try {
								followerService.deleteFollower(categoryId,
										typeId, FollowerPhase.followed,
										follower.getId());
							} catch (ServiceException ex) {
								logger.error("Exception", ex);

								throw new ActionException(ex);
							}

							logger.debug(String
									.format("Unfollow follower failed, followerUserId = %s",
											follower.getUserId()));
						}
					}

					logger.debug(String
							.format("End to unfollow followers, categoryId = %s, typeId = %s, followerSize = %s",
									categoryId, typeId, followerSize));
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

		defaultHttpClient.getConnectionManager().shutdown();
	}

}
