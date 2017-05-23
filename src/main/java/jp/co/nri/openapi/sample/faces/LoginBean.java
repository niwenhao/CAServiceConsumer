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

@ManagedBean
public class LoginBean {

	@PersistenceContext
	EntityManager em;

	private String username;
	private String password;
	private String errorMessage;

	public String login() {
		List<User> rst = em.createNamedQuery(User.Q_LOGIN_WITH_USERNAME_AND_PASSWORD, User.class)
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

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getErrorMessage() {
		return errorMessage;
	}

	public void setErrorMessage(String errorMessage) {
		this.errorMessage = errorMessage;
	}

}
