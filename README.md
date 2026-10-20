# FixFlow

FixFlow es un sistema web para la gestión de servicios técnicos y reparación de equipos informáticos. El proyecto está orientado a pequeñas empresas de soporte técnico y permite administrar usuarios, clientes, técnicos, equipos, solicitudes, diagnósticos, reparaciones, asignaciones y el flujo de atención de cada caso.

## Tecnologías

- Java 17
- Spring Boot 3.3.x
- Maven
- Spring Web
- Spring Data JPA
- Hibernate
- Spring Security
- JWT
- MySQL 8+
- HTML5
- CSS3
- JavaScript ES6+
- Fetch API

## Arquitectura

El backend sigue una arquitectura de tres capas:

- Controller
- Service
- Repository
- MySQL

La estructura principal está en:

- backend/src/main/java/com/fixflow
- frontend/
- database/

## Requisitos

- Java 17+
- Maven 3.9+
- MySQL 8+
- Visual Studio Code
- Git
- Navegador web

## Ejecución rápida con XAMPP

El proyecto está preparado para desplegarse sobre XAMPP (Apache + MariaDB/MySQL).
El frontend se sirve con Apache y el backend Spring Boot se ejecuta con Java.

1. Copiar el proyecto a `/opt/lampp/htdocs/fixflow`.
2. Arrancar todo (Apache + MySQL + importa la base + backend):

   ```bash
   ./scripts/start-all.sh --db
   ```

   O por pasos:

   ```bash
   ./scripts/import-db.sh     # importa database/fixflow.sql
   ./scripts/start-backend.sh # compila (si hace falta) y arranca el backend
   ```

3. Abrir en el navegador:

   - Frontend: <http://localhost/fixflow/frontend/login.html>
   - Raíz del proyecto: <http://localhost/fixflow/> (redirige al login)
   - API REST: <http://localhost:8080/api>

Para detener el backend:

```bash
./scripts/stop-backend.sh
```

> El backend debe iniciarse después de que MySQL de XAMPP esté en ejecución.

## Instalación

1. Clonar el repositorio.
2. Crear la base de datos MySQL: `fixflow`.
3. Configurar variables de entorno:
   - DB_HOST
   - DB_PORT
   - DB_NAME
   - DB_USERNAME
   - DB_PASSWORD
   - JWT_SECRET
4. Ejecutar el backend con Maven.
5. Abrir los archivos HTML del frontend en un navegador o servirlos con un servidor local.

## Configuración de MySQL

Ejemplo de configuración:

```properties
DB_HOST=127.0.0.1
DB_PORT=3306
DB_NAME=fixflow
DB_USERNAME=root
DB_PASSWORD=
```

XAMPP deja MySQL/MariaDB en el puerto 3306 por defecto, por lo que el proyecto usa 3306. Si otra instalación local de MySQL ocupara el 3306, se puede cambiar [a otro puerto en XAMPP] y ajustar DB_PORT para que coincida.

La base de datos se configura en:

- backend/src/main/resources/application.properties

## Configuración del backend

```bash
cd backend
mvn spring-boot:run
```

## Ejecución del frontend

Se puede abrir directamente `frontend/login.html` o servir el directorio con un servidor sencillo:

```bash
cd frontend
python -m http.server 5500
```

Luego abrir:

- http://localhost:5500/login.html

Un usuario sin cuenta puede crearse una desde el propio login (enlace "Crear cuenta" → `register.html`), sin intervención del administrador.

## Usuarios de prueba

El script `database/fixflow.sql` incluye estos usuarios de prueba. Las contraseñas se almacenan con BCrypt.

