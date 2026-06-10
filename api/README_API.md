# SENA API — PHP REST

API REST para el sistema de usuarios SENA.  
Desarrollada en PHP puro con PDO y arquitectura MVC simple.

---

## Requisitos del servidor

- PHP 8.0 o superior
- MySQL 5.7+ o MariaDB 10.3+
- Apache con `mod_rewrite` activo
- XAMPP / WAMP / servidor Linux

---

## Instalación

### 1. Copiar archivos

Extrae la carpeta `senaUsuario/` dentro de:

```
XAMPP  →  C:\xampp\htdocs\api\senaUsuario\
WAMP   →  C:\wamp64\www\api\senaUsuario\
Linux  →  /var/www/html/api/senaUsuario/
```

### 2. Crear la base de datos

Importa el archivo `database.sql` desde phpMyAdmin o por consola:

```bash
mysql -u root -p < database.sql
```

Esto crea la base de datos `sena_db` y la tabla `usuarios` automáticamente.

### 3. Configurar credenciales

Edita `config/database.php` con los datos de tu servidor MySQL:

```php
private string $host   = 'localhost';
private string $dbname = 'sena_db';
private string $user   = 'root';
private string $pass   = '';        // tu contraseña aquí
```

### 4. Activar mod_rewrite (Apache)

En `httpd.conf` asegúrate de tener:

```apache
LoadModule rewrite_module modules/mod_rewrite.so
AllowOverride All
```

Reinicia Apache después del cambio.

---

## Estructura del proyecto

```
senaUsuario/
├── config/
│   ├── database.php        # Conexión PDO (Singleton)
│   └── cors.php            # Cabeceras CORS
├── models/
│   └── Usuario.php         # Lógica de negocio y consultas SQL
├── controllers/
│   └── UsuarioController.php  # Validaciones y respuestas HTTP
├── middleware/             # Disponible para auth futura (JWT, etc.)
├── index.php               # Router principal
├── .htaccess               # Rewrite rules para Apache
├── database.sql            # Script de instalación de la BD
└── README.md
```

---

## Endpoints

URL base: `http://192.168.xxx.xxx/api/senaUsuario`

| Método   | Endpoint              | Descripción          | Auth |
|----------|-----------------------|----------------------|------|
| `POST`   | `/senaUsuario`        | Registrar usuario    | No   |
| `POST`   | `/senaUsuario/login`  | Iniciar sesión       | No   |
| `GET`    | `/senaUsuario`        | Listar todos         | No   |
| `GET`    | `/senaUsuario/{id}`   | Obtener uno por ID   | No   |
| `PUT`    | `/senaUsuario/{id}`   | Actualizar usuario   | No   |
| `DELETE` | `/senaUsuario/{id}`   | Eliminar usuario     | No   |

---

## Ejemplos de uso

### Registrar usuario

```http
POST /api/senaUsuario
Content-Type: application/json

{
  "documento": "12345678",
  "nombre": "Laura",
  "apellido": "Gómez",
  "email": "laura@sena.edu.co",
  "password": "123456"
}
```

**Respuesta exitosa (201):**
```json
{
  "success": true,
  "message": "Usuario registrado correctamente",
  "data": {
    "id": 1,
    "documento": "12345678",
    "nombre": "Laura",
    "apellido": "Gómez",
    "email": "laura@sena.edu.co",
    "created_at": "2026-06-09 14:00:00"
  }
}
```

**Error — email duplicado (409):**
```json
{
  "success": false,
  "message": "El correo ya está registrado"
}
```

---

### Login

```http
POST /api/senaUsuario/login
Content-Type: application/json

{
  "email": "laura@sena.edu.co",
  "password": "123456"
}
```

**Respuesta exitosa (200):**
```json
{
  "success": true,
  "message": "Login exitoso",
  "data": {
    "id": 1,
    "documento": "12345678",
    "nombre": "Laura",
    "apellido": "Gómez",
    "email": "laura@sena.edu.co",
    "created_at": "2026-06-09 14:00:00"
  }
}
```

> ⚠️ El campo `password` nunca se retorna en ninguna respuesta.

**Error — credenciales incorrectas (401):**
```json
{
  "success": false,
  "message": "Credenciales inválidas"
}
```

---

### Listar todos los usuarios

```http
GET /api/senaUsuario
```

**Respuesta (200):**
```json
{
  "success": true,
  "total": 2,
  "data": [
    {
      "id": 1,
      "documento": "12345678",
      "nombre": "Laura",
      "apellido": "Gómez",
      "email": "laura@sena.edu.co",
      "created_at": "2026-06-09 14:00:00"
    }
  ]
}
```

---

### Obtener un usuario por ID

```http
GET /api/senaUsuario/1
```

---

### Actualizar usuario

```http
PUT /api/senaUsuario/1
Content-Type: application/json

{
  "nombre": "Laura Marcela",
  "apellido": "Gómez Torres"
}
```

---

### Eliminar usuario

```http
DELETE /api/senaUsuario/1
```

**Respuesta (200):**
```json
{
  "success": true,
  "message": "Usuario eliminado"
}
```

---

## Estructura de la base de datos

```sql
CREATE TABLE usuarios (
    id          INT UNSIGNED  AUTO_INCREMENT PRIMARY KEY,
    documento   VARCHAR(15)   NOT NULL UNIQUE,
    nombre      VARCHAR(80)   NOT NULL,
    apellido    VARCHAR(80)   NOT NULL,
    email       VARCHAR(120)  NOT NULL UNIQUE,
    password    VARCHAR(255)  NOT NULL,
    created_at  DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP
);
```

---

## Códigos de respuesta HTTP

| Código | Significado                        |
|--------|------------------------------------|
| 200    | OK                                 |
| 201    | Creado correctamente               |
| 400    | Datos inválidos o faltantes        |
| 401    | Credenciales incorrectas           |
| 404    | Usuario no encontrado              |
| 405    | Método HTTP no permitido           |
| 409    | Conflicto (email o documento duplicado) |
| 500    | Error interno del servidor         |

---

## Notas

- Todas las respuestas son `application/json; charset=utf-8`
- CORS habilitado para cualquier origen (`*`) — restringir en producción
- El campo `password` nunca se incluye en las respuestas
- Validaciones: documento 6–15 dígitos, email válido, contraseña mínimo 6 caracteres
