# reto-ci: defectos encontrados y como se corrigieron

Un commit por defecto. El commit inicial del repositorio contiene los 7
defectos del material; cada commit posterior arregla exactamente uno.

## Resumen

| # | Capa responsable   | Defecto                                                                 | Como se manifiesta                                              | Commit  | Build |
|---|--------------------|------------------------------------------------------------------------|-----------------------------------------------------------------|---------|-------|
| 1 | Configuracion de Jenkins | El bloque `tools` pide `maven 'Maven 3.8.5'`, y en Manage Jenkins -> Tools solo existe `Maven-3.9` | El build muere antes de la etapa Compilar: `No such tool 'Maven 3.8.5'` | `fix 1` | #2    |
| 2 | Jenkinsfile        | Usa `bat` en vez de `sh`                                              | El agente es Linux, `bat` no existe: `bat: command not found`   | `fix 2` | #3    |
| 3 | Proyecto           | La clase de pruebas se llama `CalculadoraDescuentosPrueba`, y Surefire solo corre `*Test`, `Test*`, `*Tests`, `*TestCase` | Build verde con **0 pruebas**, sin Test Result, con el `.jar` archivado igual | `fix 3` | #4    |
| 4 | Jenkinsfile        | `-Dmaven.test.failure.ignore=true` y no hay paso `junit`             | 13 pruebas correr, 2 fallan, y el build sigue **verde y sin informe** | `fix 4` | #5    |
| 5 | Codigo             | `porcentajeDescuento >= 100` rechaza el 100, que la especificacion dice que es valido (precio final 0) | `descuentoTotalDejaElPrecioEnCero` lanza `IllegalArgumentException` | `fix 5` | #6    |
| 6 | Codigo             | `redondear` hace `(int)(valor*100)/100.0`, es decir **trunca** en vez de redondear | `33.33 con 10%` da `29.99` en vez de `30.00`                   | `fix 6` | #7    |
| 7 | Jenkinsfile        | Falta el bloque `post { unstable { ... } }` que si tiene factorial-app | El build amarillo (pruebas fallidas) no explica por que     | `fix 7` | #8    |

## El detalle que importa: verde no es sano

Los builds #3 y #4 son **verdes** y **no sirven para nada**:

- #3 ejecuto 0 pruebas. La causa no es el pipeline: es que Surefire no
  encontro la clase, porque `CalculadoraDescuentosPrueba` no matchea los
  patrones de nombre por defecto de Surefire.
- #4 ejecuto 13 pruebas, 2 fallaron, y el build quedo verde igual, porque
  `-Dmaven.test.failure.ignore=true` le dice a Maven que no importen los
  fallos. Sin el paso `junit` tampoco habia informe: nadie podia ver que
  dos pruebas estaban en rojo.

Un pipeline que no ejecuta pruebas, o que ignora las que fallan, es peor que
no tener pipeline: da una falsa seguridad. El build #5, en el que se quito
`-Dmaven.test.failure.ignore` y se publico el informe JUnit, es el primer
build que dice la verdad.

## Los dos bugs de codigo

Ambos estaban ocultos por el defecto 4. Con el pipeline corregido, las pruebas
dicen exactamente que pasa:

```
CalculadoraDescuentosTest.descuentoTotalDejaElPrecioEnCero  » IllegalArgumentException
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

| Indicador           | Estado                                        |
|---------------------|-----------------------------------------------|
| Resultado           | SUCCESS (verde)                                |
| Pruebas ejecutadas  | 13 (las mismas que `mvn test` en local)        |
| Pruebas fallidas    | 0 fallos, 0 errores                            |
| Informe publicado   | Test Result del build + Test Result Trend      |
| Artefacto           | `calculadora-descuentos-1.0-SNAPSHOT.jar`     |
| Disparo automatico  | `Started by SCM change`                        |
| Reproducibilidad    | el pipeline esta entero en el Jenkinsfile      |
