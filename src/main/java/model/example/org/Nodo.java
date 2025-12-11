package model.example.org;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Nodo {
    private final String id;
    private String nombre;
    private final TipoNodo tipo;
    private String contenido;
    private final List<Nodo> hijos;
    private transient Nodo padre;  // Marcado como transient

    public Nodo(String nombre, TipoNodo tipo) {
        this.id = UUID.randomUUID().toString();
        this.nombre = nombre;
        this.tipo = tipo;
        this.contenido = "";
        this.hijos = new ArrayList<>();
    }

    // Getters y Setters
    public String getId() { return id; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public TipoNodo getTipo() { return tipo; }
    public String getContenido() { return contenido; }
    public void setContenido(String contenido) { this.contenido = contenido; }
    public List<Nodo> getHijos() { return hijos; }
    public Nodo getPadre() { return padre; }
    public void setPadre(Nodo padre) { this.padre = padre; }

    public String getRutaCompleta() {
        if (padre == null) {
            return nombre;
        }
        return padre.getRutaCompleta() + "/" + nombre;
    }

    public void agregarHijo(Nodo hijo) {
        hijo.setPadre(this);
        this.hijos.add(hijo);
    }

    public boolean eliminarHijo(String idHijo) {
        return hijos.removeIf(nodo -> nodo.getId().equals(idHijo));
    }
}