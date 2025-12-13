# 🚀 Desplegar Backend en Railway

## Paso 1: Crear cuenta en Railway

1. Ve a https://railway.app
2. Haz clic en "Start a New Project"
3. Inicia sesión con GitHub (recomendado)

## Paso 2: Preparar el proyecto

### Opción A: Desde GitHub (Recomendado)
1. Sube tu código a un repositorio de GitHub
2. En Railway, selecciona "Deploy from GitHub repo"
3. Selecciona el repositorio `nexus-pos-backend`

### Opción B: Desde Railway CLI
```bash
# Instalar Railway CLI
npm i -g @railway/cli

# Login
railway login

# Inicializar proyecto
railway init

# Desplegar
railway up
```

## Paso 3: Agregar PostgreSQL

1. En tu proyecto de Railway, haz clic en "+ New"
2. Selecciona "Database" → "PostgreSQL"
3. Railway automáticamente creará las variables de entorno:
   - `DATABASE_URL`
   - `PGHOST`
   - `PGPORT`
   - `PGUSER`
   - `PGPASSWORD`
   - `PGDATABASE`

## Paso 4: Configurar Variables de Entorno

En Railway, ve a tu servicio backend y agrega:

```
SPRING_PROFILES_ACTIVE=prod
PORT=8080
```

Railway automáticamente conectará la base de datos PostgreSQL.

## Paso 5: Configurar Build

Railway debería detectar automáticamente Maven, pero si no:

**Build Command:**
```
./mvnw clean package -DskipTests
```

**Start Command:**
```
java -Dserver.port=$PORT -Dspring.profiles.active=prod -jar target/*.jar
```

## Paso 6: Desplegar

1. Railway desplegará automáticamente
2. Espera 3-5 minutos
3. Copia la URL generada (ej: `https://nexuspos-backend-production.up.railway.app`)

## Paso 7: Verificar

Abre en el navegador:
```
https://tu-app.railway.app/api/productos
```

Deberías ver una respuesta JSON (aunque vacía al inicio).

## ⚠️ Importante

- **Primer despliegue**: Las tablas se crearán automáticamente (ddl-auto=update)
- **Base de datos**: Railway incluye PostgreSQL gratis con 500MB
- **Horas gratis**: 500 horas/mes (suficiente para desarrollo)

## 🔗 URL Final

Tu backend estará disponible en:
```
https://[nombre-proyecto].up.railway.app
```

Esta URL la usarás en el frontend para conectar.
