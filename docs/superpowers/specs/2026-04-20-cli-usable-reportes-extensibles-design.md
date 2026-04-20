# CLI Usable Y Reportes Extensibles - Design

Fecha: 2026-04-20

## Objetivo

La Iteracion 3 convierte el MVP en algo que una persona pueda ejecutar sin conocer la estructura interna del repo y prepara el modelo de reporte para crecer con nuevas reglas.

El objetivo no es agregar mas diagnosticos todavia. El objetivo es mejorar la adopcion: una CLI empaquetable, reportes ordenados por categorias y una guia clara para sumar reglas futuras.

## Problema

El proyecto ya puede inspeccionar un proyecto Maven, ejecutar reglas y renderizar Markdown, pero todavia tiene dos debilidades:

- la CLI no esta empaquetada como un jar autocontenido facil de probar;
- el reporte lista findings de forma plana, lo que va a envejecer mal cuando aparezcan reglas de lenguaje, concurrencia, performance, observabilidad, tooling y automatizacion.

Si se agregan mas reglas sobre esta base, el reporte se vuelve ruidoso y dificil de usar en demos, blogs o charlas.

## Decisiones

### CLI

La primera distribucion sera un jar ejecutable construido desde el modulo `cli`.

El comando objetivo de esta iteracion es:

```bash
java -jar cli/target/modern-java-upgrade-lab-cli.jar analyze --path examples/spring-boot-2-java-8 --target 21
```

No se agrega instalador, Homebrew, SDKMAN ni Docker todavia. Esas opciones tienen sentido despues de validar el flujo basico.

### Categorias De Findings

Se agrega una taxonomia explicita para clasificar findings:

- `RISK`
- `BUILD`
- `FRAMEWORK`
- `LANGUAGE`
- `CONCURRENCY`
- `PERFORMANCE`
- `OBSERVABILITY`
- `AUTOMATION`
- `BASELINE`

La categoria no reemplaza la severidad. La severidad responde "que tan importante es". La categoria responde "donde vive este hallazgo en el reporte".

### Reporte Por Secciones

El renderer Markdown agrupara findings en secciones estables:

- `Project Summary`
- `Migration Risks`
- `Build & Tooling`
- `Framework Compatibility`
- `Language Modernization`
- `Concurrency`
- `Performance`
- `Observability`
- `Automation Suggestions`
- `Baseline & Planning`
- `Other Findings`

Las secciones sin findings se omiten para mantener el reporte legible.

### Extension Futura

Las reglas nuevas deben poder agregarse creando un `MigrationRule` que produzca `Finding` con categoria, severidad, evidencia y recomendacion.

Esta iteracion no introduce carga dinamica de plugins ni `ServiceLoader`. Todavia seria sobreingenieria. Primero conviene que el catalogo default sea simple, testeable y facil de leer.

## No Objetivos

- No crear UI.
- No crear HTML avanzado.
- No agregar JavaParser todavia.
- No agregar benchmarks.
- No crear un sistema de plugins prematuro.
- No ejecutar OpenRewrite automaticamente.

## Exito

La iteracion se considera exitosa si:

- `mvn package` produce un jar ejecutable en el modulo `cli`;
- el jar puede correr `analyze` contra el ejemplo Spring Boot Java 8;
- el reporte Markdown agrupa findings por categoria;
- los tests cubren categorias y packaging;
- la documentacion explica como agregar una regla nueva;
- la bitacora registra la decision en primera persona.
