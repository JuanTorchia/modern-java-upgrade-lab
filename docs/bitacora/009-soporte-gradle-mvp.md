# 009 - Soporte Gradle MVP

Fecha: 2026-04-20

## Objetivo Del Paso

En este paso quiero que Modern Java Upgrade Lab deje de ser solo util para proyectos Maven.

Gradle es demasiado comun en proyectos Spring Boot como para postergarlo mucho mas si el objetivo es impacto en comunidad.

## Que Decidi

Decidi empezar con soporte Gradle minimo, textual y honesto.

La herramienta va a leer `build.gradle` y `build.gradle.kts`, detectar version Java, plugin de Spring Boot, dependencias y plugins visibles.

## Que Descarte

Descarte ejecutar Gradle o resolver el modelo efectivo.

Tambien descarte soporte profundo para multi-modulo, `buildSrc`, version catalogs y convenciones internas. Son importantes, pero meter todo eso ahora haria crecer demasiado el alcance.

## Por Que

Para el MVP me importa mas que la herramienta funcione rapido sobre proyectos comunes y que no haga cosas sorpresivas al analizar codigo ajeno.

Ejecutar Gradle puede disparar plugins, descargar dependencias o depender del entorno local. Para una primera version de analisis, prefiero una lectura conservadora.

## Resultado Esperado

Quiero poder ejecutar el CLI sobre un ejemplo Gradle Spring Boot y obtener un reporte comparable al de Maven.

La promesa no es "entiendo todo Gradle". La promesa es "puedo detectar lo mas visible y darte un primer reporte de migracion".

## Como Lo Contaria En Un Blog

"El soporte Gradle empezo con una decision incomoda pero sana: no ejecutar Gradle. Para una herramienta de migracion que analiza proyectos ajenos, la primera version tenia que priorizar seguridad, velocidad y explicabilidad antes que precision absoluta."

## Proximo Paso

Despues de este MVP, el siguiente paso seria medir en proyectos reales que patrones Gradle quedan fuera y decidir si conviene soportar version catalogs, multi-modulo o una integracion opcional con Tooling API.
