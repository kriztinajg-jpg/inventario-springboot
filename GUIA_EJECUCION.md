# Guia para ejecutar el proyecto: Inventario Noche

API REST de inventario con Spring Boot 4.0.8, Java 21 y PostgreSQL.

## 1. Requisitos previos

| Herramienta | Version | Como verificar |
|---|---|---|
| JDK | 21 | `java -version` |
| Maven | 3.9+ (o usar el wrapper del IDE) | `mvn -version` |
| PostgreSQL | 14+ | `psql --version` |
| IDE (opcional) | IntelliJ IDEA / Eclipse / VS Code | Con plugin de Lombok activado |

> **Lombok en el IDE:** en IntelliJ activa `Settings > Build > Compiler > Annotation Processors > Enable annotation processing`. Sin esto veras errores falsos en los getters y setters.

## 2. Crear la base de datos

Abre `psql` (o pgAdmin) y ejecuta:

```sql
CREATE DATABASE inventario;
```

Las tablas (`productos` y `movimientos`) se crean solas al arrancar la aplicacion porque `spring.jpa.hibernate.ddl-auto=update`.

## 3. Configurar la conexion

Edita `src/main/resources/application.properties` y cambia la contrasena por la tuya:

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/inventario
spring.datasource.username=postgres
spring.datasource.password=tu_password
```

## 4. Ejecutar

Desde la carpeta del proyecto (donde esta el `pom.xml`):

```bash
mvn clean install
mvn spring-boot:run
```

O desde el IDE: abrir `InventarionocheApplication.java` y darle **Run**.

La API queda en `http://localhost:8080`.

## 5. Swagger (documentacion de la API)

Abre en el navegador: <http://localhost:8080/swagger-ui/index.html>

Desde ahi puedes probar todos los endpoints sin usar Postman.

## 6. Endpoints

| Metodo | Ruta | Descripcion |
|---|---|---|
| GET | `/api/productos` | Listar productos |
| GET | `/api/productos/{id}` | Obtener producto |
| POST | `/api/productos` | Crear producto |
| PUT | `/api/productos` | Actualizar producto |
| DELETE | `/api/productos/{id}` | Eliminar producto |
| GET | `/api/movimientos` | Listar movimientos |
| GET | `/api/movimientos/{id}` | Obtener movimiento |
| POST | `/api/movimientos` | Crear movimiento (actualiza stock) |
| PUT | `/api/movimientos` | Actualizar movimiento |
| DELETE | `/api/movimientos/{id}` | Eliminar movimiento |

## 7. Pruebas paso a paso (para la sustentacion)

### 7.1 Crear un producto

`POST /api/productos`

```json
{
  "nombre": "Teclado mecanico",
  "descripcion": "Teclado RGB switch rojo",
  "precio": 150000,
  "stock": 10
}
```

Respuesta esperada: **201 Created** con el producto y su `id`.

### 7.2 Producto duplicado (error 409)

Enviar de nuevo el mismo JSON anterior.

```json
{
  "status": 409,
  "error": "Conflict",
  "mensaje": "Ya existe un producto con el nombre: Teclado mecanico"
}
```

### 7.3 Validaciones (error 400)

```json
{ "nombre": "", "precio": -5, "stock": -1 }
```

Responde **400** indicando los campos invalidos (nombre vacio, precio negativo, stock negativo).

### 7.4 Movimiento de ENTRADA (suma stock)

`POST /api/movimientos`

```json
{
  "producto": { "id": 1 },
  "cantidad": 5,
  "tipo": "ENTRADA"
}
```

Luego `GET /api/productos/1`: el stock pasa de 10 a **15**.

### 7.5 Movimiento de SALIDA (resta stock)

```json
{
  "producto": { "id": 1 },
  "cantidad": 3,
  "tipo": "SALIDA"
}
```

El stock queda en **12**.

### 7.6 Salida sin stock suficiente (error 400)

```json
{
  "producto": { "id": 1 },
  "cantidad": 1000,
  "tipo": "SALIDA"
}
```

```json
{
  "status": 400,
  "error": "Bad Request",
  "mensaje": "Stock insuficiente para el producto 'Teclado mecanico'. Disponible: 12, solicitado: 1000"
}
```

### 7.7 Recurso inexistente (error 404)

`GET /api/productos/999` responde **404** con el mensaje "No se encontro el producto con id: 999".

## 8. Resumen de codigos HTTP

| Excepcion | HTTP |
|---|---|
| `ResourceNotFoundException` | 404 |
| `ValidationException` | 400 |
| `InsufficientStockException` | 400 |
| `DuplicateResourceException` | 409 |
| Errores de `@Valid` | 400 |

## 9. Problemas comunes

- **`password authentication failed for user "postgres"`**: la contrasena en `application.properties` no coincide con la de tu PostgreSQL.
- **`Connection refused`**: PostgreSQL no esta corriendo o usa otro puerto.
- **`database "inventario" does not exist`**: falta el `CREATE DATABASE inventario;`.
- **Errores de `getNombre()` / `setId()` en el IDE**: activa el procesamiento de anotaciones de Lombok.
- **`Port 8080 was already in use`**: agrega `server.port=8081` en `application.properties`.

## 10. Entrega

1. Subir el proyecto a GitHub/GitLab (`git init`, `git add .`, `git commit`, `git push`).
2. Comprimir el proyecto en ZIP.
3. Entregar ambos en OneDrive, carpeta `semana11`.
