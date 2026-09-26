# reto-ci: los seis defectos, diagnosticados y corregidos

El material inicial traia **seis** defectos sembrados. Cada uno se corrigio con
**un commit**, y cada commit dejo que el polling lanzara el build siguiente, que
revelo el defecto que estaba escondido debajo. La cadena completa:

| # | Sintoma | Build que lo revela | Commit que lo corrige | Build que lo confirma |
|---|---------|---------------------|----------------------|-----------------------|
| 1 | `Tool type "maven" does not have an install of "Maven 3.8.5" configured` | #1 | `ffc0766` | #2 |
| 2 | `Batch scripts can only be run on Windows nodes` | #2 | `e2c2de1` | #3 |
| 3 | Verde sin `Tests run:` y sin Test Result | #3 | `822dbc2` | #4 |
| 4 | Verde con `Tests run: 13, Failures: 1, Errors: 1` | #4 | `d8a16de` | #5 |
| 5 | `descuentoTotalDejaElPrecioEnCero` lanza `IllegalArgumentException` | #5 | `392ef32` | #6 |
| 6 | `aplicaElDescuentoYRedondeaADosDecimales[4]`: 29,99 en vez de 30,00 | #6 | `68d7439` | #7 |

El **build #7** es el primero que cumple las siete filas del build sano.

Ademas se aplico una septima correccion, **fuera del reto**: `302cec1`, que
agrega el bloque `post { unstable }` del `Jenkinsfile`. No es un defecto
sembrado; se hizo para que el `Jenkinsfile` de `reto-ci` quedara simetrico con
el de `factorial-app`. Se documenta aparte, al final, para no Mezclarlo con los
seis.

---

## Defecto 1 — la herramienta que no existe

| Campo | Contenido |
|-------|-----------|
| **Sintoma observado** | Build **#1**, `FAILURE` en 4 segundos. **No llega a ninguna etapa**: la Stage View sale vacia. Consola: `WorkflowScript: 9: Tool type "maven" does not have an install of "Maven 3.8.5" configured - did you mean "Maven-3.9"? @ line 9, column 15.` y `startup failed`, `1 error`, `Finished: FAILURE` |
| **¿Se reproduce en local?** | **No.** `mvn -B clean compile` en `~/Practicas6/reto-ci` da `BUILD SUCCESS`. En local no hay bloque `tools`: Maven esta en el `PATH`. |
| **Capa** | **Jenkinsfile**, no del entorno. El Jenkins existe y la herramienta existe, pero con otro nombre. |
| **Indicador o regla violada** | Se rompen a la vez "Resultado: SUCCESS" y "Pruebas ejecutadas: 13", porque el pipeline nunca arranca. |
| **Archivo y linea** | `reto-ci/Jenkinsfile:9` — `maven 'Maven 3.8.5'` |
| **Correccion aplicada** | Ver abajo. Commit `ffc0766`; lo confirma el build **#2**. |

```groovy
// ANTES
        maven 'Maven 3.8.5'
// DESPUES
        maven 'Maven-3.9'
```

La regla del reto descarta crear en Jenkins una herramienta con el nombre
`Maven 3.8.5`, y con razon: si cada proyecto pide su propio nombre, el servidor
termina con diez instalaciones de Maven identicas. Jenkins hasta lo sugiere:
`did you mean "Maven-3.9"?`.

---

## Defecto 2 — `bat` en un agente Linux

| Campo | Contenido |
|-------|-----------|
| **Sintoma observado** | Build **#2**, `FAILURE` en la etapa **Compilar**. Consola: `java.io.IOException: Batch scripts can only be run on Windows nodes.` |
| **¿Se reproduce en local?** | **No.** `mvn -B clean compile` funciona: en local el shell es Bash. `bat` es el paso de Jenkins para scripts `.bat` de Windows y no existe en Linux. |
| **Capa** | **Jenkinsfile**. |
| **Indicador o regla violada** | "Resultado: SUCCESS" y "Pruebas ejecutadas: 13". |
| **Archivo y linea** | `reto-ci/Jenkinsfile:26`, `:33` y `:40` — las **tres** etapas, no solo la primera. |
| **Correccion aplicada** | `bat` -> `sh` en las tres etapas. Commit `e2c2de1`; lo confirma el build **#3**. |

```groovy
// ANTES
                    bat 'mvn -B clean compile'
// DESPUES
                    sh 'mvn -B clean compile'
```

---

## Defecto 3 — la clase de pruebas que Surefire no corre

