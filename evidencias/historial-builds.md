# Historial de builds (evidencia para el entregable)

Datos leidos de la API de Jenkins el 25/09/2026, con los jobs ya configurados
y el pipeline leyendose desde el repositorio.

## Job `factorial-app` (Parte A)

| Build | Resultado | Causa                       | Pruebas              | Artefacto                    |
|-------|-----------|-----------------------------|----------------------|------------------------------|
| #1    | SUCCESS   | Started by user admin       | 9 pasan, 0 fallan    | `factorial-app-1.0-SNAPSHOT.jar` |
| #2    | SUCCESS   | Started by an SCM change    | 9 pasan, 0 fallan    | `factorial-app-1.0-SNAPSHOT.jar` |
| #3    | SUCCESS   | Started by an SCM change    | 9 pasan, 0 fallan    | `factorial-app-1.0-SNAPSHOT.jar` |
| #4    | SUCCESS   | Started by an SCM change    | 9 pasan, 0 fallan    | `factorial-app-1.0-SNAPSHOT.jar` |

Las 9 pruebas coinciden con las que da `mvn -B clean test` en local, que es el
numero que pide el Paso 2 de la guia.

Etapas de cada build: `Declarative: Checkout SCM`, `Declarative: Tool Install`,
`Compilar`, `Pruebas`, `Empaquetar`, `Declarative: Post Actions`, todas SUCCESS.

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

El build #9 se produjo solo, menos de un minuto despues del push, sin
pulsar Build Now: es la prueba de que `pollSCM('H/2 * * * *')` funciona.

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
