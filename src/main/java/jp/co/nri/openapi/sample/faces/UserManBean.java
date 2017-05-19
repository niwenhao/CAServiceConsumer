package jp.co.nri.openapi.sample.faces;

import javax.faces.bean.*;
import javax.transaction.UserTransaction;
import javax.persistence.*;
import javax.persistence.criteria.*;
import javax.annotation.Resource;

import java.util.List;
import jp.co.nri.openapi.sample.persistence.User;

@ManagedBean
public class UserManBean {

	@Resource
    UserTransaction ut;

	@PersistenceContext
	EntityManager em;

	private String updatePassword1;
	private String updatePassword2;
	private String appendUser;
	private String appendPassword1;
	private String appendPassword2;
	
	public List<User> listUsers() {
		CriteriaQuery<User> query = em.getCriteriaBuilder().createQuery(User.class);
		Root<User> root = query.from(User.class);
		return em.createQuery(query.select(root)).getResultList();
	}
	
	private void clearInput() {
		appendUser = "";
		appendPassword1 = "";
		appendPassword2 = "";
		updatePassword1 = "";
		updatePassword2 = "";
	}

	public String update(long id) throws Exception {
		ut.begin();
		User u = em.find(User.class, id);
		u.setPassword(updatePassword1);
		
		em.persist(u);
		ut.commit();
		clearInput();
		return null;
	}

	public String delete(long id) throws Exception {
		ut.begin();

		User u = em.find(User.class, id);
		em.remove(u);
		ut.commit();
		clearInput();
		return null;
	}
	
	public String append() throws Exception {
		System.out.println(String.format("u=%s, p=%s, p=%s", appendUser, appendPassword1, appendPassword2));
		User u = new User();
		u.setName(this.appendUser);
		u.setPassword(this.appendPassword1);
		ut.begin();
		em.persist(u);
		ut.commit();
		clearInput();
		return null;
	}


	public String getUpdatePassword1() {
		return updatePassword1;
	}

	public void setUpdatePassword1(String updatePassword1) {
		this.updatePassword1 = updatePassword1;
	}

	public String getUpdatePassword2() {
		return updatePassword2;
	}

	public void setUpdatePassword2(String updatePassword2) {
		this.updatePassword2 = updatePassword2;
	}

	public String getAppendUser() {
		return appendUser;
	}

	public void setAppendUser(String appendUser) {
		this.appendUser = appendUser;
	}

	public String getAppendPassword1() {
		return appendPassword1;
	}

	public void setAppendPassword1(String appendPassword1) {
		this.appendPassword1 = appendPassword1;
	}

	public String getAppendPassword2() {
		return appendPassword2;
	}

	public void setAppendPassword2(String appendPassword2) {
		this.appendPassword2 = appendPassword2;
	}

}
