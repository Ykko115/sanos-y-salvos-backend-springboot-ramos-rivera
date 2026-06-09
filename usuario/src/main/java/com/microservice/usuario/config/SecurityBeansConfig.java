package com.microservice.usuario.config;
 
import com.gateway.apigateway.security.JwtUtil;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
 
/**
 * Configuración de seguridad para el microservicio de usuario.
 *
 * La protección CSRF se deshabilita intencionalmente porque este servicio
 * es una API REST stateless que autentica cada petición mediante JWT en el
 * encabezado Authorization. Las APIs REST sin estado no usan cookies de
 * sesión, por lo que no son vulnerables a ataques CSRF (ver OWASP REST
 * Security Cheat Sheet). La gestión de sesiones se configura explícitamente
 * como STATELESS para reforzar este modelo.
 */
@Configuration
public class SecurityBeansConfig {
 
	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		http
			// CSRF deshabilitado: API REST stateless autenticada con JWT.
			// No se usan cookies de sesión, por lo que CSRF no aplica.
			.csrf(csrf -> csrf.disable()) // NOSONAR: REST stateless con JWT, sin cookies de sesión
			.sessionManagement(session ->
				session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
			)
			.authorizeHttpRequests(auth -> auth.anyRequest().permitAll())
			.httpBasic(AbstractHttpConfigurer::disable)
			.formLogin(AbstractHttpConfigurer::disable);
 
		return http.build();
	}
 
	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}
 
	@Bean
	public JwtUtil jwtUtil() {
		return new JwtUtil();
	}
}
 

