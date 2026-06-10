# SENA API — PHP

API REST para la app Android del SENA.  
URL base: `http://192.168.0.21/api/senaUsuario`

## Instalación

1. Copia la carpeta `senaUsuario/` dentro de `htdocs/api/` (XAMPP) o `www/api/` (WAMP).
2. Importa `database.sql` en MySQL/MariaDB.
3. Ajusta credenciales en `config/database.php` si es necesario.
4. Asegúrate de que `mod_rewrite` esté activo en Apache.

## Endpoints

| Método | URL | Descripción |
|--------|-----|-------------|
| POST | `/senaUsuario` | Registrar usuario |
| POST | `/senaUsuario/login` | Login |
| GET | `/senaUsuario` | Listar todos |
| GET | `/senaUsuario/{id}` | Obtener uno |
| PUT | `/senaUsuario/{id}` | Actualizar |
| DELETE | `/senaUsuario/{id}` | Eliminar |

## Ejemplos

### Registrar
```json
POST /senaUsuario
{
  "documento": "12345678",
  "nombre": "Juan",
  "apellido": "Pérez",
  "email": "juan@example.com",
  "password": "secreto123"
}
```

### Login
```json
POST /senaUsuario/login
{
  "email": "juan@example.com",
  "password": "secreto123"
}
```
