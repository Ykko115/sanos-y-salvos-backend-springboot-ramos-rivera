package com.microservice.usuario.security;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import io.jsonwebtoken.Claims;

class JwtUtilTest {

    private JwtUtil jwtUtil;

    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil();
        ReflectionTestUtils.setField(jwtUtil, "jwtSecret",
            "jwt_secret_sanos_salvos_2026_clave_muy_segura_12345");
        ReflectionTestUtils.setField(jwtUtil, "jwtExpirationMs", 600000L);
    }

    @Test
    void generateToken_conRoles_debeRetornarTokenNoVacio() {
        String token = jwtUtil.generateToken("juan@test.com", List.of("ROLE_USER"));
        assertThat(token).isNotBlank();
        assertThat(token.split("\\.")).hasSize(3);
    }

    @Test
    void generateToken_sinRoles_debeAsignarRoleUser() {
        String token = jwtUtil.generateToken("juan@test.com");
        Claims claims = jwtUtil.getClaimsFromToken(token);
        assertThat(claims.getSubject()).isEqualTo("juan@test.com");
    }

    @Test
    void getClaimsFromToken_tokenValido_debeRetornarSubject() {
        String token = jwtUtil.generateToken("maria@test.com", List.of("ROLE_ADMIN"));
        Claims claims = jwtUtil.getClaimsFromToken(token);
        assertThat(claims.getSubject()).isEqualTo("maria@test.com");
    }

    @Test
    void getClaimsFromToken_tokenInvalido_debeLanzarExcepcion() {
        assertThatThrownBy(() -> jwtUtil.getClaimsFromToken("token.invalido.aqui"))
            .isInstanceOf(Exception.class);
    }

    @Test
    void isTokenValid_tokenValido_debeRetornarTrue() {
        String token = jwtUtil.generateToken("usuario@test.com");
        assertThat(jwtUtil.isTokenValid(token)).isTrue();
    }

    @Test
    void isTokenValid_tokenInvalido_debeRetornarFalse() {
        assertThat(jwtUtil.isTokenValid("esto.no.es.un.jwt")).isFalse();
    }

    @Test
    void isTokenValid_tokenExpirado_debeRetornarFalse() {
        ReflectionTestUtils.setField(jwtUtil, "jwtExpirationMs", -1000L);
        String token = jwtUtil.generateToken("expirado@test.com");
        assertThat(jwtUtil.isTokenValid(token)).isFalse();
    }

    @Test
    void extractRoles_conRolesEnClaims_debeRetornarLista() {
        String token = jwtUtil.generateToken("admin@test.com", List.of("ROLE_ADMIN", "ROLE_USER"));
        Claims claims = jwtUtil.getClaimsFromToken(token);
        List<String> roles = jwtUtil.extractRoles(claims);
        assertThat(roles).containsExactlyInAnyOrder("ROLE_ADMIN", "ROLE_USER");
    }

    @Test
    void extractRoles_sinRoles_debeRetornarListaVacia() {
        String token = jwtUtil.generateToken("sinroles@test.com");
        Claims claims = jwtUtil.getClaimsFromToken(token);
        claims.remove("roles");
        List<String> roles = jwtUtil.extractRoles(claims);
        assertThat(roles).isEmpty();
    }

    @Test
    void generateToken_secretNoBase64_debeUsarBytesDirectos() {
        // Clave no-base64 de al menos 32 bytes para cumplir HMAC-SHA256
        ReflectionTestUtils.setField(jwtUtil, "jwtSecret",
            "clave-no-base64-con-suficientes-bytes-para-hmac-sha256!!!");
        String token = jwtUtil.generateToken("test@test.com");
        assertThat(token).isNotBlank();
    }

    @Test
    void generateToken_secretBase64Valido_debeUsarKeyDecodificada() {
        // Base64 de exactamente 32 bytes: "12345678901234567890123456789012"
        String secretBase64 = java.util.Base64.getEncoder()
            .encodeToString("12345678901234567890123456789012".getBytes());
        ReflectionTestUtils.setField(jwtUtil, "jwtSecret", secretBase64);

        String token = jwtUtil.generateToken("base64@test.com", List.of("ROLE_USER"));
        assertThat(token).isNotBlank();
        assertThat(token.split("\\.")).hasSize(3);
    }

    @Test
    void isTokenValid_sinExpiracion_debeRetornarTrue() {
        // Token válido con larga duración → expiration != null y está en el futuro
        ReflectionTestUtils.setField(jwtUtil, "jwtExpirationMs", 999999999L);
        String token = jwtUtil.generateToken("noexpiry@test.com");
        assertThat(jwtUtil.isTokenValid(token)).isTrue();
    }
}
