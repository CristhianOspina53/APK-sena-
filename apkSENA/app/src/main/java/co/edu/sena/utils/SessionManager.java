package co.edu.sena.utils;

import android.content.Context;
import android.content.SharedPreferences;

import co.edu.sena.model.Usuario;

/**
 * Maneja la sesión activa del usuario usando SharedPreferences.
 */
public class SessionManager {

    private static final String PREF_NAME = "sena_session";
    private static final String KEY_LOGGED_IN = "is_logged_in";
    private static final String KEY_EMAIL = "email";
    private static final String KEY_NOMBRE = "nombre";
    private static final String KEY_APELLIDO = "apellido";
    private static final String KEY_DOCUMENTO = "documento";

    private final SharedPreferences prefs;

    public SessionManager(Context context) {
        prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public void guardarSesion(Usuario u) {
        prefs.edit()
            .putBoolean(KEY_LOGGED_IN, true)
            .putString(KEY_EMAIL, u.getEmail())
            .putString(KEY_NOMBRE, u.getNombre())
            .putString(KEY_APELLIDO, u.getApellido())
            .putString(KEY_DOCUMENTO, u.getDocumento())
            .apply();
    }

    public boolean haySesionActiva() {
        return prefs.getBoolean(KEY_LOGGED_IN, false);
    }

    public String getEmail() { return prefs.getString(KEY_EMAIL, ""); }
    public String getNombre() { return prefs.getString(KEY_NOMBRE, ""); }
    public String getApellido() { return prefs.getString(KEY_APELLIDO, ""); }
    public String getDocumento() { return prefs.getString(KEY_DOCUMENTO, ""); }
    public String getNombreCompleto() { return getNombre() + " " + getApellido(); }

    public void cerrarSesion() {
        prefs.edit().clear().apply();
    }
}
