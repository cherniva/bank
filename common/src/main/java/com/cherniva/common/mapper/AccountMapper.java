package com.cherniva.common.mapper;

import com.cherniva.common.dto.AccountDto;
import com.cherniva.common.model.Account;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface AccountMapper {

    AccountMapper INSTANCE = Mappers.getMapper(AccountMapper.class);

    @Mapping(source = "id", target = "accountId")
    @Mapping(source = "userDetails.id", target = "userDetailsId")
    @Mapping(source = "currency.code", target = "currencyCode")
    @Mapping(source = "currency.name", target = "currencyName")
    AccountDto accountToAccountDto(Account account);

    @Mapping(source = "accountId", target = "id")
    @Mapping(target = "userDetails.id", source = "userDetailsId")
    @Mapping(target = "currency.code", source = "currencyCode")
    @Mapping(target = "currency.name", source = "currencyName")
    Account accountDtoToAccount(AccountDto accountDto);
} 