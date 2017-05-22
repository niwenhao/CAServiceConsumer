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
import jp.co.nri.openapi.sample.persistence.Service;

@ManagedBean
public class ServiceManEditBean {

	@Resource
	UserTransaction ut;

	@PersistenceContext
	EntityManager em;

	private Service data = null;
	
	public Service getData() {
		return data;
	}

	public void setData(Service data) {
		this.data = data;
	}

	public void preRenderView(ComponentSystemEvent event) throws AbortProcessingException {
		Long id = (Long) FacesContext.getCurrentInstance().getExternalContext().getSessionMap()
				.get(ConstDef.SK_SERVICE_ID);
		System.out.println(String.format("preRenderView(%d)", id));

		if (null == id) {
			this.data = new Service();
		} else {
			this.data = em.find(Service.class, id);
		}
	}

	public String update() throws Exception {
		ut.begin();

		try {

			Service d = em.merge(data);

			em.persist(d);

			ut.commit();
		} catch (Exception e) {
			ut.rollback();
		}

		return "service_man";
	}

	public String append() throws Exception {
		ut.begin();
		
		try {
			long clientId = (Long)FacesContext.getCurrentInstance().getExternalContext().getSessionMap().get(ConstDef.SK_CLIENT_ID);
			
			Client client = em.find(Client.class, clientId);
			
			data.setClient(client);

			em.persist(data);

			ut.commit();
		} catch (Exception e) {
			ut.rollback();
		}
		return "service_man";
	}
}
