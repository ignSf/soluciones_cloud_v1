package com.example.demo.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Table(name = "products")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 120, message = "El nombre no puede superar los 120 caracteres")
    @Column(nullable = false, length = 120)
    private String name;

    @Size(max = 500, message = "La descripción no puede superar los 500 caracteres")
    @Column(length = 500)
    private String description;

    @NotNull(message = "El precio es obligatorio")
    @PositiveOrZero(message = "El precio debe ser mayor o igual a cero")
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal price;

    @Size(max = 50, message = "La categoría no puede superar los 50 caracteres")
    @Column(length = 50)
    private String category;
}
