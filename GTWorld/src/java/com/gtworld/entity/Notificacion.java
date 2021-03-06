/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.gtworld.entity;

import java.io.Serializable;
import java.util.Date;
import javax.persistence.*;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author Antonio
 */
@Entity
@Table(name = "notificacion")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "Notificacion.findAll", query = "SELECT n FROM Notificacion n"),
    @NamedQuery(name = "Notificacion.findByIdNotificacion", query = "SELECT n FROM Notificacion n WHERE n.idNotificacion = :idNotificacion"),
    @NamedQuery(name = "Notificacion.findByContenidoNotificacion", query = "SELECT n FROM Notificacion n WHERE n.contenidoNotificacion = :contenidoNotificacion"),
    @NamedQuery(name = "Notificacion.findByFechaNotificacion", query = "SELECT n FROM Notificacion n WHERE n.fechaNotificacion = :fechaNotificacion"),
    @NamedQuery(name = "Notificacion.findByEstadoNotificacion", query = "SELECT n FROM Notificacion n WHERE n.estadoNotificacion = :estadoNotificacion"),
    @NamedQuery(name = "Notificacion.findByUsuarioEstadoNotificacion", query = "SELECT n FROM Notificacion n WHERE (n.estadoNotificacion = true) AND (n.idUsuario = :idUsuario) ORDER BY n.fechaNotificacion DESC")})
public class Notificacion implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID_NOTIFICACION", insertable = false, nullable = false, updatable = true)
    private Long idNotificacion;
    @Size(max = 25)
    @Column(name = "TITULO_NOTIFICACION")
    private String tituloNotificacion;
    @Size(max = 250)
    @Column(name = "CONTENIDO_NOTIFICACION")
    private String contenidoNotificacion;
    @Column(name = "FECHA_NOTIFICACION")
    @Temporal(TemporalType.TIMESTAMP)
    private Date fechaNotificacion;
    @Column(name = "ESTADO_NOTIFICACION")
    private Boolean estadoNotificacion;
    @JoinColumn(name = "ID_USUARIO", referencedColumnName = "ID_USUARIO")
    @ManyToOne(optional = false)
    private Usuario idUsuario;

    public Notificacion() {
    }

    public Notificacion(Long idNotificacion) {
        this.idNotificacion = idNotificacion;
    }

    public Long getIdNotificacion() {
        return idNotificacion;
    }

    public void setIdNotificacion(Long idNotificacion) {
        this.idNotificacion = idNotificacion;
    }

    public String getTituloNotificacion() {
        return tituloNotificacion;
    }

    public void setTituloNotificacion(String tituloNotificacion) {
        this.tituloNotificacion = tituloNotificacion;
    }

    public String getContenidoNotificacion() {
        return contenidoNotificacion;
    }

    public void setContenidoNotificacion(String contenidoNotificacion) {
        this.contenidoNotificacion = contenidoNotificacion;
    }

    public Date getFechaNotificacion() {
        return fechaNotificacion;
    }

    public void setFechaNotificacion(Date fechaNotificacion) {
        this.fechaNotificacion = fechaNotificacion;
    }

    public Boolean getEstadoNotificacion() {
        return estadoNotificacion;
    }

    public void setEstadoNotificacion(Boolean estadoNotificacion) {
        this.estadoNotificacion = estadoNotificacion;
    }

    public Usuario getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(Usuario idUsuario) {
        this.idUsuario = idUsuario;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (idNotificacion != null ? idNotificacion.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof Notificacion)) {
            return false;
        }
        Notificacion other = (Notificacion) object;
        if ((this.idNotificacion == null && other.idNotificacion != null) || (this.idNotificacion != null && !this.idNotificacion.equals(other.idNotificacion))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "com.gtworld.entity.Notificacion[ idNotificacion=" + idNotificacion + " ]";
    }
}
