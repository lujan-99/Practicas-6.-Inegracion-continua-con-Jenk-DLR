# Historial de builds (evidencia para el entregable)

Datos leidos de la API de Jenkins entre el 25 y el 26/09/2026, con los jobs ya
configurados y el pipeline leyendose desde el repositorio.

## Job `factorial-app` (Parte A)

| Build | Resultado | Causa                       | Pruebas              | Artefacto                    |
|-------|-----------|-----------------------------|----------------------|------------------------------|
| #1    | SUCCESS   | Started by user admin       | 9 pasan, 0 fallan    | `factorial-app-1.0-SNAPSHOT.jar` |
| #2    | SUCCESS   | Started by an SCM change    | 9 pasan, 0 fallan    | `factorial-app-1.0-SNAPSHOT.jar` |
| #3    | SUCCESS   | Started by an SCM change    | 9 pasan, 0 fallan    | `factorial-app-1.0-SNAPSHOT.jar` |
| #4    | SUCCESS   | Started by an SCM change    | 9 pasan, 0 fallan    | `factorial-app-1.0-SNAPSHOT.jar` |
| #5    | SUCCESS   | Started by an SCM change    | 9 pasan, 0 fallan    | `factorial-app-1.0-SNAPSHOT.jar` |
| #6    | **FAILURE** | Started by an SCM change  | **5 pasan, 4 fallan** | ninguno: Empaquetar omitida  |
| #7    | SUCCESS   | Started by an SCM change    | 9 pasan, 0 fallan    | `factorial-app-1.0-SNAPSHOT.jar` |

Las 9 pruebas coinciden con las que da `mvn -B clean test` en local, que es el
numero que pide el Paso 2 de la guia. Esa comparacion es la que en la Parte B
separa un build sano de uno que miente.

### El build #6: el defecto introducido a proposito (Paso 9)

Commit `Rompe el factorial a proposito`, que cambia `resultado *= i` por
`resultado += i`. Sin pulsar Build Now: Jenkins lo detecto con `pollSCM` en menos
de dos minutos.

| Etapa | Estado |
|-------|--------|
| `Declarative: Checkout SCM` | SUCCESS |
| `Declarative: Tool Install` | SUCCESS |
| `Compilar` | SUCCESS |
| `Pruebas` | **FAILED** |
| `Empaquetar` | omitida: `Stage "Empaquetar" skipped due to earlier failure(s)` |
| `Declarative: Post Actions` | SUCCESS (el paso `junit` esta en `post { always }`, asi que el informe se publico igual) |

4 de las 9 pruebas fallan, cada una con el valor esperado y el obtenido:

```
expected: <2> but was: <3>                          2!
expected: <120> but was: <15>                       5!
expected: <3628800> but was: <55>                   10!
expected: <2432902008176640000> but was: <210>      20!
```

`0!`, `1!` y `3!` siguen pasando: con `resultado += i` la suma 1+2+3 da
exactamente 6, que es 3!. Con una sola prueba de 3! el defecto habria pasado
inadvertido. Las dos pruebas de excepcion tampoco fallan, porque no llegan al
bucle.

El commit `f0331b3` revierte el defecto y el build #7 vuelve a 9/9 en verde. El
historial queda rojo en el medio, que es la Captura 5.

Etapas de los builds verdes: `Declarative: Checkout SCM`,
`Declarative: Tool Install`, `Compilar`, `Pruebas`, `Empaquetar`,
`Declarative: Post Actions`, todas SUCCESS.

## Job `reto-ci` (Parte B)

| Build | Resultado | Causa                       | Pruebas                        | Lectura                          |
|-------|-----------|-----------------------------|--------------------------------|----------------------------------|
| #1    | FAILURE   | Started by user admin       | sin informe                    | no existe la herramienta `Maven 3.8.5` |
| #2    | FAILURE   | Started by user admin       | sin informe                    | `bat` en un agente Linux        |
| #3    | SUCCESS   | Started by user admin       | **sin informe, 0 ejecutadas**  | verde falso: Surefire no encontro la clase |
| #4    | SUCCESS   | Started by user admin       | **sin informe, 2 fallan**      | verde falso: `-Dmaven.test.failure.ignore` |
| #5    | FAILURE   | Started by user admin       | 11 pasan, 2 fallan             | rojo real, con Test Result      |
| #6    | FAILURE   | Started by user admin       | 12 pasan, 1 falla              | falta el redondeo               |
| #7    | SUCCESS   | Started by user admin       | 13 pasan, 0 fallan             | verde sano                      |
| #8    | SUCCESS   | Started by user admin       | 13 pasan, 0 fallan             | verde sano, con bloque `unstable` |
| #9    | SUCCESS   | **Started by an SCM change**| 13 pasan, 0 fallan             | disparo automatico, sin clic   |
| #10-#12 | SUCCESS | Started by an SCM change  | 13 pasan, 0 fallan             | los commits de `factorial-app` tambien lo despiertan (ver nota) |

El build #9 se produjo solo, menos de un minuto despues del push, sin
pulsar Build Now: es la prueba de que `pollSCM('H/2 * * * *')` funciona.

**Nota sobre el monorepo.** Los dos jobs leen el mismo repositorio, asi que
cada commit despierta a los dos. Los builds #10 a #12 de `reto-ci` se
producieron por commits de `factorial-app` (el defecto introducido a proposito
y su revert), no por cambios en `reto-ci`: compilan y prueban su propio proyecto
y salen en verde. No es un defecto del pipeline, es la consecuencia de tener las
dos carpetas en un solo repositorio.

## Las siete condiciones de un build sano, en el build #9 de reto-ci

| Indicador          | Estado                                     | Donde se lee                                  |
|--------------------|--------------------------------------------|-----------------------------------------------|
| Resultado          | SUCCESS                                    | icono del build, Stage View                   |
| Pruebas ejecutadas | 13                                         | Test Result; `Tests run: 13` en la consola    |
| Pruebas fallidas   | 0 fallos, 0 errores                        | Test Result                                   |
| Informe publicado  | si                                         | Test Result y Test Result Trend del job       |
| Artefacto          | `calculadora-descuentos-1.0-SNAPSHOT.jar`  | Build Artifacts del build                     |
| Disparo automatico | `Started by an SCM change`                 | causa del build                               |
| Reproducibilidad   | el pipeline esta en el Jenkinsfile         | configuracion del job: Pipeline script from SCM |

## Comandos para reproducir estos datos

```bash
# estado de cada build
curl -s -u lujan99:CLAVE \
  "http://localhost:8090/job/reto-ci/api/json?depth=1" | python3 -m json.tool

# pruebas de un build
curl -s -u lujan99:CLAVE \
  "http://localhost:8090/job/reto-ci/9/testReport/api/json"

# etapas de un build (Stage View por API)
curl -s -u lujan99:CLAVE \
  "http://localhost:8090/job/reto-ci/9/wfapi/describe"
```
