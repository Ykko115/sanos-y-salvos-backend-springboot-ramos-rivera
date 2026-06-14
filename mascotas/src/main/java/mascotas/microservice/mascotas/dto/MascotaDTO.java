package mascotas.microservice.mascotas.dto;

import mascotas.microservice.mascotas.entity.Mascotas;
import java.util.List;

public class MascotaDTO {

    private String nombre;
    private Mascotas.Especie especie;
    private String raza;
    private Mascotas.Color color;
    private Mascotas.Tamano tamano;
    private Mascotas.Pelaje pelaje;
    private Integer edad;
    private Mascotas.RangoEdad rangoEdad;
    private List<Mascotas.Sena> senas;
    private String descripcion;
    private String contacto;
    private String fotoUrl;
    private Double lat;
    private Double lng;
    private Mascotas.Estado estado;
    private Long usuarioId;

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public Mascotas.Especie getEspecie() { return especie; }
    public void setEspecie(Mascotas.Especie especie) { this.especie = especie; }

    public String getRaza() { return raza; }
    public void setRaza(String raza) { this.raza = raza; }

    public Mascotas.Color getColor() { return color; }
    public void setColor(Mascotas.Color color) { this.color = color; }

    public Mascotas.Tamano getTamano() { return tamano; }
    public void setTamano(Mascotas.Tamano tamano) { this.tamano = tamano; }

    public Mascotas.Pelaje getPelaje() { return pelaje; }
    public void setPelaje(Mascotas.Pelaje pelaje) { this.pelaje = pelaje; }

    public Integer getEdad() { return edad; }
    public void setEdad(Integer edad) { this.edad = edad; }

    public Mascotas.RangoEdad getRangoEdad() { return rangoEdad; }
    public void setRangoEdad(Mascotas.RangoEdad rangoEdad) { this.rangoEdad = rangoEdad; }

    public List<Mascotas.Sena> getSenas() { return senas; }
    public void setSenas(List<Mascotas.Sena> senas) { this.senas = senas; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public String getContacto() { return contacto; }
    public void setContacto(String contacto) { this.contacto = contacto; }

    public String getFotoUrl() { return fotoUrl; }
    public void setFotoUrl(String fotoUrl) { this.fotoUrl = fotoUrl; }

    public Double getLat() { return lat; }
    public void setLat(Double lat) { this.lat = lat; }

    public Double getLng() { return lng; }
    public void setLng(Double lng) { this.lng = lng; }

    public Mascotas.Estado getEstado() { return estado; }
    public void setEstado(Mascotas.Estado estado) { this.estado = estado; }

    public Long getUsuarioId() { return usuarioId; }
    public void setUsuarioId(Long usuarioId) { this.usuarioId = usuarioId; }

    /** Convierte este DTO a la entidad Mascotas. */
    public Mascotas toEntity() {
        Mascotas m = new Mascotas();
        m.setNombre(nombre);
        m.setEspecie(especie);
        m.setRaza(raza);
        m.setColor(color);
        m.setTamano(tamano);
        m.setPelaje(pelaje);
        m.setEdad(edad);
        m.setRangoEdad(rangoEdad);
        if (senas != null) m.setSenas(senas);
        m.setDescripcion(descripcion);
        m.setContacto(contacto);
        m.setFotoUrl(fotoUrl);
        m.setLat(lat);
        m.setLng(lng);
        m.setEstado(estado);
        m.setUsuarioId(usuarioId);
        return m;
    }
}
