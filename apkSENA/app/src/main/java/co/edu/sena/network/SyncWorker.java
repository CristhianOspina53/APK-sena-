package co.edu.sena.network;

import android.content.Context;
import android.database.Cursor;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import co.edu.sena.database.DatabaseHelper;
import co.edu.sena.model.Usuario;

/**
 * Worker que se ejecuta en background cuando hay conexión.
 * Envía al API todos los registros que están pendientes de sincronización.
 */
public class SyncWorker extends Worker {

    public static final String TAG = "SyncWorker";

    public SyncWorker(@NonNull Context context, @NonNull WorkerParameters params) {
        super(context, params);
    }

    @NonNull
    @Override
    public Result doWork() {
        DatabaseHelper db = new DatabaseHelper(getApplicationContext());
        ApiService api = new ApiService();

        if (!ApiService.hayConexion(getApplicationContext())) {
            Log.d(TAG, "Sin conexión, reintentando más tarde.");
            return Result.retry();
        }

        Cursor cursor = db.obtenerNoSincronizados();
        int sincronizados = 0;
        int fallidos = 0;

        if (cursor.moveToFirst()) {
            do {
                Usuario u = new Usuario();
                u.setDocumento(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_DOCUMENTO)));
                u.setNombre(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_NOMBRE)));
                u.setApellido(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_APELLIDO)));
                u.setEmail(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_EMAIL)));
                u.setPassword(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_PASSWORD)));

                boolean ok = api.registrarUsuario(u);
                if (ok) {
                    db.marcarSincronizado(u.getEmail());
                    sincronizados++;
                    Log.d(TAG, "Sincronizado: " + u.getEmail());
                } else {
                    fallidos++;
                    Log.w(TAG, "Falló sincronizar: " + u.getEmail());
                }
            } while (cursor.moveToNext());
        }
        cursor.close();

        Log.d(TAG, "Sync finalizado. OK=" + sincronizados + " Fallidos=" + fallidos);
        return fallidos > 0 ? Result.retry() : Result.success();
    }
}
