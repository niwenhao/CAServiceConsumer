package jp.co.nri.openapi.sample.common;

/**
 * 定数定義
 * 
 * @author nwh
 */
public final class ConstDef {
	/**
	 * ユーザIDを格納するセッションキー
	 */
	public static final String SK_USER_ID = "SK_USER_ID";
	/**
	 * クライアントIDを格納するセッションキー
	 */
	public static final String SK_CLIENT_ID = "SK_CLIENT_ID";
	/**
	 * サービス（API）IDを格納するセッションキー
	 */
	public static final String SK_SERVICE_ID = "SK_SERVICE_ID";
	
	/**
	 * Nonceを格納するセッションキー
	 */
	public static final String SK_NONCE_VALUE = "SK_NONCE_VALUE";

	/**
	 * stateを格納するセッションキー
	 */
	public static final String SK_STATE_VALUE = "SK_STATE_VALUE";

	/**
	 * 引き継ぎ情報を格納するセッションキー
	 */
	public static final String SK_FORWARD_VALUE = "SK_FORWARD_VALUE";
}
