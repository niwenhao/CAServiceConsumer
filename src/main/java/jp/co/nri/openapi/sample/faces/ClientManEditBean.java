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
import jp.co.nri.openapi.sample.persistence.Client;

@ManagedBean
public class ClientManEditBean {

	@Resource
	UserTransaction ut;

	@PersistenceContext
	EntityManager em;

	private Client data = null;
	
	public Client getData() {
		return data;
	}

	public void setData(Client data) {
		this.data = data;
	}

	public void preRenderView(ComponentSystemEvent event) throws AbortProcessingException {
		Long id = (Long) FacesContext.getCurrentInstance().getExternalContext().getSessionMap()
				.get(ConstDef.SK_CLIENT_ID);
		System.out.println(String.format("preRenderView(%d)", id));

		if (null == id) {
			this.data = new Client();
		} else {
			this.data = em.find(Client.class, id);
		}
	}

	public String update() throws Exception {
		ut.begin();

		try {

			// Client d = em.find(Client.class, data.getId());
			// d.setIdent(data.getIdent());
			// d.setSecret(data.getSecret());
			// d.setScope(data.getScope());
			// d.setAuthorizeUrl(data.getAuthorizeUrl());
			// d.setTokenUrl(data.getTokenUrl());

			Client d = em.merge(data);

			em.persist(d);

			ut.commit();
		} catch (Exception e) {
			ut.rollback();
		}

		return "client_man";
	}

	public String append() throws Exception {
		ut.begin();

		try {

			em.persist(data);

			ut.commit();
		} catch (Exception e) {
			ut.rollback();
		}
		return "client_man";
	}
}
