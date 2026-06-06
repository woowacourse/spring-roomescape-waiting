package roomescape.theme.repository;

import java.sql.PreparedStatement;
import java.util.List;
import java.util.Optional;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import roomescape.global.exception.NotFoundException;
import roomescape.theme.domain.Theme;
import roomescape.theme.exception.ThemeErrorCode;

@Repository
public class ThemeDao {

    private static final RowMapper<Theme> THEME_ROW_MAPPER = (resultSet, rowNum) -> new Theme(
            resultSet.getLong("id"),
            resultSet.getString("name"),
            resultSet.getString("description"),
            resultSet.getString("thumbnail_url")
    );

    private final JdbcTemplate jdbcTemplate;

    public ThemeDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public Theme save(Theme theme) {
        String sql = """
                INSERT INTO theme (name, description, thumbnail_url)
                VALUES (?, ?, ?)
                """;

        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, new String[]{"id"});
            ps.setString(1, theme.getName());
            ps.setString(2, theme.getDescription());
            ps.setString(3, theme.getThumbnailUrl());
            return ps;
        }, keyHolder);

        Long id = keyHolder.getKey().longValue();

        return new Theme(id, theme.getName(), theme.getDescription(), theme.getThumbnailUrl());
    }

    public Optional<Theme> findById(Long id) {
        String sql = """
                SELECT *
                FROM theme
                WHERE id = ?
                """;

        return jdbcTemplate.query(sql, THEME_ROW_MAPPER, id)
                .stream().findFirst();
    }

    public List<Theme> findAll() {
        String sql = """
                SELECT id, name, description, thumbnail_url
                FROM theme
                """;

        return jdbcTemplate.query(sql, THEME_ROW_MAPPER);
    }

    public void delete(Theme theme) {
        String sql = """
                DELETE FROM theme
                WHERE id = ?
                """;

        int affected = jdbcTemplate.update(sql, theme.getId());
        if (affected == 0) {
            throw new NotFoundException(ThemeErrorCode.THEME_NOT_FOUND);
        }
    }
}
