package jp.co.nri.openapi.sample.faces;

import javax.annotation.Resource;
import javax.faces.bean.ManagedBean;
import javax.faces.context.FacesContext;
import javax.faces.event.AbortProcessingException;
import javax.faces.event.ComponentSystemEvent;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.UserTransaction;

import jp.co.nri.openapi.sample.common.ConstDef;
import jp.co.nri.openapi.sample.persistence.User;

@ManagedBean
public class UserManEditBean {

	@Resource
	UserTransaction ut;

	@PersistenceContext
	EntityManager em;

	private User data = new User();
	
	public User getData() {
		return data;
	}

	public void setData(User data) {
		this.data = data;
	}

	public void preRenderView(ComponentSystemEvent event) throws AbortProcessingException {
		Long id = (Long) FacesContext.getCurrentInstance().getExternalContext().getSessionMap()
				.get(ConstDef.SK_USER_ID);
		System.out.println(String.format("preRenderView(%d)", id));

		if (null == id) {
			this.data = new User();
		} else {
			this.data = em.find(User.class, id);
		}
	}

	public String update() throws Exception {
		ut.begin();

		try {

			User d = em.merge(data);

			em.persist(d);

			ut.commit();
		} catch (Exception e) {
			ut.rollback();
		}

		return "user_man";
	}

	public String append() throws Exception {
		ut.begin();

		try {

			em.persist(data);

			ut.commit();
		} catch (Exception e) {
			ut.rollback();
		}
		return "user_man";
	}
}
