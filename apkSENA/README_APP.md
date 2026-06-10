# SENA App — Android

Aplicación Android para el sistema de gestión de usuarios SENA.  
Desarrollada en Java con SQLite local, sincronización offline y consumo de API REST.

---

## Requisitos de desarrollo

- Android Studio Hedgehog o superior
- JDK 11
- Android SDK 36
- Dispositivo o emulador con Android 7.0+ (API 24+)
- Servidor con la API SENA activo en la red local

---

## Instalación del proyecto

1. Clona o extrae el proyecto
2. Ábrelo en Android Studio: **File → Open → selecciona la carpeta `apkSENA/`**
3. Espera a que Gradle sincronice las dependencias (requiere internet)
4. Ajusta la IP del servidor en `ApiService.java`:

```java
private static final String BASE_URL = "http://192.168.0.21/api/senaUsuario";
```

5. Ejecuta con **Run → Run 'app'** (`Shift+F10`)

---

## Estructura del proyecto

```
app/src/main/
├── java/co/edu/sena/
│   ├── LoginActivity.java          # Pantalla de inicio de sesión
│   ├── RegisterActivity.java       # Pantalla de registro
│   ├── HomeActivity.java           # Pantalla principal post-login
│   ├── database/
│   │   └── DatabaseHelper.java     # CRUD SQLite local
│   ├── model/
│   │   └── Usuario.java            # Modelo de datos
│   ├── network/
│   │   ├── ApiService.java         # Llamadas HTTP al API REST
│   │   └── SyncWorker.java         # Worker de sincronización offline
│   └── utils/
│       └── SessionManager.java     # Manejo de sesión con SharedPreferences
└── res/
    ├── layout/
    │   ├── activity_login.xml
    │   ├── activity_register.xml
    │   └── activity_home.xml
    ├── values/
    │   ├── colors.xml              # Paleta de colores SENA
    │   ├── strings.xml
    │   └── themes.xml
    ├── drawable/
    │   ├── bg_gradient.xml         # Fondo verde degradado
    │   └── ic_logo.xml
    └── xml/
        └── network_security_config.xml  # Permite HTTP en red local
```

---

## Dependencias

Definidas en `app/build.gradle.kts`:

```kotlin
implementation("com.squareup.okhttp3:okhttp:4.12.0")   // Llamadas HTTP
implementation("androidx.work:work-runtime:2.9.1")      // Sincronización offline
```

---

## Flujo de la aplicación

### Login (`LoginActivity`)

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

### Registro (`RegisterActivity`)

```
Completa el formulario
        │
        ▼
Validaciones locales
  (documento, nombre, apellido, email, contraseña)
        │
        ▼
¿Email o documento ya existen en SQLite?
    Sí ──► Muestra error
    No
        │
        ▼
¿Hay conexión a internet?
    Sí ──► Registra en API + guarda en SQLite (sincronizado=true)
    No ──► Guarda solo en SQLite (sincronizado=false)
              + programa WorkManager para sincronizar después
        │
        ▼
Ir al Home
```

### Sincronización offline (`SyncWorker`)

- Se activa automáticamente cuando el dispositivo recupera conexión
- Envía al API todos los registros con `sincronizado = 0`
- Si el envío es exitoso, marca el registro como `sincronizado = 1`
- Si falla, WorkManager lo reintenta automáticamente

---

## Base de datos local (SQLite)

Archivo: `sena.db`  
Tabla: `usuarios`

| Campo        | Tipo    | Descripción                        |
|--------------|---------|------------------------------------|
| id           | INTEGER | Clave primaria autoincremental     |
| documento    | TEXT    | Único — cédula o tarjeta           |
| nombre       | TEXT    | Nombre del usuario                 |
| apellido     | TEXT    | Apellido del usuario               |
| email        | TEXT    | Único — correo electrónico         |
| password     | TEXT    | Contraseña en texto plano (local)  |
| sincronizado | INTEGER | 0 = pendiente, 1 = enviado al API  |

---

## Validaciones del formulario de registro

| Campo      | Regla                                  |
|------------|----------------------------------------|
| Documento  | Solo números, entre 6 y 15 dígitos     |
| Nombre     | Mínimo 2 caracteres                    |
| Apellido   | Mínimo 2 caracteres                    |
| Email      | Formato válido (usuario@dominio.com)   |
| Contraseña | Mínimo 6 caracteres                    |

---

## Configuración de red

Para permitir conexiones HTTP a la IP local del servidor, el archivo
`res/xml/network_security_config.xml` debe contener:

```xml
<?xml version="1.0" encoding="utf-8"?>
<network-security-config>
    <domain-config cleartextTrafficPermitted="true">
        <domain includeSubdomains="false">192.168.0.21</domain>
    </domain-config>
</network-security-config>
```

Y en `AndroidManifest.xml`:

```xml
<application
    android:networkSecurityConfig="@xml/network_security_config"
    ...>
```

> Si cambias la IP del servidor, actualízala tanto en `ApiService.java`
> como en `network_security_config.xml`.

---

## Permisos requeridos

Declarados en `AndroidManifest.xml`:

```xml
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
```

---

## Colores de la interfaz

| Nombre            | Hex       | Uso                        |
|-------------------|-----------|----------------------------|
| `sena_green`      | `#39A900` | Color principal SENA       |
| `sena_green_dark` | `#2d8400` | Degradado / hover          |
| `sena_blue`       | `#003087` | Color secundario           |
| `background`      | `#F8F9FA` | Fondo de pantallas         |
| `error_red`       | `#D32F2F` | Mensajes de error          |

---

## Solución de problemas comunes

| Error | Causa | Solución |
|-------|-------|----------|
| `Cannot resolve symbol 'MediaType'` | OkHttp no sincronizado | File → Sync Project with Gradle Files |
| `CLEARTEXT not permitted` | Android bloquea HTTP | Agregar `network_security_config.xml` |
| `401 Credenciales inválidas` | `password_verify()` activo en API | Cambiar a comparación directa `!==` |
| Login local no funciona | Contraseña hasheada en SQLite | Eliminar el usuario y registrar de nuevo |
| Registro no llega al API | Sin conexión al momento | WorkManager lo sincroniza automáticamente |
