# Guia de capturas para el PDF del entregable

Jenkins ya esta configurado y los dos jobs tienen historial. Abri
<http://localhost:8090>, entrar como `lujan99`, y sacar estas capturas.

La numeracion sigue la de la guia de la practica.

## Parte A

| Captura | Que hay que capturar | URL |
|---------|---------------------|-----|
| **1** | `mvn -B clean test` en local, con `Tests run: 9, Failures: 0` | terminal, en `~/Practicas6/factorial-app` |
| **2** | Panel principal de Jenkins, con el usuario `lujan99` arriba a la derecha | `/` |
| **3** | Pagina del job con la Stage View en verde: Compilar, Pruebas, Empaquetar y sus duraciones | `/job/factorial-app/` |
| **4** | Test Result con las 9 pruebas desplegadas, 0 fallos | `/job/factorial-app/7/testReport/` |
| **5** | Stage View con el historial **rojo -> verde** | `/job/factorial-app/` |
| **6** | Pagina del build del Paso 9 con **Started by an SCM change** | `/job/factorial-app/6/` |
| **7** | Pagina del job `reto-ci` con la Stage View **y** el Test Result Trend | `/job/reto-ci/` |

### Captura 7 en detalle

Es la verificacion de la Parte B. Se toma cuando el job `reto-ci` ya cumple las
**siete** filas del build sano. La pagina del job muestra las dos cosas que pide
la guia:

- La **Stage View** con `Compilar`, `Pruebas` y `Empaquetar` en verde.
- El **Test Result Trend**, el grafico con la evolucion de los resultados de las
  pruebas build a build. Es la prueba visual de que el pipeline paso de
  ejecuciones vacias o engañosas a 13 pruebas limpias.

Ojo: la Stage View de `reto-ci` aparece en la pagina del job, pero las
**duraciones** por etapa hay que sacarlas de la vista de pipeline de un build
concreto (`/job/reto-ci/14/workflow-stage`).

### Captura 6 en detalle

Es la pagina del build **#6**, el del defecto introducido a proposito. El texto
`Started by an SCM change` sale en la barra lateral izquierda, arriba del todo,
junto al nombre del build. Es la prueba de que el build lo lanzo el `pollSCM` del
Jenkinsfile y no un clic.

Como apoyo, el **Git Polling Log** del job esta en
`/job/factorial-app/scmPollLog/`: ahi se ve cada consulta que Jenkins le hizo a
GitHub, con el rango de commits que comparo (`git log <anterior>..<nuevo>`) y si
encontro cambios o no.

### Capturas de apoyo del Paso 8

No son numeradas por la guia, pero son las que hacen legible el entregable:

| Que capturar | URL |
|--------------|-----|
| Manage Jenkins -> Tools, con `Maven-3.9` | **`/configureTools/`** (no `/manage/tools/`, da 404) |
| Configuracion del job: Pipeline script from SCM, URL y Script Path | `/job/factorial-app/configure` |
| Console Output del build #1, con `Unpacking ... apache-maven-3.9.16-bin.zip` | `/job/factorial-app/1/console` |
| Test Result Trend del job | `/job/factorial-app/` |
| Build Artifacts con el `.jar` | seccion *Build Artifacts* de `/job/factorial-app/7/` |

**Ojo:** la guia dice que en la consola del build #1 se ve
`apache-maven-3.9.11-bin.zip`. Aqui es **3.9.16**, porque se instalo la 3.9 mas
reciente que Jenkins ofrece. No es una discrepancia.

## Captura 5 en detalle

El historial de `factorial-app` quedo asi:

| Build | Resultado | Pruebas | Que paso |
|-------|-----------|---------|----------|
| #1-#5 | SUCCESS | 9/9 | los builds verdes de la Parte A |
| **#6** | **FAILURE** | **5/9** | el defecto introducido a proposito |
| **#7** | **SUCCESS** | **9/9** | el revert |

Para que se lea el rojo en medio del verde, captura la pagina del job
`/job/factorial-app/` con la columna de builds completa a la vista. Las tres
etapas del build #6 se ven asi:

- `Compilar` en verde
- `Pruebas` en rojo
- `Empaquetar` en gris, porque quedo **omitida** (la consola dice
  `Stage "Empaquetar" skipped due to earlier failure(s)`), aunque la Stage View
  la reporte con estado FAILED

Del build #6 tambien vale la pena capturar el Test Result
(`/job/factorial-app/6/testReport/`), donde se ven las 4 pruebas que fallan
con el valor esperado y el obtenido:

```
expected: <120> but was: <15>            5!
expected: <2> but was: <3>               2!
expected: <3628800> but was: <55>        10!
expected: <2432902008176640000> but was: <210>   20!
```

y que 0!, 1! y 3! siguen pasando: con una sola prueba de 3! este defecto habria
pasado inadvertido.

## Parte B: los builds que hay que mirar

| Que demuestra | Donde |
|---------------|-------|
| **verde con 0 pruebas** y `.jar` archivado igual | `/job/reto-ci/3/console` |
| **verde con `Failures: 1, Errors: 1` -> `BUILD SUCCESS`** | `/job/reto-ci/4/console` |
| rojo real, 11 pasan / 2 fallan, con Test Result | `/job/reto-ci/5/testReport/` |
| 12 pasan / 1 falla (el redondeo) | `/job/reto-ci/6/testReport/` |
| sano, 13/13, causa `Started by an SCM change` | `/job/reto-ci/9/` |
| errores de los builds #1 y #2 | `/job/reto-ci/1/console` y `/job/reto-ci/2/console` |

Los dos primeros son los importantes: los dos estan **verdes** y los dos mienten.

Los errores de #1 y #2 estan mas abajo en la consola: usa el enlace
**Full Log** arriba a la derecha, o `Ctrl+F` para `Maven 3.8.5` y `Windows nodes`.

## Tablas para el PDF

- Historial de builds: `historial-builds.md`
- Tabla de los 7 defectos: `../reto-ci/DEFECTOS.md`
