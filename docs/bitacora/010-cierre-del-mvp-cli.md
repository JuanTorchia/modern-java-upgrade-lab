# 010 - Cierre Del MVP CLI

Fecha: 2026-04-20

## Objetivo Del Paso

En este paso quiero empezar a cerrar la primera version usable de Modern Java Upgrade Lab.

Ya no se trata de sumar mas detecciones. Se trata de que una persona pueda probar la herramienta, generar un reporte y entender que paso si algo falla.

## Que Decidi

Decidi enfocar el cierre del MVP en experiencia de CLI:

- guardar reportes con `--output`;
- errores claros sin stacktrace para casos esperables;
- CI basico para validar contribuciones;
- README mas orientado a uso real.

## Que Descarte

Descarte una UI.

Tambien descarte instaladores, publicacion en Maven Central y reglas nuevas. Todo eso puede venir despues, pero no bloquea una demo seria.

## Por Que

Una herramienta open source temprana necesita una primera experiencia cuidada.

Si el usuario corre el CLI y recibe un stacktrace por no tener `pom.xml`, la confianza cae. Si puede guardar un reporte Markdown con un comando y subirlo a una PR o compartirlo en un equipo, el proyecto empieza a ser util.

## Resultado Esperado

Quiero que el MVP pueda demostrarse con un flujo simple:

1. empaquetar el CLI;
2. analizar un ejemplo Maven o Gradle;
3. guardar el reporte;
4. mostrar el archivo Markdown resultante;
5. explicar las limitaciones sin esconderlas.

## Resultado Concreto

Agregue `--output` al comando `analyze`.

Ahora puedo imprimir el reporte por stdout, como antes, o guardarlo en un archivo Markdown. Si el directorio de destino no existe, el CLI lo crea.

Tambien cambie el manejo de errores esperables. Si el proyecto no tiene `pom.xml`, `build.gradle` ni `build.gradle.kts`, el CLI devuelve exit code `1` y muestra un mensaje corto. Ya no imprime un stacktrace para ese caso.

Finalmente agregue CI con GitHub Actions para correr `mvn test` en pull requests y pushes a `master`.

## Detalle Del Proceso

Primero escribi un test para `--output`. El RED fue claro: picocli rechazaba la opcion porque todavia no existia.

Despues escribi un test para un directorio sin build file. El RED tambien fue util: el CLI devolvia error, pero mostraba el stacktrace completo. Eso no era una buena primera experiencia para alguien probando la herramienta.

La implementacion quedo acotada a `AnalyzeCommand`: generar el Markdown sigue igual, solo cambia donde se escribe y como se tratan errores esperables.

## Como Lo Contaria En Un Blog

"El cierre del MVP no fue agregar mas features modernas de Java. Fue hacer que la herramienta se pudiera usar sin que yo estuviera al lado explicando cada paso."

## Proximo Paso

Despues de este cierre, el proyecto puede entrar en una fase de robustez: version catalogs, multi-modulo, mas source patterns o JavaParser.
