# reto-ci: defectos encontrados y como se corrigieron

Un commit por defecto. El commit inicial del repositorio contiene los 7
defectos del material; cada commit posterior arregla exactamente uno.

## Historial de builds

| Build | Commit    | Resultado | Que paso                                              |
|-------|-----------|-----------|-------------------------------------------------------|
| #1    | inicial   | FAILURE   | `Tool type "maven" does not have an install of "Maven 3.8.5" configured` |
| #2    | fix 1     | FAILURE   | `java.io.IOException: Batch scripts can only be run on Windows nodes` |
| #3    | fix 2     | SUCCESS   | **verde falso**: 0 pruebas, sin informe, `.jar` archivado igual |
| #4    | fix 3     | SUCCESS   | **verde falso**: 13 pruebas, 2 fallan, ignoradas, sin informe |
| #5    | fix 4     | FAILURE   | rojo real: 11 pasan, 2 fallan, con Test Result publicado |
| #6    | fix 5     | FAILURE   | 12 pasan, 1 falla (el redondeo)                       |
| #7    | fix 6     | SUCCESS   | verde sano: 13/13, con informe y artefacto             |
| #8    | fix 7     | SUCCESS   | verde sano, con el bloque `unstable` agregado         |

## Los 7 defectos

| # | Capa responsable       | Defecto                                                                 | Se revela en | Commit que lo corrige |
|---|------------------------|------------------------------------------------------------------------|--------------|-----------------------|
| 1 | Configuracion de Jenkins | `tools` pide `maven 'Maven 3.8.5'`, y en Manage Jenkins -> Tools solo existe `Maven-3.9` | build #1 | fix 1 |
| 2 | Jenkinsfile            | Usa `bat` en vez de `sh`                                              | build #2 | fix 2 |
| 3 | Proyecto               | La clase se llama `CalculadoraDescuentosPrueba`; Surefire solo corre `Test*`, `*Test`, `*Tests`, `*TestCase` | build #3 | fix 3 |
| 4 | Jenkinsfile            | `-Dmaven.test.failure.ignore=true` y falta el paso `junit`           | build #4 | fix 4 |
| 5 | Codigo                 | `porcentajeDescuento >= 100` rechaza el 100, que la especificacion da por valido | build #5 | fix 5 |
| 6 | Codigo                 | `redondear` hace `(int)(valor*100)/100.0`: **trunca** en vez de redondear | build #6 | fix 6 |
| 7 | Jenkinsfile            | Falta el bloque `post { unstable { ... } }` que si tiene factorial-app | revision | fix 7 |

## Verde no es sano

Los builds #3 y #4 son **verdes** y **no sirven para nada**:

- **#3** ejecuto 0 pruebas. La causa no estaba en el pipeline: Surefire
  encuentro el `.java` de las pruebas, lo compilo, y no ejecuto nada porque
  `CalculadoraDescuentosPrueba` no matchea los patrones de nombre por
  defecto. El `.jar` se archivo igual y el build quedo en verde.
- **#4** ejecuto 13 pruebas, 2 fallaron, y el build quedo verde igual, porque
  `-Dmaven.test.failure.ignore=true` le dice a Maven que el resultado no
  importa. Sin el paso `junit` tampoco habia Test Result: nadie podia ver que
  dos pruebas estaban en rojo.

Un pipeline que no ejecuta pruebas, o que ignora las que fallan, es peor que
no tener pipeline: da una falsa seguridad. El build #5 es el primero que dice
la verdad.

## Los dos bugs de codigo

Estaban ocultos por el defecto 4. Con el pipeline corregido, las pruebas
dicen exactamente que pasa:

```
CalculadoraDescuentosTest.descuentoTotalDejaElPrecioEnCero » IllegalArgumentException
    El porcentaje de descuento debe estar entre 0 y 100
CalculadoraDescuentosTest.aplicaElDescuentoYRedondeaADosDecimales:[4]
    expected: <30.0> but was: <29.99>
```

Correcciones:

```java
// el 100 es valido: el precio final es 0
if (porcentajeDescuento < 0 || porcentajeDescuento > 100) { ... }

// redondeo a dos decimales, medio hacia arriba
private double redondear(double valor) {
    return Math.round(valor * 100.0) / 100.0;
}
```

## Build sano final (#8)

| Indicador          | Estado                                      |
|--------------------|---------------------------------------------|
| Resultado          | SUCCESS (verde)                             |
| Pruebas ejecutadas | 13 (las mismas que `mvn test` en local)     |
| Pruebas fallidas   | 0 fallos, 0 errores                         |
| Informe publicado  | Test Result del build + Test Result Trend   |
| Artefacto          | `calculadora-descuentos-1.0-SNAPSHOT.jar`   |
| Disparo automatico | `Started by an SCM change`                  |
| Reproducibilidad   | el pipeline esta entero en el Jenkinsfile   |
