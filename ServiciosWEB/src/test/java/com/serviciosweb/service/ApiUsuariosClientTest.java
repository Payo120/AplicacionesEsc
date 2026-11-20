package com.serviciosweb.service;

import com.serviciosweb.model.PerfilUsuario;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ApiUsuariosClientTest {

    @Test
    void parseaListadoJsonCorrectamente() throws IOException {
        String json = """
                [
                  {
                    "id": 1,
                    "name": "Leanne Graham",
                    "username": "Bret",
                    "email": "Sincere@april.biz",
                    "phone": "1-770-736-8031",
                    "website": "hildegard.org",
                    "address": { "city": "Gwenborough" },
                    "company": { "name": "Romaguera-Crona" }
                  }
                ]
                """;

        ApiUsuariosClient client = new ApiUsuariosClient();
        List<PerfilUsuario> resultado = client.parsearRespuesta(json);

        assertEquals(1, resultado.size());
        PerfilUsuario primero = resultado.getFirst();
        assertEquals(1, primero.id());
        assertEquals("Leanne Graham", primero.nombreCompleto());
        assertEquals("Bret", primero.usuario());
        assertEquals("Sincere@april.biz", primero.correo());
        assertEquals("1-770-736-8031", primero.telefono());
        assertEquals("Gwenborough", primero.ciudad());
        assertEquals("Romaguera-Crona", primero.organizacion());
        assertEquals("hildegard.org", primero.sitioWeb());
        assertTrue(primero.coincideCon("gwen"));
        assertFalse(primero.coincideCon("inexistente"));
    }

    @Test
    void rechazaContenidoVacio() {
        ApiUsuariosClient client = new ApiUsuariosClient();
        assertThrows(IOException.class, () -> client.parsearRespuesta("   "));
    }
}
