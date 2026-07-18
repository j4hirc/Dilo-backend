package com.example.dilo.DiloBackend.dto.response;

import lombok.Data;
import java.util.List;
import com.example.dilo.DiloBackend.dto.response.NegocioResponseDTO;

@Data
public class AuthResponseDTO {

    private List<NegocioResponseDTO> businesses;
    private Long selectedBusinessId;
    
    private List<String> roles; // List of roles in the selected business
    private String rol; // The primary role (if only one)
    private boolean needsRoleSelection; // True if they have multiple roles
    
    private boolean needsBusinessSelection;
    private String token;
    private String tokenType = "Bearer ";
    private Long idUsuario;
    private String email;
    private String nombreCompleto;
    private String fotoPerfil;
    private boolean isSuperAdmin;

}
