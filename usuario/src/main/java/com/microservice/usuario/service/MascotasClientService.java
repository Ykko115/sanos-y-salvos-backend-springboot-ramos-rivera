package com.microservice.usuario.service;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import com.microservice.usuario.entitie.dto.Mascotas;

@Service
public class MascotasClientService {

    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${mascotas.service.url:http://mascotas:8082}")
    private String mascotasServiceUrl;

    public List<Mascotas> obtenerMascotasPorUsuarioId(Long usuarioId) {
        try {
            String url = mascotasServiceUrl + "/api/mascotas/usuario/" + usuarioId;
            Mascotas[] mascotas = restTemplate.getForObject(url, Mascotas[].class);
            if (mascotas == null) {
                return Collections.emptyList();
            }
            return Arrays.asList(mascotas);
        } catch (RestClientException ex) {
            return Collections.emptyList();
        }
    }
}
