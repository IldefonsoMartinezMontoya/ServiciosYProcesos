# Dos procesos Java que se comunican

Ejemplo mínimo en Java 17. El programa inicia dos JVM independientes con `ProcessBuilder` y conecta ambos procesos mediante un socket TCP de loopback. El proceso A actúa como servidor y el proceso B como cliente; intercambian mensajes en ambos sentidos.

## Requisitos

- JDK 17 o posterior
- Maven

## Ejecutar

Desde esta carpeta, compila el proyecto y arranca la demo:

```powershell
mvn compile
java -cp target/classes es.ejemplo.procesos.Main
```

El puerto local se elige automáticamente cada vez que se ejecuta. La salida de ambos procesos aparece identificada en la misma consola. El coordinador espera a que los dos procesos terminen y muestra un error si falla alguno.
