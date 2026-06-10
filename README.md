# Sistema SENA — App Android + API PHP

Sistema de gestión de usuarios del Servicio Nacional de Aprendizaje (SENA).  
Compuesto por una aplicación Android con soporte offline y una API REST en PHP.

---

## Contenido del repositorio

```
repositorio/
├── apkSENA/          # Proyecto Android (Java)
├── senaUsuario/      # API REST (PHP)
└── README.md
```

---

## Tecnologías

| Capa | Tecnología |
|------|------------|
| App móvil | Java, Android SDK 36, Material3 |
| Base de datos local | SQLite |
| Sincronización offline | WorkManager |
| HTTP | OkHttp 4.12 |
| API | PHP 8.0, PDO |
| Base de datos servidor | MySQL / MariaDB |
| Servidor web | Apache + mod_rewrite |

---

## Requisitos

### Servidor
- PHP 8.0+
- MySQL 5.7+ o MariaDB 10.3+
- Apache con `mod_rewrite` activo
- XAMPP / WAMP / Linux Apache

### App
- Android Studio Hedgehog o superior
- JDK 11
- Android 7.0+ (API 24+)

---

## Instalación — API PHP

### 1. Copiar archivos al servidor

```
XAMPP  →  C:\xampp\htdocs\api\senaUsuario\
WAMP   →  C:\wamp64\www\api\senaUsuario\
Linux  →  /var/www/html/api/senaUsuario/
```

### 2. Crear la base de datos

Importa `senaUsuario/database.sql` desde phpMyAdmin o por consola:

```bash
mysql -u root -p < database.sql
```

### 3. Configurar credenciales

Edita `senaUsuario/config/database.php`:

```php
private string $host   = 'localhost';
private string $dbname = 'sena_db';
private string $user   = 'root';
private string $pass   = '';       // tu contraseña aquí
```

### 4. Activar mod_rewrite

En `httpd.conf` verifica que esté habilitado:

```apache
LoadModule rewrite_module modules/mod_rewrite.so
AllowOverride All
```

Reinicia Apache.

---

## Instalación — App Android

### 1. Abrir el proyecto

En Android Studio: **File → Open → carpeta `apkSENA/`**  
Espera a que Gradle descargue las dependencias (requiere internet).

### 2. Ajustar la IP del servidor

En `apkSENA/app/src/main/java/co/edu/sena/network/ApiService.java`:

```java
private static final String BASE_URL = "http://192.168.xxx.xxx/api/senaUsuario";
```

### 3. Ajustar la configuración de red

En `apkSENA/app/src/main/res/xml/network_security_config.xml`:

```xml
<domain-config cleartextTrafficPermitted="true">
    <domain includeSubdomains="false">192.168.xxx.xxx</domain>
</domain-config>
```

> Cambia `192.168.xxx.xxx` por la IP real de tu servidor en ambos archivos.

### 4. Ejecutar

**Run → Run 'app'** o `Shift+F10`.

---

## Estructura del proyecto

### API PHP — `senaUsuario/`

```
senaUsuario/
├── config/
│   ├── database.php          # Conexión PDO (Singleton)
│   └── cors.php              # Cabeceras CORS
├── models/
│   └── Usuario.php           # Lógica de negocio y consultas SQL
├── controllers/
│   └── UsuarioController.php # Validaciones y respuestas HTTP
├── index.php                 # Router principal
├── .htaccess                 # Rewrite rules para Apache
└── database.sql              # Script de instalación de la BD
```

### App Android — `apkSENA/`

```
app/src/main/java/co/edu/sena/
├── LoginActivity.java         # Pantalla de inicio de sesión
├── RegisterActivity.java      # Pantalla de registro
├── HomeActivity.java          # Pantalla principal post-login
├── database/
│   └── DatabaseHelper.java    # CRUD SQLite local
├── model/
│   └── Usuario.java           # Modelo de datos
├── network/
│   ├── ApiService.java        # Llamadas HTTP al API REST
│   └── SyncWorker.java        # Sincronización offline automática
└── utils/
    └── SessionManager.java    # Sesión con SharedPreferences
```

---

## Endpoints del API

URL base: `http://192.168.xxx.xxx/api/senaUsuario`

| Método | Endpoint | Descripción |
|--------|----------|-------------|
| `POST` | `/senaUsuario` | Registrar usuario |
| `POST` | `/senaUsuario/login` | Iniciar sesión |
| `GET` | `/senaUsuario` | Listar todos los usuarios |
| `GET` | `/senaUsuario/{id}` | Obtener usuario por ID |
| `PUT` | `/senaUsuario/{id}` | Actualizar usuario |
| `DELETE` | `/senaUsuario/{id}` | Eliminar usuario |

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

> El campo `password` nunca se retorna en ninguna respuesta.

---

## Flujo de la aplicación

### Login

```
Ingresa email + contraseña
        │
        ▼
¿Existe en SQLite local?
    Sí ──► Entra al Home
    No
        │
        ▼
¿Hay conexión a internet?
    No ──► Muestra error
    Sí
        │
        ▼
Consulta en API REST
    Encontrado ──► Guarda en SQLite ──► Entra al Home
    No encontrado ──► Muestra error
```

### Registro

```
Completa el formulario
        │
        ▼
Validaciones locales
        │
        ▼
¿Hay conexión?
    Sí ──► Registra en API + guarda en SQLite (sincronizado = true)
    No ──► Guarda en SQLite (sincronizado = false)
               + programa WorkManager para sincronizar después
        │
        ▼
Ir al Home
```

---

## Base de datos

### Servidor MySQL — tabla `usuarios`

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

### Local SQLite — mismos campos + columna de sincronización

| Campo | Tipo | Descripción |
|-------|------|-------------|
| id | INTEGER | Clave primaria |
| documento | TEXT | Único |
| nombre | TEXT | — |
| apellido | TEXT | — |
| email | TEXT | Único |
| password | TEXT | Texto plano |
| sincronizado | INTEGER | 0 = pendiente, 1 = enviado al API |

---

## Validaciones del registro

| Campo | Regla |
|-------|-------|
| Documento | Solo números, 6–15 dígitos |
| Nombre | Mínimo 2 caracteres |
| Apellido | Mínimo 2 caracteres |
| Email | Formato válido |
| Contraseña | Mínimo 6 caracteres |

---

## Códigos de respuesta HTTP

| Código | Significado |
|--------|-------------|
| 200 | OK |
| 201 | Creado correctamente |
| 400 | Datos inválidos o faltantes |
| 401 | Credenciales incorrectas |
| 404 | Usuario no encontrado |
| 405 | Método no permitido |
| 409 | Email o documento duplicado |
| 500 | Error interno del servidor |

---

## Solución de problemas

| Error | Causa | Solución |
|-------|-------|----------|
| `Cannot resolve symbol 'MediaType'` | OkHttp no sincronizado | File → Sync Project with Gradle Files |
| `CLEARTEXT not permitted` | Android bloquea HTTP plano | Agregar `network_security_config.xml` con la IP del servidor |
| `401 Credenciales inválidas` | `password_verify()` activo en API | Cambiar a comparación directa `!==` en `Usuario.php` |
| Registro no llega al API | Sin conexión al registrarse | WorkManager lo sincroniza automáticamente al recuperar red |
| JSON `email` no encontrado | Leer desde raíz en vez de `data` | Usar `obj.getJSONObject("data").optString("email")` |
