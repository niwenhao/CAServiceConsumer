package jp.co.nri.openapi.sample.faces;

import javax.faces.bean.*;

@ManagedBean()
public class Login {
	private String userId;
	private String password;

	public String tryLogin() {
		System.out.println(String.format("uid=%s pwd=%s", this.userId, this.password));
		return "List";
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}
}
