# 🚀 PRÓXIMOS PASOS - NexusPOS Backend

## ✅ LO QUE YA TENEMOS

### 1. Arquitectura Empresarial
- ✅ Separación en 3 capas (Controller → Service → Repository)
- ✅ Inyección de dependencias
- ✅ Manejo de transacciones
- ✅ Validaciones automáticas

### 2. Entidad Producto COMPLETA
- ✅ Modelo JPA con validaciones
- ✅ Repository con queries automáticas
- ✅ Service con lógica de negocio
- ✅ Controller REST con 15+ endpoints
- ✅ CORS configurado

### 3. Funcionalidades de Producto
- ✅ CRUD completo (Create, Read, Update, Delete)
- ✅ Búsqueda por nombre (parcial, case-insensitive)
- ✅ Filtrado por categoría
- ✅ Detección de stock bajo/crítico
- ✅ Agregar/reducir stock
- ✅ Contador de productos
- ✅ Listado de categorías

---

## 📋 PRÓXIMOS PASOS

### FASE 1: Completar el Backend (Esta semana)

#### 1. Entidad Mesa
```java
@Entity
@Table(name = "mesas")
public class Mesa {
    @Id @GeneratedValue
    private Long id;
    private Integer numero;
    private String nombre;
    private String estado; // "libre" o "ocupada"
    private Double total;

    @OneToMany(mappedBy = "mesa")
    private List<Pedido> pedidos;
}
```

**Crear:**
- `model/Mesa.java`
- `repository/MesaRepository.java`
- `service/MesaService.java`
- `controller/MesaController.java`

**Endpoints:**
- GET `/api/mesas` - Todas las mesas
- POST `/api/mesas` - Crear mesa
- PATCH `/api/mesas/{id}/estado` - Cambiar estado (libre/ocupada)
- GET `/api/mesas/activas` - Solo mesas ocupadas

---

#### 2. Entidad Pedido (Relación Mesa-Producto)
```java
@Entity
@Table(name = "pedidos")
public class Pedido {
    @Id @GeneratedValue
    private Long id;

    @ManyToOne
    @JoinColumn(name = "mesa_id")
    private Mesa mesa;

    @ManyToOne
    @JoinColumn(name = "producto_id")
    private Producto producto;

    private Integer cantidad;
    private Double precioUnitario;
    private Double subtotal; // cantidad * precioUnitario
}
```

**Concepto Clave: Relaciones JPA**

`@ManyToOne`: Muchos pedidos pertenecen a una mesa
`@OneToMany`: Una mesa tiene muchos pedidos

---

#### 3. Entidad Caja
```java
@Entity
@Table(name = "caja")
public class Caja {
    @Id
    private String id; // "actual"

    private Double efectivo;
    private Double transferencias;
    private Double saldoPorCobrar;
    private Boolean abierta;

    @Column(name = "fecha_apertura")
    private LocalDateTime fechaApertura;

    @Column(name = "fecha_cierre")
    private LocalDateTime fechaCierre;
}
```

**Endpoints:**
- GET `/api/caja/actual` - Estado actual de la caja
- POST `/api/caja/abrir` - Abrir caja
- POST `/api/caja/cerrar` - Cerrar caja (genera reporte)
- GET `/api/caja/totales` - Efectivo, transferencias, total

---

#### 4. Entidad Gasto
```java
@Entity
@Table(name = "gastos")
public class Gasto {
    @Id @GeneratedValue
    private Long id;

    private String concepto;
    private Double monto;
    private String tipoPago; // "efectivo" o "transferencia"

    @CreationTimestamp
    private LocalDateTime fecha;

    private String periodo; // ID del periodo de caja
}
```

**Endpoints:**
- POST `/api/gastos` - Registrar gasto
- GET `/api/gastos` - Todos los gastos
- GET `/api/gastos/periodo/{id}` - Gastos de un periodo
- GET `/api/gastos/totales` - Total gastado

---

#### 5. Entidad Venta
```java
@Entity
@Table(name = "ventas")
public class Venta {
    @Id @GeneratedValue
    private Long id;

    @ManyToOne
    private Mesa mesa;

    private Double total;
    private Double efectivo;
    private Double transferencias;
    private Double cambio;

    @OneToMany
    private List<Pedido> pedidos;

    @CreationTimestamp
    private LocalDateTime fecha;
}
```

**Endpoints:**
- POST `/api/ventas` - Registrar venta (cerrar mesa)
- GET `/api/ventas` - Historial de ventas
- GET `/api/ventas/hoy` - Ventas del día
- GET `/api/ventas/reporte?desde=...&hasta=...` - Reporte por fechas

