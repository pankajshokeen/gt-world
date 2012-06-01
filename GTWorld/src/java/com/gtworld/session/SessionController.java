package com.gtworld.session;

import com.gtworld.controller.util.JsfUtil;
import com.gtworld.entity.Notificacion;
import com.gtworld.entity.Usuario;
import com.gtworld.entity.VisitaPoi;
import com.gtworld.facade.NotificacionFacade;
import com.gtworld.facade.UsuarioFacade;
import com.gtworld.facade.VisitaPoiFacade;
import java.io.IOException;
import java.io.Serializable;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.ejb.EJB;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.event.ActionEvent;
import org.primefaces.event.CloseEvent;
import org.primefaces.event.map.OverlaySelectEvent;
import org.primefaces.model.map.DefaultMapModel;
import org.primefaces.model.map.LatLng;
import org.primefaces.model.map.MapModel;
import org.primefaces.model.map.Marker;

@ManagedBean(name = "sessionController")
@SessionScoped
public class SessionController implements Serializable {

    private Usuario user;
    private String idUser;
    private String passUser;
    private String currentUI;
    private MapModel lastVisitPoi;
    private Marker selectedMarker;
    private List<Notificacion> unreadNotificaciones;
    private Notificacion selectedNotificacion;
    private String centerMap;
    @EJB
    private com.gtworld.facade.UsuarioFacade usuarioFacade;
    @EJB
    private com.gtworld.facade.NotificacionFacade notificacionFacade;
    @EJB
    private com.gtworld.facade.VisitaPoiFacade visitaFacade;

    public SessionController() {
    }

    /**
     * Inicia Sesión de Usuario
     */
    public void login() {
        Object[] parameters = {"idUsuario", getIdUser(), "contrasenaUsuario",
            getPassUser()};
        try {
            setUser(getUsuarioFacade().getSingleResult("Usuario.findByLogin", parameters));
            setCurrentUI("UI/home/main.xhtml");
            //loadVisits();
            JsfUtil.redirect("faces/home.xhtml");
        } catch (Exception ex) {
            JsfUtil.addErrorMessage(ex, "Usuario No Válido");
        }
    }

    /**
     * Cierra Sesión de Usuario
     */
    public void logout() {
        setUser(null);
        try {
            JsfUtil.redirect("index.xhtml");
        } catch (IOException ex) {
            JsfUtil.addErrorMessage(ex, null);
        }
    }

    /**
     * Verifica si la Sesión se encuentra activa
     */
    public void checkLogin() {
        if (getUser() != null) {
            try {
                JsfUtil.redirect("faces/home.xhtml");
            } catch (IOException ex) {
                JsfUtil.addErrorMessage(ex, null);
            }
        }
    }

    /**
     * Verifica si la sesión ha expirado
     */
    public void checkLogout() {
        if (getUser() == null) {
            try {
                JsfUtil.redirect("faces/index.xhtml");
            } catch (IOException ex) {
                JsfUtil.addErrorMessage(ex, null);
            }
        }
    }

