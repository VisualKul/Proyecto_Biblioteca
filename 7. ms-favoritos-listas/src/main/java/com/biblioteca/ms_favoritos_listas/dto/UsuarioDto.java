package com.biblioteca.ms_favoritos_listas.dto;

import lombok.Data;

/** DTO ligero del usuario expuesto por user-service via Feign. */
@Data
public class UsuarioDto {
    private Long id;
    private String email;
    private String nombre;
}
