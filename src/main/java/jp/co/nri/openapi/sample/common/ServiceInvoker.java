package jp.co.nri.openapi.sample.common;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.servlet.http.HttpSession;
import javax.transaction.SystemException;
import javax.transaction.UserTransaction;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.entity.ContentType;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;

import jp.co.nri.openapi.sample.persistence.Client;
import jp.co.nri.openapi.sample.persistence.Service;
import jp.co.nri.openapi.sample.persistence.Token;
import jp.co.nri.openapi.sample.persistence.User;

/**
 * Oauth認証、遷移機能を提供するヘルパー
 */
/**
 * @author nwh
 *
 */
public abstract class ServiceInvoker implements JsonHelper, OpenIdHelper {

	/**
	 * Token取得完了後、APPに戻るURLを保持するStateキー
	 */
	public static final String RETURN_URL = "returnUrl";
	/**
	 * APPに戻るとき、渡すパラメータを保持するStateキー
	 */
	public static final String FOLLOW_PARAMETERS = "followParameters";
	/**
	 * Token取得処理に使われるClientテーブルのIDを保持するStateキー
	 */
	public static final String CLIENT_ID = "clientId";
	/**
	 * Token取得処理に使われるUserテーブルのIDを保持するStateキー
	 */
	public static final String USER_ID = "userId";

	private User user;
	private Token token;
	private Service service;
	private Client client;

	/**
	 * トークンの状態。
	 */
	enum TOKEN_STATUS {
		NOT_EXIST, EXPIRED, GRANTED
	}

	/**
	 * 認証システムに遷移する必要をAPPに通知する例外。
	 *
	 */
	public static class OAuthRedirectException extends Exception implements JsonHelper {
		static final long serialVersionUID = 0;
		private String responseType;
		private String clientId;
		private String requestUri;
		private String scope;
		private String state;
		private String nonce;

		private String authorizeUrl;

