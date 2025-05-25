package roomescape.business.application_service.reader;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import roomescape.business.dto.UserDto;
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
                .query(UserDto.ROW_MAPPER)
                .list();
    }

    public UserDto getById(final String userIdValue) {
        String sql = "SELECT * FROM users u WHERE u.id = :id";

        return jdbcClient.sql(sql)
                .param("id", userIdValue)
                .query(UserDto.ROW_MAPPER)
                .optional()
                .orElseThrow(() -> new NotFoundException(USER_NOT_EXIST));
    }
}
