# Ejercicio 4: medicion de la latencia del polling

Trigger activo durante la medicion: `pollSCM('H/2 * * * *')`, o sea que el
build arranca en el proximo multiple de 2 minutos, con un desfase fijo por
job. Cinco commits separados, medidos contra la hora real de push.

## Tabla 1 — polling

| # | Commit | Hora del push (UTC) | Build | Inicio del build (UTC) | Latencia |
|---|--------|---------------------|-------|------------------------|----------|
| 1/5 | `b287815` | 06:37:58 | #24 | 06:38:45 | **47 s** |
| 2/5 | `54c1f36` | 06:39:14 | #25 | 06:40:44 | **90 s** |
| 3/5 | `c765be4` | 06:41:11 | #26 | 06:42:44 | **93 s** |
| 4/5 | `cd02d7b` | 06:43:09 | #27 | 06:44:47 | **98 s** |
| 5/5 | `aa487c1` | 06:45:13 | #28 | 06:46:44 | **91 s** |

**Promedio: 84 s. Minimo 47 s, maximo 98 s.** Ninguno baja de 0, que es lo
correcto: con un trigger de 2 minutos el piso teorico es 0 s y el techo son
120 s.

El patron se explica solo. Los cuatro ultimos builds arrancaron entre 90 y 98
s despues del push, y ese retardo no es del build: es la espera hasta el
proximo multiple de 2 minutos. El caso de 47 s (build #24) fue el unico que
llego justo despues de que pasara el ciclo, asi que solo tuvo que esperar el
resto.

Un detalle que confirma la teoria: los cinco pushes ocurrieron a menos de un
minuto de distancia entre si (06:37, 06:39, 06:41, 06:43, 06:45) y aun asi
cada uno produjo su propio build. El polling agrupa lo que encuentra, pero como
cada commit llego en un ciclo distinto, ninguno se perdio.

## Tabla 2 — webhook

Pendiente de completar. Requiere:

1. Exponer Jenkins con un tunel. **Hecho**: `https://silent-ducks-march.loca.lt`
   (localtunnel), verificado con `POST /github-webhook/`, que responde 400 de
   Jenkins, o sea que el tunel llega.
2. Registrar el webhook en GitHub con payload
   `https://silent-ducks-march.loca.lt/github-webhook/`, content type
   `application/json`, evento `push`.
3. Configurar el "GitHub server" en Jenkins (Manage Jenkins -> System ->
   GitHub Servers) con las credenciales, para que `githubPush()` sepa que
   repositorio y que hook vigilar.
4. Cambiar el trigger del Jenkinsfile de `pollSCM(...)` a `githubPush()`.

Los pasos 2 y 3 necesitan un **personal access token** de GitHub con alcance
`admin:repo_hook`. Son el unico punto del Ejercicio 4 que no puedo hacer sin
credenciales.
