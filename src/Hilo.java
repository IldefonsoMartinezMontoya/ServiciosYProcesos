public class Hilo {
    public static void main(String[] args) {
        Thread hilo = new Thread(() -> {
            try {
                new ProcessBuilder(
                        "cmd.exe", "/c", "start", "", "cmd.exe", "/k"
                ).start();
            } catch (Exception e) {
                System.err.println("No se pudo abrir CMD: " + e.getMessage());
            }
        });

        hilo.start();
    }
}