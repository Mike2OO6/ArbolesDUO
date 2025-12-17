package model.example;

import model.example.org.GestorArbol;
import model.example.org.Nodo;
import model.example.org.TipoNodo;

import java.util.List;
import java.util.Scanner;

public class Main {
    private static final Scanner scanner = new Scanner(System.in);
    private static final GestorArbol gestor = new GestorArbol();

    public static void main(String[] args) {
        mostrarMenu();
    }

    private static void mostrarMenu() {
        while (true) {
            limpiarPantalla();

            System.out.println("Sistema de Gestión de Archivos - Árboles DUO");
            System.out.println("\n--- MENÚ PRINCIPAL ---");
            System.out.println("Ruta actual: " + gestor.getRutaActual());
            System.out.println("1. Crear carpeta");
            System.out.println("2. Crear archivo");
            System.out.println("3. Listar contenido");
            System.out.println("4. Buscar por prefijo");
            System.out.println("5. Exportar recorrido preorden");
            System.out.println("6. Mover archivo a carpeta (por números)");
            System.out.println("7. Borrar archivo/carpeta a Recycle Bin (por número)");
            System.out.println("8. Dibujar árbol");
            System.out.println("9. Salir");
            System.out.print("Seleccione una opción: ");

            int opcion = leerEntero(1, 9);

            try {
                switch (opcion) {
                    case 1 -> crearNodo(TipoNodo.CARPETA);
                    case 2 -> crearNodo(TipoNodo.ARCHIVO);
                    case 3 -> listarContenido();
                    case 4 -> buscarPorPrefijo();
                    case 5 -> gestor.exportarPreorden();
                    case 6 -> moverArchivoACarpeta();
                    case 7 -> borrarARecycleBin();
                    case 8 -> dibujarArbol();
                    case 9 -> {
                        System.out.println("¡Hasta luego!");
                        return;
                    }
                }

                System.out.println("\nPresiona ENTER para continuar...");
                scanner.nextLine();
            } catch (Exception e) {
                System.err.println("Error: " + e.getMessage());
                System.out.println("\nPresiona ENTER para continuar...");
                scanner.nextLine();
            }
        }
    }

    private static void limpiarPantalla() {
        System.out.print("\033[H\033[2J");
        System.out.flush();
        for (int i = 0; i < 25; i++) System.out.println();
    }

    private static void crearNodo(TipoNodo tipo) {
        System.out.print("Ingrese el nombre del " + (tipo == TipoNodo.CARPETA ? "directorio" : "archivo") + ": ");
        String nombre = scanner.nextLine();

        if (nombre == null || nombre.trim().isEmpty()) {
            System.out.println("El nombre no puede estar vacío.");
            return;
        }

        gestor.crearNodo(nombre.trim(), tipo);
        System.out.println((tipo == TipoNodo.CARPETA ? "Directorio" : "Archivo") + " creado exitosamente.");
    }

    private static void listarContenido() {
        List<String> contenido = gestor.listarHijos();
        if (contenido.isEmpty()) {
            System.out.println("El directorio actual está vacío.");
            return;
        }
        System.out.println("Contenido del directorio actual:");
        for (int i = 0; i < contenido.size(); i++) {
            System.out.println((i + 1) + ". " + contenido.get(i));
        }
    }

    private static void buscarPorPrefijo() {
        System.out.print("Ingrese el prefijo a buscar: ");
        String prefijo = scanner.nextLine();
        List<String> resultados = gestor.buscarPorPrefijo(prefijo);

        if (resultados.isEmpty()) {
            System.out.println("No se encontraron coincidencias.");
            return;
        }

        System.out.println("Coincidencias encontradas:");
        for (String r : resultados) {
            System.out.println("- " + r);
        }
    }

    private static void moverArchivoACarpeta() {
        List<Nodo> carpetas = gestor.listarCarpetasActuales();
        if (carpetas.isEmpty()) {
            System.out.println("No hay carpetas en el directorio actual.");
            return;
        }

        System.out.println("Carpetas disponibles:");
        for (int i = 0; i < carpetas.size(); i++) {
            System.out.println((i + 1) + ". " + carpetas.get(i).getNombre());
        }
        System.out.print("Seleccione el número de la carpeta destino: ");
        int idxCarpeta = leerEntero(1, carpetas.size());

        List<Nodo> archivos = gestor.listarArchivosActuales();
        if (archivos.isEmpty()) {
            System.out.println("No hay archivos en el directorio actual.");
            return;
        }

        System.out.println("Archivos disponibles (ordenados por fecha de creación ascendente):");
        for (int i = 0; i < archivos.size(); i++) {
            System.out.println((i + 1) + ". " + archivos.get(i).getNombre());
        }
        System.out.print("Seleccione el número del archivo a mover: ");
        int idxArchivo = leerEntero(1, archivos.size());

        boolean ok = gestor.moverArchivoPorIndices(idxCarpeta, idxArchivo);
        System.out.println(ok ? "Archivo movido correctamente." : "No se pudo mover el archivo.");
    }

    private static void borrarARecycleBin() {
        List<String> contenido = gestor.listarHijos();
        if (contenido.isEmpty()) {
            System.out.println("No hay nada que borrar en el directorio actual.");
            return;
        }

        System.out.println("Elementos disponibles para borrar (se enviarán a Recycle Bin):");
        for (int i = 0; i < contenido.size(); i++) {
            System.out.println((i + 1) + ". " + contenido.get(i));
        }
        System.out.print("Seleccione el número del elemento a borrar: ");
        int idx = leerEntero(1, contenido.size());

        boolean ok = gestor.eliminarPorIndice(idx);
        System.out.println(ok ? "Enviado a Recycle Bin correctamente." : "No se pudo borrar/enviar a Recycle Bin.");
    }

    private static void dibujarArbol() {
        List<String> lineas = gestor.dibujarArbol();
        for (String l : lineas) System.out.println(l);
    }

    @SuppressWarnings("SameParameterValue")
    private static int leerEntero(int min, int max) {
        while (true) {
            try {
                int valor = Integer.parseInt(scanner.nextLine().trim());
                if (valor >= min && valor <= max) return valor;
                System.out.print("Por favor ingrese un número entre " + min + " y " + max + ": ");
            } catch (NumberFormatException e) {
                System.out.print("Entrada inválida. Ingrese un número: ");
            }
        }
    }
}