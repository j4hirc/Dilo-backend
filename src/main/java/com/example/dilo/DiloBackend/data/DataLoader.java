package com.example.dilo.DiloBackend.data;

import com.example.dilo.DiloBackend.model.Role;
import com.example.dilo.DiloBackend.model.Usuario;
import com.example.dilo.DiloBackend.repository.RoleRepository;
import com.example.dilo.DiloBackend.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Component
@RequiredArgsConstructor
public class DataLoader implements CommandLineRunner {

    private final RoleRepository roleRepository;
    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final MiembroEspacioRepository miembroEspacioRepository;

    @Override
    public void run(String... args) throws Exception {


        List<String> rolesToCreate = Arrays.asList("SUPER_ADMIN", "PROPIETARIO", "VENDEDOR", "BODEGUERO");

        for (String roleName : rolesToCreate) {
            roleRepository.findByNombre(roleName).orElseGet(() -> {
                Role newRole = new Role();
                newRole.setNombre(roleName);
                newRole.setDescripcion("Rol de " + roleName.toLowerCase() + " del sistema");
                return roleRepository.save(newRole);
            });
        }

        Role superAdminRole = roleRepository.findByNombre("SUPER_ADMIN").get();
        if (usuarioRepository.findByEmail("admin@dilo.com").isEmpty()) {

            Usuario admin = new Usuario();
            admin.setDni("9999999999");
            admin.setPrimerNombre("Super");
            admin.setApellidoPaterno("Admin");
            admin.setEmail("admin@dilo.com");
            admin.setPassword(passwordEncoder.encode("admin123"));

            admin = usuarioRepository.save(admin);

            MiembroEspacio miembroEspacio = new MiembroEspacio();
            miembroEspacio.setUsuario(admin);
            miembroEspacio.setRol(superAdminRole);
            miembroEspacio.setEspacio(null);
            miembroEspacio.setEstadoInvitacion("ACEPTADO");

            miembroEspacioRepository.save(miembroEspacio);

            System.out.println("Roles iniciales y Usuario Super Admin creados con éxito.");
        }

    }
}
