package model.example.org;

import model.example.org.util.GestorJSON;
import model.example.org.util.Trie;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class GestorArbol {
    private Nodo raiz;
    private Nodo nodoActual;
    private final Trie trieNombres;
    private final String rutaArchivo = "arbol.json";

    public GestorArbol() {
        this.trieNombres = new Trie();
        cargarArbol();
    }

    private void cargarArbol() {
        File archivo = new File(rutaArchivo);

        if (!archivo.exists()) {
            inicializarArbol();
            return;
        }

        try {
            this.raiz = GestorJSON.cargarArbol(rutaArchivo);

            if (this.raiz == null) {
                inicializarArbol();
                return;
            }

            establecerPadres(this.raiz, null);
            this.nodoActual = this.raiz;

            indexarNombres(this.raiz);
        } catch (IOException e) {
            System.err.println("Error al cargar el árbol: " + e.getMessage());
            inicializarArbol();
        }
    }

    private void inicializarArbol() {
        this.raiz = new Nodo("raiz", TipoNodo.CARPETA);
        this.nodoActual = this.raiz;
        guardarArbol();
    }

    private void establecerPadres(Nodo nodo, Nodo padre) {
        if (nodo == null) return;

        nodo.setPadre(padre);

        List<Nodo> hijos = nodo.getHijos();
        if (hijos == null) return;

        for (Nodo hijo : hijos) {
            establecerPadres(hijo, nodo);
        }
    }

    private void indexarNombres(Nodo nodo) {
        if (nodo == null) return;

        String nombre = nodo.getNombre();
        if (nombre != null) {
            trieNombres.insertar(nombre);
        }

        List<Nodo> hijos = nodo.getHijos();
        if (hijos == null) return;

        for (Nodo hijo : hijos) {
            indexarNombres(hijo);
        }
    }

    public void guardarArbol() {
        try {
            GestorJSON.guardarArbol(raiz, rutaArchivo);
        } catch (IOException e) {
            System.err.println("Error al guardar el árbol: " + e.getMessage());
        }
    }

    public void crearNodo(String nombre, TipoNodo tipo) {
        if (nombre == null || nombre.trim().isEmpty()) {
            System.out.println("El nombre no puede estar vacío.");
            return;
        }

        nombre = nombre.trim();

        for (Nodo hijo : nodoActual.getHijos()) {
            if (hijo != null && nombre.equals(hijo.getNombre())) {
                System.out.println("Ya existe un " +
                        (tipo == TipoNodo.CARPETA ? "directorio" : "archivo") +
                        " con el nombre '" + nombre + "' en la ruta actual.");
                return;
            }
        }

        Nodo nuevo = new Nodo(nombre, tipo);
        nodoActual.agregarHijo(nuevo);

        trieNombres.insertar(nombre);
        guardarArbol();
    }

    public List<String> buscarPorPrefijo(String prefijo) {
        if (prefijo == null) prefijo = "";
        return trieNombres.buscarPrefijo(prefijo.trim());
    }

    public String getRutaActual() {
        if (nodoActual == null) return "";
        return nodoActual.getRutaCompleta();
    }

    public List<String> listarHijos() {
        List<String> nombresHijos = new ArrayList<>();

        if (nodoActual == null || nodoActual.getHijos() == null) {
            return nombresHijos;
        }

        for (Nodo hijo : nodoActual.getHijos()) {
            if (hijo == null) continue;

            String tipoTexto = (hijo.getTipo() == TipoNodo.CARPETA) ? "carpeta" : "archivo";
            nombresHijos.add(hijo.getNombre() + " (" + tipoTexto + ")");
        }

        return nombresHijos;
    }

    public void exportarPreorden() {
        List<String> recorrido = new ArrayList<>();
        preorden(raiz, recorrido, 0);

        String nombreArchivo = "recorrido_preorden_" + System.currentTimeMillis() + ".txt";

        try (java.io.FileWriter writer = new java.io.FileWriter(nombreArchivo)) {
            for (String linea : recorrido) {
                writer.write(linea + "\n");
            }
            System.out.println("Recorrido exportado a: " + nombreArchivo);
        } catch (IOException e) {
            System.err.println("Error al exportar el recorrido: " + e.getMessage());
        }
    }

    private void preorden(Nodo nodo, List<String> resultado, int nivel) {
        if (nodo == null) return;

        String tipoTexto = (nodo.getTipo() == TipoNodo.CARPETA) ? "carpeta" : "archivo";
        resultado.add("  ".repeat(Math.max(0, nivel)) + nodo.getNombre() + " (" + tipoTexto + ")");

        List<Nodo> hijos = nodo.getHijos();
        if (hijos == null) return;

        for (Nodo hijo : hijos) {
            preorden(hijo, resultado, nivel + 1);
        }
    }
}