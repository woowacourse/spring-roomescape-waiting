package roomescape.business.service.reader;

import roomescape.business.dto.UserDto;

import java.util.List;

public interface UserReader {

    List<UserDto> getAll();

    UserDto getById(String userIdValue);
}
