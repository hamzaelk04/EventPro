package org.example.eventpro.mapper;

import org.example.eventpro.dto.promotionDto.PromotionRequest;
import org.example.eventpro.dto.promotionDto.PromotionResponse;
import org.example.eventpro.entity.Promotion;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring")
public interface PromotionMapper {

    @BeanMapping(builder = @Builder(disableBuilder = true))
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "usageCount", ignore = true)
    @Mapping(target = "ticketCategory", ignore = true)
    Promotion toEntity(PromotionRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "usageCount", ignore = true)
    @Mapping(target = "ticketCategory", ignore = true)
    void updateEntity(PromotionRequest request, @MappingTarget Promotion promotion);

    @Mapping(source = "ticketCategory.id", target = "ticketCategoryId")
    @Mapping(source = "ticketCategory.name", target = "ticketCategoryName")
    PromotionResponse toResponse(Promotion promotion);

    List<PromotionResponse> toResponseList(List<Promotion> promotions);
}