| Campo | Contenido |
|-------|-----------|
| **Sintoma observado** | Build **#3**, `SUCCESS`. Tres etapas verdes y `.jar` archivado, pero **la pagina del build no tiene enlace Test Result** y en la etapa Pruebas **no aparece ninguna linea `Tests run:`**. Maven no protesta. |
| **¿Se reproduce en local?** | **Si.** `mvn -B test` en local tampoco corre ninguna prueba y tampoco se queja. |
| **Capa** | **Proyecto** (estructura de nombres), no del pipeline. |
| **Indicador o regla violada** | "Pruebas ejecutadas: 13" (se ejecutaron **0**) y "Informe publicado" (no hay informe). |
| **Archivo y linea** | `reto-ci/src/test/java/bo/edu/usfx/descuentos/CalculadoraDescuentosPrueba.java` — el nombre de la clase. |
| **Correccion aplicada** | Renombrar la clase a `CalculadoraDescuentosTest`. Commit `822dbc2`; lo confirma el build **#4**. |

Surefire solo ejecuta las clases cuyo nombre sigue ciertos patrones
(`Test*`, `*Test`, `*Tests`, `*TestCase`, Anexo C de la guia). Se eligio renombrar
la clase y no cambiar la configuracion de Surefire en el `pom.xml`, porque
renombrar no obliga a configurar nada.

---

## Defecto 4 — el pipeline que ignora los fallos y no publica el informe

| Campo | Contenido |
|-------|-----------|
| **Sintoma observado** | Build **#4**, `SUCCESS` otra vez. Consola de la etapa Pruebas: `Tests run: 13, Failures: 1, Errors: 1, Skipped: 0` y despues **`BUILD SUCCESS`**. Sin Test Result. |
| **¿Se reproduce en local?** | **Si.** `mvn -B test -Dmaven.test.failure.ignore=true` en local corre las 13 pruebas, 2 fallan, y **el comando sale con codigo 0**. |
| **Capa** | **Jenkinsfile**, dos cambios en la misma etapa. |
| **Indicador o regla violada** | "Pruebas fallidas: 0" (el build se declara sano sin haber mirado el informe) y "Informe publicado" (no hay Test Result). |
| **Archivo y linea** | `reto-ci/Jenkinsfile:33` — el flag `-Dmaven.test.failure.ignore=true`, y la ausencia del paso `junit` en la etapa Pruebas. |
| **Correccion aplicada** | Ver abajo. Commit `d8a16de`; lo confirma el build **#5**. |

```groovy
// ANTES
        stage('Pruebas') {
            steps {
                dir('reto-ci') {
                    bat 'mvn -B test -Dmaven.test.failure.ignore=true'
                }
            }
        }
// DESPUES
        stage('Pruebas') {
            steps {
                dir('reto-ci') {
                    sh 'mvn -B test'
                }
            }
            post {
                always {
                    // Publica el informe de JUnit aunque haya pruebas fallidas
                    junit 'reto-ci/target/surefire-reports/*.xml'
                }
            }
        }
```

El paso `junit` va en `post { always { } }` y no en `steps`, para que el
informe se publique tambien cuando las pruebas fallan. Es exactamente lo que
hacia el pipeline de `factorial-app`.

---

## Defecto 5 — el codigo rechaza un descuento del 100 %

| Campo | Contenido |
|-------|-----------|
| **Sintoma observado** | Build **#5**, `FAILURE` **con Test Result**. Dos de las 13 pruebas en rojo: `descuentoTotalDejaElPrecioEnCero` y `aplicaElDescuentoYRedondeaADosDecimales[4]`. `Empaquetar` ya no se ejecuta. Error: `IllegalArgumentException: El porcentaje de descuento debe estar entre 0 y 100` |
| **¿Se reproduce en local?** | **Si.** `mvn -B test` en local da las mismas 2 pruebas en rojo. |
| **Capa** | **Codigo**. |
| **Indicador o regla violada** | Regla de negocio **"Descuento 100: el precio final es 0"**, y "Pruebas fallidas: 0". |
| **Archivo y linea** | `reto-ci/src/main/java/bo/edu/usfx/descuentos/CalculadoraDescuentos.java:16` |
| **Correccion aplicada** | Ver abajo. Commit `392ef32`; lo confirma el build **#6**. |

```java
// ANTES
        if (porcentajeDescuento < 0 || porcentajeDescuento >= 100) {
// DESPUES
        if (porcentajeDescuento < 0 || porcentajeDescuento > 100) {
```

La regla dice "menor que 0 o mayor que 100", o sea que el **100 es valido**. El
`>=` lo rechazaba.

---

## Defecto 6 — truncar en vez de redondear

