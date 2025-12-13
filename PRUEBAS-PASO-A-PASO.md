# 🧪 PRUEBAS PASO A PASO - API NexusPOS

## 📝 INSTRUCCIONES

Copia cada petición y pégala en Thunder Client o Postman.
Sigue el orden para entender cómo funciona todo.

---

## ✅ PASO 1: Verificar que el servidor está corriendo

### GET - Lista de productos (vacía inicialmente)

```
GET http://localhost:8080/api/productos
```

**Respuesta esperada:**
```json
[]
```

✅ Si ves `[]` → ¡El servidor funciona!

---

## 📦 PASO 2: Crear productos de prueba

Vamos a crear 10 productos para tener datos de prueba.

### Producto 1: Coca Cola

```
POST http://localhost:8080/api/productos
Content-Type: application/json

{
  "nombre": "Coca Cola 400ml",
  "categoria": "Bebidas",
  "precio": 3000.0,
  "stock": 50
}
```

**Respuesta esperada (201 Created):**
```json
{
  "id": 1,
  "nombre": "Coca Cola 400ml",
  "categoria": "Bebidas",
  "precio": 3000.0,
  "stock": 50,
  "createdAt": "2025-12-09T...",
  "updatedAt": "2025-12-09T..."
}
```

✅ **Nota el `id: 1`** - Es auto-generado por la base de datos

---

### Producto 2: Hamburguesa

```
POST http://localhost:8080/api/productos
Content-Type: application/json

{
  "nombre": "Hamburguesa Clásica",
  "categoria": "Alimentos",
  "precio": 15000.0,
  "stock": 25
}
```

---

### Producto 3: Café

```
POST http://localhost:8080/api/productos
Content-Type: application/json

{
  "nombre": "Café Americano",
  "categoria": "Bebidas",
  "precio": 2500.0,
  "stock": 40
}
```

---

### Producto 4: Papas Fritas (Stock Crítico)

```
POST http://localhost:8080/api/productos
Content-Type: application/json

{
  "nombre": "Papas Fritas",
  "categoria": "Acompañamientos",
  "precio": 5000.0,
  "stock": 8
}
```

✅ **Stock 8** → Detectado como stock crítico (< 10)

---

### Producto 5: Helado

```
POST http://localhost:8080/api/productos
Content-Type: application/json

{
  "nombre": "Helado de Vainilla",
  "categoria": "Postres",
  "precio": 6000.0,
  "stock": 15
}
```

✅ **Stock 15** → Detectado como stock bajo (< 20)

---

### Producto 6: Pizza

```
POST http://localhost:8080/api/productos
Content-Type: application/json

{
  "nombre": "Pizza Margarita",
  "categoria": "Alimentos",
  "precio": 25000.0,
  "stock": 12
}
```

---

### Producto 7: Limonada

```
POST http://localhost:8080/api/productos
Content-Type: application/json

{
  "nombre": "Limonada Natural",
  "categoria": "Bebidas",
  "precio": 4000.0,
  "stock": 30
}
```

---

### Producto 8: Ensalada

```
POST http://localhost:8080/api/productos
Content-Type: application/json

{
  "nombre": "Ensalada César",
  "categoria": "Alimentos",
  "precio": 12000.0,
  "stock": 5
}
```

✅ **Stock 5** → Stock crítico

---

### Producto 9: Té Helado

```
POST http://localhost:8080/api/productos
Content-Type: application/json

{
  "nombre": "Té Helado de Limón",
  "categoria": "Bebidas",
  "precio": 3500.0,
  "stock": 45
}
```

---

### Producto 10: Brownie

```
POST http://localhost:8080/api/productos
Content-Type: application/json

{
  "nombre": "Brownie con Helado",
  "categoria": "Postres",
  "precio": 8000.0,
  "stock": 18
}
```

✅ **Stock 18** → Stock bajo

---

## 📋 PASO 3: Obtener todos los productos

```
GET http://localhost:8080/api/productos
```

**Respuesta esperada:**
- Array con 10 productos
- Cada uno con su `id` único
- Fechas `createdAt` y `updatedAt`

✅ **Verifica** que todos los productos aparezcan

---

## 🔍 PASO 4: Buscar producto por ID

```
GET http://localhost:8080/api/productos/1
```

