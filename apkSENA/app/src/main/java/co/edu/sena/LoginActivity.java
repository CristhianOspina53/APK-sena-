package co.edu.sena;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.ProgressBar;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import co.edu.sena.database.DatabaseHelper;
import co.edu.sena.model.Usuario;
import co.edu.sena.network.ApiService;
import co.edu.sena.utils.SessionManager;

public class LoginActivity extends AppCompatActivity {

    private TextInputLayout tilEmail, tilPassword;
    private TextInputEditText etEmail, etPassword;
    private MaterialButton btnLogin, btnGoRegister;
    private ProgressBar progressBar;

    private DatabaseHelper db;
    private SessionManager session;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        db = new DatabaseHelper(this);
        session = new SessionManager(this);

        // Si ya hay sesión, ir directo al Home
        if (session.haySesionActiva()) {
            irAHome();
            return;
        }

        setContentView(R.layout.activity_login);

        tilEmail = findViewById(R.id.tilEmail);
        tilPassword = findViewById(R.id.tilPassword);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        btnGoRegister = findViewById(R.id.btnGoRegister);
        progressBar = findViewById(R.id.progressBar);

        btnLogin.setOnClickListener(v -> intentarLogin());
        btnGoRegister.setOnClickListener(v ->
            startActivity(new Intent(this, RegisterActivity.class)));
    }

    private void intentarLogin() {
        String email = etEmail.getText() != null ? etEmail.getText().toString().trim() : "";
        String password = etPassword.getText() != null ? etPassword.getText().toString().trim() : "";

        // Limpiar errores previos
        tilEmail.setError(null);
        tilPassword.setError(null);

        // Validaciones básicas
        if (email.isEmpty()) {
            tilEmail.setError("Ingresa tu correo electrónico");
            return;
        }
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            tilEmail.setError(getString(R.string.error_invalid_email));
            return;
        }
        if (password.isEmpty()) {
            tilPassword.setError("Ingresa tu contraseña");
            return;
        }

        setLoading(true);

        executor.execute(() -> {
            // 1. Buscar localmente en SQLite
            Usuario usuario = db.buscarPorLogin(email, password);

            if (usuario != null) {
                // Encontrado local → entrar

                mainHandler.post(() -> {
                    setLoading(false);
                    session.guardarSesion(usuario);
                    irAHome();
                });
                return;
            }

            // 2. No está local: consultar API si hay conexión
            if (ApiService.hayConexion(LoginActivity.this)) {
                ApiService api = new ApiService();
                Usuario usuarioApi = api.loginEnApi(email, password);

                if (usuarioApi != null) {
                    // Guardar localmente para futuros logins offline
                    usuarioApi.setSincronizado(true);
                    long id = db.insertarUsuario(usuarioApi);
                    if (id == -1) {
                        // Ya existía pero con distinta contraseña —
                        // igual dejamos entrar porque el API lo validó.
                    }
                    mainHandler.post(() -> {
                        setLoading(false);
                        session.guardarSesion(usuarioApi);
                        irAHome();
                    });
                    return;
                }
            }

            // 3. No encontrado ni local ni en API
            mainHandler.post(() -> {
                setLoading(false);
                Snackbar.make(btnLogin, getString(R.string.error_login),
                    Snackbar.LENGTH_LONG).show();
            });
        });
    }

    private void irAHome() {
        startActivity(new Intent(this, HomeActivity.class));
        finish();
    }

    private void setLoading(boolean loading) {
        progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
        btnLogin.setEnabled(!loading);
        btnGoRegister.setEnabled(!loading);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executor.shutdown();
    }
}
