package com.uca.parcialfinalncapas.controller;

import com.uca.parcialfinalncapas.dto.request.LoginRequest;
import com.uca.parcialfinalncapas.dto.request.UserCreateRequest;
import com.uca.parcialfinalncapas.dto.response.LoginResponse;
import com.uca.parcialfinalncapas.dto.response.GeneralResponse;
import com.uca.parcialfinalncapas.dto.response.UserResponse;
import com.uca.parcialfinalncapas.service.UserService;
import com.uca.parcialfinalncapas.security.JwtService;
import lombok.RequiredArgsConstructor;
import com.uca.parcialfinalncapas.utils.ResponseBuilderUtil;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final com.uca.parcialfinalncapas.security.CustomUserDetailsService userDetailsService;
    private final UserService userService;

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getCorreo(), request.getPassword()));
        UserDetails user = (UserDetails) authentication.getPrincipal();
        String token = jwtService.generateToken(user);
        return ResponseEntity.ok(LoginResponse.builder().token(token).build());
    }

    @PostMapping("/register")
    public ResponseEntity<GeneralResponse> register(@RequestBody UserCreateRequest request) {
        UserResponse user = userService.save(request);
        return ResponseBuilderUtil.buildResponse("Usuario creado", HttpStatus.CREATED, user);
    }
}
