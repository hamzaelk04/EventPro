package org.example.eventpro.mapper;

import org.example.eventpro.dto.reservationDto.ReservationResponse;
import org.example.eventpro.entity.Reservation;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring", uses = ReservationLineMapper.class)
public interface ReservationMapper {

    @Mapping(source = "client.id", target = "clientId")
    @Mapping(source = "reservationLines", target = "lines")
    @Mapping(target = "totalAmount", expression = "java(reservation.totalAmount())")
    ReservationResponse toResponse(Reservation reservation);

    List<ReservationResponse> toResponseList(List<Reservation> reservations);
}