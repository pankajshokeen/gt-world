package com.gtworld.controller;

import com.gtworld.controller.util.JsfUtil;
import com.gtworld.controller.util.PaginationHelper;
import com.gtworld.entity.Imagen;
import com.gtworld.entity.Poi;
import com.gtworld.entity.Usuario;
import com.gtworld.entity.VisitaPoi;
import com.gtworld.facade.ImagenFacade;
import com.gtworld.facade.PoiFacade;
import com.gtworld.facade.VisitaPoiFacade;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import javax.ejb.EJB;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;
import javax.faces.event.ActionEvent;
import javax.faces.model.DataModel;
import javax.faces.model.ListDataModel;
import javax.faces.model.SelectItem;
import javax.servlet.ServletContext;
import org.primefaces.event.FileUploadEvent;
import org.primefaces.event.ToggleEvent;
import org.primefaces.model.UploadedFile;
import org.primefaces.model.map.DefaultMapModel;
import org.primefaces.model.map.LatLng;
import org.primefaces.model.map.MapModel;
import org.primefaces.model.map.Marker;

@ManagedBean(name = "poiController")
@SessionScoped
public class PoiController implements Serializable {

    private Poi current;
    private DataModel items = null;
    @EJB
    private com.gtworld.facade.PoiFacade ejbFacade;
    @EJB
    private com.gtworld.facade.ImagenFacade imagenFacade;
    @EJB
    private com.gtworld.facade.VisitaPoiFacade visitaFacade;
    private List<Imagen> uploaded = new ArrayList<Imagen>();
    private List<Imagen> viewSelected;
    private PaginationHelper pagination;
    private int selectedItemIndex;
    private MapModel poiModel;
    private boolean isEditing;
    private String descripcionFotos;

    public PoiController() {
    }

    public Poi getSelected() {
        if (current == null) {
            current = new Poi();
            selectedItemIndex = -1;
        }
        return current;
    }

    private PoiFacade getFacade() {
        return ejbFacade;
    }

    public PaginationHelper getPagination() {
        if (pagination == null) {
            pagination = new PaginationHelper(9) {

                @Override
                public int getItemsCount() {
                    return getFacade().count();
                }

                @Override
                public DataModel createPageDataModel() {
                    return new ListDataModel(getFacade().findRange(new int[]{getPageFirstItem(), getPageFirstItem() + getPageSize()}, "Poi.findAll"));
                }
            };
        }
        return pagination;
    }

    public void prepareList() {
        recreateModel();
    }

    public void prepareView() {
        current = (Poi) getItems().getRowData();
        selectedItemIndex = pagination.getPageFirstItem() + getItems().getRowIndex();
    }

    public void prepareViewOnMap() {
        current = (Poi) getItems().getRowData();
        selectedItemIndex = pagination.getPageFirstItem() + getItems().getRowIndex();
        setPoiModel(new DefaultMapModel());
        LatLng coord = new LatLng(current.getIdUbicacion().getLatitudUbicacion(),
                current.getIdUbicacion().getLongitudUbicacion());
        poiModel.addOverlay(new Marker(coord,
                current.getNombrePoi(), current,
                current.getIdTipoPoi().getUrlIconoPoi()));
    }

    public void prepareViewImages() {
        current = (Poi) getItems().getRowData();
        selectedItemIndex = pagination.getPageFirstItem() + getItems().getRowIndex();
        Object[] parameters = {"idPoi", current};
        try {
            viewSelected = getImagenFacade().find("Imagen.findByIdPoi", parameters);
            if (viewSelected.isEmpty()) {
                Imagen img = new Imagen();
                img.setTituloImagen("Imagen no Encontrada");
                img.setDescripcionImagen("Imagen no Encontrada");
                img.setUrlImagen("Images/POIs/image_not_found.jpg");
                viewSelected.add(img);
            }
        } catch (Exception e) {
            JsfUtil.addErrorMessage(e, ResourceBundle.getBundle("/Bundle").getString("PersistenceErrorOccured"));
        }

    }

