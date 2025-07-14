package com.cherniva.common.mapper;

import com.cherniva.common.model.UserDetails;
import com.cherniva.common.dto.UserAccountResponseDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface UserDetailsMapper {

    UserDetailsMapper INSTANCE = Mappers.getMapper(UserDetailsMapper.class);

    @Mapping(source = "id", target = "userId")
    @Mapping(source = "accounts", target = "accounts")
    UserAccountResponseDto userToUserAccountResponse(UserDetails userDetails);

    @Mapping(source = "userId", target = "id")
    @Mapping(source = "accounts", target = "accounts")
    UserDetails userAccountResponseToUser(UserAccountResponseDto dto);
} 