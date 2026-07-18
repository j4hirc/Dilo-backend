package com.example.dilo.DiloBackend.dto.response;

import lombok.Data;
import java.util.List;
import com.example.dilo.DiloBackend.dto.response.NegocioResponseDTO;

@Data
public class AuthResponseDTO {

    private List<NegocioResponseDTO> businesses;

    private Long selectedBusinessId;

    private boolean needsBusinessSelection;
    private String token;
    private String tokenType = "Bearer ";
    private Long idUsuario;
    private String email;
    private String nombreCompleto;
    private String fotoPerfil;
    private boolean isSuperAdmin;

}
