package org.example.eventpro.service;

import lombok.RequiredArgsConstructor;
import org.example.eventpro.dto.*;
import org.example.eventpro.dto.enums.Role;
import org.example.eventpro.entity.*;
import org.example.eventpro.entity.abstracts.User;
import org.example.eventpro.exception.EmailAlreadyUsedException;
import org.example.eventpro.exception.ResourceNotFoundException;
import org.example.eventpro.exception.ValidationException;
import org.example.eventpro.mapper.UserMapper;
import org.example.eventpro.repository.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {

    private static final int MAX_PAGE_SIZE = 100;

    private final UserRepository userRepository;
    private final ClientRepository clientRepository;
    private final OrganizerRepository organizerRepository;
    private final AgentControlRepository agentControlRepository;
    private final AdminRepository adminRepository;
    private final UserMapper userMapper;

    // =====================================================
    // CREATE
    // =====================================================

    @Transactional
    public UserResponse createClient(CreateClientRequest request) {
        String email = normalizeAndCheckEmail(request.getEmail());
        Client client = userMapper.toEntity(request);
        client.setEmail(email);
        client.setName(request.getName().trim());
        return userMapper.toResponse(clientRepository.save(client));
    }

    @Transactional
    public UserResponse createOrganizer(CreateOrganizerRequest request) {
        String email = normalizeAndCheckEmail(request.getEmail());
        Organizer organizer = userMapper.toEntity(request);
        organizer.setEmail(email);
        organizer.setName(request.getName().trim());
        organizer.setOrganizationName(request.getOrganizationName().trim());
        return userMapper.toResponse(organizerRepository.save(organizer));
    }

    @Transactional
    public UserResponse createAgentControl(CreateAgentRequest request) {
        String email = normalizeAndCheckEmail(request.getEmail());
        AgentControl agent = userMapper.toEntity(request);
        agent.setEmail(email);
        agent.setName(request.getName().trim());
        return userMapper.toResponse(agentControlRepository.save(agent));
    }

//    @Transactional
//    public UserResponse createAdmin(CreateAdminRequest request) {
//        String email = normalizeAndCheckEmail(request.getEmail());
//        Admin admin = userMapper.toEntity(request);
//        admin.setEmail(email);
//        admin.setName(request.getName().trim());
//        return userMapper.toResponse(adminRepository.save(admin));
//    }

    // =====================================================
    // READ
    // =====================================================

    @Transactional(readOnly = true)
    public UserResponse getById(UUID id) {
        return userMapper.toResponse(findUserOrThrow(id));
    }

    @Transactional(readOnly = true)
    public PageResponse<UserResponse> list(Role role, int page, int size) {
        int safePage = Math.max(page, 0);
        int safeSize = Math.min(Math.max(size, 1), MAX_PAGE_SIZE);
        Pageable pageable = PageRequest.of(safePage, safeSize, Sort.by("id"));

        Page<? extends User> result = findPage(role, pageable);
        return PageResponse.from(result.map(userMapper::toResponse));
    }

    // =====================================================
    // UPDATE
    // =====================================================

    @Transactional
    public UserResponse update(UUID id, UpdateUserRequest request) {
        User user = findUserOrThrow(id);

        if (user instanceof Client client) {
            rejectIfPresent(request.getOrganizationName(), "nomOrganisation", "un client");
            userMapper.updateClient(request, client);

        } else if (user instanceof Organizer organizer) {
            rejectIfPresent(request.getPhone(), "telephone", "un organisateur");
            if (request.getOrganizationName() == null || request.getOrganizationName().isBlank()) {
                throw new ValidationException("Le nom de l'organisation est obligatoire");
            }
            userMapper.updateOrganizer(request, organizer);
            organizer.setOrganizationName(organizer.getOrganizationName().trim());

        } else if (user instanceof AgentControl agent) {
            rejectIfPresent(request.getPhone(), "telephone", "un agent");
            rejectIfPresent(request.getOrganizationName(), "nomOrganisation", "un agent");
            userMapper.updateAgent(request, agent);

        } else if (user instanceof Admin admin) {
            rejectIfPresent(request.getPhone(), "telephone", "un administrateur");
            rejectIfPresent(request.getOrganizationName(), "nomOrganisation", "un administrateur");
            userMapper.updateAdmin(request, admin);
        }

        user.setName(user.getName().trim());
        // no save() needed: the entity is managed, so Hibernate flushes the changes at commit
        return userMapper.toResponse(user);
    }

    // =====================================================
    // LOOKUPS RETURNING ENTITIES (used later by the event and reservation services)
    // =====================================================

    @Transactional(readOnly = true)
    public Client getClientEntity(UUID id) {
        return clientRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Client", id));
    }

    @Transactional(readOnly = true)
    public Organizer getOrganizerEntity(UUID id) {
        return organizerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Organisateur", id));
    }

    // =====================================================
    // PRIVATE HELPERS
    // =====================================================

    private User findUserOrThrow(UUID id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur", id));
    }

    private String normalizeAndCheckEmail(String rawEmail) {
        String email = rawEmail.trim().toLowerCase(Locale.ROOT);
        if (userRepository.existsByEmail(email)) {
            throw new EmailAlreadyUsedException(email);
        }
        return email;
    }

    private void rejectIfPresent(String value, String field, String roleLabel) {
        if (value != null && !value.isBlank()) {
            throw new ValidationException(
                    "Le champ '" + field + "' n'est pas applicable pour " + roleLabel);
        }
    }

    private Page<? extends User> findPage(Role role, Pageable pageable) {
        if (role == null) {
            return userRepository.findAll(pageable);
        }
        return switch (role) {
            case CLIENT    -> clientRepository.findAll(pageable);
            case ORGANIZER -> organizerRepository.findAll(pageable);
            case AGENT     -> agentControlRepository.findAll(pageable);
            case ADMIN     -> adminRepository.findAll(pageable);
        };
    }
}