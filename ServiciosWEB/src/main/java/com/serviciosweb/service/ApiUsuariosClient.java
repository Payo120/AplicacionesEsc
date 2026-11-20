package com.serviciosweb.service;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.serviciosweb.model.PerfilUsuario;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.time.Duration;
import java.util.List;

public class ApiUsuariosClient {

    private static final String ENDPOINT_DEFAULT = "https://jsonplaceholder.typicode.com/users";
    private static final Duration TIMEOUT = Duration.ofSeconds(15);

    private final URI endpoint;
    private final HttpClient httpClient;
    private final ObjectMapper mapper;

    public ApiUsuariosClient() {
        this(ENDPOINT_DEFAULT);
    }

    public ApiUsuariosClient(String endpointUrl) {
        this.endpoint = URI.create(endpointUrl);
        HttpClient.Builder builder = HttpClient.newBuilder()
                .connectTimeout(TIMEOUT)
                .followRedirects(HttpClient.Redirect.NORMAL);

        SSLContext contextoSsl = resolverContextoSsl();
        if (contextoSsl != null) {
            builder.sslContext(contextoSsl);
        }

        this.httpClient = builder.build();
        this.mapper = new ObjectMapper();
    }

    public List<PerfilUsuario> cargarDirectorio() throws IOException, InterruptedException {
        HttpRequest solicitud = HttpRequest.newBuilder()
                .uri(endpoint)
                .timeout(TIMEOUT)
                .header("Accept", "application/json")
                .GET()
                .build();

        HttpResponse<String> respuesta = httpClient.send(
                solicitud,
                HttpResponse.BodyHandlers.ofString()
        );

        int codigo = respuesta.statusCode();
        if (codigo < 200 || codigo >= 300) {
            throw new IOException("El servidor respondió con código HTTP " + codigo);
        }

        return parsearRespuesta(respuesta.body());
    }

    public List<PerfilUsuario> parsearRespuesta(String contenidoJson) throws IOException {
        if (contenidoJson == null || contenidoJson.isBlank()) {
            throw new IOException("La respuesta del servicio llegó vacía");
        }

        List<UsuarioDto> registros = mapper.readValue(
                contenidoJson,
                new TypeReference<List<UsuarioDto>>() {}
        );

        return registros.stream()
                .map(ApiUsuariosClient::convertir)
                .toList();
    }

    public String getEndpoint() {
        return endpoint.toString();
    }

    private static SSLContext resolverContextoSsl() {
        String sistema = System.getProperty("os.name", "").toLowerCase();
        if (!sistema.contains("win")) {
            return null;
        }
        try {
            KeyStore almacen = KeyStore.getInstance("WINDOWS-ROOT");
            almacen.load(null, null);
            TrustManagerFactory fabrica = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            fabrica.init(almacen);
            SSLContext contexto = SSLContext.getInstance("TLS");
            contexto.init(null, fabrica.getTrustManagers(), null);
            return contexto;
        } catch (GeneralSecurityException | IOException e) {
            return null;
        }
    }

    private static PerfilUsuario convertir(UsuarioDto dto) {
        String ciudad = dto.address() != null ? normalizar(dto.address().city()) : "";
        String organizacion = dto.company() != null ? normalizar(dto.company().name()) : "";

        return new PerfilUsuario(
                dto.id(),
                normalizar(dto.name()),
                normalizar(dto.username()),
                normalizar(dto.email()),
                normalizar(dto.phone()),
                ciudad,
                organizacion,
                normalizar(dto.website())
        );
    }

    private static String normalizar(String texto) {
        return texto == null ? "" : texto.trim();
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    record UsuarioDto(
            int id,
            String name,
            String username,
            String email,
            String phone,
            String website,
            DireccionDto address,
            CompaniaDto company
    ) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    record DireccionDto(String city) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    record CompaniaDto(String name) {
    }
}
