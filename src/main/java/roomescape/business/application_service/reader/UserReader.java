package roomescape.business.application_service.reader;

import roomescape.business.dto.UserDto;

import java.util.List;

public interface UserReader {

    List<UserDto> getAll();

    UserDto getById(String userIdValue);
}
