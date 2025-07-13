package com.cherniva.common.mapper;

import com.cherniva.common.model.User;
import com.cherniva.common.dto.UserAccountResponseDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface UserMapper {

    UserMapper INSTANCE = Mappers.getMapper(UserMapper.class);

    @Mapping(source = "id", target = "userId")
    @Mapping(source = "accounts", target = "accounts")
    UserAccountResponseDto userToUserAccountResponse(User user);

    @Mapping(source = "userId", target = "id")
    @Mapping(source = "accounts", target = "accounts")
    User userAccountResponseToUser(UserAccountResponseDto dto);
} 