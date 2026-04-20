# Source Pattern Detection - Design

Fecha: 2026-04-20

## Objetivo

La Iteracion 4 agrega la primera deteccion real de patrones de codigo fuente Java.

El objetivo es que el reporte deje de depender solamente de metadata de build y empiece a mostrar oportunidades concretas encontradas en archivos `.java`.

## Decision Principal

Voy a empezar con un scanner liviano, sin JavaParser.

Esta decision es intencional. Un parser AST va a servir mas adelante, pero para el MVP todavia conviene aprender con patrones chicos, testeados y faciles de explicar en una charla:

- `Map<String, Object>` en controllers como senal de DTO candidato a `record`;
- `SimpleDateFormat` como senal de migracion hacia `java.time`;
- `Executors.newFixedThreadPool` / `newCachedThreadPool` como senal de revision antes de adoptar virtual threads.

## Arquitectura

Se agregan dos piezas al `analyzer-core`:

- `SourcePattern`: evidencia detectada en un archivo fuente;
- `SourcePatternScanner`: scanner recursivo de archivos `.java`.

`ProjectMetadata` empieza a transportar tambien `sourcePatterns`.

El CLI queda responsable de:

1. inspeccionar metadata Maven;
2. escanear codigo fuente;
3. construir metadata enriquecida;
4. ejecutar el analyzer.

El `DefaultMigrationRules` convierte patrones detectados en findings categorizados.

## No Objetivos

- No parsear AST todavia.
- No modificar codigo.
- No generar OpenRewrite recipes propias.
- No inferir que todo `Map<String, Object>` debe transformarse en `record`.
- No recomendar virtual threads como reemplazo automatico.

## Criterio De Exito

La iteracion termina bien si:

- el scanner detecta patrones en archivos Java;
- los findings aparecen en `Language Modernization` y `Concurrency`;
- el CLI muestra oportunidades reales en el ejemplo Spring Boot;
- hay tests RED/GREEN para scanner, reglas y CLI;
- la bitacora registra por que se eligio scanner liviano antes de JavaParser.