**Respuesta esperada:**
```json
{
  "id": 1,
  "nombre": "Coca Cola 400ml",
  "categoria": "Bebidas",
  "precio": 3000.0,
  "stock": 50,
  ...
}
```

### Prueba con ID inexistente:

```
GET http://localhost:8080/api/productos/999
```

**Respuesta esperada (404 Not Found):**
```
Producto no encontrado con id: 999
```

---

## 🔎 PASO 5: Búsquedas y Filtros

### Buscar por nombre (case-insensitive, parcial)

```
GET http://localhost:8080/api/productos/buscar?nombre=coca
```

**Resultado:** Encuentra "Coca Cola 400ml"

```
GET http://localhost:8080/api/productos/buscar?nombre=he
```

**Resultado:** Encuentra "Helado de Vainilla" y "Té Helado de Limón"

---

### Filtrar por categoría

```
GET http://localhost:8080/api/productos/categoria/Bebidas
```

**Resultado:** Coca Cola, Café, Limonada, Té Helado (4 productos)

```
GET http://localhost:8080/api/productos/categoria/Alimentos
```

**Resultado:** Hamburguesa, Pizza, Ensalada (3 productos)

```
GET http://localhost:8080/api/productos/categoria/Postres
```

**Resultado:** Helado, Brownie (2 productos)

---

### Obtener todas las categorías

```
GET http://localhost:8080/api/productos/categorias
```

**Respuesta esperada:**
```json
[
  "Acompañamientos",
  "Alimentos",
  "Bebidas",
  "Postres"
]
```

✅ Ordenadas alfabéticamente

---

## ⚠️ PASO 6: Alertas de Stock

### Productos con stock bajo (< 20)

```
GET http://localhost:8080/api/productos/stock/bajo
```

**Resultado esperado:**
- Papas Fritas (stock: 8)
- Helado de Vainilla (stock: 15)
- Pizza Margarita (stock: 12)
- Ensalada César (stock: 5)
- Brownie con Helado (stock: 18)

✅ **5 productos** con stock < 20

---

### Productos con stock crítico (< 10)

```
GET http://localhost:8080/api/productos/stock/critico
```

**Resultado esperado:**
- Ensalada César (stock: 5)
- Papas Fritas (stock: 8)

✅ **2 productos** con stock < 10 (ordenados por stock ascendente)

---

## ✏️ PASO 7: Actualizar un producto

### Actualizar nombre y precio de Coca Cola

```
PUT http://localhost:8080/api/productos/1
Content-Type: application/json

{
  "nombre": "Coca Cola 600ml",
  "categoria": "Bebidas",
  "precio": 4500.0,
  "stock": 50
}
```

**Respuesta (200 OK):**
```json
{
  "id": 1,
  "nombre": "Coca Cola 600ml",
  "precio": 4500.0,
  ...
}
```

✅ **Nota:** El `updatedAt` cambió, pero `createdAt` sigue igual

### Verificar la actualización:

```
GET http://localhost:8080/api/productos/1
```

Deberías ver el nuevo nombre y precio.

---

## 📊 PASO 8: Operaciones sobre Stock

### Agregar stock (ejemplo: recibimos más Papas Fritas)

```
PATCH http://localhost:8080/api/productos/4/stock/agregar
Content-Type: application/json

{
  "cantidad": 20
}
```

**Stock ANTES:** 8
**Stock DESPUÉS:** 28

✅ Verifica con:
```
GET http://localhost:8080/api/productos/4
```

---

### Reducir stock (ejemplo: vendimos 3 Hamburguesas)

```
PATCH http://localhost:8080/api/productos/2/stock/reducir
Content-Type: application/json

{
  "cantidad": 3
}
```

**Stock ANTES:** 25
**Stock DESPUÉS:** 22

---

### Intentar reducir más stock del disponible (error)

```
PATCH http://localhost:8080/api/productos/8/stock/reducir
Content-Type: application/json

{
  "cantidad": 100
}
```

**Respuesta esperada (400 Bad Request):**
```json
{
  "error": "Stock insuficiente. Disponible: 5, Solicitado: 100"
}
```

✅ **Validación funciona** - No permite stock negativo

---

## 🧮 PASO 9: Estadísticas

### Contar total de productos

```
GET http://localhost:8080/api/productos/count
```

**Respuesta esperada:**
```json
{
  "total": 10
}
```

---

