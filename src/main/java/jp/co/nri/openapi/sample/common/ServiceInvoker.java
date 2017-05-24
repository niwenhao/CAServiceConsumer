package jp.co.nri.openapi.sample.common;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.json.Json;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.UserTransaction;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.entity.ContentType;
import org.apache.http.impl.client.HttpClients;

import jp.co.nri.openapi.sample.persistence.Client;
import jp.co.nri.openapi.sample.persistence.Service;
import jp.co.nri.openapi.sample.persistence.Token;
import jp.co.nri.openapi.sample.persistence.User;

public abstract class ServiceInvoker implements JsonHelper {
	
	public static final String RETURN_URL = "returnUrl";
	public static final String FOLLOW_PARAMETERS = "followParameters";
	public static final String CLIENT_ID = "clientId";
	public static final String USER_ID = "userId";
	
	private User user;
	private Token token;
	private Service service;
	private Client client;

	enum TOKEN_STATUS {
		NOT_EXIST, EXPIRED, GRANTED
	}

	public static class OAuthRedirectException extends Exception implements JsonHelper {
		static final long serialVersionUID = 0;
		private String responseType;
		private String clientId;
		private String requestUri;
		private String scope;
		private Map<String, Object> state = new HashMap<>();
		private String nonce;
		
		private String authorizeUrl;


		public String transRedirectUrl() {
			try {
				StringBuilder sb = new StringBuilder(authorizeUrl);
				sb.append("?response_type=code");
				sb.append("&client_id=").append(URLEncoder.encode(clientId, StandardCharsets.UTF_8.name()));
				sb.append("&request_uri=").append(URLEncoder.encode(clientId, StandardCharsets.UTF_8.name()));
				sb.append("&scope=").append(URLEncoder.encode(scope, StandardCharsets.UTF_8.name()));
				sb.append("&nonce=").append(URLEncoder.encode(nonce, StandardCharsets.UTF_8.name()));

				sb.append("&state=").append(URLEncoder.encode(new String(map2Json(state), StandardCharsets.UTF_8),
						StandardCharsets.UTF_8.name()));
				return sb.toString();
			} catch (UnsupportedEncodingException e) {
				throw new RuntimeException(e);
			}
		}

		public String getResponseType() {
			return responseType;
		}

		public void setResponseType(String responseType) {
			this.responseType = responseType;
		}

		public String getClientId() {
			return clientId;
		}

		public void setClientId(String clientId) {
			this.clientId = clientId;
		}

		public String getRequestUri() {
			return requestUri;
		}

		public void setRequestUri(String requestUri) {
			this.requestUri = requestUri;
		}

		public String getScope() {
			return scope;
		}

		public void setScope(String scope) {
			this.scope = scope;
		}

		public Map<String, Object> getState() {
			return state;
		}

		public String getNonce() {
			return nonce;
		}

		public void setNonce(String nonce) {
			this.nonce = nonce;
		}

		public String getAuthorizeUrl() {
			return authorizeUrl;
		}

		public void setAuthorizeUrl(String authorizeUrl) {
			this.authorizeUrl = authorizeUrl;
		}
	}

	@Resource
	private UserTransaction ut;

	@PersistenceContext
	private EntityManager em;

	private TOKEN_STATUS checkToken(String name) {
		List<Service> services = em.createNamedQuery(Service.FIND_BY_NAME, Service.class).setParameter("name", name).getResultList();
		if (services.size() != 1) {
			throw new RuntimeException(String.format("Find service by %s failed.", name)); 
		}
		this.service = services.get(0);
		this.client = service.getClient();
		
		this.user = em.find(User.class, this.getUserId());
		
		List<Token> tokens = em.createNamedQuery(Token.FIND_ALL_BY_USERID_AND_SERVICE_NAME, Token.class).setParameter("user_id", this.getUserId()).setParameter("name", name).getResultList();
		if (tokens.size() != 1) {
			this.token = null;
			return TOKEN_STATUS.NOT_EXIST;
		}
		
		this.token = tokens.get(0);
		if (token.getTimeLimit().getTime() < System.currentTimeMillis()) {
			return TOKEN_STATUS.EXPIRED;
		}
		
		return TOKEN_STATUS.GRANTED;
	}

	private void redirectToTokenRequire() throws OAuthRedirectException {
		OAuthRedirectException re = new OAuthRedirectException();
		re.authorizeUrl = this.client.getAuthorizeUrl();
		re.clientId = this.client.getIdent();
		re.nonce = "xxx";
		re.requestUri = this.client.getRequestUrl();
		re.scope = this.client.getScope();
		Map<String, Object> state = new HashMap<>();
		
		state.put(RETURN_URL, this.getReturnURL());
		state.put(FOLLOW_PARAMETERS, this.getAppParameters());
		state.put(CLIENT_ID, this.client.getId());
		state.put(USER_ID, user.getId());
		re.state = state;
		
		throw re;
	}

	private void refreshToken() {
		throw new RuntimeException("RefreshToken was not implemented.....");
	}

	private Map<String, Object> httpCall(Map<String, Object> inDto) {
		HttpUriRequest request = null;
		HttpClient httpClient = HttpClients.createDefault();
		HttpResponse response = null;
		if (inDto != null) {
			HttpPost post = new HttpPost(this.service.getUrl());
			post.setEntity(new ByteArrayEntity(map2Json(inDto), ContentType.APPLICATION_JSON));
			request = post;
		} else {
			request = new HttpGet(this.service.getUrl());
		}

		try {
			response = httpClient.execute(request);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}

		if (response.getStatusLine().getStatusCode() != 200) {
			throw new RuntimeException(
					"Status is " + response.getStatusLine().getStatusCode() + "\\n" + response.getEntity().toString());
		}

		if (response.getEntity().getContentType().getValue().replaceAll("^.*application/json.*$", "").length() == 0) {
			throw new RuntimeException(
					"Content-type is " + response.getEntity().getContentType().getValue() + "\\n" + response.getEntity().toString());
		}
		
		try {
			return json2Map(response.getEntity().getContent());
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public Map<String, Object> invokeService(String name, Map<String, Object> inDto) throws OAuthRedirectException {
		TOKEN_STATUS ts = checkToken(name);
		if (TOKEN_STATUS.GRANTED == ts) {
			return httpCall(inDto);
		} else if (TOKEN_STATUS.NOT_EXIST == ts) {
			redirectToTokenRequire();
		} else if (TOKEN_STATUS.EXPIRED == ts) {
			refreshToken();
			return httpCall(inDto);
		}
		return null;
	}

	protected abstract long getUserId();
	
	protected abstract Map<String, String> getAppParameters();

	protected abstract String getReturnURL();

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public Token getToken() {
		return token;
	}

	public void setToken(Token token) {
		this.token = token;
	}

	public Service getService() {
		return service;
	}

	public void setService(Service service) {
		this.service = service;
	}

	public Client getClient() {
		return client;
	}

	public void setClient(Client client) {
		this.client = client;
	}
	
}
