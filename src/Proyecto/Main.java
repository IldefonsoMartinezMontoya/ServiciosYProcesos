package Proyecto;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Locale;

/** Inicia dos JVM independientes que intercambian mensajes por TCP local. */
public final class Main {
    private static final int TIMEOUT_MILLIS = (int) Duration.ofSeconds(15).toMillis();

    private Main() {
    }

    public static void main(String[] args) throws Exception {
        if (args.length == 0) {
            runDemo();
            return;
        }

        switch (args[0]) {
            case "server" -> runServer();
            case "client" -> {
                if (args.length != 2) {
                    throw new IllegalArgumentException("Uso interno: client <puerto>");
                }
                runClient(Integer.parseInt(args[1]));
            }
            default -> throw new IllegalArgumentException(
                    "Modo desconocido. Ejecuta el programa sin argumentos para iniciar la demo.");
        }
    }

    private static void runDemo() throws Exception {
        Path javaExecutable = javaExecutable();
        Path classpath = Path.of(Main.class.getProtectionDomain()
                .getCodeSource().getLocation().toURI());
        String mainClass = Main.class.getName();

        Process server = new ProcessBuilder(
                javaExecutable.toString(), "-cp", classpath.toString(), mainClass, "server")
                .redirectErrorStream(true)
                .start();

        try (BufferedReader serverOutput = new BufferedReader(new InputStreamReader(
                server.getInputStream(), StandardCharsets.UTF_8))) {
            String readyLine = serverOutput.readLine();
            if (readyLine == null || !readyLine.startsWith("READY ")) {
                String details = readyLine == null ? "sin salida" : readyLine;
                server.destroyForcibly();
                throw new IOException("El proceso A no pudo arrancar: " + details);
            }

            int port = Integer.parseInt(readyLine.substring("READY ".length()));
            Thread serverOutputThread = forwardOutput("Proceso A", serverOutput);
            Process client = new ProcessBuilder(
                    javaExecutable.toString(), "-cp", classpath.toString(), mainClass,
                    "client", Integer.toString(port))
                    .redirectErrorStream(true)
                    .start();
            Thread clientOutputThread = forwardOutput("Proceso B", new BufferedReader(
                    new InputStreamReader(client.getInputStream(), StandardCharsets.UTF_8)));

            int clientExitCode = client.waitFor();
            int serverExitCode = server.waitFor();
            clientOutputThread.join();
            serverOutputThread.join();

            if (clientExitCode != 0 || serverExitCode != 0) {
                throw new IOException("Falló un proceso: A=" + serverExitCode
                        + ", B=" + clientExitCode);
            }
            System.out.println("Intercambio terminado correctamente.");
        } finally {
            if (server.isAlive()) {
                server.destroyForcibly();
            }
        }
    }

    private static Thread forwardOutput(String processName, BufferedReader output) {
        Thread thread = new Thread(() -> {
            try (output) {
                String line;
                while ((line = output.readLine()) != null) {
                    System.out.println("[" + processName + "] " + line);
                }
            } catch (IOException exception) {
                System.err.println("[" + processName + "] Error leyendo la salida: "
                        + exception.getMessage());
            }
        }, "salida-" + processName.toLowerCase(Locale.ROOT).replace(' ', '-'));
        thread.setDaemon(true);
        thread.start();
        return thread;
    }

    private static Path javaExecutable() {
        String executableName = System.getProperty("os.name")
                .toLowerCase(Locale.ROOT).contains("win") ? "java.exe" : "java";
        return Path.of(System.getProperty("java.home"), "bin", executableName);
    }

    private static void runServer() throws IOException {
        InetAddress loopback = InetAddress.getLoopbackAddress();
        try (ServerSocket serverSocket = new ServerSocket(0, 1, loopback)) {
            serverSocket.setSoTimeout(TIMEOUT_MILLIS);
            System.out.println("READY " + serverSocket.getLocalPort());

            try (Socket socket = serverSocket.accept()) {
                socket.setSoTimeout(TIMEOUT_MILLIS);
                BufferedReader input = new BufferedReader(new InputStreamReader(
                        socket.getInputStream(), StandardCharsets.UTF_8));
                PrintWriter output = new PrintWriter(
                        new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8), true);

                String message = input.readLine();
                if (message == null) {
                    throw new IOException("El proceso B cerró la conexión antes de enviar un mensaje.");
                }
                System.out.println("Recibido: " + message);

                output.println("Hola, proceso B. Soy el proceso A.");
                String confirmation = input.readLine();
                if (confirmation == null) {
                    throw new IOException("El proceso B cerró la conexión sin confirmar la respuesta.");
                }
                System.out.println("Confirmación recibida: " + confirmation);
            } catch (SocketTimeoutException exception) {
                throw new IOException("Se agotó el tiempo de espera de la conexión.", exception);
            }
        }
    }

    private static void runClient(int port) throws IOException {
        InetAddress loopback = InetAddress.getLoopbackAddress();
        try (Socket socket = new Socket()) {
            socket.connect(new java.net.InetSocketAddress(loopback, port), TIMEOUT_MILLIS);
            socket.setSoTimeout(TIMEOUT_MILLIS);

            BufferedReader input = new BufferedReader(new InputStreamReader(
                    socket.getInputStream(), StandardCharsets.UTF_8));
            PrintWriter output = new PrintWriter(
                    new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8), true);

            output.println("Hola, proceso A. Soy el proceso B.");
            String reply = input.readLine();
            if (reply == null) {
                throw new IOException("El proceso A cerró la conexión antes de responder.");
            }
            System.out.println("Respuesta recibida: " + reply);
            output.println("Confirmación: recibí tu mensaje.");
        } catch (SocketTimeoutException exception) {
            throw new IOException("Se agotó el tiempo de espera de la conexión.", exception);
        }
    }
}
