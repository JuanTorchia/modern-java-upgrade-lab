# 005 - Revision final del MVP

## Fecha

2026-04-20

## Objetivo Del Paso

Queria cerrar el primer scaffold con una mirada critica, no solo con la sensacion de que compila.

## Que Decidi

Decidi revisar la arquitectura despues del ultimo ajuste funcional y corregir una dependencia que no me convencia: `analyzer-core` habia quedado dependiendo de `rewrite-adapter` solo para renderizar el comando sugerido de OpenRewrite.

## Que Descarte

Descarte dejar esa dependencia como estaba por comodidad. Tambien descarte meter una abstraccion grande para comandos de migracion porque todavia no hay suficiente complejidad real que la justifique.

## Por Que

El core tiene que ser una base estable para el analisis y el reporte. Si empieza dependiendo de adapters concretos demasiado pronto, el proyecto se vuelve mas dificil de extender cuando entren Gradle, mas reglas o nuevas integraciones.

## Que Aprendi

Aprendi que incluso en un MVP conviene distinguir entre deuda aceptable y deuda que distorsiona la direccion del proyecto. Esta era chica, pero tocaba la arquitectura central.

## Resultado Concreto

El reporte sigue mostrando la receta y el comando runnable de OpenRewrite, pero `analyzer-core` ya no depende de `rewrite-adapter`. El scaffold queda mas alineado con la arquitectura por capas que quiero sostener.

## Como Lo Contaria En Un Blog

Antes de dar por terminado el primer MVP, hice una revision incomoda: buscar que parte del codigo ya estaba empujando el diseno hacia un lugar que no queria.

Encontre una dependencia pequena pero reveladora. El modulo core estaba usando una clase del adapter de OpenRewrite solo para imprimir un comando. Funcionaba, pero la direccion de dependencia era mala para un proyecto que quiere crecer con varios analizadores e integraciones.

No hice una gran refactorizacion. Solo quite ese acoplamiento y deje el core independiente. Para mi, ese fue el tipo de decision que vuelve serio a un MVP: no hacerlo grande, hacerlo coherente.

## Proximo Paso

Correr verificacion completa con JDK 25 y decidir si integro la rama al branch principal o si hago una pasada mas de pulido del flujo `analyze`.