    public String prepareCreate() {
        current = new Poi();
        selectedItemIndex = -1;
        return "Create";
    }

    public void create() {
        try {
            getFacade().create(current);
            JsfUtil.addSuccessMessage(ResourceBundle.getBundle("/Bundle").getString("PoiCreated"));
        } catch (Exception e) {
            JsfUtil.addErrorMessage(e, ResourceBundle.getBundle("/Bundle").getString("PersistenceErrorOccured"));
        }
    }

    public void prepareEdit() {
        current = (Poi) getItems().getRowData();
        selectedItemIndex = pagination.getPageFirstItem() + getItems().getRowIndex();
        setIsEditing(true);
    }

    public void closeEdit(ToggleEvent event) {
        String status = event.getVisibility().name();
        if (status.equals("VISIBLE") && !isEditing) {
            setIsEditing(true); //forzar cierre de panel
        }

        if (status.equals("HIDDEN") && isEditing) {
            setIsEditing(false);
            current = null;
        }
    }

    public void poiImageUpload(FileUploadEvent event) {

        UploadedFile imageUpload = event.getFile();
        ServletContext servletContext = (ServletContext) FacesContext.getCurrentInstance().getExternalContext().getContext();
        String path = servletContext.getRealPath("/Images/POIs/").concat("\\");
        if (JsfUtil.saveImage(imageUpload.getContents(),
                path + imageUpload.getFileName())) {
            Imagen imagen = new Imagen();
            imagen.setIdPoi(current);
            imagen.setTituloImagen(current.getNombrePoi());
            imagen.setDescripcionImagen("");
            imagen.setUrlImagen("Images/POIs/" + imageUpload.getFileName());
            try {
                getImagenFacade().create(imagen);
                getUploaded().add(imagen);
                JsfUtil.addSuccessMessage(imageUpload.getFileName() + " Guardado");
            } catch (Exception e) {
                JsfUtil.addErrorMessage(e, ResourceBundle.getBundle("/Bundle").getString("PersistenceErrorOccured"));
            }
        } else {
            JsfUtil.addErrorMessage(imageUpload.getFileName() + " no guardado");
        }
    }

    public void update() {
        try {
            getFacade().edit(current);
            for (Imagen img : uploaded) {
                img.setDescripcionImagen(descripcionFotos);
                getImagenFacade().edit(img);
                current.getImagenList().add(img);
            }
            descripcionFotos = "";
            setUploaded(new ArrayList<Imagen>());
            JsfUtil.addSuccessMessage(ResourceBundle.getBundle("/Bundle").getString("PoiUpdated"));
        } catch (Exception e) {
            JsfUtil.addErrorMessage(e, ResourceBundle.getBundle("/Bundle").getString("PersistenceErrorOccured"));
        }
    }

    public void destroy() {
        current = (Poi) getItems().getRowData();
        selectedItemIndex = pagination.getPageFirstItem() + getItems().getRowIndex();
        try {
            for (Imagen img : current.getImagenList()) {
                getImagenFacade().remove(img);
            }
            for (VisitaPoi vp : current.getVisitaPoiList()) {
                getVisitaFacade().remove(vp);
            }
            performDestroy();
            recreatePagination();
            recreateModel();
        } catch (Exception e) {
            JsfUtil.addErrorMessage(e, ResourceBundle.getBundle("/Bundle").getString("PersistenceErrorOccured"));
        }
    }

    public void destroyAndView() {
        try {
            for (Imagen img : current.getImagenList()) {
                getImagenFacade().remove(img);
            }
            for (VisitaPoi vp : current.getVisitaPoiList()) {
                getVisitaFacade().remove(vp);
            }
            performDestroy();
            recreatePagination();
            recreateModel();
        } catch (Exception e) {
            JsfUtil.addErrorMessage(e, ResourceBundle.getBundle("/Bundle").getString("PersistenceErrorOccured"));
        }
    }

