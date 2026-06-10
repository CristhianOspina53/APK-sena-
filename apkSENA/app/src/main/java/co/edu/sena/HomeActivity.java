package co.edu.sena;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.work.Constraints;
import androidx.work.NetworkType;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.snackbar.Snackbar;

import android.widget.TextView;

import co.edu.sena.network.ApiService;
import co.edu.sena.network.SyncWorker;
import co.edu.sena.utils.SessionManager;

public class HomeActivity extends AppCompatActivity {

    private SessionManager session;
    private Chip chipSyncStatus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        session = new SessionManager(this);

        if (!session.haySesionActiva()) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        setContentView(R.layout.activity_home);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        TextView tvWelcome = findViewById(R.id.tvWelcome);
        TextView tvUserName = findViewById(R.id.tvUserName);
        TextView tvUserEmail = findViewById(R.id.tvUserEmail);
        TextView tvDocument = findViewById(R.id.tvDocument);
        TextView tvSyncInfo = findViewById(R.id.tvSyncInfo);
        MaterialButton btnLogout = findViewById(R.id.btnLogout);
        chipSyncStatus = findViewById(R.id.chipSyncStatus);

        // Datos del usuario
        tvWelcome.setText("¡Bienvenido!");
        tvUserName.setText(session.getNombreCompleto());
        tvUserEmail.setText(session.getEmail());
        tvDocument.setText("Documento: " + session.getDocumento());

        // Estado de sincronización
        boolean conectado = ApiService.hayConexion(this);
        tvSyncInfo.setText(conectado ? "✓ Conectado al servidor" : "⚠ Modo sin conexión");
        tvSyncInfo.setTextColor(conectado
            ? getResources().getColor(R.color.sena_green, null)
            : getResources().getColor(R.color.error_red, null));

        // Intentar sincronizar pendientes si hay conexión
        if (conectado) {
            verificarSincronizacionPendiente();
        }

        btnLogout.setOnClickListener(v -> {
            session.cerrarSesion();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        });
    }

    private void verificarSincronizacionPendiente() {
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
                androidx.work.ExistingWorkPolicy.KEEP,
                syncRequest
            );

        WorkManager.getInstance(getApplicationContext())
            .getWorkInfosByTagLiveData(SyncWorker.TAG)
            .observe(this, workInfos -> {
                if (workInfos != null && !workInfos.isEmpty()) {
                    androidx.work.WorkInfo info = workInfos.get(0);
                    if (info.getState() == androidx.work.WorkInfo.State.RUNNING) {
                        chipSyncStatus.setVisibility(View.VISIBLE);
                    } else if (info.getState() == androidx.work.WorkInfo.State.SUCCEEDED
                        || info.getState() == androidx.work.WorkInfo.State.FAILED) {
                        chipSyncStatus.setVisibility(View.GONE);
                    }
                }
            });
    }
}
