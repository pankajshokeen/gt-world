/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.gtworld.service;

import com.gtworld.entity.AsistenciaEvento;
import com.gtworld.entity.AsistenciaEventoPK;
import java.util.List;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.ws.rs.*;

/**
 *
 * @author Antonio
 */
@Stateless
@Path("com.gtworld.entity.asistenciaevento")
public class AsistenciaEventoFacadeREST extends AbstractFacade<AsistenciaEvento> {
    @PersistenceContext(unitName = "GTWorldPU")
    private EntityManager em;

    public AsistenciaEventoFacadeREST() {
        super(AsistenciaEvento.class);
    }

    @POST
    @Override
    @Consumes({"application/xml", "application/json"})
    public void create(AsistenciaEvento entity) {
        super.create(entity);
    }

    @PUT
    @Override
    @Consumes({"application/xml", "application/json"})
    public void edit(AsistenciaEvento entity) {
        super.edit(entity);
    }

    /*
    @DELETE
    @Path("{id}")
    public void remove(@PathParam("id") AsistenciaEventoPK id) {
        super.remove(super.find(id));
    }

    @GET
    @Path("{id}")
    @Produces({"application/xml", "application/json"})
    public AsistenciaEvento find(@PathParam("id") AsistenciaEventoPK id) {
        return super.find(id);
    }*/

    @GET
    @Override
    @Produces({"application/xml", "application/json"})
    public List<AsistenciaEvento> findAll() {
        return super.findAll();
    }

    @GET
    @Path("{from}/{to}")
    @Produces({"application/xml", "application/json"})
    public List<AsistenciaEvento> findRange(@PathParam("from") Integer from, @PathParam("to") Integer to) {
        return super.findRange(new int[]{from, to});
    }

    @GET
    @Path("count")
    @Produces("text/plain")
    public String countREST() {
        return String.valueOf(super.count());
    }

    @java.lang.Override
    protected EntityManager getEntityManager() {
        return em;
    }
    
}
