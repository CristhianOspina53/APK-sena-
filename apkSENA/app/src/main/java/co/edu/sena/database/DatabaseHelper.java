package co.edu.sena.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import co.edu.sena.model.Usuario;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DB_NAME = "sena.db";
    private static final int DB_VERSION = 1;

    public static final String TABLE_USUARIOS = "usuarios";
    public static final String COL_ID = "id";
    public static final String COL_DOCUMENTO = "documento";
    public static final String COL_NOMBRE = "nombre";
    public static final String COL_APELLIDO = "apellido";
    public static final String COL_EMAIL = "email";
    public static final String COL_PASSWORD = "password";
    public static final String COL_SINCRONIZADO = "sincronizado";

    private static final String CREATE_TABLE =


        "CREATE TABLE " + TABLE_USUARIOS + " (" +
            COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            COL_DOCUMENTO + " TEXT UNIQUE NOT NULL, " +
            COL_NOMBRE + " TEXT NOT NULL, " +
            COL_APELLIDO + " TEXT NOT NULL, " +
            COL_EMAIL + " TEXT UNIQUE NOT NULL, " +
            COL_PASSWORD + " TEXT NOT NULL, " +
            COL_SINCRONIZADO + " INTEGER DEFAULT 0" +
        ")";

    public DatabaseHelper(Context context) {

        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USUARIOS);
        onCreate(db);
    }

    /** Inserta un usuario. Retorna -1 si el email/documento ya existe. */
    public long insertarUsuario(Usuario u) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COL_DOCUMENTO, u.getDocumento());
        cv.put(COL_NOMBRE, u.getNombre());
        cv.put(COL_APELLIDO, u.getApellido());
        cv.put(COL_EMAIL, u.getEmail());
        cv.put(COL_PASSWORD, u.getPassword());
        cv.put(COL_SINCRONIZADO, u.isSincronizado() ? 1 : 0);
        try {
            return db.insertOrThrow(TABLE_USUARIOS, null, cv);
        } catch (Exception e) {
            Log.e("DB", "insertarUsuario error: " + e.getMessage());
            return -1;
        } finally {
            db.close();
        }
    }

    /** Busca usuario por email y contraseña para login. */
    public Usuario buscarPorLogin(String email, String password) {
        SQLiteDatabase db = getReadableDatabase();
        //select * from usuarios where email=? and password=?
        Cursor c = db.query(
                TABLE_USUARIOS, null,COL_EMAIL + "=? AND " + COL_PASSWORD + "=?",

            new String[]{email, password}, null, null, null);

        Usuario u = null;
        if (c.moveToFirst()) {
            u = cursorToUsuario(c);
        }
        c.close();
        db.close();
        return u;
    }

    /** Busca por email (para verificar existencia o traer datos del API). */
    public Usuario buscarPorEmail(String email) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.query(TABLE_USUARIOS, null,
            COL_EMAIL + "=?", new String[]{email}, null, null, null);
        Usuario u = null;
        if (c.moveToFirst()) {
            u = cursorToUsuario(c);
        }
        c.close();
        db.close();
        return u;
    }

    /** Verifica si el email o documento ya está registrado localmente. */
    public boolean existeEmailODocumento(String email, String documento) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.query(TABLE_USUARIOS, new String[]{COL_ID},
            COL_EMAIL + "=? OR " + COL_DOCUMENTO + "=?",
            new String[]{email, documento}, null, null, null);
        boolean existe = c.getCount() > 0;
        c.close();
        db.close();
        return existe;
    }

    /** Retorna todos los usuarios que aún no se han sincronizado con el API. */
    public Cursor obtenerNoSincronizados() {
        SQLiteDatabase db = getReadableDatabase();
        return db.query(TABLE_USUARIOS, null,
            COL_SINCRONIZADO + "=0", null, null, null, null);
    }

    /** Marca un usuario como sincronizado dado su email. */
    public void marcarSincronizado(String email) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COL_SINCRONIZADO, 1);
        db.update(TABLE_USUARIOS, cv, COL_EMAIL + "=?", new String[]{email});
        db.close();
    }

    private Usuario cursorToUsuario(Cursor c) {
        Usuario u = new Usuario();
        u.setId(c.getInt(c.getColumnIndexOrThrow(COL_ID)));
        u.setDocumento(c.getString(c.getColumnIndexOrThrow(COL_DOCUMENTO)));
        u.setNombre(c.getString(c.getColumnIndexOrThrow(COL_NOMBRE)));
        u.setApellido(c.getString(c.getColumnIndexOrThrow(COL_APELLIDO)));
        u.setEmail(c.getString(c.getColumnIndexOrThrow(COL_EMAIL)));
        u.setPassword(c.getString(c.getColumnIndexOrThrow(COL_PASSWORD)));
        u.setSincronizado(c.getInt(c.getColumnIndexOrThrow(COL_SINCRONIZADO)) == 1);
        return u;
    }
}
