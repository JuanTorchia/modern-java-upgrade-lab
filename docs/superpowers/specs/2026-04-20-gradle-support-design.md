# Gradle Support MVP Design

Fecha: 2026-04-20

## Contexto

Modern Java Upgrade Lab ya analiza proyectos Maven, genera reportes Markdown y escanea patrones simples de codigo Java. La siguiente mejora con mayor impacto comunitario es soportar Gradle, porque muchos equipos Spring Boot usan Gradle y hoy el CLI falla si no encuentra `pom.xml`.

## Decision

Voy a implementar soporte Gradle minimo y honesto mediante inspeccion textual de `build.gradle` y `build.gradle.kts`.

No voy a ejecutar Gradle, resolver el modelo efectivo ni descargar dependencias. Eso seria mas preciso, pero tambien mas lento, mas fragil para un MVP y menos seguro al analizar proyectos desconocidos.

## Alcance

El MVP debe:

- detectar `build.gradle` y `build.gradle.kts`;
- reportar `buildTool = gradle`;
- detectar version Java desde `sourceCompatibility`, `targetCompatibility` o toolchains `JavaLanguageVersion.of(...)`;
- detectar version de Spring Boot desde el plugin `org.springframework.boot`;
- recolectar dependencias declaradas con notacion `group:artifact:version` o `group:artifact`;
- recolectar plugins declarados con `id 'plugin'` o `id("plugin")`;
- permitir que el CLI elija Maven o Gradle segun los archivos presentes;
- mantener el scanner de codigo fuente igual para Maven y Gradle.

## Fuera De Alcance

- Resolver `libs.versions.toml`;
- interpretar `buildSrc`;
- ejecutar `gradle dependencies`;
- soportar proyectos multi-modulo en profundidad;
- detectar versiones indirectas desde variables complejas;
- modificar codigo o build files.

## Arquitectura

Agrego un `GradleProjectInspector` en `build-inspectors`, paralelo a `MavenProjectInspector`.

Agrego un `ProjectInspector` pequeno que decide:

1. Si hay `pom.xml`, usa Maven.
2. Si hay `build.gradle` o `build.gradle.kts`, usa Gradle.
3. Si no hay build conocido, falla con un mensaje claro.

El CLI depende de `ProjectInspector`, no de `MavenProjectInspector` directo.

## Testing

El desarrollo debe seguir TDD:

- fixture Groovy Gradle con Spring Boot 2 y Java 17;
- fixture Kotlin DSL Gradle con Spring Boot 3 y Java toolchain 21;
- test del selector de inspector;
- test del CLI sobre un ejemplo Gradle real con codigo fuente.

## Riesgos

La inspeccion textual puede tener falsos negativos. Lo acepto para el MVP porque el reporte debe ser claro: "detectado desde build file visible", no "modelo Gradle resuelto".

El mayor riesgo seria prometer precision que no tenemos. Por eso no voy a exponer esto como auditoria completa de Gradle, sino como soporte inicial para proyectos comunes.

## Resultado Esperado

Al ejecutar:

```powershell
java -jar cli\target\modern-java-upgrade-lab-cli.jar analyze --path examples\spring-boot-3-gradle-java-21 --target 25
```

el reporte debe detectar Gradle, Java 21, Spring Boot 3 y seguir mostrando oportunidades de modernizacion de codigo fuente si existen.
