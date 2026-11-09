package org.example.eventpro.mapper;

import org.example.eventpro.dto.reservationDto.ReservationLineResponse;
import org.example.eventpro.entity.ReservationLine;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ReservationLineMapper {

    @Mapping(source = "category.id", target = "categoryId")
    @Mapping(source = "category.name", target = "categoryName")
    @Mapping(source = "promotion.code", target = "promoCode")
    ReservationLineResponse toResponse(ReservationLine line);

    List<ReservationLineResponse> toResponseList(List<ReservationLine> lines);
}