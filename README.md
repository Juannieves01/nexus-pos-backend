# 🚀 NexusPOS Backend

Sistema POS empresarial construido con **Spring Boot 3**, **PostgreSQL** y **JPA/Hibernate**.

## 📋 Tecnologías

- **Java 17+** - Lenguaje de programación
- **Spring Boot 3.2.1** - Framework backend
- **Spring Data JPA** - ORM (Hibernate)
- **PostgreSQL** - Base de datos relacional
- **Lombok** - Reducción de boilerplate
- **Maven** - Gestión de dependencias

## 🏗️ Arquitectura

```
Controller (REST API)
    ↓
Service (Lógica de negocio)
    ↓
Repository (Acceso a datos)
    ↓
PostgreSQL (Base de datos)
```

## 📦 Estructura del Proyecto

```
src/main/java/com/nexuspos/backend/
├── config/          # Configuración (CORS, etc.)
├── controller/      # Endpoints REST
├── service/         # Lógica de negocio
├── repository/      # Acceso a datos (JPA)
├── model/           # Entidades JPA
├── dto/             # Data Transfer Objects
└── exception/       # Manejo de excepciones
```

## ⚙️ Configuración

### 1. Requisitos Previos

- Java JDK 17 o superior
- PostgreSQL 12+
- IntelliJ IDEA (recomendado) o VS Code
- Maven (incluido en IntelliJ)

### 2. Configurar Base de Datos

Abre PostgreSQL (pgAdmin o terminal) y ejecuta:

```sql
CREATE DATABASE nexuspos_db;
```

### 3. Configurar application.properties

Edita `src/main/resources/application.properties`:

```properties
spring.datasource.username=postgres
spring.datasource.password=TU_CONTRASEÑA_AQUÍ
```

### 4. Ejecutar el Proyecto

**Opción A: IntelliJ IDEA**
1. Abre el proyecto (carpeta `nexus-pos-backend`)
2. Espera a que Maven descargue dependencias
3. Click derecho en `NexusPosBackendApplication.java`
4. Run 'NexusPosBackendApplication'

**Opción B: Terminal/CMD**
```bash
cd nexus-pos-backend
mvnw spring-boot:run
```

**Opción C: Maven**
```bash
mvn clean install
mvn spring-boot:run
```

## 🌐 Endpoints Disponibles

### Productos

| Método | Endpoint | Descripción |
|--------|----------|-------------|
| GET | `/api/productos` | Obtener todos los productos |
| GET | `/api/productos/{id}` | Obtener producto por ID |
| GET | `/api/productos/buscar?nombre=x` | Buscar por nombre |
| GET | `/api/productos/categoria/{cat}` | Filtrar por categoría |
| GET | `/api/productos/categorias` | Obtener categorías |
| GET | `/api/productos/stock/bajo` | Stock < 20 |
| GET | `/api/productos/stock/critico` | Stock < 10 |
| POST | `/api/productos` | Crear producto |
| PUT | `/api/productos/{id}` | Actualizar producto |
| DELETE | `/api/productos/{id}` | Eliminar producto |
| PATCH | `/api/productos/{id}/stock/agregar` | Agregar stock |
| PATCH | `/api/productos/{id}/stock/reducir` | Reducir stock |

### Ejemplo: Crear Producto

```http
POST http://localhost:8080/api/productos
Content-Type: application/json

{
  "nombre": "Coca Cola",
  "categoria": "Bebidas",
  "precio": 3000.0,
  "stock": 50
}
```

## 🔧 Próximos Pasos

1. Implementar entidades: Mesa, Caja, Gasto, Venta
2. Agregar Spring Security (autenticación JWT)
3. Implementar WebSockets para tiempo real
4. Agregar tests unitarios (JUnit + Mockito)
5. Documentar API con Swagger/OpenAPI

## 🐛 Troubleshooting

### Error: Could not connect to database

- Verifica que PostgreSQL esté corriendo
- Revisa usuario/contraseña en `application.properties`
- Verifica que exista la base de datos `nexuspos_db`

### Error: Port 8080 is already in use

- Cambia el puerto en `application.properties`:
  ```properties
  server.port=8081
  ```

### Error: Lombok annotations not working

- IntelliJ: Settings → Plugins → Install "Lombok"
- IntelliJ: Settings → Build → Compiler → Annotation Processors → Enable
- VS Code: Instala extensión "Lombok Annotations Support"

## 📚 Recursos de Aprendizaje

- [Spring Boot Docs](https://spring.io/projects/spring-boot)
- [JPA/Hibernate](https://docs.spring.io/spring-data/jpa/docs/current/reference/html/)
- [PostgreSQL Docs](https://www.postgresql.org/docs/)

## 👨‍💻 Autor

Juan Diego Nieves
