package org.example.eventpro.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.eventpro.dto.*;
import org.example.eventpro.dto.enums.Role;
import org.example.eventpro.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.UUID;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Tag(name = "Users", description = "Gestion des utilisateurs (client, organisateur, agent, admin)")
public class UserController {
    private final UserService userService;

//    create
    @PostMapping("/clients")
    @Operation(summary = "Créer un client")
    public ResponseEntity<UserResponse> createClient(
            @Valid @RequestBody CreateClientRequest request
    ) { return created(userService.createClient(request)); }

    @PostMapping("/organizers")
    @Operation(summary = "Créer un organisateur")
    public ResponseEntity<UserResponse> createOrganizer(
            @Valid @RequestBody CreateOrganizerRequest request) {
        return created(userService.createOrganizer(request));
    }

    @PostMapping("/agents")
    @Operation(summary = "Créer un agent de contrôle")
    public ResponseEntity<UserResponse> createAgentControl(
            @Valid @RequestBody CreateAgentRequest request) {
        return created(userService.createAgentControl(request));
    }

//    read
    @GetMapping
    @Operation(summary = "Lister les utilisateurs (admin), avec filtre optionnel par rôle")
    public ResponseEntity<PageResponse<UserResponse>> list(
            @RequestParam(required = false)Role role,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
            ) { return ResponseEntity.ok(userService.list(role, page, size));}

    @GetMapping("/{id}")
    @Operation(summary = "Consulter un utilisateur")
    public ResponseEntity<UserResponse> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(userService.getById(id));
    }

//    update
@PutMapping("/{id}")
@Operation(summary = "Modifier un utilisateur (l'email n'est pas modifiable)")
public ResponseEntity<UserResponse> update(
        @PathVariable UUID id,
        @Valid @RequestBody UpdateUserRequest request) {
    return ResponseEntity.ok(userService.update(id, request));
}

//    Helper
    private ResponseEntity<UserResponse> created(UserResponse body) {
        return  ResponseEntity
                .created(URI.create("/api/users/" + body.getId()))
                .body(body);
    }
}
