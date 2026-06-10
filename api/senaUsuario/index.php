<?php
/**
 * ============================================================
 *  SENA API — Router principal
 *  Ruta base: http://192.168.56.1/api/senaUsuario
 * ============================================================
 *
 *  Endpoints disponibles:
 *  ┌──────────────────────────────────────────────────────────┐
 *  │ POST   /senaUsuario          → Registrar usuario         │
 *  │ POST   /senaUsuario/login    → Login                     │
 *  │ GET    /senaUsuario          → Listar todos              │
 *  │ GET    /senaUsuario/{id}     → Obtener uno               │
 *  │ PUT    /senaUsuario/{id}     → Actualizar                │
 *  │ DELETE /senaUsuario/{id}     → Eliminar                  │
 *  └──────────────────────────────────────────────────────────┘
 */

require_once __DIR__ . '/config/cors.php';
require_once __DIR__ . '/config/database.php';
require_once __DIR__ . '/controllers/UsuarioController.php';

// ── Cabeceras CORS + Content-Type ────────────────────────────
setCorsHeaders();

// ── Parsear la URL ───────────────────────────────────────────
$requestUri    = $_SERVER['REQUEST_URI'];
$requestMethod = $_SERVER['REQUEST_METHOD'];

// Quitar query-string
$path = parse_url($requestUri, PHP_URL_PATH);

// Normalizar: eliminar prefijo /api/senaUsuario y trailing slashes
$path = preg_replace('#^/api/senaUsuario#', '', $path);
$path = rtrim($path, '/');

// Segmentos del path residual  ("/login"  → ["login"])
$segments = array_values(array_filter(explode('/', $path)));

$controller = new UsuarioController();

// ── Enrutamiento ─────────────────────────────────────────────
try {
    // POST /senaUsuario/login
    if ($requestMethod === 'POST' && isset($segments[0]) && $segments[0] === 'login') {
        $controller->login();
        exit;
    }

    // Rutas con ID   /senaUsuario/{id}
    if (isset($segments[0]) && is_numeric($segments[0])) {
        $id = (int) $segments[0];
        match ($requestMethod) {
            'GET'    => $controller->obtener($id),
            'PUT'    => $controller->actualizar($id),
            'DELETE' => $controller->eliminar($id),
            default  => responder405(),
        };
        exit;
    }

    // Rutas base   /senaUsuario
    match ($requestMethod) {
        'GET'  => $controller->listar(),
        'POST' => $controller->registrar(),
        default => responder405(),
    };

} catch (Throwable $e) {
    http_response_code(500);
    echo json_encode([
        'success' => false,
        'message' => 'Error interno del servidor',
        'detail'  => $e->getMessage(),   // quitar en producción
    ], JSON_UNESCAPED_UNICODE);
}

function responder405(): void {
    http_response_code(405);
    echo json_encode(['success' => false, 'message' => 'Método no permitido']);
}
