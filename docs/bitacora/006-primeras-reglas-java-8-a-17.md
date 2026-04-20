# 006 - Primeras reglas Java 8 a 17

## Fecha

2026-04-20

## Objetivo Del Paso

Queria empezar la segunda iteracion sin improvisar sobre el codigo. La base del MVP ya estaba armada, pero todavia faltaba convertirla en una herramienta con reglas reales y extensibles.

## Que Decidi

Decidi que el siguiente paso no sea agregar mas texto al reporte ni detectar features modernas sueltas. El paso correcto es crear un motor chico de reglas y mover ahi las primeras recomendaciones Java 8 a Java 17.

## Que Descarte

Descarte empezar con JavaParser, Gradle o un dashboard. Tambien descarte seguir metiendo findings directamente en el CLI, porque eso vuelve dificil que la comunidad entienda donde contribuir.

## Por Que

Si quiero que este proyecto tenga impacto, las reglas tienen que ser faciles de leer, probar y discutir. Un contributor deberia poder abrir una regla, entender que evidencia usa, que riesgo detecta y que recomendacion genera.

## Que Aprendi

Aprendi que despues del scaffold viene una decision importante: seguir construyendo demo o empezar a construir producto. Para este proyecto, producto significa reglas verificables y reportes que expliquen decisiones de migracion.

## Resultado Concreto

Quedo definido el diseno y el plan de la Iteracion 2: motor de reglas en `analyzer-core`, primeras reglas Java 8 a 17, CLI delegando al analyzer y una verificacion completa con JDK 25.

## Como Lo Contaria En Un Blog

Despues del primer MVP, el proyecto ya podia generar un reporte. Pero eso no alcanzaba. Si el CLI fabrica los findings a mano, el repo sigue siendo una demo.

El siguiente paso fue preguntarme donde iba a vivir el conocimiento de migracion. La respuesta fue un motor de reglas simple. No un plugin system, no una arquitectura gigante, solo un lugar claro para poner evidencia, severidad y recomendacion.

Ese cambio marca el inicio real del proyecto: de reporte hardcodeado a laboratorio de reglas de migracion.

## Proximo Paso

Implementar el motor de reglas y las primeras reglas Java 8 a 17 siguiendo el plan documentado.
