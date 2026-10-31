package org.example.eventpro.service;

import org.example.eventpro.dto.userDto.*;
import org.example.eventpro.dto.enums.Role;
import org.example.eventpro.entity.Admin;
import org.example.eventpro.entity.Client;
import org.example.eventpro.entity.Organizer;
import org.example.eventpro.entity.abstracts.User;
import org.example.eventpro.exception.userException.EmailAlreadyUsedException;
import org.example.eventpro.exception.userException.ResourceNotFoundException;
import org.example.eventpro.exception.userException.ValidationException;
import org.example.eventpro.mapper.UserMapperImpl;
import org.example.eventpro.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.List;
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

    //    =====READ
    @Test
    @DisplayName("getById: unknown id throws ResourceNotFoundException")
    void getById_notFound_throws() {
        UUID fixedId = UUID.randomUUID();
        when(userRepository.findById(fixedId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getById(fixedId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining(fixedId.toString());
    }

    @Test
    @DisplayName("getById: returns the right role for each entity type")
    void getById_returnsRole() {
        UUID fixedId = UUID.randomUUID();

        Admin admin = Admin.builder().id(fixedId).name("Admin").email("admin@events.ma").build();
        when(userRepository.findById(fixedId)).thenReturn(Optional.of(admin));

        UserResponse response = userService.getById(fixedId);

        assertThat(response.getRole()).isEqualTo(Role.ADMIN);
        assertThat(response.getEmail()).isEqualTo("admin@events.ma");
    }

    @Test
    @DisplayName("getClientEntity: an id that is not a client throws ResourceNotFoundException")
    void getClientEntity_notAClient_throws() {
        UUID fixedId = UUID.randomUUID();

        when(clientRepository.findById(fixedId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getClientEntity(fixedId))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    //    ========== List ======
    @Test
    @DisplayName("list: without role uses UserRepository, negative page becomes 0, size is capped at 100")
    void list_noRole_clampsPaging() {
        Client client = Client.builder().id(UUID.randomUUID()).name("A").email("a@x.ma").build();
        Page<User> page = new PageImpl<>(List.<User>of(client));
        when(userRepository.findAll(any(Pageable.class))).thenReturn(page);

        PageResponse<UserResponse> result = userService.list(null, -5, 1000);

        ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);
        verify(userRepository).findAll(captor.capture());
        assertThat(captor.getValue().getPageNumber()).isZero();
        assertThat(captor.getValue().getPageSize()).isEqualTo(100);
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getRole()).isEqualTo(Role.CLIENT);
    }

    @Test
    @DisplayName("list: with role CLIENT only the client repository is queried")
    void list_roleClient_usesClientRepository() {
        Client client = Client.builder().id(UUID.randomUUID()).name("A").email("a@x.ma").build();
        Page<Client> page = new PageImpl<>(List.of(client));
        when(clientRepository.findAll(any(Pageable.class))).thenReturn(page);

        PageResponse<UserResponse> result = userService.list(Role.CLIENT, 0, 20);

        assertThat(result.getTotalElements()).isEqualTo(1);
        verifyNoInteractions(userRepository);
    }

    // ======================= UPDATE =======================

    @Test
    @DisplayName("update client: name and phone change, email and id do not")
    void update_client_success() {
        UUID fixedId = UUID.randomUUID();
        Client client = Client.builder()
                .id(fixedId).name("Old").email("old@mail.com").phone("0600000000").build();
        when(userRepository.findById(fixedId)).thenReturn(Optional.of(client));
        UpdateUserRequest request = UpdateUserRequest.builder()
                .name("  New Name ").phone("0611111111").build();

        UserResponse response = userService.update(fixedId, request);

        assertThat(response.getName()).isEqualTo("New Name");
        assertThat(response.getPhone()).isEqualTo("0611111111");
        assertThat(response.getEmail()).isEqualTo("old@mail.com");
        assertThat(response.getId()).isEqualTo(fixedId);
    }

    @Test
    @DisplayName("update client: no phone in the request clears it (PUT = full replacement)")
    void update_client_withoutPhone_clearsPhone() {
        UUID fixedId = UUID.randomUUID();
        Client client = Client.builder()
                .id(fixedId).name("Old").email("old@mail.com").phone("0600000000").build();
        when(userRepository.findById(fixedId)).thenReturn(Optional.of(client));

        UserResponse response = userService.update(fixedId, UpdateUserRequest.builder().name("Old").build());

        assertThat(response.getPhone()).isNull();
    }

    @Test
    @DisplayName("update client: nameOrganisation is rejected")
    void update_client_withOrganization_throws() {
        UUID fixedId = UUID.randomUUID();
        Client client = Client.builder().id(fixedId).name("A").email("a@x.ma").build();
        when(userRepository.findById(fixedId)).thenReturn(Optional.of(client));
        UpdateUserRequest request = UpdateUserRequest.builder()
                .name("A").organizationName("Atlas").build();

        assertThatThrownBy(() -> userService.update(fixedId, request))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("organizationName");
    }
}
