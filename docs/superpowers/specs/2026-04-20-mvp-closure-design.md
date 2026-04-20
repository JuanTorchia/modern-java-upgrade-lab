# MVP Closure Design

Fecha: 2026-04-20

## Contexto

Modern Java Upgrade Lab ya analiza Maven y Gradle, genera reportes Markdown, detecta algunos patrones de codigo y tiene ejemplos reproducibles. Para empezar a cerrar el MVP, no necesito hacerlo mas complejo. Necesito que sea mas usable, publicable y confiable para una primera demo.

## Decision

Voy a enfocar esta iteracion en cierre de producto minimo:

- permitir guardar reportes con `--output`;
- mostrar errores amigables en el CLI cuando no se puede analizar un proyecto;
- agregar CI basico con `mvn test`;
- documentar un flujo de uso que una persona pueda copiar.

## Que No Voy A Hacer

No voy a agregar UI.

No voy a ampliar reglas de migracion.

No voy a resolver Gradle en profundidad.

No voy a crear instaladores o publicacion en Maven Central todavia.

## Arquitectura

`AnalyzeCommand` sigue siendo el punto de entrada. La logica de analisis no cambia: inspecciona build metadata, escanea source patterns, analiza y renderiza Markdown.

La mejora es de UX:

- si `--output` esta presente, escribe el Markdown a ese archivo y crea directorios padre si hacen falta;
- si ocurre un `IllegalArgumentException` esperado, devuelve exit code `1` y escribe un mensaje corto a stderr;
- si no hay `--output`, mantiene la salida actual por stdout.

## Testing

El cambio debe seguir TDD:

- test para `--output`, verificando que stdout no contenga el reporte completo y el archivo si;
- test para proyecto sin build file, verificando exit code `1`, mensaje claro y ausencia de stacktrace.

## Resultado Esperado

Una persona debe poder correr:

```powershell
java -jar cli\target\modern-java-upgrade-lab-cli.jar analyze --path examples\spring-boot-3-gradle-java-21 --target 25 --output reports\local-gradle-report.md
```

y obtener un archivo Markdown listo para compartir.
