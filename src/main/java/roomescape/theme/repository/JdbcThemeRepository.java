package roomescape.theme.repository;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import roomescape.exception.ConflictException;
import roomescape.exception.ErrorCode;
import roomescape.theme.Theme;

@Repository
public class JdbcThemeRepository implements ThemeRepository {

    private static final RowMapper<Theme> themeRowMapper = (resultSet, rowNum) ->
            Theme.of(
                    resultSet.getLong("id"),
                    resultSet.getString("name"),
                    resultSet.getString("description"),
                    resultSet.getString("thumbnail_url")
            );

    private final JdbcTemplate jdbcTemplate;

    public JdbcThemeRepository(final JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }


    @Override
    public List<Theme> findAll() {
        String sql = "SELECT * FROM theme";
        return jdbcTemplate.query(sql, themeRowMapper);
    }

    @Override
    public Optional<Theme> findById(final long id) {
        String sql = "SELECT * FROM theme WHERE id = ?";
        return jdbcTemplate.query(sql, themeRowMapper, id)
                .stream()
                .findFirst();
    }

    @Override
    public int deleteById(final long id) {
        String sql = "DELETE FROM theme WHERE id = ?";
        try {
            return jdbcTemplate.update(sql, id);
        } catch (DataIntegrityViolationException exception) {
            throw new ConflictException(ErrorCode.THEME_IN_USE, "이미 예약된 테마는 삭제할 수 없습니다.");
        }
    }

    @Override
    public Theme save(final Theme theme) {
        String sql = "INSERT INTO theme (name, description, thumbnail_url) VALUES (?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();

        try {
            jdbcTemplate.update(connection -> {
                PreparedStatement preparedStatement = connection.prepareStatement(sql, new String[]{"id"});
                preparedStatement.setString(1, theme.getName());
                preparedStatement.setString(2, theme.getDescription());
                preparedStatement.setString(3, theme.getThumbnailUrl());
                return preparedStatement;
            }, keyHolder);
        } catch (DataIntegrityViolationException exception) {
            throw new ConflictException(ErrorCode.THEME_NAME_DUPLICATED, "테마 이름 중복은 불가능합니다.");
        }

        Number key = keyHolder.getKey();
        if (key == null) {
            throw new IllegalStateException("[ERROR] 테마 ID를 생성하지 못했습니다.");
        }

        return theme.withId(key.longValue());
    }

    @Override
    public boolean existsByName(final String name) {
        String sql = "SELECT EXISTS (SELECT 1 FROM theme WHERE name = ?)";

        return Boolean.TRUE.equals(jdbcTemplate.queryForObject(
                sql,
                Boolean.class,
                name
        ));
    }

    @Override
    public List<Theme> findPopularThemes(final int period, final int limit) {
        String sql = """
                SELECT t.id,
                       t.name,
                       t.description,
                       t.thumbnail_url
                FROM reservation r
                INNER JOIN theme t ON r.theme_id = t.id
                WHERE r.date >= ? AND r.date <= ?
                GROUP BY t.id, t.name, t.description, t.thumbnail_url
                ORDER BY COUNT(*) DESC, t.id ASC
                LIMIT ?
                """;
        LocalDate end = LocalDate.now();
        LocalDate start = end.minusDays(period);

        return jdbcTemplate.query(sql, themeRowMapper, Date.valueOf(start), Date.valueOf(end), limit);
    }
}
