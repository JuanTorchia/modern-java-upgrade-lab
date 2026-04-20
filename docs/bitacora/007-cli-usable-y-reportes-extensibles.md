# 007 - CLI Usable Y Reportes Extensibles

Fecha: 2026-04-20

## Objetivo Del Paso

En este paso decidi que el proximo avance no tenia que ser "mas inteligencia" sino mas adopcion.

La herramienta ya podia analizar un ejemplo Maven/Spring Boot Java 8 y generar findings, pero todavia tenia dos problemas practicos: no era comoda de ejecutar como herramienta externa y el reporte estaba creciendo como una lista plana.

Si quiero que Modern Java Upgrade Lab genere impacto en comunidad, alguien tiene que poder correrlo rapido y entender el reporte sin leer el codigo.

## Que Decidi

Decidi armar una iteracion con dos objetivos:

- empaquetar la CLI como un jar ejecutable;
- convertir los findings en elementos categorizados para que el reporte pueda crecer por secciones.

Tambien decidi documentar desde ahora como agregar una regla nueva. No quiero que contribuir requiera entender todo el proyecto.

## Que Descarte

Descarte hacer una UI en esta etapa.

Tambien descarte meter JavaParser, benchmarks, HTML avanzado o carga dinamica de plugins. Todas esas ideas pueden ser buenas mas adelante, pero ahora habrian agregado superficie sin resolver el problema basico: que la herramienta sea facil de probar y que el reporte escale.

## Por Que

Una herramienta open source gana comunidad cuando el primer contacto es claro.

Si alguien entra al repo y tiene que pelearse con Maven internamente para saber como correr la CLI, pierde interes. Si el reporte mezcla riesgos, build, framework y automatizacion en una lista unica, tambien pierde valor como material de charla o blog.

Por eso priorice el flujo minimo:

```bash
java -jar cli/target/modern-java-upgrade-lab-cli.jar analyze --path examples/spring-boot-2-java-8 --target 21
```

## Que Aprendi

Aprendi que el proyecto no necesita parecer mas grande para ser mas serio. Necesita tener mejores bordes.

La categoria del finding parece un detalle chico, pero cambia la arquitectura del reporte: permite que futuras reglas de lenguaje, concurrencia, performance, observabilidad o tooling se agreguen sin romper la lectura.

## Resultado Esperado

Al terminar esta iteracion deberia tener:

- un jar ejecutable;
- reportes Markdown por secciones;
- una taxonomia inicial de findings;
- documentacion para contributors;
- tests que protejan el flujo.

## Como Lo Contaria En Un Blog

"Antes de sumar reglas, hice una pausa incomoda: mire el proyecto como si fuera la primera persona que lo encuentra en GitHub. La conclusion fue simple: no necesitaba una UI, necesitaba poder ejecutarse facil y explicar mejor lo que ya sabia."

## Proximo Paso

Despues de esta base, el siguiente paso con mas impacto probablemente sea deteccion real de patrones de codigo con una libreria de parsing o agregar soporte Gradle. La decision deberia depender de que historia quiero contar primero: profundidad tecnica de migracion o cobertura de proyectos reales.
