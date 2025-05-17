package roomescape.common.jdbc;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import java.util.Optional;

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
