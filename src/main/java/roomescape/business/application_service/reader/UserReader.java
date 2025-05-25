package roomescape.business.application_service.reader;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import roomescape.business.dto.UserDto;
import roomescape.business.model.vo.Email;
import roomescape.business.model.vo.Id;
import roomescape.business.model.vo.UserName;
import roomescape.business.model.vo.UserRole;
import roomescape.exception.business.NotFoundException;

import java.util.List;

import static roomescape.exception.ErrorCode.USER_NOT_EXIST;

@Repository
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserReader {

    private final JdbcClient jdbcClient;

    public List<UserDto> getAll() {
        String sql = "SELECT * FROM users";
        
        return jdbcClient.sql(sql)
                .query((rs, rowNum) -> new UserDto(
                        Id.create(rs.getString("id")),
                        UserRole.valueOf(rs.getString("user_role")),
                        new UserName(rs.getString("user_name")),
                        new Email(rs.getString("email"))
                )).list();
    }

    public UserDto getById(final String userIdValue) {
        String sql = "SELECT * FROM users u WHERE u.id = :id";

        return jdbcClient.sql(sql)
                .param("id", userIdValue)
                .query((rs, rowNum) -> new UserDto(
                        Id.create(rs.getString("id")),
                        UserRole.valueOf(rs.getString("user_role")),
                        new UserName(rs.getString("user_name")),
                        new Email(rs.getString("email"))
                )).optional()
                .orElseThrow(() -> new NotFoundException(USER_NOT_EXIST));
    }
}
