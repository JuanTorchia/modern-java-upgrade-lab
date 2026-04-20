# 008 - Deteccion De Patrones De Codigo

Fecha: 2026-04-20

## Objetivo Del Paso

En este paso quiero que Modern Java Upgrade Lab empiece a mirar codigo fuente real, no solo metadata de Maven o Spring Boot.

La idea es detectar senales pequenas pero utiles en archivos `.java` y convertirlas en oportunidades de modernizacion explicables.

## Que Decidi

Decidi empezar con un scanner liviano de patrones antes de incorporar JavaParser.

Los primeros patrones son:

- `Map<String, Object>` como posible DTO candidato a `record`;
- `SimpleDateFormat` como senal para revisar `java.time`;
- `Executors.newFixedThreadPool` o `newCachedThreadPool` como senal para evaluar virtual threads en Java 21+.

## Que Descarte

Descarte transformar codigo automaticamente.

Tambien descarte hacer un parser AST desde el primer paso. No porque sea mala idea, sino porque todavia necesito validar que el reporte y la arquitectura soporten evidencia de codigo de forma simple.

## Por Que

Una recomendacion de migracion vale mas cuando puede apuntar a evidencia concreta.

Decir "Java moderno tiene records" es superficial. Decir "encontre un controller devolviendo `Map<String, Object>` y quizas conviene modelar un DTO explicito" es mas util.

## Resultado Esperado

Quiero que el reporte muestre una seccion `Language Modernization` con oportunidades reales del ejemplo Spring Boot Java 8.

Si aparece concurrencia clasica, tambien quiero que pueda aparecer en `Concurrency`, especialmente al apuntar a Java 21 o superior.

## Como Lo Contaria En Un Blog

"El primer salto de valor fue dejar de hablar de features en abstracto. En vez de decir que Java tiene records, empece a buscar codigo donde un record podria mejorar el modelo. La herramienta no cambia codigo: muestra evidencia para conversar mejor la migracion."

## Proximo Paso

Si estos patrones resultan utiles, el siguiente paso natural es decidir entre dos caminos:

- agregar mas patrones simples;
- incorporar JavaParser para detectar estructuras con mas precision.
