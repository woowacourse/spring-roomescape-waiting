package roomescape.common.utils;

import java.util.Optional;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class JdbcUtils {

    public static <T> Optional<T> queryForOptional(
            final JdbcTemplate jdbcTemplate,
            final String sql,
            final RowMapper<T> mapper,
            final Object... args
    ) {
        try {
            return Optional.ofNullable(jdbcTemplate.queryForObject(sql, mapper, args));
        } catch (final EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }
}
