    package com.example.dilo.DiloBackend.dto.request;

    import jakarta.validation.constraints.NotBlank;
    import jakarta.validation.constraints.NotNull;
    import lombok.Data;

    @Data
    public class NegocioRequestDTO {

        @NotBlank(message = "El RUC es obligatorio")
        private String ruc;

        @NotBlank(message = "La razón social es obligatoria")
        private String razonSocial;

        @NotBlank(message = "El nombre comercial es obligatorio")
        private String nombreComercial;

        @NotBlank(message = "La dirección de la matriz es obligatoria para el SRI")
        private String direccion;

        @NotNull(message = "Debe especificar si está obligado a llevar contabilidad")
        private Boolean obligadoContabilidad;

    }