| Nombre | Rol | Email | Contraseña | Estado |
| --- | --- | --- | --- | --- |
| Admin FixFlow | ADMIN | admin@fixflow.com | admin123 | ACTIVO |
| Laura Martínez | ADMIN | admin2@fixflow.com | admin123 | ACTIVO |
| Técnico Soporte | TECNICO | tecnico@fixflow.com | tecnico123 | ACTIVO |
| Carlos Ramírez | TECNICO | carlos.tecnico@fixflow.com | tecnico123 | ACTIVO |
| María Gómez | TECNICO | maria.tecnico@fixflow.com | tecnico123 | ACTIVO |
| Andrés Torres | TECNICO | andres.tecnico@fixflow.com | tecnico123 | INACTIVO |
| Cliente Demo | CLIENTE | cliente@fixflow.com | cliente123 | ACTIVO |
| Sofía Herrera | CLIENTE | sofia@fixflow.com | cliente123 | ACTIVO |
| Diego Castro | CLIENTE | diego@fixflow.com | cliente123 | ACTIVO |
| Valentina Ortiz | CLIENTE | valentina@fixflow.com | cliente123 | ACTIVO |
| Ricardo Mendoza | CLIENTE | ricardo@fixflow.com | cliente123 | ACTIVO |
| Camila Rojas | CLIENTE | camila@fixflow.com | cliente123 | INACTIVO |

Notas:

- **Administradores** (2): acceso total de gestión y visualización (no ejecutan tareas técnicas).
- **Técnicos** (4): `andres.tecnico@fixflow.com` está **INACTIVO** (útil para probar que no aparece
  en el selector de asignación). Los demás pueden recibir solicitudes.
- **Clientes** (6): `camila@fixflow.com` está **INACTIVO** (no puede iniciar sesión). Cada cliente
  tiene sus propios equipos y solicitudes.

### Credenciales por rol (resumen rápido)

```
ADMIN   : admin@fixflow.com          / admin123
ADMIN   : admin2@fixflow.com         / admin123
TECNICO : tecnico@fixflow.com        / tecnico123
TECNICO : carlos.tecnico@fixflow.com / tecnico123
TECNICO : maria.tecnico@fixflow.com  / tecnico123
TECNICO : andres.tecnico@fixflow.com / tecnico123   (INACTIVO)
CLIENTE : cliente@fixflow.com        / cliente123
CLIENTE : sofia@fixflow.com          / cliente123
CLIENTE : diego@fixflow.com          / cliente123
CLIENTE : valentina@fixflow.com      / cliente123
CLIENTE : ricardo@fixflow.com        / cliente123
CLIENTE : camila@fixflow.com         / cliente123   (INACTIVO)
```

Si la base de datos ya existía antes de importar esta versión del script, vuelve a ejecutar
`./scripts/import-db.sh` (o importa `database/fixflow.sql`) para cargar todos estos usuarios.

## Permisos por rol (separación estricta de funciones)

Cada rol accede únicamente a sus funciones. El **ADMIN administra y visualiza**, pero
**no ejecuta tareas técnicas** (no diagnostica ni repara).

| Función | ADMIN | TECNICO | CLIENTE |
| --- | :---: | :---: | :---: |
| Iniciar sesión | ✔ | ✔ | ✔ |
| Gestión de usuarios (crear/editar/desactivar/eliminar) | ✔ | ✘ | ✘ |
| Gestión de clientes y técnicos | ✔ | ✘ | ✘ |
| Ver equipos / solicitudes / diagnósticos / reparaciones | ✔ (lectura) | ✔ (los suyos) | ✔ (los suyos) |
| Ver detalle de una solicitud (diagnóstico y reparación) | ✔ | ✔ (asignadas) | ✔ (las suyas) |
| Crear / editar equipos | ✔ | ✘ | ✔ (los suyos) |
| Eliminar equipos | ✔ | ✘ | ✔ (los suyos, sin solicitudes) |
| Crear / editar solicitudes | ✔ | ✘ | ✔ (las suyos) |
| Asignar técnico (`ASIGNADA`, US-13) | ✔ | ✘ | ✘ |
| Cambiar estado del servicio (US-12) | ✘ | ✔ (asignadas) | ✘ |
| Registrar diagnóstico (US-14) | ✘ | ✔ (asignadas) | ✘ |
| Registrar reparación (US-15) | ✘ | ✔ (asignadas) | ✘ |

Reglas clave:

- El **CLIENTE no tiene sección "Clientes"**: no ve el listado ni la gestión de clientes
  (esa sección es exclusiva del ADMIN). El enlace no existe en el HTML: se **agrega en tiempo de
  ejecución solo cuando la sesión es ADMIN** (`js/auth.js`), y además `frontend/.htaccess` manda
  `Cache-Control: no-cache` en HTML/JS para que el navegador no muestre menús antiguos.
