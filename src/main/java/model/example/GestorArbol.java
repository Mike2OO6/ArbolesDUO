import model.example.org.Nodo;
import model.example.org.TipoNodo;
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
        if (archivo.exists()) {
            try {
                this.raiz = GestorJSON.cargarArbol(rutaArchivo);
                establecerPadres(this.raiz, null);
                this.nodoActual = raiz;
                indexarNombres(raiz);
            } catch (IOException e) {
                System.err.println("Error al cargar el árbol: " + e.getMessage());
                inicializarArbol();
            }
        } else {
            inicializarArbol();
        }
    }

    private void establecerPadres(Nodo nodo, Nodo padre) {
        if (nodo == null) return;
        nodo.setPadre(padre);
        for (Nodo hijo : nodo.getHijos()) {
            establecerPadres(hijo, nodo);
        }
    }

    private void inicializarArbol() {
        this.raiz = new Nodo("raiz", TipoNodo.CARPETA);
        this.nodoActual = raiz;
        guardarArbol();
    }

    private void indexarNombres(Nodo nodo) {
        if (nodo == null) return;
        trieNombres.insertar(nodo.getNombre());
        for (Nodo hijo : nodo.getHijos()) {
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
        for (Nodo hijo : nodoActual.getHijos()) {
            if (hijo.getNombre().equals(nombre)) {
                System.out.println("Ya existe un " +
                        (tipo.equals(TipoNodo.CARPETA) ? "directorio" : "archivo") +
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
        return trieNombres.buscarPrefijo(prefijo);
    }

    public String getRutaActual() {
        return nodoActual.getRutaCompleta();
    }

    public List<String> listarHijos() {
        List<String> nombresHijos = new ArrayList<>();
        for (Nodo hijo : nodoActual.getHijos()) {
            nombresHijos.add(hijo.getNombre() + " (" +
                    (hijo.getTipo().equals(TipoNodo.CARPETA) ? "carpeta" : "archivo") + ")");
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
        resultado.add("  ".repeat(nivel) + nodo.getNombre() +
                " (" + (nodo.getTipo().equals(TipoNodo.CARPETA) ? "carpeta" : "archivo") + ")");
        for (Nodo hijo : nodo.getHijos()) {
            preorden(hijo, resultado, nivel + 1);
        }
    }
}