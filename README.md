# Sistema de Biblioteca - Arquitectura de Microservicios

Proyecto semestral DSY1103 (Desarrollo FullStack 1). Sistema distribuido para la
gestion completa de una biblioteca: usuarios, roles, prestamos, inventario,
multas, listas personales, notificaciones, sugerencias y valoraciones.

## Integrantes del equipo

| Nombre | Aporte |
|---|---|
| Diego Patricio Soto León | Auth - User - Security - Inventario - Prestamos - Multas |
| Nabih Aballay | Sugerencias - Favoritos |
| Cristobal Retamal Carrera| Notificaciones - Valoraciones |


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

## Pasos para ejecutar

Abrir el proyecto completo en VS-Code, abrir una instancia de XAMPP, levantando el servicio de apache y Mysql, ejecutar y esperar a levantar el servicio de Eureka, ejecutar el resto de microservicios en orden, por ultimo abriendo Api-Getaway.

Verifica el registro en `http://localhost:8761` (dashboard de Eureka). Los 10
microservicios + el gateway deben aparecer en `Instances currently registered`.

### SQL

Para la creación de las bases de datos, se adjuntan 2 SQL en el proyecto, en los cuales hay que ejecutarlos en orden en el perfil de phpadmin de la instancia de XAMPP


## Endpoints principales (via API Gateway en `http://localhost:8080`)

| Recurso | Endpoint base | Ejemplo |
|---|---|---|
| Auth | `/api/auth/login` | `POST /api/auth/login` (body JSON con email/password) |
| Usuarios | `/api/users` | `GET /api/users/email/{email}` |
| Roles | `/api/security/roles`, `/api/security/asignar` | `POST /api/security/asignar` |
| Inventario | `/inventario` | `POST /inventario/crear` |
| Prestamos | `/prestamos` | `POST /prestamos/registrar` |
| Multas | `/multas` | `POST /multas/calcular/{prestamoId}` |
| Listas | `/api/listas` | `POST /api/listas` |
| Notificaciones | `/api/notificaciones` | `POST /api/notificaciones/vencimiento/{id}` |
| Sugerencias | `/api/sugerencias` | `POST /api/sugerencias` |
| Valoraciones | `/api/valoraciones` | `GET /api/valoraciones/libro/{id}/promedio` |

## Licencia

Proyecto academico - Duoc UC - DSY1103 - 2026
