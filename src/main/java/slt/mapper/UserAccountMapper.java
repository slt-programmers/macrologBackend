package slt.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.NullValueMappingStrategy;
import org.mapstruct.factory.Mappers;
import slt.database.entities.UserAccount;
import slt.dto.UserAccountDto;

import java.util.List;

@Mapper(nullValueMappingStrategy = NullValueMappingStrategy.RETURN_DEFAULT)
public interface UserAccountMapper {

    UserAccountMapper INSTANCE = Mappers.getMapper(UserAccountMapper.class);

    UserAccountDto map(final UserAccount userAccount, final String token);

    List<UserAccountDto> map(final List<UserAccount> userAccounts);
}
