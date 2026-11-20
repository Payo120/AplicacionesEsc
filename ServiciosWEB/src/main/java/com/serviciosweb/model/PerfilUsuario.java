package com.serviciosweb.model;

public record PerfilUsuario(
        int id,
        String nombreCompleto,
        String usuario,
        String correo,
        String telefono,
        String ciudad,
        String organizacion,
        String sitioWeb
) {
    public boolean coincideCon(String filtro) {
        if (filtro == null || filtro.isBlank()) {
            return true;
        }
        String texto = filtro.trim().toLowerCase();
        return String.valueOf(id).contains(texto)
                || contiene(nombreCompleto, texto)
                || contiene(usuario, texto)
                || contiene(correo, texto)
                || contiene(telefono, texto)
                || contiene(ciudad, texto)
                || contiene(organizacion, texto)
                || contiene(sitioWeb, texto);
    }

    private static boolean contiene(String valor, String busqueda) {
        return valor != null && valor.toLowerCase().contains(busqueda);
    }
}
