package com.fixflow.controller;

import com.fixflow.dto.JwtResponse;
import com.fixflow.dto.LoginRequest;
import com.fixflow.dto.RegistroRequest;
import com.fixflow.model.Usuario;
import com.fixflow.repository.UsuarioRepository;
import com.fixflow.security.JwtUtil;
import com.fixflow.service.UsuarioService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final UsuarioRepository usuarioRepository;
    private final UsuarioService usuarioService;
    private final JwtUtil jwtUtil;

    public AuthController(AuthenticationManager authenticationManager,
                          UsuarioRepository usuarioRepository,
                          UsuarioService usuarioService,
                          JwtUtil jwtUtil) {
        this.authenticationManager = authenticationManager;
        this.usuarioRepository = usuarioRepository;
        this.usuarioService = usuarioService;
        this.jwtUtil = jwtUtil;
    }

    @PostMapping("/auth/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest loginRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPassword())
        );

        Usuario usuario = usuarioRepository.findByEmail(loginRequest.getEmail())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        String token = jwtUtil.generateToken(authentication);
        return ResponseEntity.ok(new JwtResponse(token, usuario.getIdUsuario(), usuario.getEmail(), usuario.getRol().name()));
    }

    @PostMapping("/auth/registro")
    public ResponseEntity<?> register(@Valid @RequestBody RegistroRequest request) {
        Usuario nuevo = usuarioService.registrarCliente(request);
        String token = jwtUtil.generateTokenFromEmail(nuevo.getEmail());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new JwtResponse(token, nuevo.getIdUsuario(), nuevo.getEmail(), nuevo.getRol().name()));
    }
}
