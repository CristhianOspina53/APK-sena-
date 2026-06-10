package co.edu.sena.model;

public class Usuario {
    private int id;
    private String documento;
    private String nombre;
    private String apellido;
    private String email;
    private String password;
    private boolean sincronizado;

    public Usuario() {}

    public Usuario(String documento, String nombre, String apellido, String email, String password) {
        this.documento = documento;
        this.nombre = nombre;
        this.apellido = apellido;
        this.email = email;
        this.password = password;
        this.sincronizado = false;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getDocumento() { return documento; }
    public void setDocumento(String documento) { this.documento = documento; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getApellido() { return apellido; }
    public void setApellido(String apellido) { this.apellido = apellido; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public boolean isSincronizado() { return sincronizado; }
    public void setSincronizado(boolean sincronizado) { this.sincronizado = sincronizado; }

    public String getNombreCompleto() { return nombre + " " + apellido; }
}
