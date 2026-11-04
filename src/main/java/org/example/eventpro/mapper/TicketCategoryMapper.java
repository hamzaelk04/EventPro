package org.example.eventpro.mapper;

import org.example.eventpro.dto.ticketCategoryDto.TicketCategoryRequest;
import org.example.eventpro.dto.ticketCategoryDto.TicketCategoryResponse;
import org.example.eventpro.entity.TicketCategory;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring")
public interface TicketCategoryMapper {

    @BeanMapping(builder = @Builder(disableBuilder = true))
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "event", ignore = true)
    @Mapping(target = "reservedPlaces", ignore = true)
    @Mapping(target = "soldPlaces", ignore = true)
    TicketCategory toEntity(TicketCategoryRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "event", ignore = true)
    @Mapping(target = "reservedPlaces", ignore = true)
    @Mapping(target = "soldPlaces", ignore = true)
    void updateEntity(TicketCategoryRequest request, @MappingTarget TicketCategory category);

    @Mapping(target = "availablePlaces", expression = "java(category.availablePlaces())")
    TicketCategoryResponse toResponse(TicketCategory category);

    List<TicketCategoryResponse> toResponseList(List<TicketCategory> categories);
}
