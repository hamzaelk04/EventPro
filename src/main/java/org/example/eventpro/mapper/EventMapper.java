package org.example.eventpro.mapper;

import org.example.eventpro.dto.eventDto.*;
import org.example.eventpro.dto.ticketCategoryDto.TicketCategoryRequest;
import org.example.eventpro.dto.ticketCategoryDto.TicketCategoryResponse;
import org.example.eventpro.entity.Event;
import org.example.eventpro.entity.TicketCategory;
import org.mapstruct.*;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;

@Mapper(componentModel = "spring")
public interface EventMapper {
    @BeanMapping(builder = @Builder(disableBuilder = true))
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "organizer", ignore = true)
    @Mapping(target = "categories", ignore = true)
    Event toEntity(EventCreateRequest request);

    @BeanMapping(builder = @Builder(disableBuilder = true))
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "event", ignore = true)
    @Mapping(target = "reservedPlaces", ignore = true)
    @Mapping(target = "soldPlaces", ignore = true)
    @Mapping(source = "name", target = "name")
    @Mapping(source = "unitPrice", target = "unitPrice")
    @Mapping(source = "totalQuantity", target = "totalQuantity")
    @Mapping(source = "quotaMaxPerClient", target = "quotaMaxPerClient")
    TicketCategory toEntity(TicketCategoryRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "organizer", ignore = true)
    @Mapping(target = "categories", ignore = true)
    void updateEntity(EventUpdateRequest request, @MappingTarget Event event);

    @Mapping(source = "organizer.id", target = "organizerId")
    EventResponse toResponse(Event event);

    @Mapping(target = "availablePlaces", expression = "java(availablePlaces(category))")
    TicketCategoryResponse toCategoryResponse(TicketCategory category);

    default EventSummaryResponse toSummary(Event event) {
        List<TicketCategory> categories = event.getCategories();
        BigDecimal minPrice = categories.stream()
                .map(TicketCategory::getUnitPrice)
                .min(Comparator.naturalOrder())
                .orElse(null);
        int totalAvailable = categories
                .stream()
                .mapToInt(this::availablePlaces)
                .sum();

        return EventSummaryResponse.builder()
                .id(event.getId())
                .title(event.getTitle())
                .address(event.getAddress())
                .startDate(event.getStartDate())
                .endDate(event.getEndDate())
                .status(event.getStatus())
                .minPrice(minPrice)
                .totalAvailablePlaces(totalAvailable)
                .build();
    }

    default EventSalesResponse toSales(Event event) {
        List<TicketCategory> categories = event.getCategories();
        int capacity = categories.stream().mapToInt(TicketCategory::getTotalQuantity).sum();
        int sold = categories.stream().mapToInt(TicketCategory::getSoldPlaces).sum();
        int reserved = categories.stream().mapToInt(TicketCategory::getReservedPlaces).sum();
        BigDecimal revenue = categories.stream()
                .map(c -> c.getUnitPrice().multiply(BigDecimal.valueOf(c.getSoldPlaces())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return EventSalesResponse.builder()
                .eventId(event.getId())
                .title(event.getTitle())
                .totalCapacity(capacity)
                .ticketsSold(sold)
                .placesReserved(reserved)
                .placesAvailable(capacity - sold - reserved)
                .grossRevenue(revenue)
                .build();
    }

    default int availablePlaces(TicketCategory c) {
        return c.getTotalQuantity() - (c.getReservedPlaces() + c.getSoldPlaces());
    }
}
