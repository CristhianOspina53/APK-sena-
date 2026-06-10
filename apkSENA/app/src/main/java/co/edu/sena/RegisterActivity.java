package co.edu.sena;

import android.content.Intent;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.ProgressBar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.work.Constraints;
import androidx.work.NetworkType;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import co.edu.sena.database.DatabaseHelper;
import co.edu.sena.model.Usuario;
import co.edu.sena.network.ApiService;
import co.edu.sena.network.SyncWorker;
import co.edu.sena.utils.SessionManager;

public class RegisterActivity extends AppCompatActivity {

    private TextInputLayout tilDocument, tilName, tilLastname, tilEmail, tilPassword;
    private TextInputEditText etDocument, etName, etLastname, etEmail, etPassword;
    private MaterialButton btnRegister, btnGoLogin;
    private ProgressBar progressBar;
    private Chip chipOfflineStatus;

    private DatabaseHelper db;
    private SessionManager session;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        db = new DatabaseHelper(this);
        session = new SessionManager(this);

        tilDocument = findViewById(R.id.tilDocument);
        tilName = findViewById(R.id.tilName);
        tilLastname = findViewById(R.id.tilLastname);
        tilEmail = findViewById(R.id.tilEmail);
        tilPassword = findViewById(R.id.tilPassword);
        etDocument = findViewById(R.id.etDocument);
        etName = findViewById(R.id.etName);
        etLastname = findViewById(R.id.etLastname);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnRegister = findViewById(R.id.btnRegister);
        btnGoLogin = findViewById(R.id.btnGoLogin);
        progressBar = findViewById(R.id.progressBar);
        chipOfflineStatus = findViewById(R.id.chipOfflineStatus);

        // Mostrar chip offline si no hay conexión
        actualizarEstadoConexion();

        btnRegister.setOnClickListener(v -> intentarRegistro());
        btnGoLogin.setOnClickListener(v -> finish());
    }

    @Override
    protected void onResume() {
        super.onResume();
        actualizarEstadoConexion();
    }

    private void actualizarEstadoConexion() {
        boolean conectado = ApiService.hayConexion(this);
        chipOfflineStatus.setVisibility(conectado ? View.GONE : View.VISIBLE);
    }

    private void intentarRegistro() {
        String documento = etDocument.getText() != null ? etDocument.getText().toString().trim() : "";
        String nombre = etName.getText() != null ? etName.getText().toString().trim() : "";
        String apellido = etLastname.getText() != null ? etLastname.getText().toString().trim() : "";
        String email = etEmail.getText() != null ? etEmail.getText().toString().trim() : "";
        String password = etPassword.getText() != null ? etPassword.getText().toString().trim() : "";

        // Limpiar errores
        tilDocument.setError(null);
        tilName.setError(null);
        tilLastname.setError(null);
        tilEmail.setError(null);
        tilPassword.setError(null);

        // ---- Validaciones ----
        boolean valido = true;

        if (documento.isEmpty()) {
            tilDocument.setError("El documento es obligatorio");
            valido = false;
        } else if (documento.length() < 6 || documento.length() > 15) {
            tilDocument.setError(getString(R.string.error_invalid_document));
            valido = false;
        }

        if (nombre.isEmpty()) {
            tilName.setError("El nombre es obligatorio");
            valido = false;
        } else if (nombre.length() < 2) {
            tilName.setError("Nombre muy corto");
            valido = false;
        }

        if (apellido.isEmpty()) {
            tilLastname.setError("El apellido es obligatorio");
            valido = false;
        } else if (apellido.length() < 2) {
            tilLastname.setError("Apellido muy corto");
            valido = false;
        }

        if (email.isEmpty()) {
            tilEmail.setError("El correo es obligatorio");
            valido = false;
        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            tilEmail.setError(getString(R.string.error_invalid_email));
            valido = false;
        }

        if (password.isEmpty()) {
            tilPassword.setError("La contraseña es obligatoria");
            valido = false;
        } else if (password.length() < 6) {
            tilPassword.setError(getString(R.string.error_short_password));
            valido = false;
        }

        if (!valido) return;

        setLoading(true);

        String finalDocumento = documento;
        String finalNombre = nombre;
        String finalApellido = apellido;
        String finalEmail = email;
        String finalPassword = password;

        executor.execute(() -> {
            // Verificar duplicado local
            if (db.existeEmailODocumento(finalEmail, finalDocumento)) {
                mainHandler.post(() -> {
                    setLoading(false);
                    tilEmail.setError("Este correo o documento ya está registrado");
                });
                return;
            }

            Usuario u = new Usuario(finalDocumento, finalNombre, finalApellido, finalEmail, finalPassword);
            boolean hayConexion = ApiService.hayConexion(RegisterActivity.this);

            if (hayConexion) {
                // Intentar registrar en el API primero
                ApiService api = new ApiService();
                boolean apiOk = api.registrarUsuario(u);
                u.setSincronizado(apiOk);
            }

            // Guardar siempre en SQLite (sincronizado o pendiente)
            long id = db.insertarUsuario(u);

            if (id == -1) {
                mainHandler.post(() -> {
                    setLoading(false);
                    Snackbar.make(btnRegister, "Error al guardar. Intenta de nuevo.",
                        Snackbar.LENGTH_LONG).show();
                });
                return;
            }

            // Si no está sincronizado, programar WorkManager para cuando haya red
            if (!u.isSincronizado()) {
                programarSincronizacion();
            }

            mainHandler.post(() -> {
                setLoading(false);
                String msg = u.isSincronizado()
                    ? getString(R.string.success_register)
                    : getString(R.string.offline_register);

                Snackbar.make(btnRegister, msg, Snackbar.LENGTH_LONG)
                    .addCallback(new com.google.android.material.snackbar.Snackbar.Callback() {
                        @Override
                        public void onDismissed(com.google.android.material.snackbar.Snackbar snackbar, int event) {
                            // Ir al login luego del snackbar
                            session.guardarSesion(u);
                            startActivity(new Intent(RegisterActivity.this, HomeActivity.class));
                            finish();
                        }
                    }).show();
            });
        });
    }

    /** Programa un trabajo con WorkManager que se dispara cuando haya red. */
    private void programarSincronizacion() {
        Constraints constraints = new Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build();

        OneTimeWorkRequest syncRequest = new OneTimeWorkRequest.Builder(SyncWorker.class)
            .setConstraints(constraints)
            .addTag(SyncWorker.TAG)
            .build();

        WorkManager.getInstance(getApplicationContext())
            .enqueueUniqueWork(
                SyncWorker.TAG,
                androidx.work.ExistingWorkPolicy.REPLACE,
                syncRequest
            );
    }

    private void setLoading(boolean loading) {
        progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
        btnRegister.setEnabled(!loading);
        btnGoLogin.setEnabled(!loading);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executor.shutdown();
    }
}
