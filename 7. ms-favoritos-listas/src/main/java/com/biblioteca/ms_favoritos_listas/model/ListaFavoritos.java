package com.biblioteca.ms_favoritos_listas.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "listas_favoritos")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ListaFavoritos {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String nombre;

    @Column(length = 255)
    private String descripcion;

    /** Email del propietario (validado contra user-service via Feign). */
    @Column(nullable = false)
    private String emailUsuario;

    @Column(nullable = false)
    private Boolean publica;
}
