# AldeanoNetherite

Plugin para Paper 1.21.11. Un aldeano especial da misiones y, al completarlas,
ofrece intercambiar equipo de diamante por netherite.

## Cómo funciona

1. Un admin coloca al aldeano con `/aldeano set`.
2. El jugador hace clic derecho en el aldeano y recibe 1 misión (50 en total:
   15 media-alta, 25 alta, 10 muy alta). El progreso se ve en una bossbar.
3. Al completarla, el aldeano muestra 2 intercambios:
   - **Base:** pieza de diamante + 2 a 8 lingotes de netherite → pieza de netherite.
   - **Mejorado:** lo mismo + 1 plantilla de mejora de netherite → pieza de netherite
     con encantamientos de nivel 4 a 6.
4. Tras intercambiar, el aldeano da una misión nueva.

## Compilar en GitHub

1. Crea un repositorio y sube todo el contenido de esta carpeta.
2. Entra a la pestaña **Actions**: se compila solo con cada subida.
3. Abre la ejecución terminada y descarga `AldeanoNetherite-jar`.
4. Copia el `.jar` a la carpeta `plugins` del servidor y reinicia.

## Comandos

| Comando | Permiso | Qué hace |
|---|---|---|
| `/aldeano set` | `aldeanonetherite.admin` | Coloca el aldeano donde estás |
| `/aldeano remove` | `aldeanonetherite.admin` | Quita el aldeano |
| `/aldeano reset <jugador>` | `aldeanonetherite.admin` | Reinicia el progreso |
| `/aldeano reload` | `aldeanonetherite.admin` | Recarga `config.yml` |
| `/aldeano mision` | todos | Ver misión actual |
| `/aldeano abandonar` | todos | Cambiar de misión |

## Configuración

Edita `plugins/AldeanoNetherite/config.yml` para cambiar el nombre del aldeano,
el rango de lingotes (2 a 8) y los niveles de encantamiento (4 a 6).
