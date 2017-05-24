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
public class UserManBean {

	@Resource
    UserTransaction ut;

	@PersistenceContext
	EntityManager em;

	public List<User> listUsers() {
		return em.createNamedQuery(User.LIST_ALL_USERS, User.class).getResultList();
	}
	
	public String update(long id) throws Exception {
		FacesContext.getCurrentInstance().getExternalContext().getSessionMap().put(ConstDef.SK_USER_ID, id);
		return "user_man_edit";
	}

	public String delete(long id) throws Exception {
		ut.begin();

		User u = em.find(User.class, id);
		em.remove(u);
		ut.commit();

		return null;
	}
	
	public String append() throws Exception {
		Map<String, Object> map = FacesContext.getCurrentInstance().getExternalContext().getSessionMap();
		if (map.containsKey(ConstDef.SK_USER_ID))
			map.remove(ConstDef.SK_USER_ID);
		return "user_man_edit";

	}

}
