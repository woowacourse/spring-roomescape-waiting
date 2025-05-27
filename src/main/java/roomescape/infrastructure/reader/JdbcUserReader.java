package roomescape.infrastructure.reader;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import roomescape.business.dto.UserDto;
import roomescape.business.model.vo.Email;
import roomescape.business.model.vo.Id;
import roomescape.business.model.vo.UserName;
import roomescape.business.model.vo.UserRole;
import roomescape.business.service.reader.UserReader;
import roomescape.exception.business.NotFoundException;

import java.util.List;

import static roomescape.exception.ErrorCode.USER_NOT_EXIST;

@Repository
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class JdbcUserReader implements UserReader {

    private static final RowMapper<UserDto> USER_ROW_MAPPER = (rs, rowNum) -> new UserDto(
            Id.create(rs.getString("id")),
            UserRole.valueOf(rs.getString("user_role")),
            new UserName(rs.getString("user_name")),
            new Email(rs.getString("email"))
    );

    private final JdbcClient jdbcClient;

    @Override
    public List<UserDto> getAll() {
        String sql = "SELECT * FROM users";

        return jdbcClient.sql(sql)
                .query(USER_ROW_MAPPER)
                .list();
    }

    @Override
    public UserDto getById(final String userIdValue) {
        String sql = "SELECT * FROM users u WHERE u.id = :id";

        return jdbcClient.sql(sql)
                .param("id", userIdValue)
                .query(USER_ROW_MAPPER)
                .optional()
                .orElseThrow(() -> new NotFoundException(USER_NOT_EXIST));
    }
}
