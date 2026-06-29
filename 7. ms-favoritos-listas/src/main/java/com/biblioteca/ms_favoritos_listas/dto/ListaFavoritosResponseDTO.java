package com.biblioteca.ms_favoritos_listas.dto;

import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ListaFavoritosResponseDTO {
    private Long id;
    private String nombre;
    private String descripcion;
    private String emailUsuario;
    private Boolean publica;
}
