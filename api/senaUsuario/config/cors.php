<?php
/**
 * Cabeceras CORS — permite peticiones desde la app Android (cualquier origen).
 * En producción restringe Access-Control-Allow-Origin al dominio/IP real.
 */
function setCorsHeaders(): void {
    header('Access-Control-Allow-Origin: *');
    header('Access-Control-Allow-Methods: GET, POST, PUT, DELETE, OPTIONS');
    header('Access-Control-Allow-Headers: Content-Type, Authorization, X-Requested-With');
    header('Content-Type: application/json; charset=utf-8');

    // Pre-flight OPTIONS
    if ($_SERVER['REQUEST_METHOD'] === 'OPTIONS') {
        http_response_code(200);
        exit;
    }
}
