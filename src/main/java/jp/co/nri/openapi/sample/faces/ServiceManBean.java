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
import jp.co.nri.openapi.sample.persistence.Service;

@ManagedBean
public class ServiceManBean {

	@Resource
    UserTransaction ut;

	@PersistenceContext
	EntityManager em;
	
	public List<Service> listServices() throws Exception {
		
		long id = (Long)FacesContext.getCurrentInstance().getExternalContext().getSessionMap().get(ConstDef.SK_CLIENT_ID);
		return em.createNamedQuery(Service.FIND_BY_CLIENT_ID, Service.class).setParameter("client_id", id).getResultList();
	}
	
	public String update(long id) throws Exception {
		FacesContext.getCurrentInstance().getExternalContext().getSessionMap().put(ConstDef.SK_SERVICE_ID, id);
		return "service_man_edit";
	}

	public String delete(long id) throws Exception {
		ut.begin();

		Service u = em.find(Service.class, Long.valueOf(id));
		em.remove(u);
		ut.commit();
		return null;
	}
	
	public String append() throws Exception {
		Map<String, Object> map = FacesContext.getCurrentInstance().getExternalContext().getSessionMap();
		if (map.containsKey(ConstDef.SK_SERVICE_ID))
			map.remove(ConstDef.SK_SERVICE_ID);
		return "service_man_edit";
	}
}
