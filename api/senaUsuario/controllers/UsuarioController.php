<?php
require_once __DIR__ . '/../models/Usuario.php';

class UsuarioController {

    private Usuario $model;

    public function __construct() {
        $this->model = new Usuario();
    }

    // ──────────────────────────────────────────────
    //  POST /senaUsuario           → Registrar usuario
    // ──────────────────────────────────────────────
    public function registrar(): void {
        $data = $this->getJsonBody();

        $campos = ['documento', 'nombre', 'apellido', 'email', 'password'];
        $faltantes = array_filter($campos, fn($c) => empty(trim($data[$c] ?? '')));
        if (!empty($faltantes)) {
            $this->responder(400, [
                'success' => false,
                'message' => 'Campos requeridos faltantes: ' . implode(', ', $faltantes),
            ]);
            return;
        }

        // Validar email
        if (!filter_var($data['email'], FILTER_VALIDATE_EMAIL)) {
            $this->responder(400, ['success' => false, 'message' => 'Formato de email inválido']);
            return;
        }

        // Validar documento numérico 6-15 dígitos
        if (!preg_match('/^\d{6,15}$/', $data['documento'])) {
            $this->responder(400, ['success' => false, 'message' => 'Documento debe tener entre 6 y 15 dígitos numéricos']);
            return;
        }

        // Validar contraseña mínimo 6 caracteres
        if (strlen($data['password']) < 6) {
            $this->responder(400, ['success' => false, 'message' => 'La contraseña debe tener al menos 6 caracteres']);
            return;
        }

        $this->model->documento = trim($data['documento']);
        $this->model->nombre    = trim($data['nombre']);
        $this->model->apellido  = trim($data['apellido']);
        $this->model->email     = strtolower(trim($data['email']));
        $this->model->password  = $data['password'];

        $resultado = $this->model->crear();
        $this->responder($resultado['code'] ?? 201, $resultado);
    }

    // ──────────────────────────────────────────────
    //  POST /senaUsuario/login     → Login
    // ──────────────────────────────────────────────
    public function login(): void {
        $data = $this->getJsonBody();

        if (empty($data['email']) || empty($data['password'])) {
            $this->responder(400, ['success' => false, 'message' => 'Email y contraseña son requeridos']);
            return;
        }

        $resultado = $this->model->login(
            strtolower(trim($data['email'])),
            $data['password']
        );
        $this->responder($resultado['code'] ?? 200, $resultado);
    }

    // ──────────────────────────────────────────────
    //  GET /senaUsuario            → Listar todos
    // ──────────────────────────────────────────────
    public function listar(): void {
        $usuarios = $this->model->obtenerTodos();
        $this->responder(200, [
            'success' => true,
            'total'   => count($usuarios),
            'data'    => $usuarios,
        ]);
    }

    // ──────────────────────────────────────────────
    //  GET /senaUsuario/{id}       → Obtener uno
    // ──────────────────────────────────────────────
    public function obtener(int $id): void {
        $resultado = $this->model->obtenerUno($id);
        $this->responder($resultado['code'] ?? 200, $resultado);
    }

    // ──────────────────────────────────────────────
    //  PUT /senaUsuario/{id}       → Actualizar
    // ──────────────────────────────────────────────
    public function actualizar(int $id): void {
        $data = $this->getJsonBody();

        if (isset($data['nombre']))    $this->model->nombre    = trim($data['nombre']);
        if (isset($data['apellido']))  $this->model->apellido  = trim($data['apellido']);
        if (isset($data['documento'])) $this->model->documento = trim($data['documento']);

        $resultado = $this->model->actualizar($id);
        $this->responder($resultado['code'] ?? 200, $resultado);
    }

    // ──────────────────────────────────────────────
    //  DELETE /senaUsuario/{id}    → Eliminar
    // ──────────────────────────────────────────────
    public function eliminar(int $id): void {
        $resultado = $this->model->eliminar($id);
        $this->responder($resultado['code'] ?? 200, $resultado);
    }

    // ──────────────────────────────────────────────
    //  HELPERS
    // ──────────────────────────────────────────────
    private function getJsonBody(): array {
        $raw = file_get_contents('php://input');
        $data = json_decode($raw, true);
        return is_array($data) ? $data : [];
    }

    private function responder(int $httpCode, array $body): void {
        // Eliminar clave 'code' interna antes de enviar
        unset($body['code']);
        http_response_code($httpCode);
        echo json_encode($body, JSON_UNESCAPED_UNICODE | JSON_PRETTY_PRINT);
    }
}
