# 🧪 PRUEBAS COMPLETAS - TODAS LAS APIs

## 🚀 PASO 1: REINICIAR EL BACKEND

**IMPORTANTE:** Antes de probar, reinicia el backend para que detecte las nuevas entidades.

1. En IntelliJ: Click en ⬛ (Stop) → ▶️ (Run)
2. Espera a ver el mensaje:
   ```
   🚀 NEXUSPOS BACKEND INICIADO CON ÉXITO 🚀
   ```

3. **Hibernate creará automáticamente las nuevas tablas:**
   ```
   Hibernate: create table mesas (...)
   Hibernate: create table pedidos (...)
   Hibernate: create table caja (...)
   Hibernate: create table gastos (...)
   ```

✅ Si ves estos mensajes = Todo funcionó

---

## 📦 APIS DE PRODUCTOS (Ya probadas antes)

Endpoints:
- `GET /api/productos` - Todos los productos
- `POST /api/productos` - Crear producto
- etc...

✅ Ya funcionan

---

## 🪑 APIS DE MESAS

### 1. Crear Mesas

```
POST http://localhost:8080/api/mesas
Content-Type: application/json

{
  "numero": 1,
  "nombre": "Terraza"
}
```

**Crea más mesas:**

```json
{ "numero": 2, "nombre": "Ventana" }
{ "numero": 3, "nombre": "VIP" }
{ "numero": 4, "nombre": "Jardín" }
```

---

### 2. Obtener Todas las Mesas

```
GET http://localhost:8080/api/mesas
```

**Respuesta esperada:**
```json
[
  {
    "id": 1,
    "numero": 1,
    "nombre": "Terraza",
    "estado": "libre",
    "total": 0.0,
    "pedidos": [],
    "createdAt": "...",
    "updatedAt": "..."
  },
  ...
]
```

---

### 3. Agregar Pedido a Mesa

**Primero, asegúrate de tener productos creados (de antes)**

```
POST http://localhost:8080/api/mesas/1/pedidos
Content-Type: application/json

{
  "productoId": 1,
  "cantidad": 3
}
```

**¿Qué pasa?**
1. Se crea un pedido de 3 unidades del producto 1
2. La mesa cambia a estado "ocupada"
3. Se reduce el stock del producto
4. Se calcula el subtotal (cantidad × precio)
5. Se actualiza el total de la mesa

**Respuesta esperada:**
```json
{
  "id": 1,
  "numero": 1,
  "nombre": "Terraza",
  "estado": "ocupada",
  "total": 9000.0,
  "pedidos": [
    {
      "id": 1,
      "nombreProducto": "Coca Cola 400ml",
      "cantidad": 3,
      "precioUnitario": 3000.0,
      "subtotal": 9000.0,
      "createdAt": "..."
    }
  ]
}
```

---

### 4. Agregar Más Pedidos

```
POST http://localhost:8080/api/mesas/1/pedidos
Content-Type: application/json

{
  "productoId": 2,
  "cantidad": 2
}
```

Ahora la mesa tiene 2 pedidos diferentes.

---

### 5. Actualizar Cantidad de un Pedido

```
PATCH http://localhost:8080/api/mesas/1/pedidos/1
Content-Type: application/json

{
  "cantidad": 5
}
```

Cambia la cantidad de 3 a 5. El stock se ajusta automáticamente.

---

### 6. Quitar un Pedido

```
DELETE http://localhost:8080/api/mesas/1/pedidos/1
```

Elimina el pedido y devuelve el stock al producto.

---

### 7. Obtener Mesa por ID

```
GET http://localhost:8080/api/mesas/1
```

---

### 8. Mesas Ocupadas

```
GET http://localhost:8080/api/mesas/ocupadas
```

Solo muestra mesas con estado "ocupada".

---

### 9. Mesas Libres

```
GET http://localhost:8080/api/mesas/libres
```

Solo muestra mesas con estado "libre".

---

### 10. Estadísticas de Mesas

```
GET http://localhost:8080/api/mesas/count
```

**Respuesta:**
```json
{
  "total": 4,
  "ocupadas": 1,
  "libres": 3
}
```

---

### 11. Liberar Mesa (cerrar sin guardar venta)

```
PATCH http://localhost:8080/api/mesas/1/liberar
```

Limpia los pedidos y marca la mesa como libre.

---

## 💰 APIS DE CAJA

### 1. Abrir Caja

```
POST http://localhost:8080/api/caja/abrir
Content-Type: application/json

{
  "montoInicial": 100000
}
```

**Respuesta:**
```json
{
  "id": "actual",
  "efectivo": 100000.0,
  "transferencias": 0.0,
  "saldoPorCobrar": 0.0,
  "abierta": true,
  "baseInicial": 100000.0,
  "fechaApertura": "2025-12-09T...",
  "fechaCierre": null
}
```

---

### 2. Obtener Estado Actual de Caja

```
GET http://localhost:8080/api/caja/actual
```

---

### 3. Obtener Totales

```
GET http://localhost:8080/api/caja/totales
```

**Respuesta:**
```json
{
  "efectivo": 100000.0,
  "transferencias": 0.0,
  "total": 100000.0,
  "saldoPorCobrar": 0.0
}
```

---

### 4. Actualizar Saldo por Cobrar

```
PATCH http://localhost:8080/api/caja/saldo-por-cobrar
Content-Type: application/json

{
  "saldo": 50000
}
```

---

### 5. Cerrar Caja

```
POST http://localhost:8080/api/caja/cerrar
```

Cierra la caja y guarda la fecha de cierre.

---

## 💸 APIS DE GASTOS

### 1. Crear Gasto

