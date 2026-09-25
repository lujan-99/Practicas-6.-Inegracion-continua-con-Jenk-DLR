# Guia de capturas para el PDF del entregable

Jenkins ya esta configurado y los dos jobs tienen historial. Abri
<http://localhost:8090>, entrar como `lujan99`, y sacar estas capturas.

## Parte A

| # | Captura | URL |
|---|---------|-----|
| 1 | `mvn -B clean test` en local, con `Tests run: 9` | terminal en `factorial-app/` |
| 2 | Panel principal de Jenkins con el usuario `lujan99` arriba a la derecha | `/` |
| 3 | Manage Jenkins -> Tools, con `Maven-3.9` en Maven installations | `/manage/tools/` |
| 4 | Configuracion del job: **Pipeline script from SCM** con la URL del repo y el Script Path | `/job/factorial-app/configure` |
| 5 | Stage View del build sano, las 3 etapas en verde | `/job/factorial-app/4/` |
| 6 | Console Output con `Tests run: 9, Failures: 0, Errors: 0` | `/job/factorial-app/4/console` |
| 7 | Test Result con las 9 pruebas y **Failures: 0, Errors: 0** | `/job/factorial-app/4/testReport/` |
| 8 | Build Artifacts con el `.jar` archivado | seccion *Build Artifacts* del build |
| 9 | Test Result Trend del job | `/job/factorial-app/` |

## Parte B: los builds que hay que mirar

| # | Captura | URL | Que demuestra |
|---|---------|-----|---------------|
| 10 | Build #1 en rojo | `/job/reto-ci/1/console` | `Tool type "maven" does not have an install of "Maven 3.8.5"` |
| 11 | Build #2, Compilar en rojo y las otras etapas saltadas | `/job/reto-ci/2/` | `Batch scripts can only be run on Windows nodes` |
| 12 | Build #3 **verde** | `/job/reto-ci/3/console` | `Tests run` no aparece: 0 pruebas, y aun asi hay `.jar` archivado |
| 13 | Build #4 **verde** | `/job/reto-ci/4/console` | `Tests run: 13, Failures: 1, Errors: 1` y despues `BUILD SUCCESS` |
| 14 | Build #5 en rojo, con Test Result | `/job/reto-ci/5/testReport/` | el primer build que dice la verdad: 11 pasan, 2 fallan |
| 15 | Build #6 en rojo | `/job/reto-ci/6/testReport/` | 12 pasan, 1 falla: el redondeo |
| 16 | Build #9 verde y sano | `/job/reto-ci/9/` | 13/13, informe, artefacto y causa `Started by an SCM change` |

Las capturas 12 y 13 son las importantes: son las dos que demuestran que
**verde no es sano**. En la 12 el build pasa y no se ejecuto ninguna prueba;
en la 13 hay dos pruebas en rojo y el build tambien pasa.

## Historial de builds

La tabla completa esta en `historial-builds.md`, y la tabla de defectos en
`../reto-ci/DEFECTOS.md`. En el PDF hay que incluir las dos.

## Notas

- El puerto es **8090**, no 8080: el 8080 lo usa la API de la Practica 5.
- Los builds #2, #3 y #4 de `factorial-app` salieron solos (`Started by an SCM
  change`). Es normal: al ser un solo repositorio con las dos carpetas, cada
  commit de `reto-ci` tambien despierta a `factorial-app`, que compila y prueba
  su proyecto y sale verde.
- Para que la ultima linea del build #9 se vea honesta, no hay que volver a
  pulsar Build Now: ese build ya lo disparo `pollSCM` solo tras el push.
