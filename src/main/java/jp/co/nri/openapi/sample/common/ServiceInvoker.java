package jp.co.nri.openapi.sample.common;

import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObjectBuilder;
import javax.json.JsonStructure;
import javax.json.JsonValue;
import javax.json.JsonWriter;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.UserTransaction;

import org.apache.commons.codec.binary.Base64;

public abstract class ServiceInvoker {
	enum TOKEN_STATUS {
		NOT_EXIST, EXPIRED, GRANTED
	}

	public static class OAuthRedirectException extends Exception {
		private String responseType;
		private String clientId;
		private String requestUri;
		private String scope;
		private Map<String, Object> state = new HashMap<>();
		private String nonce;

		private String authorizeUrl;

		private JsonObjectBuilder convertJsonObject(Map<String, Object> map) {
			JsonObjectBuilder jb = Json.createObjectBuilder();
			map.forEach((k, v) -> {
				if (v instanceof List) {
					jb.add(k, convertJsonList((List<Object>) v));
				} else if (v instanceof Map) {
					jb.add(k, convertJsonObject((Map<String, Object>) v));
				} else {
					jb.add(k, new ConvertableJsonValue(v));
				}
			});
			return jb;
		}

		private JsonArrayBuilder convertJsonList(List<Object> list) {
			JsonArrayBuilder jb = Json.createArrayBuilder();
			list.forEach((v) -> {
				if (v instanceof List) {
					jb.add(convertJsonList((List<Object>) v));
				} else if (v instanceof Map) {
					jb.add(convertJsonObject((Map<String, Object>) v));
				} else {
					jb.add(new ConvertableJsonValue(v));
				}
			});

			return jb;
		}

		public static class ConvertableJsonValue implements JsonValue {
			private Object value;

			public ConvertableJsonValue(Object value) {
				this.value = value;
			}

			@Override
			public ValueType getValueType() {
				if (value instanceof Long || value instanceof Integer || value instanceof Float
						|| value instanceof Double) {
					return ValueType.NUMBER;
				}
				if (value instanceof Boolean) {
					return ((Boolean) value).booleanValue() ? ValueType.TRUE : ValueType.FALSE;
				}

				return ValueType.STRING;
			}

			@Override
			public String toString() {
				return value.toString();
			}
		}

		public String transRedirectUrl() {
			try {
				StringBuilder sb = new StringBuilder(authorizeUrl);
				sb.append("?response_type=code");
				sb.append("&client_id=").append(URLEncoder.encode(clientId, StandardCharsets.UTF_8.name()));
				sb.append("&request_uri=").append(URLEncoder.encode(clientId, StandardCharsets.UTF_8.name()));
				sb.append("&scope=").append(URLEncoder.encode(scope, StandardCharsets.UTF_8.name()));
				sb.append("&nonce=").append(URLEncoder.encode(nonce, StandardCharsets.UTF_8.name()));

				ByteArrayOutputStream bos = new ByteArrayOutputStream();
				JsonWriter jw = Json.createWriter(bos);
				jw.writeObject(convertJsonObject(state).build());
				sb.append("&state=").append(
						URLEncoder.encode(bos.toString(StandardCharsets.UTF_8.name()), StandardCharsets.UTF_8.name()));
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

	private TOKEN_STATUS checkToken() {
		return TOKEN_STATUS.GRANTED;
	}

	private void redirectToTokenRequire() {

	}

	private void refreshToken() {

	}

	public JsonStructure invokeService(String name, JsonStructure inDto) {
		TOKEN_STATUS ts = checkToken();
		if (TOKEN_STATUS.GRANTED == ts) {

		} else if (TOKEN_STATUS.NOT_EXIST == ts) {
			redirectToTokenRequire();
		} else if (TOKEN_STATUS.EXPIRED == ts) {
			refreshToken();
		}
		return null;
	}

	protected abstract long getUserId();
}
