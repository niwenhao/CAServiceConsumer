package jp.co.nri.openapi.sample.faces;

import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.faces.bean.ManagedBean;
import javax.faces.context.FacesContext;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.UserTransaction;

import jp.co.nri.openapi.sample.common.ConstDef;
import jp.co.nri.openapi.sample.persistence.User;

/**
 * ログインView
 * テンプレート：<a href="../../../../../../templates/login.txt">login.xhtml</a>
 */
@ManagedBean
public class LoginBean {

	@PersistenceContext
	EntityManager em;

	private String username;
	private String password;
	private String errorMessage;

	/**
	 * ログインボタンを押下した場合の処理。
	 * 
	 * ユーザ名とパスワードをチェックする
	 * @return 遷移先、成功する場合"app_menu"に行く。
	 */
	public String login() {
		List<User> rst = em.createNamedQuery(User.FIND_BY_USERNAME_AND_PASSWORD, User.class)
				.setParameter("username", this.getUsername()).setParameter("password", this.getPassword())
				.getResultList();
		if (rst.size() == 1) {
			FacesContext.getCurrentInstance().getExternalContext().getSessionMap().put(ConstDef.SK_USER_ID, rst.get(0).getId());
			return "app_menu";
		} else {
			this.setErrorMessage("指定されたユーザ名とパスワードの組み合わせが存在しません！");
			return "login";
		}
	}

	/**
	 * @return	ユーザ名
	 */
	public String getUsername() {
		return username;
	}

	/**
	 * @param username	ユーザ名
	 */
	public void setUsername(String username) {
		this.username = username;
	}

	/**
	 * @return パスワード
	 */
	public String getPassword() {
		return password;
	}

	/**
	 * @param password パスワード
	 */
	public void setPassword(String password) {
		this.password = password;
	}

	/**
	 * @return エラーメッセージ
	 */
	public String getErrorMessage() {
		return errorMessage;
	}

	/**
	 * @param errorMessage エラーメッセージ
	 */
	public void setErrorMessage(String errorMessage) {
		this.errorMessage = errorMessage;
	}

}
