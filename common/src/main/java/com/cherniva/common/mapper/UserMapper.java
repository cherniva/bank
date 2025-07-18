package com.cherniva.common.mapper;

import com.cherniva.common.model.UserDetails;
import com.cherniva.common.dto.UserAccountResponseDto;
import com.cherniva.common.dto.UserRegistrationDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper(componentModel = "spring", uses = {AccountMapper.class})
public interface UserMapper {

    UserMapper INSTANCE = Mappers.getMapper(UserMapper.class);

    @Mapping(source = "id", target = "userId")
    @Mapping(source = "accounts", target = "accounts")
    @Mapping(source = "name", target = "name", qualifiedByName = "capitalizeName")
    UserAccountResponseDto userToUserAccountResponse(UserDetails userDetails);

    @Mapping(source = "userId", target = "id")
    @Mapping(source = "accounts", target = "accounts")
    UserDetails userAccountResponseToUser(UserAccountResponseDto dto);

    // Registration mapping
    UserDetails userRegistrationToUser(UserRegistrationDto dto);

    // Custom mapping method for name formatting
    @Named("capitalizeName")
    default String capitalizeName(String name) {
        if (name == null || name.isEmpty()) {
            return name;
        }
        return name.substring(0, 1).toUpperCase() + name.substring(1).toLowerCase();
    }

    // Collection mapping
    List<UserAccountResponseDto> usersToUserAccountResponses(List<UserDetails> userDetails);
} 