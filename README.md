# 🚀 SISTEMA DE MICROSERVICIOS MULTIMÓDULO - ENTREGA FINAL

## 📦 COMPONENTES DE DISTRIBUCIÓN Y DEFENSA TÉCNICA

Utilice los siguientes enlaces externos para descargar las versiones listas para producción y visualizar la defensa del proyecto:

| Componente | Descripción | Enlace de Descarga (Nube externa) |
| :--- | :--- | :--- |
| **📦 Versión Sin Docker** <br>*(Arranque Nativo)* | Archivo `.zip` que contiene la carpeta `apps/` con los `.jar` compilados y el script `arrancar-nativo.bat` ordenado por fases. | [Descargar ZIP Nativo aquí](https://drive.google.com/drive/folders/1GsalC1TAFQZbySb_aux3RguYJAoXqfIL?usp=sharing) |
| **🐳 Versión Con Docker** <br>*(Avance Examen Transversal)* | Archivo `.zip` que contiene la carpeta `apps/` con los `.jar`, el archivo `docker-compose.yml` y el script automatizado `arrancar-sistema.bat`. | [PENDIENTE] |
| **🎥 Video de Defensa Técnica** <br>*(Evaluación Individual)* | Enlace directo al video explicativo donde se evidencia el funcionamiento, testing y el aporte técnico individual. **Duración ideal: 15 minutos (Máximo permitido: 18 minutos).** | [Enlace del video](https://www.youtube.com/watch?v=6g8FUE_jNdU) |



---

# Sistema de Biblioteca - Arquitectura de Microservicios

Proyecto semestral DSY1103 (Desarrollo FullStack 1). Sistema distribuido para la
gestion completa de una biblioteca: usuarios, roles, prestamos, inventario,
multas, listas personales, notificaciones, sugerencias y valoraciones.
Construido como **proyecto Maven multi-módulo** con un POM padre que gobierna
los 12 módulos (Spring Boot 4.0.6 / Spring Cloud 2025.1.1).

## Integrantes del equipo

| Nombre | Aporte |
|---|---|
| Diego Patricio Soto León | Auth - User - Security - Inventario - Prestamos - Multas - Sugerencias - Favoritos - Notificaciones - Valoraciones|

## Microservicios

| # | Servicio | Puerto | BD | Descripcion |
|---|---|---|---|---|
| 0 | `eureka-server` | 8761 | — | Registry / Service Discovery |
| 1 | `auth-service` | 8083 | `library_auth_db` | Login y emision de JWT |
| 2 | `user-service` | 8081 | `library_users_db` | CRUD de usuarios |
| 3 | `security-service` | 8082 | `library_security_db` | Roles y asignaciones |
| 4 | `ms-inventario` | 8084 | `library_inventory_db` | Catalogo de libros |
| 5 | `ms-prestamos` | 8085 | `library_loans_db` | Registro de prestamos |
| 6 | `ms-multas` | 8086 | `library_fines_db` | Calculo y cobro de multas |
| 7 | `ms-favoritos-listas` | 8087 | `library_favorites_db` | Listas personales |
| 8 | `ms-notificaciones` | 8088 | `library_notifications_db` | Alertas y avisos |
| 9 | `ms-sugerencias` | 8089 | `library_suggestions_db` | Propuestas de libros |
| 10 | `ms-valoraciones` | 8090 | `library_reviews_db` | Reviews y rating |
| 99 | `api-gateway` | 8080 | — | Routing centralizado |

## Comunicacion entre microservicios (Feign)

| Origen | Destino | Proposito |
|---|---|---|
| auth-service | user-service | Obtener usuario y validar password |
| auth-service | security-service | Obtener roles para el JWT |
| security-service | user-service | Validar existencia del usuario al asignar rol |
| ms-prestamos | user-service | Validar email del solicitante |
| ms-prestamos | ms-inventario | Validar libro y stock |
| ms-prestamos | ms-multas | Bloquear si tiene multas pendientes |
| ms-multas | ms-prestamos | Obtener fecha de devolucion del prestamo |
| ms-multas | user-service | Validar usuario del prestamo |
| ms-favoritos-listas | user-service | Validar email del propietario |
| ms-notificaciones | user-service | Validar destinatario |
| ms-notificaciones | ms-prestamos | Obtener fechaDevolucion (vencimientos) |
| ms-sugerencias | user-service | Validar socio que sugiere |
| ms-sugerencias | ms-inventario | Verificar que el ISBN no este en catalogo |
| ms-valoraciones | user-service | Validar autor de la resenia |
| ms-valoraciones | ms-inventario | Validar libro a valorar |

## Funcionalidades implementadas
- Autenticacion JWT (`POST /api/auth/login`)
- CRUD completo de usuarios, libros, roles, prestamos, listas, sugerencias,
  valoraciones, notificaciones y multas
- Asignacion y revocacion de roles a usuarios
- Validacion cruzada de referencias por Feign antes de persistir
- Bloqueo automatico de prestamos cuando el usuario tiene multas pendientes
- Calculo automatico de multas en base a dias de retraso (`multas.tarifa-por-dia`)
- Promedio de valoraciones por libro
- Notificaciones automaticas de vencimiento de prestamo
- Validacion JSR 380 en todos los endpoints de entrada
- Manejo centralizado de excepciones con `@RestControllerAdvice`
- Logs estructurados SLF4J en todas las capas

---

## 🛠️ Instrucciones de uso

### Requisitos previos
- **JDK 21** o superior.
- **Maven** (o usar el wrapper `mvnw` / `mvnw.cmd` incluido en cada módulo).
- **MySQL** activo en el puerto **3307** (por ejemplo vía XAMPP). Cada servicio
  crea su BD con `createDatabaseIfNotExist=true`.

### 1) Preparar las bases de datos
En phpMyAdmin (XAMPP) ejecuta, **en orden**, los scripts de la carpeta `Bases de datos/`:

1. `01_create_databases.sql`
2. `02_seed_roles.sql`

### 2) Construir los `.jar` (proyecto multi-módulo)
Desde la raíz del proyecto, una sola orden compila y empaqueta **todo el reactor**:

```bat
mvn clean package
```

O usa el script incluido (omite los tests para ir más rápido):

```bat
construir-jars.bat
```

Cada módulo genera su jar ejecutable en `<módulo>\target\<artifactId>-0.0.1-SNAPSHOT.jar`.

### 3) Arrancar el sistema (modo nativo, por fases cronometradas)
```bat
arrancar-nativo.bat
```
Orden de arranque que respeta los tiempos:
1. **Eureka Server** → espera **30 s**.
2. **Los 10 microservicios de negocio** (todos menos el gateway) → espera **40 s**.
3. **API Gateway**.

Cada servicio abre su propia ventana con sus logs. El script usa rutas relativas
a su ubicación (`%~dp0`), por lo que **funciona aunque muevas la carpeta**.

### 4) Verificar
- Dashboard de Eureka: `http://localhost:8761` (deben aparecer los 12 registros).
- Acceso unificado por el gateway: `http://localhost:8080` (p. ej. `/api/users/...`).

### 5) Detener el sistema
```bat
detener-nativo.bat
```
Cierra **solo** los procesos `java` lanzados desde la carpeta del proyecto.

### Ejecutar los tests
```bat
mvn test
```
Los tests unitarios (JUnit 5 + Mockito) usan **H2 en memoria** y **no requieren**
MySQL ni Eureka levantados.

---

## 📖 Documentación de la API (Swagger / OpenAPI)

Cada microservicio expone su documentación interactiva con **springdoc-openapi**.
Con el sistema arrancado, abre en el navegador la Swagger UI del servicio que quieras:

```
http://localhost:<puerto>/swagger-ui.html
```

| Servicio | Swagger UI | OpenAPI (JSON) |
|---|---|---|
| user-service | http://localhost:8081/swagger-ui.html | http://localhost:8081/v3/api-docs |
| security-service | http://localhost:8082/swagger-ui.html | http://localhost:8082/v3/api-docs |
| auth-service | http://localhost:8083/swagger-ui.html | http://localhost:8083/v3/api-docs |
| ms-inventario | http://localhost:8084/swagger-ui.html | http://localhost:8084/v3/api-docs |
| ms-prestamos | http://localhost:8085/swagger-ui.html | http://localhost:8085/v3/api-docs |
| ms-multas | http://localhost:8086/swagger-ui.html | http://localhost:8086/v3/api-docs |
| ms-favoritos-listas | http://localhost:8087/swagger-ui.html | http://localhost:8087/v3/api-docs |
| ms-notificaciones | http://localhost:8088/swagger-ui.html | http://localhost:8088/v3/api-docs |
| ms-sugerencias | http://localhost:8089/swagger-ui.html | http://localhost:8089/v3/api-docs |
| ms-valoraciones | http://localhost:8090/swagger-ui.html | http://localhost:8090/v3/api-docs |

> Swagger se consulta en el **puerto propio de cada servicio**, no a través del gateway.

### Cómo probar un endpoint en Swagger UI
1. Abre la URL de la Swagger UI del servicio.
2. Despliega el endpoint que quieras y pulsa **"Try it out"**.
3. Completa el cuerpo de la petición (JSON) o los parámetros.
4. Pulsa **"Execute"** y revisa la respuesta (código HTTP y body).

### Ejemplo de flujo de uso (end-to-end)

**1) Crear un usuario** — `user-service` → `POST /api/users` (`http://localhost:8081/swagger-ui.html`)
```json
{
  "nombre": "Diego Soto",
  "email": "diego@biblioteca.com",
  "password": "1234",
  "telefono": "912345678"
}
```

**2) Iniciar sesión y obtener el JWT** — `auth-service` → `POST /api/auth/login` (`http://localhost:8083/swagger-ui.html`)
```json
{
  "email": "diego@biblioteca.com",
  "password": "1234"
}
```
Respuesta:
```json
{ "token": "eyJhbGciOiJIUzI1NiJ9..." }
```