- El `ADMIN` solo obtiene `200` en `GET` de diagnósticos/reparaciones; cualquier `POST/PUT/DELETE`
  sobre ellos responde `403 FORBIDDEN`.
- El `ADMIN` no puede cambiar estados por `PATCH /api/solicitudes/{id}/estado` (`403`); el estado
  `ASIGNADA` solo se produce al asignar un técnico activo.
- El `TECNICO` solo ve las solicitudes que le fueron asignadas y no puede gestionar usuarios,
  clientes, técnicos ni equipos.
- El `CLIENTE` solo ve su propia información; no accede a datos de otros clientes.

### Gestión de usuarios (3 acciones)

En *Usuarios* (solo ADMIN) cada fila tiene **tres botones**:

- **Editar**: modifica los datos del usuario (y su perfil de técnico si aplica).
- **Desactivar** (`PATCH /api/usuarios/{id}/desactivar`): deja el usuario en `INACTIVO` pero lo
  **conserva** en la base de datos (no puede iniciar sesión). Se deshabilita si ya está inactivo.
- **Eliminar** (`DELETE /api/usuarios/{id}`): **borra definitivamente** el usuario de la base de
  datos junto con sus dependencias (perfil de cliente/técnico, equipos, solicitudes y asignaciones),
  respetando el orden de las claves foráneas. Pide confirmación porque no se puede deshacer.

Al seleccionar el rol **Técnico** en el formulario aparece el recuadro **"Datos del técnico"** con
los campos **Especialidad** y **Teléfono**, que son **obligatorios**: se muestran en la tabla del
modal *Asignar técnico* de cada solicitud. El backend rechaza (`400`) la creación de un técnico
sin especialidad o sin teléfono, por lo que ya no se generan valores por defecto como
`General` / `000000000`.


> El usuario base `admin@fixflow.com` no debería eliminarse desde la interfaz para no perder el
> acceso de administrador principal; se recomienda desactivar en su lugar.

### Datos de demostración

El script `database/fixflow.sql` carga un escenario con **movimiento de ~6 meses**:

- **2 administradores**, **4 técnicos** (uno inactivo) y **6 clientes** (uno inactivo).
- **10 equipos** y **28 solicitudes** repartidas por todos los estados
  (`PENDIENTE`, `ASIGNADA`, `EN_DIAGNOSTICO`, `EN_REPARACION`, `FINALIZADA`, `CANCELADA`).
- **20 diagnósticos** y **18 reparaciones** con fechas históricas.

Credenciales (todas con BCrypt): `admin@fixflow.com`/`admin123`, `admin2@fixflow.com`/`admin123`,
`tecnico@fixflow.com`/`tecnico123` (y `carlos`/`maria`.tecnico), `cliente@fixflow.com`/`cliente123`
(y `sofia`/`diego`/`valentina`/`ricardo`). El script es idempotente y usa variables para mapear los
IDs reales por email, de modo que se puede reimportar sin romper las relaciones.

### Asignación de técnico (interfaz)

En *Solicitudes*, el ADMIN pulsa **Asignar técnico** y se abre una ventana con una **tabla de
técnicos activos** (nombre, especialidad y teléfono). Al pulsar **Seleccionar** en una fila, la
solicitud pasa a `ASIGNADA` y queda asociada a ese técnico. No se usa una lista desplegable simple:
se elige visualmente de la tabla.

> La tabla lista los **perfiles de técnico** (tabla `tecnico`). Al crear o editar un usuario con
> rol `TECNICO` en *Usuarios*, aparecen los campos **Especialidad** y **Teléfono** y el sistema
> crea/actualiza automáticamente su perfil, por lo que el técnico nuevo queda disponible de
> inmediato para asignarle solicitudes. Crear un técnico como usuario sin perfil (datos antiguos)
> se repara con el `INSERT ... SELECT` documentado en los scripts de base de datos.


### Detalle de la solicitud (diagnóstico y reparación)

