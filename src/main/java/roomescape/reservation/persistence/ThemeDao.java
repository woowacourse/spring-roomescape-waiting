package roomescape.reservation.persistence;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import roomescape.reservation.domain.Theme;
import roomescape.reservation.domain.ThemeRepository;

import javax.sql.DataSource;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Repository
public class ThemeDao implements ThemeRepository {

    private final JdbcTemplate jdbcTemplate;
    private final SimpleJdbcInsert jdbcInsert;

    public ThemeDao(DataSource source) {
        this.jdbcTemplate = new JdbcTemplate(source);
        this.jdbcInsert = new SimpleJdbcInsert(source)
                .withTableName("theme")
                .usingGeneratedKeyColumns("id");
    }

    @Override
    public Theme save(Theme theme) {
        SqlParameterSource params = new BeanPropertySqlParameterSource(theme);
        Long id = jdbcInsert.executeAndReturnKey(params).longValue();
        return new Theme(Objects.requireNonNull(id), theme);
    }

    @Override
    public List<Theme> findAll() {
        String sql = "SELECT * FROM theme";
        return jdbcTemplate.query(sql, this::mapRowToObject);
    }

    @Override
    public Optional<Theme> findById(Long id) {
        try {
            String sql = "SELECT * FROM theme WHERE id = ?";
            return Optional.ofNullable(jdbcTemplate.queryForObject(sql, this::mapRowToObject, id));
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public void deleteById(Long id) {
        String sql = "DELETE FROM theme WHERE id = ?";
        jdbcTemplate.update(sql, id);
    }

    @Override
    public List<Theme> findAllByDateBetweenAndOrderByReservationCount(LocalDate startDate, LocalDate endDate, int limit) {
        String sql = """
                SELECT
                    th.id AS id, 
                    th.name AS name, 
                    th.description AS description, 
                    th.thumbnail AS thumbnail,
                    COUNT(r.id) AS reservation_count
                FROM
                    theme th
                LEFT OUTER JOIN
                    reservation r
                ON
                    r.theme_id = th.id  AND  r.date >= ? AND r.date <= ?
                GROUP BY
                    th.id, th.name, th.description, th.thumbnail
                ORDER BY
                    reservation_count DESC
                LIMIT
                    ?
                """;
        return jdbcTemplate.query(sql, this::mapRowToObject, startDate, endDate, limit);
    }

    private Theme mapRowToObject(ResultSet resultSet, int rowNumber) throws SQLException {
        return new Theme(
                resultSet.getLong("id"),
                resultSet.getString("name"),
                resultSet.getString("description"),
                resultSet.getString("thumbnail")
        );
    }
}
