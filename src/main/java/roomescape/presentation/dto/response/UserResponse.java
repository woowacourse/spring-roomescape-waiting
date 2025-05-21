package roomescape.presentation.dto.response;

import java.util.Comparator;
import java.util.List;
import roomescape.business.dto.UserDto;

public record UserResponse(
        String id,
        String name,
        String email
) {
    public static UserResponse from(final UserDto dto) {
        return new UserResponse(dto.id().value(), dto.name().value(), dto.email().value());
    }

    public static List<UserResponse> from(final List<UserDto> dtos) {
        return dtos.stream()
                .map(UserResponse::from)
                .sorted(Comparator.comparing(UserResponse::name))
                .toList();
    }
}
