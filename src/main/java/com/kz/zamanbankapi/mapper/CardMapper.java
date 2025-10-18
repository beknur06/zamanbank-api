package com.kz.zamanbankapi.mapper;

import com.kz.zamanbankapi.dao.entities.Card;
import com.kz.zamanbankapi.dto.CardDto;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE,
        componentModel = MappingConstants.ComponentModel.SPRING)
public interface CardMapper {
    CardDto toCardDto(Card card);
}