    private void performDestroy() {
        try {
            getFacade().remove(current);
            JsfUtil.addSuccessMessage(ResourceBundle.getBundle("/Bundle").getString("PoiDeleted"));
        } catch (Exception e) {
            JsfUtil.addErrorMessage(e, ResourceBundle.getBundle("/Bundle").getString("PersistenceErrorOccured"));
        }
    }

    private void updateCurrentItem() {
        int count = getFacade().count();
        if (selectedItemIndex >= count) {
            // selected index cannot be bigger than number of items:
            selectedItemIndex = count - 1;
            // go to previous page if last page disappeared:
            if (pagination.getPageFirstItem() >= count) {
                pagination.previousPage();
            }
        }
        if (selectedItemIndex >= 0) {
            current = getFacade().findRange(new int[]{selectedItemIndex, selectedItemIndex + 1}).get(0);
        }
    }

    public DataModel getItems() {
        if (items == null) {
            items = getPagination().createPageDataModel();
        }
        return items;
    }

    private void recreateModel() {
        items = null;
    }

    private void recreatePagination() {
        pagination = null;
    }

    public String next() {
        getPagination().nextPage();
        recreateModel();
        return "List";
    }

    public String previous() {
        getPagination().previousPage();
        recreateModel();
        return "List";
    }

    public SelectItem[] getItemsAvailableSelectMany() {
        return JsfUtil.getSelectItems(ejbFacade.findAll(), false);
    }

    public SelectItem[] getItemsAvailableSelectOne() {
        return JsfUtil.getSelectItems(ejbFacade.findAll(), true);
    }

    public boolean isEditing() {
        return isEditing;
    }

    public void setIsEditing(boolean isEditing) {
        this.isEditing = isEditing;
    }

    public MapModel getPoiModel() {
        return poiModel;
    }

    public void setPoiModel(MapModel poiModel) {
        this.poiModel = poiModel;
    }

    public ImagenFacade getImagenFacade() {
        return imagenFacade;
    }

    public void setImagenFacade(ImagenFacade imagenFacade) {
        this.imagenFacade = imagenFacade;
    }

    public List<Imagen> getUploaded() {
        return uploaded;
    }

    public void setUploaded(List<Imagen> uploaded) {
        this.uploaded = uploaded;
    }

    public String getDescripcionFotos() {
        return descripcionFotos;
    }

    public void setDescripcionFotos(String descripcionFotos) {
        this.descripcionFotos = descripcionFotos;
    }

    public VisitaPoiFacade getVisitaFacade() {
        return visitaFacade;
    }

    public void setVisitaFacade(VisitaPoiFacade visitaFacade) {
        this.visitaFacade = visitaFacade;
    }

    public List<Imagen> getViewSelected() {
        return viewSelected;
    }

    public void setViewSelected(List<Imagen> viewSelected) {
        this.viewSelected = viewSelected;
    }

    @FacesConverter(forClass = Poi.class)
    public static class PoiControllerConverter implements Converter {

        public Object getAsObject(FacesContext facesContext, UIComponent component, String value) {
            if (value == null || value.length() == 0) {
                return null;
            }
            PoiController controller = (PoiController) facesContext.getApplication().getELResolver().
                    getValue(facesContext.getELContext(), null, "poiController");
            return controller.ejbFacade.find(getKey(value));
        }

        java.lang.Long getKey(String value) {
            java.lang.Long key;
            key = Long.valueOf(value);
            return key;
        }

        String getStringKey(java.lang.Long value) {
            StringBuffer sb = new StringBuffer();
            sb.append(value);
            return sb.toString();
        }

        public String getAsString(FacesContext facesContext, UIComponent component, Object object) {
            if (object == null) {
                return null;
            }
            if (object instanceof Poi) {
                Poi o = (Poi) object;
                return getStringKey(o.getIdPoi());
            } else {
                throw new IllegalArgumentException("object " + object + " is of type " + object.getClass().getName() + "; expected type: " + PoiController.class.getName());
            }
        }
    }
}
