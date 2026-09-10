# Diagramas del proyecto

Esta carpeta debe contener los diagramas académicos del proyecto FixFlow.

## Archivos esperados

| Archivo | Contenido |
| --- | --- |
| `DER.png` | Diagrama Entidad-Relación de la base de datos. |
| `diagrama-clases.png` | Diagrama de clases del backend (entidades, servicios, controladores, seguridad). |
| `casos-uso.png` | Diagrama de casos de uso por rol (ADMIN, TECNICO, CLIENTE). |

## Cómo generarlos

Se recomienda usar [draw.io](https://app.diagrams.net/) o cualquier herramienta de modelado.

- El **DER** se deriva directamente de `database/fixflow.sql` (tablas, PK, FK, UNIQUE y ENUM).
- El **diagrama de clases** refleja las entidades JPA de `backend/src/main/java/com/fixflow/model` y las relaciones definidas en ellas.
- El **diagrama de casos de uso** cubre: login, gestión de usuarios/equipos/solicitudes, asignación de técnicos, diagnóstico, reparación y seguimiento de estados.

Una vez generados, colocar los PNG en esta carpeta y actualizar el README principal si se desea referenciarlos.