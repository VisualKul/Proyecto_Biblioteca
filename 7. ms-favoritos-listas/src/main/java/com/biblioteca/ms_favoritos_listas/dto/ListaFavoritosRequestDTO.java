package com.biblioteca.ms_favoritos_listas.dto;

import jakarta.validation.constraints.*;
import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class ListaFavoritosRequestDTO {

    @NotBlank(message = "El nombre es obligatorio")
    @Size(min = 3, max = 100)
    private String nombre;

    @Size(max = 255)
    private String descripcion;

    @NotBlank(message = "El emailUsuario es obligatorio")
    @Email(message = "El emailUsuario debe ser un correo valido")
    private String emailUsuario;

    @NotNull(message = "Debe indicar si la lista es publica")
    private Boolean publica;
}