El endpoint `GET /api/solicitudes/{id}/detalle` devuelve, en un solo JSON, la solicitud con su
cliente, equipo, técnico asignado, diagnóstico y reparación. Es accesible para ADMIN, TÉCNICO
(solo las suyas) y CLIENTE (solo las suyas), lo que permite que en la interfaz el cliente o el
admin puedan pulsar **Ver detalle** y ver el diagnóstico y la solución del técnico (útil en la
fase final, `FINALIZADA`).


## Flujo funcional (guía de pruebas end-to-end)

Flujo principal del servicio:

```
Cliente crea solicitud (PENDIENTE)
      │  US-10
      ▼
Admin asigna técnico activo  ─►  ASIGNADA   (US-13)
      │
      ▼
Técnico registra diagnóstico ─►  EN_DIAGNOSTICO   (US-14)
      │
      ▼
Técnico registra reparación  ─►  EN_REPARACION    (US-15)
      │
      ▼
Técnico finaliza             ─►  FINALIZADA       (US-12)
      (en cualquier punto no terminal puede pasar a CANCELADA)
```

Prueba recomendada en el navegador:

1. **Cliente** (`cliente@fixflow.com` / `cliente123`): en *Equipos* registra uno (US-06);
   en *Solicitudes* crea una con prioridad y descripción → queda `PENDIENTE` (US-10).
   Desde *Dashboard* ve el estado de sus servicios.
2. **Admin** (`admin@fixflow.com` / `admin123`): en *Solicitudes* pulsa **Asignar técnico**
   → se abre una tabla con los técnicos y sus especialidades; elige uno → la solicitud pasa a
   `ASIGNADA` (US-13). Verifica que en *Diagnósticos* y *Reparaciones* **no aparece el formulario**
   (solo lectura). Cuando la solicitud esté atendida, pulsa **Ver detalle** para ver el
   diagnóstico y la reparación del técnico.
3. **Técnico** (`tecnico@fixflow.com` / `tecnico123`): en *Solicitudes* solo ve las asignadas;
   con los botones avanza a `EN_DIAGNOSTICO` (o registra el diagnóstico en *Diagnósticos*),
   luego registra la reparación en *Reparaciones* y finaliza (US-12/14/15).
4. **Cliente**: vuelve a *Solicitudes* y pulsa **Ver detalle** en su solicitud para ver el
   diagnóstico y la solución del técnico (especialmente cuando está `FINALIZADA`) (US-11/US-16).
   Comprueba además que **no existe el enlace "Clientes"** en el menú.

Pruebas negativas de roles (deben devolver `403`):

- Admin registrando un diagnóstico o reparación.
- Admin cambiando el estado con `PATCH /api/solicitudes/{id}/estado`.
- Técnico o cliente gestionando usuarios (`/api/usuarios`).
- Cliente registrando diagnósticos o accediendo a datos de otro cliente.


## Endpoints

### Autenticación

- POST /api/auth/login
- POST /api/auth/registro — crea una cuenta de cliente (crea el `Usuario` con rol `CLIENTE` y su perfil de `Cliente` asociado). Cuerpo: `nombre`, `apellido`, `email`, `password` (mínimo 6 caracteres), `telefono`, `direccion`.

Un nuevo usuario puede registrarse desde la pantalla de login (`frontend/login.html`), que enlaza con `frontend/register.html`. Tras el registro la sesión se inicia automáticamente.

### Usuarios

- GET /api/usuarios
- GET /api/usuarios/{id}
- POST /api/usuarios
- PUT /api/usuarios/{id}
- PATCH /api/usuarios/{id}/desactivar
- DELETE /api/usuarios/{id}

### Clientes

- GET /api/clientes
- GET /api/clientes/{id}
- POST /api/clientes
- PUT /api/clientes/{id}
- DELETE /api/clientes/{id}

### Técnicos

- GET /api/tecnicos
- GET /api/tecnicos/{id}
- POST /api/tecnicos
- PUT /api/tecnicos/{id}
- DELETE /api/tecnicos/{id}

### Equipos

- GET /api/equipos
- GET /api/equipos/{id}
- POST /api/equipos
- PUT /api/equipos/{id}
- DELETE /api/equipos/{id}

### Solicitudes