    /**
     * Redirecciona a la Fan Page de Facebook
     */
    public void goFb() {
        try {
            JsfUtil.redirect("https://www.facebook.com/");
        } catch (IOException ex) {
            Logger.getLogger(SessionController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Redirecciona a la página de Twitter
     */
    public void goTwitter() {
        try {
            JsfUtil.redirect("https://www.twitter.com/");
        } catch (IOException ex) {
            Logger.getLogger(SessionController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Redireccion a la Ayuda del Sistema
     */
    public void goHelp() {
        try {
            JsfUtil.redirect("");
        } catch (IOException ex) {
            Logger.getLogger(SessionController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Redirecciona a la página de Registro
     */
    public void goRegister() {
        try {
            JsfUtil.redirect("faces/register.xhtml");
        } catch (IOException ex) {
            Logger.getLogger(SessionController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Redirecciona a la página de inicio de sesión
     */
    public void goLogin() {

        try {
            JsfUtil.redirect("faces/index.xhtml");
        } catch (IOException ex) {
            Logger.getLogger(SessionController.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    /**
     * Crea un nuevo usuario
     */
    public void createUser() {

        Pattern p = Pattern.compile(".+@.+\\.[a-z]+");
        Matcher m = p.matcher(getUser().getEmailUsuario());
        boolean matchFound = m.matches();
        if (!matchFound) {
            JsfUtil.addErrorMessage("Email Incorrecto");
        } else {
            Calendar calendar = new GregorianCalendar();
            getUser().setFechaIngresoUsuario(calendar.getTime());
            getUser().setTipoUsuario(false);
            Notificacion nueva = new Notificacion();
            nueva.setEstadoNotificacion(true);
            nueva.setIdUsuario(user);
            nueva.setTituloNotificacion("Bienvenido a GTWorld!");
            nueva.setFechaNotificacion(calendar.getTime());
            nueva.setContenidoNotificacion("Tu cuenta de GTWorld ha sido creada"
                    + " con éxito, empieza a descubrir y explorar tus Puntos"
                    + " de Interés!");
            try {
                getUsuarioFacade().create(user);
                getNotificacionFacade().create(nueva);
                getUser().getNotificacionList().add(nueva);
                Thread.sleep(2000);
                setCurrentUI("UI/home/main.xhtml");
                JsfUtil.redirect("faces/home.xhtml");
            } catch (Exception e) {
                JsfUtil.addErrorMessage(e, ResourceBundle.getBundle("/Bundle").getString("PersistenceErrorOccured"));

            }
        }

    }

    public void updateUI(ActionEvent event) {
        String ui = (String) event.getComponent().getAttributes().get("uiName");
        setCurrentUI("UI/" + ui + ".xhtml");
    }

    /**
     * Prepara la creación de un nuevo usuario
     */
    public void prepareCreate() {
        setUser(new Usuario());
    }

    /**
     * Carga las últimas 5 visitas a un POI del usuario
     */
    public void loadVisits() {
        setLastVisitPoi(new DefaultMapModel());
        Object[] parameters = {"idUsuario", getUser().getIdUsuario()};

        try {

            List<VisitaPoi> visits = getVisitaFacade().findRange(new int[]{0, 5},
                    "VisitaPoi.findByLastFechaVisitaPoi", parameters);
            if (!visits.isEmpty()) {
                boolean isFirst = true;

                for (VisitaPoi visita : visits) {
                    Float lat = visita.getPoi().getIdUbicacion().getLatitudUbicacion();
                    Float lon = visita.getPoi().getIdUbicacion().getLongitudUbicacion();
                    LatLng coord = new LatLng(lat, lon);
                    lastVisitPoi.addOverlay(new Marker(coord,
                            visita.getPoi().getNombrePoi(), visita,
                            visita.getPoi().getIdTipoPoi().getUrlIconoPoi()));
                    if (isFirst) {
                        isFirst = false;
                        centerMap = lat.toString() + "," + lon.toString();
                    }

                }
            } else {
                centerMap = "13.734,-89.29389";
            }

        } catch (Exception e) {
        }

    }

    /**
     * Carga las notificaciones no leidas por el Usuario
     */
    public void loadNotificactions() {
        Object[] parameters = {"idUsuario", getUser()};
        try {
            setUnreadNotificaciones(getNotificacionFacade().find(
                    "Notificacion.findByUsuarioEstadoNotificacion", parameters));
        } catch (Exception e) {
            JsfUtil.addErrorMessage(e, ResourceBundle.getBundle("/Bundle").getString("NotificationsError"));
        }
    }

    /**
     * Cierra una notificación
     */
    public void closeNotification(CloseEvent event) {
        try {
            Notificacion close = (Notificacion) event.getComponent().getAttributes().get("noti");
            close.setEstadoNotificacion(false);
            getNotificacionFacade().edit(close);
            getUnreadNotificaciones().remove(close);
        } catch (Exception e) {
            JsfUtil.addErrorMessage(e, ResourceBundle.getBundle("/Bundle").getString("NotificationsError"));
        }
    }

    public void onMarkerSelect(OverlaySelectEvent event) {
        selectedMarker = (Marker) event.getOverlay();
    }

    public String getCurrentUI() {
        return currentUI;
    }

    public void setCurrentUI(String currentUI) {
        this.currentUI = currentUI;
    }

    private UsuarioFacade getUsuarioFacade() {
        return usuarioFacade;
    }

    public NotificacionFacade getNotificacionFacade() {
        return notificacionFacade;
    }

    public VisitaPoiFacade getVisitaFacade() {
        return visitaFacade;
    }

    public void setVisitaFacade(VisitaPoiFacade visitaFacade) {
        this.visitaFacade = visitaFacade;
    }

    public Usuario getUser() {
        return user;
    }

    public void setUser(Usuario user) {
        this.user = user;
    }

    public String getIdUser() {
        return idUser;
    }

    public void setIdUser(String idUser) {
        this.idUser = idUser;
    }

    public String getPassUser() {
        return passUser;
    }

    public void setPassUser(String passUser) {
        this.passUser = passUser;
    }

    public MapModel getLastVisitPoi() {
        return lastVisitPoi;
    }

    public void setLastVisitPoi(MapModel lastVisitPoi) {
        this.lastVisitPoi = lastVisitPoi;
    }

    public Marker getSelectedMarker() {
        return selectedMarker;
    }

    public void setSelectedMarker(Marker selectedMarker) {
        this.selectedMarker = selectedMarker;
    }

    public Notificacion getSelectedNotificacion() {
        return selectedNotificacion;
    }

    public void setSelectedNotificacion(Notificacion selectedNotificacion) {
        this.selectedNotificacion = selectedNotificacion;
    }

    public List<Notificacion> getUnreadNotificaciones() {
        return unreadNotificaciones;
    }

    public void setUnreadNotificaciones(List<Notificacion> unreadNotificaciones) {
        this.unreadNotificaciones = unreadNotificaciones;
    }

    public String getCenterMap() {
        return centerMap;
    }

    public void setCenterMap(String centerMap) {
        this.centerMap = centerMap;
    }
}