---

### FASE 2: Migrar Frontend React (Próxima semana)

#### 1. Crear servicio de API en React

```javascript
// src/services/api.js
const API_URL = 'http://localhost:8080/api';

export const productosAPI = {
  getAll: () => fetch(`${API_URL}/productos`).then(r => r.json()),
  create: (data) => fetch(`${API_URL}/productos`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(data)
  }).then(r => r.json()),
  // ... más métodos
};

export const mesasAPI = { ... };
export const cajaAPI = { ... };
```

#### 2. Reemplazar Firebase en DataContext

```javascript
// ANTES: Firebase
const unsubscribe = onSnapshot(collection(db, 'productos'), ...);

// DESPUÉS: REST API
useEffect(() => {
  const fetchData = async () => {
    const data = await productosAPI.getAll();
    setProductos(data);
  };

  fetchData();
  const interval = setInterval(fetchData, 5000); // Polling cada 5s
  return () => clearInterval(interval);
}, []);
```

#### 3. Actualizar Componentes

**Inventario.js:**
```javascript
const handleCreate = async (producto) => {
  try {
    const nuevo = await productosAPI.create(producto);
    // Actualizar estado local o refetch
  } catch (error) {
    alert('Error: ' + error.message);
  }
};
```

---

### FASE 3: Mejoras al Backend (Opcional)

#### 1. Spring Security + JWT
- Autenticación con usuarios y contraseñas
- Tokens JWT para sesiones
- Roles: Admin, Cajero, Mesero

#### 2. WebSockets para Tiempo Real
```java
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig { ... }
```

#### 3. Documentación con Swagger
```xml
<dependency>
    <groupId>org.springdoc</groupId>
    <artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
</dependency>
```

Acceder a: `http://localhost:8080/swagger-ui.html`

#### 4. Tests Unitarios
```java
@Test
public void testCrearProducto() {
    Producto p = new Producto();
    p.setNombre("Test");
    p.setPrecio(100.0);
    p.setStock(10);

    Producto saved = productoService.create(p);
    assertNotNull(saved.getId());
}
```

---

### FASE 4: Capacitor (App Móvil)

```bash
cd nexus-pos
npm install @capacitor/core @capacitor/cli
npx cap init
npx cap add android
npm run build
npx cap sync
npx cap open android
```

---

## 🎯 ORDEN RECOMENDADO

### Esta Semana:
1. ✅ Ejecutar y probar backend con Postman
2. ⏳ Crear entidad Mesa + Pedido
3. ⏳ Crear entidad Caja
4. ⏳ Crear entidad Gasto
5. ⏳ Crear entidad Venta

### Próxima Semana:
6. Migrar frontend React a REST API
7. Probar integración completa
8. Corregir bugs

### En 2-3 Semanas:
9. Configurar Capacitor
10. Compilar para Android
11. Probar en dispositivo real

---

## 📚 RECURSOS DE APRENDIZAJE

### Spring Boot:
- [Spring Boot Reference](https://spring.io/projects/spring-boot)
- [Baeldung Spring Tutorials](https://www.baeldung.com/spring-tutorial)

### JPA/Hibernate:
- [JPA Relationships](https://www.baeldung.com/jpa-one-to-one)
- [Hibernate Guide](https://docs.jboss.org/hibernate/orm/current/userguide/html_single/Hibernate_User_Guide.html)

### REST API Design:
- [REST API Best Practices](https://www.freecodecamp.org/news/rest-api-best-practices-rest-endpoint-design-examples/)

---

## ❓ PREGUNTAS FRECUENTES

### ¿Por qué no usar Firebase?
- Mayor control de datos
- Sin límites de consultas
- Mejor para reportes complejos
- Aprendes tecnologías empresariales
- Más barato a largo plazo

### ¿Por qué 3 capas?
- Separación de responsabilidades
- Fácil de mantener y testear
- Escalable
- Estándar en la industria

### ¿Cuándo usar @Transactional?
- Siempre en Services que escriben datos
- readOnly=true para queries
- Garantiza ACID (todo o nada)

### ¿Cómo debuggear?
- Revisa logs en consola
- Usa log.debug() liberalmente
- Activa SQL logs (ya configurado)
- Usa breakpoints en IntelliJ

---

## 🎉 ¡FELICITACIONES!

Has construido un backend empresarial profesional con:
- Arquitectura de 3 capas
- Base de datos PostgreSQL
- API REST completa
- Validaciones robustas
- Transacciones automáticas
- Inyección de dependencias

**Siguiente paso:** Abre IntelliJ y ejecuta el proyecto!