## 🗑️ PASO 10: Eliminar un producto

### Eliminar Té Helado (id: 9)

```
DELETE http://localhost:8080/api/productos/9
```

**Respuesta (200 OK):**
```json
{
  "message": "Producto eliminado exitosamente",
  "id": 9
}
```

### Verificar eliminación:

```
GET http://localhost:8080/api/productos
```

Deberías ver solo 9 productos (el Té Helado ya no está)

### Verificar total:

```
GET http://localhost:8080/api/productos/count
```

**Respuesta:**
```json
{
  "total": 9
}
```

---

## ❌ PASO 11: Probar Validaciones

### Error: Nombre vacío

```
POST http://localhost:8080/api/productos
Content-Type: application/json

{
  "nombre": "",
  "categoria": "Test",
  "precio": 1000.0,
  "stock": 10
}
```

**Respuesta esperada (400 Bad Request):**
```json
{
  "nombre": "El nombre del producto es obligatorio"
}
```

---

### Error: Precio negativo

```
POST http://localhost:8080/api/productos
Content-Type: application/json

{
  "nombre": "Producto Test",
  "categoria": "Test",
  "precio": -100.0,
  "stock": 10
}
```

**Respuesta esperada (400):**
```json
{
  "precio": "El precio debe ser mayor a 0"
}
```

---

### Error: Stock negativo

```
POST http://localhost:8080/api/productos
Content-Type: application/json

{
  "nombre": "Producto Test",
  "categoria": "Test",
  "precio": 100.0,
  "stock": -5
}
```

**Respuesta esperada (400):**
```json
{
  "stock": "El stock no puede ser negativo"
}
```

---

### Error: Producto duplicado

```
POST http://localhost:8080/api/productos
Content-Type: application/json

{
  "nombre": "Coca Cola 600ml",
  "categoria": "Bebidas",
  "precio": 3000.0,
  "stock": 10
}
```

**Respuesta esperada (400):**
```json
{
  "error": "Ya existe un producto con el nombre: Coca Cola 600ml"
}
```

✅ **La validación de negocio funciona**

---

## 📊 PASO 12: Resumen de lo que probamos

✅ **CRUD Completo:**
- ✅ CREATE (POST) - Crear productos
- ✅ READ (GET) - Leer productos
- ✅ UPDATE (PUT) - Actualizar productos
- ✅ DELETE (DELETE) - Eliminar productos

✅ **Búsquedas:**
- ✅ Por nombre (parcial, case-insensitive)
- ✅ Por categoría
- ✅ Por ID

✅ **Filtros Especiales:**
- ✅ Stock bajo
- ✅ Stock crítico
- ✅ Categorías únicas

✅ **Operaciones de Stock:**
- ✅ Agregar stock
- ✅ Reducir stock
- ✅ Validación de stock insuficiente

✅ **Validaciones:**
- ✅ Campos obligatorios
- ✅ Valores positivos
- ✅ Nombres únicos
- ✅ Stock no negativo

✅ **Estadísticas:**
- ✅ Contador de productos

---

## 🎯 SIGUIENTE PASO

Verifica los datos en PostgreSQL:

1. Abre **pgAdmin**
2. Navega a: `nexuspos_db → Schemas → Tables → productos`
3. Click derecho → **View/Edit Data → All Rows**
4. Verás todos los productos que creaste

✅ **Si ves los productos en la base de datos** → ¡API 100% funcional!

---

## 📝 NOTAS IMPORTANTES

### Logs en IntelliJ

Mientras haces las peticiones, observa la consola de IntelliJ.
Verás:

```
INFO  - GET /api/productos - Obteniendo todos los productos
DEBUG - Se encontraron 9 productos
Hibernate: select producto0_.id as id1_0_, producto0_.nombre as nombre2_0_, ...
```

✅ Puedes ver exactamente qué SQL ejecuta Hibernate

### Códigos HTTP

- **200 OK** - Operación exitosa
- **201 Created** - Recurso creado
- **400 Bad Request** - Datos inválidos
- **404 Not Found** - Recurso no encontrado
- **500 Internal Server Error** - Error del servidor

---

## 🎉 ¡FELICITACIONES!

Has probado completamente una API REST empresarial con:
- CRUD completo
- Validaciones robustas
- Búsquedas y filtros
- Manejo de errores
- Persistencia en PostgreSQL
