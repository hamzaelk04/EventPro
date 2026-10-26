package org.example.eventpro.service;

import org.example.eventpro.dto.CreateAgentRequest;
import org.example.eventpro.dto.CreateClientRequest;
import org.example.eventpro.dto.CreateOrganizerRequest;
import org.example.eventpro.dto.UserResponse;
import org.example.eventpro.dto.enums.Role;
import org.example.eventpro.entity.Client;
import org.example.eventpro.entity.Organizer;
import org.example.eventpro.exception.EmailAlreadyUsedException;
import org.example.eventpro.exception.ResourceNotFoundException;
import org.example.eventpro.mapper.UserMapperImpl;
import org.example.eventpro.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {
    @Mock
    private UserRepository userRepository;
    @Mock
    private ClientRepository clientRepository;
    @Mock
    private OrganizerRepository organizerRepository;
    @Mock
    private AgentControlRepository agentControlRepository;
    @Mock
    private AdminRepository adminRepository;

    private UserService userService;

    @BeforeEach
    void setUp() {
        userService = new UserService(
                userRepository, clientRepository, organizerRepository,
                agentControlRepository, adminRepository, new UserMapperImpl());
    }

//    ===Create test===

    @Test
    @DisplayName("createClient: email is trimmed and lowercased, name is trimmed, role is CLIENT")
    void createClient_normalizesFields() {
        UUID random = UUID.randomUUID();
        CreateClientRequest request = CreateClientRequest.builder()
                .name("Hamza")
                .email("hamza@gmail.com")
                .phone("0652454482")
                .build();
        when(userRepository.existsByEmail("hamza@gmail.com")).thenReturn(false);
        when(clientRepository.save(any(Client.class))).thenAnswer(invocation -> {
            Client c = invocation.getArgument(0);
            c.setId(random);
            return c;
        });

        UserResponse response = userService.createClient(request);
        ArgumentCaptor<Client> captor = ArgumentCaptor.forClass(Client.class);
        verify(clientRepository).save(captor.capture());
        assertThat(captor.getValue().getEmail()).isEqualTo("hamza@gmail.com");
        assertThat(captor.getValue().getName()).isEqualTo("Hamza");

        assertThat(response.getId()).isEqualTo(random);
        assertThat(response.getRole()).isEqualTo(Role.CLIENT);
        assertThat(response.getPhone()).isEqualTo("0652454482");
        assertThat(response.getOrganizationName()).isNull();
    }

    @Test
    @DisplayName("createClient: duplicate email throws EmailAlreadyUsedException and nothing is saved")
    void createClient_duplicateEmail_throws() {
        CreateClientRequest request = CreateClientRequest.builder()
                .name("Ahmed").email("AHMED@mail.com").build();
        when(userRepository.existsByEmail("ahmed@mail.com")).thenReturn(true);

        assertThatThrownBy(() -> userService.createClient(request))
                .isInstanceOf(EmailAlreadyUsedException.class)
                .hasMessageContaining("ahmed@mail.com");

        verify(clientRepository, never()).save(any());
    }

    @Test
    @DisplayName("createOrganizer: organization name is trimmed, role is ORGANIZER, no phone")
    void createOrganizer_success() {
        UUID fixedId = UUID.randomUUID();
        CreateOrganizerRequest request = CreateOrganizerRequest.builder()
                .name("Sara").email("sara@events.ma").organizationName("  Atlas Events ").build();
        when(userRepository.existsByEmail("sara@events.ma")).thenReturn(false);
        when(organizerRepository.save(any(Organizer.class))).thenAnswer(inv -> {
            Organizer o = inv.getArgument(0);
            o.setId(fixedId);
            return o;
        });

        UserResponse response = userService.createOrganizer(request);

        assertThat(response.getRole()).isEqualTo(Role.ORGANIZER);
        assertThat(response.getOrganizationName()).isEqualTo("Atlas Events");
        assertThat(response.getPhone()).isNull();
    }

    @Test
    @DisplayName("createAgentControl: duplicate email is rejected across roles")
    void createAgentControl_duplicateEmail_throws() {
        CreateAgentRequest request = CreateAgentRequest.builder()
                .name("Youssef").email("youssef@events.ma").build();
        when(userRepository.existsByEmail("youssef@events.ma")).thenReturn(true);

        assertThatThrownBy(() -> userService.createAgentControl(request))
                .isInstanceOf(EmailAlreadyUsedException.class);

        verify(agentControlRepository, never()).save(any());
    }
}
