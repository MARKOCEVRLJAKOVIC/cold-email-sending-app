package dev.marko.EmailSender.mappers;

import dev.marko.EmailSender.dtos.RegisterUserRequest;
import dev.marko.EmailSender.dtos.UserDto;
import dev.marko.EmailSender.entities.User;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface UserMapper {

    UserDto toDto(User user);
    User toEntity(RegisterUserRequest request);

    void update(RegisterUserRequest request, @MappingTarget User user);

}
