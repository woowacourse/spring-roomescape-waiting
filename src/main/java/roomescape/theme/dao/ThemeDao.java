package roomescape.theme.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import roomescape.theme.domain.Theme;

@Repository
public class ThemeDao {
    private final JdbcTemplate jdbcTemplate;
    private final RowMapper<Theme> rowMapper;

    public ThemeDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        this.rowMapper = (resultSet, rowNum) -> new Theme(
                resultSet.getLong("id"),
                resultSet.getString("name"),
                resultSet.getString("description"),
                resultSet.getString("thumbnail")
        );
    }

    public List<Theme> findThemes() {
        String sql = """
                SELECT id, name, description, thumbnail
                FROM theme
                """;
        return jdbcTemplate.query(sql, rowMapper);
    }

    public Optional<Theme> findThemeById(Long id) {
        String sql = "SELECT id, name, description, thumbnail FROM theme WHERE id = ?";
        try {
            return Optional.ofNullable(jdbcTemplate.queryForObject(sql, rowMapper, id));
        } catch (EmptyResultDataAccessException exception) {
            return Optional.empty();
        }
    }

    public List<Theme> findThemesSortedByCountOfReservation(LocalDate startDate, LocalDate endDate, int count) {
        String sql = """
                SELECT theme.id, theme.name, theme.description, theme.thumbnail, COUNT(reservation.theme_id) AS reservation_count
                FROM theme
                JOIN reservation
                ON reservation.theme_id = theme.id
                WHERE reservation.date >= ? AND reservation.date <= ?
                GROUP BY theme.id, theme.name, theme.description, theme.thumbnail
                ORDER BY reservation_count DESC
                LIMIT ?
                """;

        return jdbcTemplate.query(sql, rowMapper, startDate, endDate, count);
    }

    public Theme createTheme(Theme theme) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        try {
            jdbcTemplate.update(connection -> createPreparedStatementForUpdate(connection, theme), keyHolder);
        } catch (DuplicateKeyException exception) {
            throw new IllegalArgumentException("해당 테마 이름은 이미 존재합니다.");
        }

        Long id = keyHolder.getKey().longValue();
        return theme.withId(id);
    }

    private PreparedStatement createPreparedStatementForUpdate(Connection connection, Theme theme)
            throws SQLException {
        String sql = "INSERT INTO theme (name, description, thumbnail) values (?, ?, ?)";
        PreparedStatement preparedStatement = connection.prepareStatement(sql, new String[]{"id"});
        preparedStatement.setString(1, theme.getName());
        preparedStatement.setString(2, theme.getDescription());
        preparedStatement.setString(3, theme.getThumbnail());
        return preparedStatement;
    }

    public void deleteTheme(Long id) {
        try {
            String sql = "DELETE FROM theme WHERE id = ?";
            jdbcTemplate.update(sql, id);
        } catch (DataIntegrityViolationException exception) {
            throw new IllegalArgumentException("해당 테마는 이미 예약되어 있어 삭제할 수 없습니다.", exception);
        }
    }
}
