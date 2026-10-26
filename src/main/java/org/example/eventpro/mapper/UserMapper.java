package org.example.eventpro.mapper;

import org.example.eventpro.dto.*;
import org.example.eventpro.entity.*;

import org.example.eventpro.entity.abstracts.User;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface UserMapper {

    //    1.Request
    @BeanMapping(builder = @Builder(disableBuilder = true))
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "reservations", ignore = true)
    Client toEntity(CreateClientRequest request);

    @BeanMapping(builder = @Builder(disableBuilder = true))
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "events", ignore = true)
    @Mapping(target = "promotions", ignore = true)
    Organizer toEntity(CreateOrganizerRequest request);

    @BeanMapping(builder = @Builder(disableBuilder = true))
    @Mapping(target = "id", ignore = true)
    AgentControl toEntity(CreateAgentRequest request);

    //    2.Response
    @Mapping(target = "role", constant = "CLIENT")
    @Mapping(target = "organizationName", ignore = true)
    UserResponse clientToResponse(Client client);

    @Mapping(target = "role", constant = "ORGANIZER")
    @Mapping(target = "phone", ignore = true)
    UserResponse organizerToResponse(Organizer organizer);

    @Mapping(target = "role", constant = "AGENT")
    @Mapping(target = "phone", ignore = true)
    @Mapping(target = "organizationName", ignore = true)
    UserResponse agentToResponse(AgentControl agent);

    @Mapping(target = "role", constant = "ADMIN")
    @Mapping(target = "phone", ignore = true)
    @Mapping(target = "organizationName", ignore = true)
    UserResponse adminToResponse(Admin admin);

    default UserResponse toResponse(User u) {
        if (u instanceof Client c) return clientToResponse(c);
        if (u instanceof Organizer o) return organizerToResponse(o);
        if (u instanceof AgentControl a) return agentToResponse(a);
        if (u instanceof Admin ad) return adminToResponse(ad);
        throw new IllegalArgumentException("Type d'utilisateur inconnu : " + u.getClass());
    }


    //    3. update
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "email", ignore = true)
    @Mapping(target = "reservations", ignore = true)
    void updateClient(UpdateUserRequest request, @MappingTarget Client client);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "email", ignore = true)
    @Mapping(target = "events", ignore = true)
    @Mapping(target = "promotions", ignore = true)
    void updateOrganizer(UpdateUserRequest request, @MappingTarget Organizer organizer);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "email", ignore = true)
    void updateAgent(UpdateUserRequest request, @MappingTarget AgentControl agent);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "email", ignore = true)
    void updateAdmin(UpdateUserRequest request, @MappingTarget Admin admin);
}