| Campo | Contenido |
|-------|-----------|
| **Sintoma observado** | Build **#6**, `FAILURE`. En Test Result queda **una sola** de las dos pruebas anteriores: `aplicaElDescuentoYRedondeaADosDecimales[4]`, con `expected: <30.0> but was: <29.99>`. `descuentoTotalDejaElPrecioEnCero` ya pasa. |
| **¿Se reproduce en local?** | **Si.** `mvn -B test` en local deja 1 prueba en rojo, la del redondeo. |
| **Capa** | **Codigo**. |
| **Indicador o regla violada** | Regla de negocio **"Redondeo a dos decimales, la mitad hacia arriba: 29,997 -> 30,00"**, y "Pruebas fallidas: 0". |
| **Archivo y linea** | `reto-ci/src/main/java/bo/edu/usfx/descuentos/CalculadoraDescuentos.java:27` |
| **Correccion aplicada** | Ver abajo. Commit `68d7439`; lo confirma el build **#7**. |

```java
// ANTES
    private double redondear(double valor) {
        return (int) (valor * 100) / 100.0;
    }
// DESPUES
    private double redondear(double valor) {
        return Math.round(valor * 100.0) / 100.0;
    }
```

El cast a `int` **trunca**: 29,997 se convierte en 29. `Math.round` redondea
hacia arriba, que es lo que pide la regla.

---

## Verde no es sano: los builds #3 y #4

Estos dos son **verdes** y **no sirvieron para nada**. Si el equipo anterior
miraba solo el color, tenia razon en decir "el pipeline esta en verde".
Incumplen **dos** indicadores cada uno:

| Build | Cuantas pruebas corrieron | Fallos reales | Que se veia |
|-------|--------------------------|---------------|-------------|
| #3 | **0** | — (no se midio nada) | verde, `.jar` archivado, sin Test Result |
| #4 | 13 | **2** | verde, `BUILD SUCCESS`, sin Test Result |

Un pipeline que no ejecuta pruebas, o que ignora las que fallan, es peor que no
tener pipeline: da una falsa seguridad. El **build #5** es el primero que dice
la verdad, y para entonces los dos fallos de codigo ya son visibles.

---

## Build sano final

El **build #7** es el primero que cumple las siete filas. El **#8** y los
siguientes tambien, porque no se toco ni el pipeline ni el codigo.

| Indicador | Estado | Donde se comprueba |
|-----------|--------|--------------------|
| Resultado | `SUCCESS` | Stage View del build |
| Pruebas ejecutadas | **13** (las mismas que `mvn -B test` en local) | Test Result; `Tests run: 13` |
| Pruebas fallidas | **0** fallos, 0 errores | Test Result |
| Informe publicado | si, con Test Result Trend creciente | Pagina del job |
| Artefacto | `calculadora-descuentos-1.0-SNAPSHOT.jar` (3.088 bytes) | Build Artifacts |
| Disparo automatico | `Started by an SCM change` | Pagina del build |
| Todo en el Jenkinsfile | si: `CpsScmFlowDefinition`, sin script pegado | Configuracion del job |

**No se cambio ni una prueba.** El reto se resolvio corrigiendo el pipeline y el
codigo, no ajustando las pruebas para que pasaran.

---

## Correccion adicional (fuera del reto)

Commit `302cec1`, build **#8**: agrega el bloque `unstable` al `post` global
del `Jenkinsfile`, que el material original no tenia y que `factorial-app` si
tiene.

No es uno de los seis defectos sembrados, y conviene decir por que **no** cambia
el color de un build con pruebas fallidas: el build se pone `UNSTABLE` por el
**paso `junit`** al leer un informe con fallos, no por ese bloque. Como
`mvn -B test` falla, el pipeline muere antes y el build queda en `FAILURE`
(rojo). El bloque solo agrega un `echo` que, en la practica, no se ejecuta.

Se dejo puesto para que los dos `Jenkinsfile` sean simetricos, pero el informe
del reto cuenta **seis** defectos, no siete.

---

## Nota sobre la rotacion de logs

Jenkins conserva por defecto **los 10 ultimos builds**. Al llegar al build #15,
los builds **#1 a #5 de `reto-ci` se borraron** de Jenkins: ya no estan en el
historial, ni su `Console Output`, ni sus Test Result.

Lo que quedo:

- El **historial de builds completo** sigue en el repositorio, en
  `evidencias/historial-builds.md`, con la linea de consola de cada sintoma
  capturada en el momento en que ocurrio.
- El **codigo de cada version** sigue en el historial de git, que es la
  evidencia mas solida: cada defecto se puede volver a ver con
  `git show <commit>:<archivo>`.
- Los builds **#6 en adelante** siguen intactos, incluido el #6 con sus 13
  pruebas y la del redondeo en rojo.

Como medida de prevencion se activo **"Conservar registros de compilacion
para siempre"** (`LogRotator` con `-1`) en los dos jobs, `factorial-app` y
`reto-ci`. No cambia el resultado de ningun build: solo evita que se repita la
perdida.
