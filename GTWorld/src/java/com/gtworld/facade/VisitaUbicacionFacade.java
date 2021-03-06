/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.gtworld.facade;

import com.gtworld.entity.VisitaUbicacion;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

/**
 *
 * @author Antonio
 */
@Stateless
public class VisitaUbicacionFacade extends AbstractFacade<VisitaUbicacion> {
    @PersistenceContext(unitName = "GTWorldPU")
    private EntityManager em;

    @Override
    protected EntityManager getEntityManager() {
        return em;
    }

    public VisitaUbicacionFacade() {
        super(VisitaUbicacion.class);
    }
    
}
