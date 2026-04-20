# 002 - Diseno del MVP

## Fecha

2026-04-20

## Objetivo Del Paso

Queria convertir la idea en un MVP concreto que pudiera ser util, publicable y facil de entender por la comunidad.

## Que Decidi

Decidi que el primer MVP debe ser una CLI que analice proyectos Maven + Spring Boot y genere un reporte Markdown de preparacion migratoria hacia una version Java LTS objetivo.

El primer caso de uso fuerte es:

> Tengo una aplicacion Maven + Spring Boot antigua. Quiero saber que tan lista esta para Java 17, 21 o 25, que bloquea la migracion, que puedo automatizar con OpenRewrite y que modernizaciones valen la pena.

El comando inicial propuesto es:

```bash
mjul analyze --path . --target 21
```

## Que Descarte

Descarte incluir dashboard, HTML report y soporte profundo de Gradle en el primer corte.

Tambien descarte ejecutar OpenRewrite automaticamente desde el MVP. Por ahora es mejor sugerir recetas y comandos, porque eso mantiene a la herramienta honesta y menos invasiva.

## Por Que

Para generar impacto comunitario, el proyecto necesita ser facil de probar y facil de explicar.

Markdown es suficiente para el primer reporte porque se puede leer en GitHub, versionar, copiar a un articulo y revisar en pull requests.

Spring Boot primero tiene sentido porque representa muchos proyectos Java reales. Aunque el analyzer core debe seguir siendo generico, el primer storytelling necesita un caso que la comunidad reconozca.

## Que Aprendi

Un MVP serio no es el que tiene mas features, sino el que resuelve una pregunta real de punta a punta.

En este caso, la pregunta es:

> Que deberia saber antes de migrar esta aplicacion Java a una version LTS mas moderna?

## Resultado Concreto

El MVP incluye:

- CLI con Picocli;
- Java 25 como runtime del tool;
- Maven multi-module;
- detector Maven;
- detector de version Java;
- detector Spring Boot;
- modelo de findings;
- reporte Markdown;
- sugerencias OpenRewrite;
- ejemplo Spring Boot Java 8;
- bitacora del proyecto.

No incluye todavia:

- dashboard;
- HTML;
- benchmarks grandes;
- migracion automatica;
- recomendaciones agresivas de features modernas.

## Como Lo Contaria En Un Blog

La tentacion al crear una herramienta sobre Java moderno es empezar por las features: records, virtual threads, pattern matching, scoped values.

Pero para una migracion real, esa no suele ser la primera conversacion. Antes aparecen preguntas menos vistosas y mas importantes: que version declara el build, que framework uso, que dependencias me atan, que recetas puedo aplicar, que cambios tienen riesgo.

Por eso el MVP empieza por un reporte. No intenta ser magico. Intenta ser claro.

## Proximo Paso

Crear la estructura inicial del repositorio open source y preparar un plan de implementacion task-by-task.
