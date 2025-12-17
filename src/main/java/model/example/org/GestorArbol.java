package model.example.org;

import model.example.org.util.GestorJSON;
import model.example.org.util.Trie;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class GestorArbol {
    private Nodo raiz;
    private Nodo nodoActual;
    private final Trie trieNombres;
    private final String rutaArchivo = "arbol.json";

    private final Path basePath = Paths.get(System.getProperty("user.dir"), "ArbolesDUO_FS");

    public GestorArbol() {
        this.trieNombres = new Trie();
        asegurarBasePath();
        cargarArbol();
    }

    private void asegurarBasePath() {
        try {
            Files.createDirectories(basePath);
        } catch (IOException e) {
            throw new RuntimeException("No se pudo crear basePath: " + basePath, e);
        }
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
            asegurarEstructuraEnDisco(this.raiz);
        } catch (Exception e) {
            System.err.println("JSON corrupto o inválido. Se reinicia el árbol. Detalle: " + e.getMessage());
            inicializarArbol();
        }
    }

    private void inicializarArbol() {
        this.raiz = new Nodo("raiz", TipoNodo.CARPETA);
        this.nodoActual = this.raiz;
        asegurarEstructuraEnDisco(this.raiz);
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

    private void asegurarEstructuraEnDisco(Nodo nodo) {
        if (nodo == null) return;

        Path path = pathDeNodo(nodo);
        try {
            if (nodo.getTipo() == TipoNodo.CARPETA) {
                Files.createDirectories(path);
            } else {
                if (Files.notExists(path)) {
                    Files.createFile(path);
                }
            }
        } catch (IOException ignored) {
        }

        List<Nodo> hijos = nodo.getHijos();
        if (hijos == null) return;

        for (Nodo hijo : hijos) {
            asegurarEstructuraEnDisco(hijo);
        }
    }

    private Path pathDeNodo(Nodo nodo) {
        List<String> partes = new ArrayList<>();
        Nodo actual = nodo;

        while (actual != null && actual.getPadre() != null) {
            partes.add(0, actual.getNombre());
            actual = actual.getPadre();
        }

        Path p = basePath;
        for (String s : partes) {
            p = p.resolve(s);
        }
        return p;
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

        List<Nodo> hijos = nodoActual.getHijos();
        if (hijos != null) {
            for (Nodo hijo : hijos) {
                if (hijo != null && nombre.equals(hijo.getNombre())) {
                    System.out.println("Ya existe un " +
                            (tipo == TipoNodo.CARPETA ? "directorio" : "archivo") +
                            " con el nombre '" + nombre + "' en la ruta actual.");
                    return;
                }
            }
        }

        Nodo nuevo = new Nodo(nombre, tipo);
        nodoActual.agregarHijo(nuevo);

        Path path = pathDeNodo(nuevo);
        try {
            if (tipo == TipoNodo.CARPETA) Files.createDirectories(path);
            else Files.createFile(path);
        } catch (IOException e) {
            System.err.println("No se pudo crear en disco: " + e.getMessage());
        }

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
        if (nodoActual == null || nodoActual.getHijos() == null) return nombresHijos;

        for (Nodo hijo : nodoActual.getHijos()) {
            if (hijo == null) continue;
            String tipoTxt = (hijo.getTipo() == TipoNodo.CARPETA) ? "carpeta" : "archivo";
            nombresHijos.add(hijo.getNombre() + " (" + tipoTxt + ")");
        }
        return nombresHijos;
    }

    public List<Nodo> listarCarpetasActuales() {
        List<Nodo> carpetas = new ArrayList<>();
        if (nodoActual == null || nodoActual.getHijos() == null) return carpetas;

        for (Nodo h : nodoActual.getHijos()) {
            if (h != null && h.getTipo() == TipoNodo.CARPETA) carpetas.add(h);
        }
        return carpetas;
    }

    public List<Nodo> listarArchivosActuales() {
        List<Nodo> archivos = new ArrayList<>();
        if (nodoActual == null || nodoActual.getHijos() == null) return archivos;

        for (Nodo h : nodoActual.getHijos()) {
            if (h != null && h.getTipo() == TipoNodo.ARCHIVO) archivos.add(h);
        }

        archivos.sort(Comparator.comparingLong(Nodo::getCreatedAt));
        return archivos;
    }

    public boolean moverArchivoPorIndices(int indiceCarpeta, int indiceArchivo) {
        List<Nodo> carpetas = listarCarpetasActuales();
        List<Nodo> archivos = listarArchivosActuales();

        if (indiceCarpeta < 1 || indiceCarpeta > carpetas.size()) return false;
        if (indiceArchivo < 1 || indiceArchivo > archivos.size()) return false;

        Nodo carpetaDestino = carpetas.get(indiceCarpeta - 1);
        Nodo archivo = archivos.get(indiceArchivo - 1);

        return moverNodoArchivo(archivo, carpetaDestino);
    }

    private boolean moverNodoArchivo(Nodo archivo, Nodo carpetaDestino) {
        if (archivo == null || carpetaDestino == null) return false;
        if (carpetaDestino.getTipo() != TipoNodo.CARPETA) return false;
        if (archivo.getTipo() != TipoNodo.ARCHIVO) return false;

        Nodo padreViejo = archivo.getPadre();
        if (padreViejo == null) return false;

        Path src = pathDeNodo(archivo);
        Path dst = pathDeNodo(carpetaDestino).resolve(archivo.getNombre());

        try {
            Files.createDirectories(pathDeNodo(carpetaDestino));
            Files.move(src, dst, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            System.err.println("No se pudo mover en disco: " + e.getMessage());
            return false;
        }

        padreViejo.eliminarHijo(archivo.getId());
        carpetaDestino.agregarHijo(archivo);

        guardarArbol();
        return true;
    }

    public boolean eliminarPorIndice(int indice) {
        if (nodoActual == null || nodoActual.getHijos() == null) return false;

        List<Nodo> items = new ArrayList<>();
        for (Nodo h : nodoActual.getHijos()) {
            if (h != null) items.add(h);
        }

        if (indice < 1 || indice > items.size()) return false;

        Nodo objetivo = items.get(indice - 1);
        return eliminarNodo(objetivo);
    }

    private boolean eliminarNodo(Nodo objetivo) {
        if (objetivo == null) return false;
        if (objetivo.getPadre() == null) return false;

        Path path = pathDeNodo(objetivo);

        boolean enviado = moverARecycleBin(path.toFile());
        if (!enviado) return false;

        objetivo.getPadre().eliminarHijo(objetivo.getId());
        guardarArbol();
        return true;
    }

    private boolean moverARecycleBin(File file) {
        try {
            if (!Desktop.isDesktopSupported()) {
                System.out.println("Tu sistema no soporta Recycle Bin desde Java Desktop.");
                return false;
            }

            Desktop desktop = Desktop.getDesktop();
            if (!desktop.isSupported(Desktop.Action.MOVE_TO_TRASH)) {
                System.out.println("MOVE_TO_TRASH no soportado en este entorno.");
                return false;
            }

            return desktop.moveToTrash(file);
        } catch (Exception e) {
            System.err.println("No se pudo enviar a la Recycle Bin: " + e.getMessage());
            return false;
        }
    }

    public List<String> dibujarArbol() {
        List<String> out = new ArrayList<>();
        dibujarArbolRec(raiz, out, 0);
        return out;
    }

    private void dibujarArbolRec(Nodo nodo, List<String> out, int nivel) {
        if (nodo == null) return;

        String indent = "  ".repeat(Math.max(0, nivel));

        if (nodo.getTipo() == TipoNodo.CARPETA) {
            long archivosDirectos = 0;
            if (nodo.getHijos() != null) {
                for (Nodo n : nodo.getHijos()) {
                    if (n != null && n.getTipo() == TipoNodo.ARCHIVO) archivosDirectos++;
                }
            }

            out.add(indent + nodo.getNombre() + "/ (archivos: " + archivosDirectos + ")");

            List<Nodo> archivos = new ArrayList<>();
            List<Nodo> carpetas = new ArrayList<>();

            if (nodo.getHijos() != null) {
                for (Nodo h : nodo.getHijos()) {
                    if (h == null) continue;
                    if (h.getTipo() == TipoNodo.ARCHIVO) archivos.add(h);
                    else carpetas.add(h);
                }
            }

            archivos.sort(Comparator.comparingLong(Nodo::getCreatedAt));
            for (Nodo a : archivos) {
                out.add(indent + "  " + a.getNombre());
            }

            carpetas.sort(Comparator.comparing(Nodo::getNombre, String.CASE_INSENSITIVE_ORDER));
            for (Nodo c : carpetas) {
                dibujarArbolRec(c, out, nivel + 1);
            }
        } else {
            out.add(indent + nodo.getNombre());
        }
    }

    public void exportarPreorden() {
        List<String> recorrido = new ArrayList<>();
        preordenExport(raiz, recorrido, 0);

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

    private void preordenExport(Nodo nodo, List<String> resultado, int nivel) {
        if (nodo == null) return;

        String tipoTexto = (nodo.getTipo() == TipoNodo.CARPETA) ? "carpeta" : "archivo";
        resultado.add("  ".repeat(Math.max(0, nivel)) + nodo.getNombre() + " (" + tipoTexto + ")");

        List<Nodo> hijos = nodo.getHijos();
        if (hijos == null) return;

        for (Nodo hijo : hijos) {
            preordenExport(hijo, resultado, nivel + 1);
        }
    }
}