**3) Registrar un libro** — `ms-inventario` → `POST /inventario/crear` (`http://localhost:8084/swagger-ui.html`)
```json
{
  "titulo": "Cien años de soledad",
  "autor": "Gabriel García Márquez",
  "isbn": "9780307474728",
  "editorial": "Sudamericana",
  "stock": 5
}
```

**4) Registrar un préstamo** — `ms-prestamos` → `POST /prestamos/registrar` (`http://localhost:8085/swagger-ui.html`)
```json
{
  "emailUsuario": "diego@biblioteca.com",
  "libroId": 1,
  "fechaDevolucion": "2026-12-31"
}
```
El servicio valida por Feign que el usuario y el libro existan, que haya stock y que
el usuario no tenga multas pendientes antes de crear el préstamo.

---

## ✅ Testing

Cobertura de tests unitarios en los 10 microservicios de negocio, en las tres capas:

- **Servicio** — Mockito puro (`@Mock` repositorios y Feign clients, `@InjectMocks`).
- **Controlador** — `MockMvc` standalone con el `GlobalExceptionHandler`.
- **Repositorio** — `@DataJpaTest` con base de datos **H2** en memoria.

---

## 📝 Changelog

### v2.0 — Entrega Final
- **Migración a Maven multi-módulo**: nuevo POM padre `com.biblioteca:biblioteca-parent`
  (`packaging pom`) que agrega y gobierna los 12 módulos; los hijos heredan versión,
  propiedades y BOM de Spring Cloud.
