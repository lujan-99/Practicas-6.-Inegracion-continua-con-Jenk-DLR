# Practica 6 - Integracion continua con Jenkins (COM450)

Ejecucion de la practica completa: Jenkins en Docker + pipeline sobre un proyecto
Maven, reto de diagnostico y registro de las correcciones.

## Estructura

| Carpeta         | Que contiene                                                        |
|-----------------|---------------------------------------------------------------------|
| `factorial-app` | Proyecto Maven correcto (Practica 1) con su Jenkinsfile. Parte A.    |
| `reto-ci`       | Proyecto con defectos sembrados. Parte B: diagnostico y correccion.  |
| `jenkins`       | `docker-compose.yml` para levantar Jenkins con volumen persistente.  |
| `evidencias/`   | Capturas del PDF del entregable.                                     |

Cada proyecto tiene su `Jenkinsfile` en la raiz de su carpeta, y su propio job
de tipo Pipeline en Jenkins.

## Levantar Jenkins

```bash
cd jenkins
docker compose up -d
docker ps                      # jenkins-com450, 0.0.0.0:8090->8080/tcp
docker exec jenkins-com450 cat /var/jenkins_home/secrets/initialAdminPassword
```

Interfaz: <http://localhost:8090> · usuario: `lujan99`

## Jobs

| Job en Jenkins              | Repositorio | Script Path               |
|-----------------------------|-------------|---------------------------|
| `factorial-app`             | este repo   | `factorial-app/Jenkinsfile` |
| `reto-ci`                   | este repo   | `reto-ci/Jenkinsfile`       |

Los dos leen el pipeline del repositorio (**Pipeline script from SCM**) y se
disparan solos con `pollSCM('H/2 * * * *')`, es decir, revisan el repositorio
cada 2 minutos.

## Adaptation al monorepo

El workspace de un job Pipeline es la raiz del repositorio, no la carpeta del
proyecto. Por eso los dos Jenkinsfiles envuelven sus pasos en
`dir('factorial-app')` / `dir('reto-ci')`. Fuera de eso, el pipeline de
`factorial-app` es identico al del material, y el de `reto-ci` conserva los
defectos del material en su commit inicial.

## Historial de reto-ci

Un commit por defecto. Cada commit se corresponde con un build:

| Build | Commit           | Resultado                                              |
|-------|------------------|--------------------------------------------------------|
| #1    | version inicial  | FAILURE: no existe la herramienta `Maven 3.8.5`        |
| #2    | fix 1            | FAILURE: `bat` no existe en un agente Linux            |
| #3    | fix 2            | SUCCESS **falso**: 0 pruebas ejecutadas, sin informe    |
| #4    | fix 3            | SUCCESS **falso**: 13 pruebas, 2 fallan, ignoradas     |
| #5    | fix 4            | FAILURE real: `-Dtest.failure.ignore` fuera, JUnit publicado |
| #6    | fix 5            | FAILURE: falta permitir el descuento 100               |
| #7    | fix 6            | SUCCESS: 13 pruebas, 0 fallos, con informe y artefacto  |

El detalle de cada defecto esta en `reto-ci/DEFECTOS.md`.
