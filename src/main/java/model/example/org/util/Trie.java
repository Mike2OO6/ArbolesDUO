package model.example.org.util;

import java.util.ArrayList;
import java.util.List;

public class Trie {
    private static class NodoTrie {
        NodoTrie[] hijos = new NodoTrie[256];
        boolean esFin;
    }

    private final NodoTrie raiz;

    public Trie() {
        raiz = new NodoTrie();
    }

    public void insertar(String palabra) {
        NodoTrie actual = raiz;
        for (char c : palabra.toCharArray()) {
            int indice = c;
            if (actual.hijos[indice] == null) {
                actual.hijos[indice] = new NodoTrie();
            }
            actual = actual.hijos[indice];
        }
        actual.esFin = true;
    }

    public List<String> buscarPrefijo(String prefijo) {
        List<String> resultados = new ArrayList<>();
        NodoTrie nodo = buscarNodo(prefijo);
        if (nodo != null) {
            buscarPalabras(nodo, prefijo, resultados);
        }
        return resultados;
    }

    private NodoTrie buscarNodo(String prefijo) {
        NodoTrie actual = raiz;
        for (char c : prefijo.toCharArray()) {
            int indice = c;
            if (actual.hijos[indice] == null) {
                return null;
            }
            actual = actual.hijos[indice];
        }
        return actual;
    }

    private void buscarPalabras(NodoTrie nodo, String prefijo, List<String> resultados) {
        if (nodo.esFin) {
            resultados.add(prefijo);
        }
        for (char c = 0; c < 256; c++) {
            if (nodo.hijos[c] != null) {
                buscarPalabras(nodo.hijos[c], prefijo + c, resultados);
            }
        }
    }
}