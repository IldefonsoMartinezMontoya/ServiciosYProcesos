package App;

public class App {
    static void main() {
        System.out.println("Iniciando programa");
        System.out.println("--- Inicio del Proceso Princial ---");

        Runnable procesosNumeros = new ProcesosNumeros();
        Runnable procesosLetras = new ProcesosLetras();

        Thread Hilo1 = new Thread(procesosNumeros);
        Thread Hilo2 = new Thread(procesosLetras);

        Hilo1.start();
        Hilo2.start();
        try {
            Hilo1.join();
            Hilo2.join();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }
}