```
POST http://localhost:8080/api/gastos
Content-Type: application/json

{
  "concepto": "Compra de tomates",
  "monto": 25000,
  "tipoPago": "efectivo",
  "categoria": "Compras"
}
```

**¿Qué pasa?**
1. Se crea el gasto
2. Se descuenta automáticamente de la caja
3. Si tipoPago = "efectivo" → reduce caja.efectivo
4. Si tipoPago = "transferencia" → reduce caja.transferencias

---

### 2. Obtener Todos los Gastos

```
GET http://localhost:8080/api/gastos
```

**Ordenados por fecha descendente (más reciente primero)**

---

### 3. Filtrar por Tipo de Pago

```
GET http://localhost:8080/api/gastos/tipo-pago/efectivo
```

Solo gastos pagados en efectivo.

---

### 4. Totales de Gastos

```
GET http://localhost:8080/api/gastos/totales
```

**Respuesta:**
```json
{
  "total": 25000.0,
  "efectivo": 25000.0,
  "transferencias": 0.0
}
```

---

### 5. Eliminar Gasto

```
DELETE http://localhost:8080/api/gastos/1
```

**NOTA:** NO devuelve el dinero a la caja.

---

## 📋 APIS DE PEDIDOS (consultas)

### 1. Obtener Pedidos de una Mesa

```
GET http://localhost:8080/api/pedidos/mesa/1
```

---

### 2. Contar Pedidos de una Mesa

```
GET http://localhost:8080/api/pedidos/mesa/1/count
```

---

### 3. Calcular Total de una Mesa

```
GET http://localhost:8080/api/pedidos/mesa/1/total
```

---

## 🧪 FLUJO COMPLETO DE PRUEBA

### ESCENARIO: Turno completo del restaurante

#### 1. **Abrir Caja**
```
POST /api/caja/abrir
{
  "montoInicial": 100000
}
```

#### 2. **Crear Mesas**
```
POST /api/mesas
{ "numero": 1, "nombre": "Terraza" }
{ "numero": 2, "nombre": "VIP" }
```

#### 3. **Cliente llega a Mesa 1**
```
POST /api/mesas/1/pedidos
{
  "productoId": 1,
  "cantidad": 2
}
```

#### 4. **Cliente pide más cosas**
```
POST /api/mesas/1/pedidos
{
  "productoId": 2,
  "cantidad": 1
}
```

#### 5. **Verificar cuenta de la mesa**
```
GET /api/mesas/1
```

#### 6. **Registrar un gasto**
```
POST /api/gastos
{
  "concepto": "Compra de ingredientes",
  "monto": 50000,
  "tipoPago": "efectivo"
}
```

#### 7. **Ver estado de caja**
```
GET /api/caja/actual
```

Deberías ver:
- Efectivo: 50000 (100000 - 50000 del gasto)
- Transferencias: 0

#### 8. **Cliente pide más (cambiar cantidad)**
```
PATCH /api/mesas/1/pedidos/1
{
  "cantidad": 5
}
```

#### 9. **Ver mesas ocupadas**
```
GET /api/mesas/ocupadas
```

#### 10. **Liberar mesa (cliente se fue)**
```
PATCH /api/mesas/1/liberar
```

#### 11. **Ver totales de gastos**
```
GET /api/gastos/totales
```

#### 12. **Cerrar Caja**
```
POST /api/caja/cerrar
```

---

## ✅ VERIFICAR EN POSTGRESQL

Abre **pgAdmin** y verifica las nuevas tablas:

1. `nexuspos_db → Tables`
2. Deberías ver:
   - ✅ `productos` (ya existía)
   - ✅ `mesas` (nueva)
   - ✅ `pedidos` (nueva)
   - ✅ `caja` (nueva)
   - ✅ `gastos` (nueva)

3. Click derecho en cada tabla → **View/Edit Data**
4. ¡Verás todos los datos que creaste!

---

## 🎯 RELACIONES JPA EN ACCIÓN

### Ver Relación Mesa → Pedidos

```
GET /api/mesas/1
```

**Respuesta:**
```json
{
  "id": 1,
  "nombre": "Terraza",
  "pedidos": [
    {
      "id": 1,
      "nombreProducto": "Coca Cola",
      "cantidad": 3,
      "subtotal": 9000.0
    }
  ],
  "total": 9000.0
}
```

✅ **La relación @OneToMany funciona**: Una mesa tiene muchos pedidos

### Verificar Reducción de Stock

1. Crea un producto con stock 50
2. Agrégalo a una mesa con cantidad 3
3. Verifica el producto: `GET /api/productos/1`
4. El stock ahora es 47 ✅

---

## 🎉 ¡FELICITACIONES!

Has completado un **backend empresarial completo** con:

✅ **5 Entidades:** Producto, Mesa, Pedido, Caja, Gasto
✅ **Relaciones JPA:** @OneToMany, @ManyToOne
✅ **CRUD Completo:** Create, Read, Update, Delete
✅ **Validaciones:** @NotBlank, @Positive, etc.
✅ **Transacciones:** @Transactional para consistencia
✅ **Lógica de Negocio:** En Services
✅ **40+ Endpoints REST:** API completa

---

## 📊 RESUMEN DE APIs CREADAS

| Entidad | Endpoints | Funcionalidades |
|---------|-----------|-----------------|
| **Producto** | 15+ | CRUD, búsquedas, stock, categorías |
| **Mesa** | 12+ | CRUD, pedidos, ocupar/liberar, stats |
| **Pedido** | 6+ | Consultas, totales por mesa |
| **Caja** | 5+ | Abrir, cerrar, totales, estado |
| **Gasto** | 7+ | CRUD, totales, filtros |

**Total: ~45 endpoints REST funcionales** 🚀
