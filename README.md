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
DB_HOST=localhost
DB_PORT=3306
DB_NAME=fixflow
DB_USERNAME=root
DB_PASSWORD=
```

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

## Usuarios de prueba

El script `database/fixflow.sql` incluye estos usuarios de prueba. Las contraseñas se almacenan con BCrypt.

| Rol | Email | Contraseña |
| --- | --- | --- |
| ADMIN | admin@fixflow.com | admin123 |
| TECNICO | tecnico@fixflow.com | tecnico123 |
| CLIENTE | cliente@fixflow.com | cliente123 |

Si la base de datos ya existía antes de importar esta versión del script, vuelve a ejecutar el `INSERT` de usuarios o actualiza sus contraseñas para aplicar estos accesos.

## Permisos por rol

- **ADMIN**: gestión total de usuarios, clientes, técnicos, equipos, solicitudes, asignaciones, diagnósticos y reparaciones.
- **CLIENTE**: consulta y edición de su propia información (su perfil, sus equipos y sus solicitudes). No puede acceder a datos de otros clientes.
- **TECNICO**: consulta únicamente las solicitudes que le fueron asignadas y actualiza estados, diagnósticos y reparaciones.

La restricción de acceso por propietario se valida en los servicios y devuelve `403 FORBIDDEN` cuando se intenta acceder a datos de otro usuario. Los usuarios desactivados (estado `INACTIVO`) no pueden iniciar sesión.

## Endpoints

### Autenticación

- POST /api/auth/login

### Usuarios

- GET /api/usuarios
- GET /api/usuarios/{id}
- POST /api/usuarios
- PUT /api/usuarios/{id}
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
