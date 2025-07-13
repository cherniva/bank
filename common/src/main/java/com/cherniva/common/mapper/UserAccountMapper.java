package com.cherniva.common.mapper;

import com.cherniva.common.model.User;
import com.cherniva.common.dto.UserAccountResponseDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper(componentModel = "spring", uses = {AccountMapper.class})
public interface UserAccountMapper {

    UserAccountMapper INSTANCE = Mappers.getMapper(UserAccountMapper.class);

    @Mapping(source = "id", target = "userId")
    @Mapping(source = "accounts", target = "accounts")
    @Mapping(source = "name", target = "name", qualifiedByName = "capitalizeName")
    UserAccountResponseDto userToUserAccountResponse(User user);

    @Mapping(source = "userId", target = "id")
    @Mapping(source = "accounts", target = "accounts")
    User userAccountResponseToUser(UserAccountResponseDto dto);

    // Custom mapping method for name formatting
    @Named("capitalizeName")
    default String capitalizeName(String name) {
        if (name == null || name.isEmpty()) {
            return name;
        }
        return name.substring(0, 1).toUpperCase() + name.substring(1).toLowerCase();
    }

    // Collection mapping
    List<UserAccountResponseDto> usersToUserAccountResponses(List<User> users);
} 