		/**
		 * 遷移先URLを取得する。
		 * 
		 * @return 遷移先URL
		 */
		public String transRedirectUrl() {
			try {
				StringBuilder sb = new StringBuilder(authorizeUrl);
				sb.append("?response_type=code");
				sb.append("&client_id=").append(URLEncoder.encode(clientId, StandardCharsets.UTF_8.name()));
				sb.append("&request_uri=").append(URLEncoder.encode(requestUri, StandardCharsets.UTF_8.name()));
				sb.append("&scope=").append(URLEncoder.encode(scope, StandardCharsets.UTF_8.name()));
				sb.append("&nonce=").append(URLEncoder.encode(nonce, StandardCharsets.UTF_8.name()));

				sb.append("&state=").append(URLEncoder.encode(state, StandardCharsets.UTF_8.name()));
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

		public String getState() {
			return state;
		}
		
		public void setState(String state) {
			this.state = state;
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

	/**
	 * トークン状態チェック
	 * 
	 * @param name
	 *            呼び出しサービスの名称。
	 * @return トークン状態
	 */
	private TOKEN_STATUS checkToken(String name) {
		List<Service> services = em.createNamedQuery(Service.FIND_BY_NAME, Service.class).setParameter("name", name)
				.getResultList();
		if (services.size() != 1) {
			throw new RuntimeException(String.format("Find service by %s failed.", name));
		}
		this.service = services.get(0);
		this.client = service.getClient();

		this.user = em.find(User.class, this.getUserId());

		List<Token> tokens = em.createNamedQuery(Token.FIND_ALL_BY_USERID_AND_SERVICE_NAME, Token.class)
				.setParameter("user_id", this.getUserId()).setParameter("name", name).getResultList();
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

	/**
	 * 認証ポイントに遷移するデータ準備。
	 * 
	 * 最後にOAuthRedirectExceptionをスルーして、APPが遷移してもらう。
	 * 
	 * @throws OAuthRedirectException
	 */
	private void redirectToTokenRequire() throws OAuthRedirectException {
		OAuthRedirectException re = new OAuthRedirectException();
		re.authorizeUrl = this.client.getAuthorizeUrl();
		re.clientId = this.client.getIdent();
		re.nonce = randomGen();
		this.getSession().setAttribute(ConstDef.SK_NONCE_VALUE, re.nonce);
		re.requestUri = this.client.getRequestUrl();
		re.scope = this.client.getScope();
		Map<String, Object> forwardValue = new HashMap<>();

		forwardValue.put(RETURN_URL, this.getReturnURL());
		forwardValue.put(FOLLOW_PARAMETERS, this.getAppParameters());
		forwardValue.put(CLIENT_ID, this.client.getId());
		forwardValue.put(USER_ID, user.getId());
		this.getSession().setAttribute(ConstDef.SK_FORWARD_VALUE, forwardValue);

		re.state = randomGen();
		this.getSession().setAttribute(ConstDef.SK_STATE_VALUE, re.getState());
		throw re;
	}

	/**
	 * 時間切れのトークンを更新する。
	 */
	private void refreshToken() {

		try {
			ut.begin();
			// トークンと関連するデータを取得しておく
			user = em.merge(user);
			client = em.merge(client);

			Map<String, Object> rst = takeRefreshToken(client, token);

			token.setAccessToken((String) rst.get("access_token"));
			token.setRefreshToken((String) rst.get("refresh_token"));
			token.setTimeLimit(
					new Date(System.currentTimeMillis() + ((BigDecimal) rst.get("expires_in")).longValue() * 1000));

			token.setUser(user);
			token.setClient(client);
			
			token = em.merge(token);

			em.persist(token);

			ut.commit();
		} catch (Exception e) {
			try {
				ut.rollback();
			} catch (Exception e1) {
				throw new RuntimeException(e1);
			} finally {
				throw new RuntimeException(e);
			}
		}
	}

	/**
	 * サービスを呼び出す。
	 * 
	 * @param inDto
	 * @return outDto
	 */
	private Map<String, Object> httpCall(Map<String, Object> inDto) {
		HttpUriRequest request = null;
		HttpClient httpClient = HttpClients.createDefault();
		HttpResponse response = null;
		// inDtoは存在しない場合、GET、でなければ、POST
		if (inDto != null) {
			HttpPost post = new HttpPost(this.service.getUrl());
			post.setEntity(new ByteArrayEntity(map2Json(inDto), ContentType.APPLICATION_JSON));
			request = post;
		} else {
			request = new HttpGet(this.service.getUrl());
		}

		// 認証情報を入れる。
		request.addHeader("Authorization", String.format("bearer %s", token.getAccessToken()));
		request.addHeader("Content-type", "application/json");

		try {
			response = httpClient.execute(request);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}

		try {
			// ステータスコードチェック
			if (response.getStatusLine().getStatusCode() != 200) {
				throw new RuntimeException("Status is " + response.getStatusLine().getStatusCode() + "\\n"
						+ response.getEntity().toString() + "\\n"
						+ new String(dumpStream(response.getEntity().getContent())));
			}

			// コンテンツタイプチェック
			if (response.getEntity().getContentType().getValue().replaceAll("^.*application/json.*$", "")
					.length() != 0) {
				throw new RuntimeException("Content-type is " + response.getEntity().getContentType().getValue() + "\\n"
						+ response.getEntity().toString() + "\\n"
						+ new String(dumpStream(response.getEntity().getContent())));
			}
			return json2Map(response.getEntity().getContent());
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * 入力ストリームから内容をバッファに入れる。
	 * 
	 * @param stream
	 *            入力ストレーム
	 * @return バッファ
	 */
	private byte[] dumpStream(InputStream stream) {
		try {
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			int len;
			byte[] buf = new byte[1024];

			while ((len = stream.read(buf)) > 0) {
				bos.write(buf, 0, len);
			}

			bos.close();
			return bos.toByteArray();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * 外部に対して、サービスを呼び出す機能を提供する。
	 * 
	 * @param name
	 *            サービス名称
	 * @param inDto
	 *            渡す情報
	 * @return サービス呼び出す結果
	 * @throws OAuthRedirectException
	 *             何らがな理由で認証ポイントに遷移するとき使う。
	 */
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
	
	/**
	 * このサービスを利用するAPPからユーザIDを取得する。
	 * 
	 * @return ユーザのID
	 */
	protected abstract long getUserId();

	/**
	 * 外部認証が必要な場合、戻すとき、APPに送るバラメータ一覧を取得する。
	 * 
	 * @return パラメータ一覧。
	 */
	protected abstract Map<String, String> getAppParameters();

	/**
	 * 外部認証が必要な場合、戻すときのURL
	 * 
	 * @return URL
	 */
	protected abstract String getReturnURL();

	/**
	 * Nonce情報を持久保存するため、セッションを取得する。
	 * @return  セッションオブジェクト
	 */
	protected abstract HttpSession getSession();
}
