<?php
class Database {
    private static $instance = null;
    private PDO $pdo;

    private string $host    = 'localhost';
    private string $dbname  = 'sena_db';
    private string $user    = 'root';
    private string $pass    = '123456';
    private string $charset = 'utf8mb4';

    private function __construct() {
        $dsn     = "mysql:host={$this->host};dbname={$this->dbname};charset={$this->charset}";
        $options = [
            PDO::ATTR_ERRMODE            => PDO::ERRMODE_EXCEPTION,
            PDO::ATTR_DEFAULT_FETCH_MODE => PDO::FETCH_ASSOC,
            PDO::ATTR_EMULATE_PREPARES   => false,
        ];
        try {
            $this->pdo = new PDO($dsn, $this->user, $this->pass, $options);
        } catch (PDOException $e) {
            http_response_code(500);
            echo json_encode(['success' => false, 'message' => 'Error de conexión a la base de datos']);
            exit;
        }
    }

    public static function getInstance(): Database {
        if (self::$instance === null) {
            self::$instance = new self();
        }
        return self::$instance;
    }

    public function getConnection(): PDO {
        return $this->pdo;
    }
}
