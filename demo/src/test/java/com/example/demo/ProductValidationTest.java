package com.example.demo;

import com.example.demo.model.Product;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class ProductValidationTest {

    private static Validator validator;

    @BeforeAll
    static void setUpValidator() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    @DisplayName("Un producto válido con datos correctos no genera violaciones")
    void validProductShouldPassValidation() {
        Product product = Product.builder()
                .name("Servidor Cloud")
                .description("Instancia de cómputo en la nube")
                .price(new BigDecimal("199.99"))
                .category("Cloud")
                .build();

        Set<ConstraintViolation<Product>> violations = validator.validate(product);
        assertTrue(violations.isEmpty(), "Un producto con datos válidos no debe tener violaciones");
    }

    @Test
    @DisplayName("Un producto con nombre vacío o nulo debe fallar validación (@NotBlank)")
    void blankNameShouldFailValidation() {
        Product product = Product.builder()
                .name("   ")
                .price(new BigDecimal("100.00"))
                .build();

        Set<ConstraintViolation<Product>> violations = validator.validate(product);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("name")));
    }

    @Test
    @DisplayName("Un producto con precio negativo debe fallar validación (@PositiveOrZero)")
    void negativePriceShouldFailValidation() {
        Product product = Product.builder()
                .name("Producto Económico")
                .price(new BigDecimal("-10.50"))
                .build();

        Set<ConstraintViolation<Product>> violations = validator.validate(product);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("price")));
    }

    @Test
    @DisplayName("Un producto con precio nulo debe fallar validación (@NotNull)")
    void nullPriceShouldFailValidation() {
        Product product = Product.builder()
                .name("Producto Sin Precio")
                .price(null)
                .build();

        Set<ConstraintViolation<Product>> violations = validator.validate(product);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("price")));
    }
}
