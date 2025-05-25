package roomescape.business.dto;

import org.springframework.jdbc.core.RowMapper;
import roomescape.business.model.entity.User;
import roomescape.business.model.vo.Email;
import roomescape.business.model.vo.Id;
import roomescape.business.model.vo.UserName;
import roomescape.business.model.vo.UserRole;

public record UserDto(
        Id id,
        UserRole userRole,
        UserName name,
        Email email
) {
    public static UserDto fromEntity(final User user) {
        return new UserDto(
                user.getId(),
                user.getUserRole(),
                user.getName(),
                user.getEmail()
        );
    }

    public static RowMapper<UserDto> ROW_MAPPER = (rs, rowNum) -> new UserDto(
            Id.create(rs.getString("id")),
            UserRole.valueOf(rs.getString("user_role")),
            new UserName(rs.getString("user_name")),
            new Email(rs.getString("email"))
    );
}
