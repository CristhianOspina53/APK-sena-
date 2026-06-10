<?php
require_once __DIR__ . '/../config/database.php';

class Usuario {
    private PDO $db;

    // Tabla
    private string $table = 'usuarios';

    // Campos
    public int    $id;
    public string $documento;
    public string $nombre;
    public string $apellido;
    public string $email;
    public string $password;
    public string $created_at;

    public function __construct() {
        $this->db = Database::getInstance()->getConnection();
    }

    // ──────────────────────────────────────────────
    //  CREAR
    // ──────────────────────────────────────────────
    public function crear(): array {
        // Verificar duplicados
        if ($this->existePorEmail($this->email)) {
            return ['success' => false, 'message' => 'El correo ya está registrado', 'code' => 409];
        }
        if ($this->existePorDocumento($this->documento)) {
            return ['success' => false, 'message' => 'El documento ya está registrado', 'code' => 409];
        }

        $sql = "INSERT INTO {$this->table}
                    (documento, nombre, apellido, email, password, created_at)
                VALUES
                    (:documento, :nombre, :apellido, :email, :password, NOW())";

        $stmt = $this->db->prepare($sql);
        $stmt->execute([
            ':documento' => $this->documento,
            ':nombre'    => $this->nombre,
            ':apellido'  => $this->apellido,
            ':email'     => $this->email,
            ':password'  => $this->password, 
        ]);

        $usuario = $this->buscarPorId((int) $this->db->lastInsertId());
        return [
            'success' => true,
            'message' => 'Usuario registrado correctamente',
            'data'    => $usuario,
        ];
    }

    // ──────────────────────────────────────────────
    //  LOGIN
    // ──────────────────────────────────────────────
    public function login(string $email, string $password): array {
        $sql  = "SELECT * FROM {$this->table} WHERE email = :email LIMIT 1";
        $stmt = $this->db->prepare($sql);
        $stmt->execute([':email' => $email]);
        $row  = $stmt->fetch();

        if (!$row) {
            return ['success' => false, 'message' => 'Credenciales inválidas', 'code' => 401];
        }

        
        return [
            'success' => true,
            'message' => 'Login exitoso',
            'data'    => $this->sanitizar($row),
        ];
    }

    // ──────────────────────────────────────────────
    //  LEER TODOS
    // ──────────────────────────────────────────────
    public function obtenerTodos(): array {
        $sql  = "SELECT id, documento, nombre, apellido, email, created_at
                 FROM {$this->table}
                 ORDER BY created_at DESC";
        $stmt = $this->db->query($sql);
        return $stmt->fetchAll();
    }

    // ──────────────────────────────────────────────
    //  LEER UNO
    // ──────────────────────────────────────────────
    public function obtenerUno(int $id): array {
        $usuario = $this->buscarPorId($id);
        if (!$usuario) {
            return ['success' => false, 'message' => 'Usuario no encontrado', 'code' => 404];
        }
        return ['success' => true, 'data' => $usuario];
    }

    // ──────────────────────────────────────────────
    //  ACTUALIZAR
    // ──────────────────────────────────────────────
    public function actualizar(int $id): array {
        $usuario = $this->buscarPorId($id);
        if (!$usuario) {
            return ['success' => false, 'message' => 'Usuario no encontrado', 'code' => 404];
        }

        $sql = "UPDATE {$this->table}
                SET nombre    = :nombre,
                    apellido  = :apellido,
                    documento = :documento
                WHERE id = :id";

        $stmt = $this->db->prepare($sql);
        $stmt->execute([
            ':nombre'    => $this->nombre    ?? $usuario['nombre'],
            ':apellido'  => $this->apellido  ?? $usuario['apellido'],
            ':documento' => $this->documento ?? $usuario['documento'],
            ':id'        => $id,
        ]);

        return [
            'success' => true,
            'message' => 'Usuario actualizado',
            'data'    => $this->buscarPorId($id),
        ];
    }

    // ──────────────────────────────────────────────
    //  ELIMINAR
    // ──────────────────────────────────────────────
    public function eliminar(int $id): array {
        if (!$this->buscarPorId($id)) {
            return ['success' => false, 'message' => 'Usuario no encontrado', 'code' => 404];
        }
        $stmt = $this->db->prepare("DELETE FROM {$this->table} WHERE id = :id");
        $stmt->execute([':id' => $id]);
        return ['success' => true, 'message' => 'Usuario eliminado'];
    }

    // ──────────────────────────────────────────────
    //  HELPERS PRIVADOS
    // ──────────────────────────────────────────────
    private function buscarPorId(int $id): array|false {
        $stmt = $this->db->prepare(
            "SELECT id, documento, nombre, apellido, email, created_at
             FROM {$this->table} WHERE id = :id LIMIT 1"
        );
        $stmt->execute([':id' => $id]);
        return $stmt->fetch();
    }

    private function existePorEmail(string $email): bool {
        $stmt = $this->db->prepare(
            "SELECT id FROM {$this->table} WHERE email = :email LIMIT 1"
        );
        $stmt->execute([':email' => $email]);
        return (bool) $stmt->fetch();
    }

    private function existePorDocumento(string $doc): bool {
        $stmt = $this->db->prepare(
            "SELECT id FROM {$this->table} WHERE documento = :doc LIMIT 1"
        );
        $stmt->execute([':doc' => $doc]);
        return (bool) $stmt->fetch();
    }

    /** Quita el campo password antes de devolver datos al cliente. */
    private function sanitizar(array $row): array {
        unset($row['password']);
        return $row;
    }
}