- **Unificación de versiones**: todo el sistema a **Spring Boot 4.0.6** /
  **Spring Cloud 2025.1.1** (eureka y gateway estaban en 3.5.13 / 2025.0.2).
- **API Gateway** migrado al starter `spring-cloud-starter-gateway-server-webflux`
  (Spring Cloud Gateway 5.0) y configuración movida al namespace
  `spring.cloud.gateway.server.webflux.*`.
- **Tests unitarios JUnit 5 + Mockito** (service / controller / repository) en los
  10 servicios de negocio; dependencia **H2** de test añadida a cada módulo.
- **Configuración de test** (`src/test/resources/application.properties`) con H2 y
  Eureka desactivado, para que los `@SpringBootTest` arranquen sin infraestructura.
- **Corrección de paquetes** de los `*ApplicationTests` (estaban en `com.bilbioteca.*`
  y `example.*`, no coincidían con la clase `@SpringBootApplication`).
- **Scripts de operación**: `construir-jars.bat`, `arrancar-nativo.bat`
  (por fases cronometradas, portable) y `detener-nativo.bat`.
- **`.gitignore`** con la política del proyecto (sin `target/`, `.jar`, instaladores,
  ejecutables `.bat`/`.sh` ni datos locales de BD).


### v1.0 — Entrega inicial
- Implementación de los 10 microservicios de negocio, `eureka-server` y `api-gateway`
  con comunicación vía Feign y descubrimiento por Eureka.

---

## Licencia

Proyecto academico - Duoc UC - DSY1103 - 2026
