# 🧪 PRUEBAS DE API - NexusPOS Backend

## ✅ Verificar que el servidor esté corriendo

**GET** `http://localhost:8080/api/productos`

**Respuesta esperada:** `[]` (array vacío)

---

## 📝 Crear un Producto

**POST** `http://localhost:8080/api/productos`

**Headers:**
```
Content-Type: application/json
```

**Body (JSON):**
```json
{
  "nombre": "Coca Cola",
  "categoria": "Bebidas",
  "precio": 3000.0,
  "stock": 50
}
```

**Respuesta esperada (201 Created):**
```json
{
  "id": 1,
  "nombre": "Coca Cola",
  "categoria": "Bebidas",
  "precio": 3000.0,
  "stock": 50,
  "createdAt": "2025-12-09T...",
  "updatedAt": "2025-12-09T..."
}
```

---

## 📋 Obtener Todos los Productos

**GET** `http://localhost:8080/api/productos`

**Respuesta esperada (200 OK):**
```json
[
  {
    "id": 1,
    "nombre": "Coca Cola",
    "categoria": "Bebidas",
    "precio": 3000.0,
    "stock": 50,
    ...
  }
]
```

---

## 🔍 Obtener un Producto por ID

**GET** `http://localhost:8080/api/productos/1`

**Respuesta esperada (200 OK):**
```json
{
  "id": 1,
  "nombre": "Coca Cola",
  ...
}
```

---

## ✏️ Actualizar un Producto

**PUT** `http://localhost:8080/api/productos/1`

**Headers:**
```
Content-Type: application/json
```

**Body:**
```json
{
  "nombre": "Coca Cola 2L",
  "categoria": "Bebidas",
  "precio": 5000.0,
  "stock": 30
}
```

**Respuesta esperada (200 OK):** Producto actualizado

---

## 🔍 Buscar Productos

**GET** `http://localhost:8080/api/productos/buscar?nombre=coca`

Encuentra todos los productos que contengan "coca" en el nombre (case-insensitive)

---

## 📊 Obtener Categorías

**GET** `http://localhost:8080/api/productos/categorias`

**Respuesta:**
```json
["Bebidas", "Alimentos", "Postres"]
```

---

## ⚠️ Stock Bajo

**GET** `http://localhost:8080/api/productos/stock/bajo`

Devuelve productos con stock < 20

---

## 🚨 Stock Crítico

**GET** `http://localhost:8080/api/productos/stock/critico`

Devuelve productos con stock < 10

---

## ➕ Agregar Stock

**PATCH** `http://localhost:8080/api/productos/1/stock/agregar`

**Body:**
```json
{
  "cantidad": 20
}
```

Agrega 20 unidades al stock actual

---

## ➖ Reducir Stock

**PATCH** `http://localhost:8080/api/productos/1/stock/reducir`

**Body:**
```json
{
  "cantidad": 5
}
```

Reduce 5 unidades del stock actual

---

## 🗑️ Eliminar Producto

**DELETE** `http://localhost:8080/api/productos/1`

**Respuesta esperada (200 OK):**
```json
{
  "message": "Producto eliminado exitosamente",
  "id": 1
}
```

---

## 🧮 Contar Productos

**GET** `http://localhost:8080/api/productos/count`

**Respuesta:**
```json
{
  "total": 5
}
```

---

## 📦 Crear Múltiples Productos de Prueba

### Producto 2:
```json
{
  "nombre": "Hamburguesa Clásica",
  "categoria": "Alimentos",
  "precio": 15000.0,
  "stock": 25
}
```

### Producto 3:
```json
{
  "nombre": "Café Americano",
  "categoria": "Bebidas",
  "precio": 2500.0,
  "stock": 40
}
```

### Producto 4:
```json
{
  "nombre": "Papas Fritas",
  "categoria": "Acompañamientos",
  "precio": 5000.0,
  "stock": 8
}
```

### Producto 5:
```json
{
  "nombre": "Helado de Vainilla",
  "categoria": "Postres",
  "precio": 6000.0,
  "stock": 15
}
```

---

## ❌ Probar Validaciones

### Error: Nombre vacío
```json
{
  "nombre": "",
  "categoria": "Bebidas",
  "precio": 1000.0,
  "stock": 10
}
```

**Respuesta esperada:** 400 Bad Request

### Error: Precio negativo
```json
{
  "nombre": "Producto Test",
  "categoria": "Test",
  "precio": -100.0,
  "stock": 10
}
```

**Respuesta esperada:** 400 Bad Request

### Error: Stock negativo
```json
{
  "nombre": "Producto Test",
  "categoria": "Test",
  "precio": 100.0,
  "stock": -5
}
```

**Respuesta esperada:** 400 Bad Request

---

## 🔐 Próximas Pruebas (cuando implementes otras entidades)

- Mesas
- Caja
- Gastos
- Ventas
