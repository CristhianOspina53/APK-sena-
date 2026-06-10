package co.edu.sena.network;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkCapabilities;
import android.os.Build;
import android.util.Log;




import org.json.JSONObject;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import co.edu.sena.model.Usuario;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ApiService {

    private static final String BASE_URL = "http://192.168.56.1/api/senaUsuario";
    private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

    private final OkHttpClient client;

    public ApiService() {
        client = new OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(10, TimeUnit.SECONDS)
            .writeTimeout(10, TimeUnit.SECONDS)
            .build();
    }

    /** Verifica si hay conexión a internet activa. */
    public static boolean hayConexion(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm == null) return false;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            NetworkCapabilities nc = cm.getNetworkCapabilities(cm.getActiveNetwork());
            return nc != null && (
                nc.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                nc.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ||
                nc.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)
            );
        } else {
            android.net.NetworkInfo info = cm.getActiveNetworkInfo();
            return info != null && info.isConnected();
        }
    }

    /**
     * Registra un usuario en el API remoto.
     * @return true si fue exitoso, false en caso contrario.
     */
    public boolean registrarUsuario(Usuario u) {
        try {
            JSONObject json = new JSONObject();
            json.put("documento", u.getDocumento());
            json.put("nombre", u.getNombre());
            json.put("apellido", u.getApellido());
            json.put("email", u.getEmail());
            json.put("password", u.getPassword());

            RequestBody body = RequestBody.create(json.toString(), JSON);
            Request request = new Request.Builder()
                .url(BASE_URL+"/")
                .post(body)
                .addHeader("Accept", "application/json")
                .build();

            try (Response response = client.newCall(request).execute()) {
                Log.d("API", "registrar status: " + response.code());
                return response.isSuccessful();
            }
        } catch (Exception e) {
            Log.e("API", "registrarUsuario error: " + e.getMessage());
            return false;
        }
    }

    /**
     * Consulta un usuario en el API por email y contraseña.
     * @return Usuario si existe, null si no o hay error.
     */
    public Usuario loginEnApi(String email, String password) {
        try {
            JSONObject json = new JSONObject();
            json.put("email", email);
            json.put("password", password);

            RequestBody body = RequestBody.create(json.toString(), JSON);
            Request request = new Request.Builder()
                .url(BASE_URL + "/login")
                .post(body)
                .addHeader("Accept", "application/json")
                .build();

            try (Response response = client.newCall(request).execute()) {
                if (response.isSuccessful() && response.body() != null) {
                    String responseBody = response.body().string();
                    JSONObject obj = new JSONObject(responseBody);
                    // El API debe devolver los datos del usuario
                    if (obj.optBoolean("success", false)) {
                        // ✅ Entrar al objeto "data" primero
                        JSONObject data = obj.getJSONObject("data");

                        Usuario u = new Usuario();
                        u.setDocumento(data.optString("documento", ""));
                        u.setNombre(data.optString("nombre", ""));
                        u.setApellido(data.optString("apellido", ""));
                        u.setEmail(data.optString("email", ""));
                        u.setPassword(password);
                        u.setSincronizado(true);
                        return u;
                    }
                }
            }
        } catch (Exception e) {
            Log.e("API", "loginEnApi error: " + e.getMessage());
        }
        return null;
    }
}
