package com.kz.zamanbankapi.mapper;

import com.kz.zamanbankapi.dao.entities.User;
import com.kz.zamanbankapi.dto.UserDto;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE,
        componentModel = MappingConstants.ComponentModel.SPRING)
public interface UserMapper {
    UserDto toUserDto(User user);
}