- GET /api/solicitudes
- GET /api/solicitudes/{id}
- GET /api/solicitudes/{id}/detalle
- POST /api/solicitudes
- PUT /api/solicitudes/{id}
- DELETE /api/solicitudes/{id}
- PATCH /api/solicitudes/{id}/estado

### Asignaciones

- POST /api/asignaciones
- GET /api/asignaciones
- GET /api/asignaciones/{id}
- PUT /api/asignaciones/{id}
- DELETE /api/asignaciones/{id}

### Diagnósticos

- POST /api/diagnosticos
- GET /api/diagnosticos/{id}
- PUT /api/diagnosticos/{id}
- DELETE /api/diagnosticos/{id}

### Reparaciones

- POST /api/reparaciones
- GET /api/reparaciones/{id}
- PUT /api/reparaciones/{id}
- DELETE /api/reparaciones/{id}

## Estructura del proyecto

```text
FixFlow/
├── backend/
│   ├── src/
│   │   ├── main/java/com/fixflow/
│   │   │   ├── controller/
│   │   │   ├── service/
│   │   │   ├── repository/
│   │   │   ├── model/
│   │   │   ├── dto/
│   │   │   ├── security/
│   │   │   ├── exception/
│   │   │   ├── config/
│   │   │   └── FixflowApplication.java
│   │   ├── main/resources/
│   │   │   └── application.properties
│   │   └── test/java/com/fixflow/
│   └── pom.xml
├── frontend/
│   ├── index.html
│   ├── login.html
│   ├── register.html
│   ├── dashboard.html
│   ├── usuarios.html
│   ├── clientes.html
│   ├── equipos.html
│   ├── solicitudes.html
│   ├── diagnosticos.html
│   ├── reparaciones.html
│   ├── css/styles.css
│   └── js/
│       ├── api.js
│       ├── auth.js
│       ├── register.js
│       ├── dashboard.js
│       ├── usuarios.js
│       ├── clientes.js
│       ├── equipos.js
│       ├── solicitudes.js
│       ├── diagnosticos.js
│       └── reparaciones.js
├── database/
│   └── fixflow.sql
├── docs/
│   └── (DER.png, diagrama-clases.png, casos-uso.png)
├── README.md
└── .gitignore
```

## Flujo de solicitudes

Solicitud -> Pendiente -> Asignada -> En diagnóstico -> En reparación -> Finalizada

Reglas de negocio implementadas:

- Una solicitud nueva se crea en estado `PENDIENTE` (US-10).
- El estado `ASIGNADA` solo se obtiene al asignar un técnico activo (US-13); no se puede forzar por `PATCH`.
- El diagnóstico solo se registra cuando la solicitud está `ASIGNADA` y por el técnico asignado (US-14).
- La reparación solo se registra cuando existe diagnóstico previo (US-15).
- Un equipo con solicitudes asociadas no se puede eliminar (integridad referencial, US-09).

También se soporta:

- Cancelada

Los cambios de estado se validan: no se permiten saltos inválidos (por ejemplo, pasar de SOLICITUD a FINALIZADA). Al finalizar una solicitud se registra automáticamente la `fecha_finalizacion` de la reparación.

## Pruebas

Las pruebas se ejecutan con H2 (perfil `test`) y no requieren MySQL:

```bash
cd backend
mvn test
```

Cubren:

- Usuario: crear, consultar, actualizar, desactivar, email duplicado, id inexistente, contraseña obligatoria.
- Equipo: crear, consultar, actualizar, eliminar, serial duplicado, id inexistente, cliente inexistente.
- Solicitud: crear, consultar, actualizar, eliminar, transiciones de estado válidas e inválidas, id inexistente, equipo inexistente.
- Configuración: preflight CORS para PATCH y exposición de colecciones de diagnóstico y reparación.

## Integrantes

Proyecto académico desarrollado para la asignatura de Desarrollo de Aplicaciones Empresariales.

## Evidencias del proyecto

- Backend con Spring Boot y autenticación JWT
- MySQL con esquema y datos de prueba
- Frontend con HTML, CSS y JavaScript
- REST API funcional
- Casos principales cubiertos con pruebas básicas
