package roomescape.reservation.dao;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import roomescape.reservation.model.Theme;
import roomescape.reservation.exception.AssociatedReservationExistsException;
import roomescape.reservation.exception.ThemeNotExistException;

@Repository
public class JdbcThemeDao implements ThemeDao {

    private final JdbcTemplate jdbcTemplate;
    private final SimpleJdbcInsert jdbcInsert;

    public JdbcThemeDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        this.jdbcInsert = new SimpleJdbcInsert(jdbcTemplate)
            .withTableName("theme")
            .usingGeneratedKeyColumns("id");
    }

    @Override
    public Theme add(Theme theme) {
        Map<String, Object> param = new HashMap<>();
        param.put("name", theme.getName());
        param.put("description", theme.getDescription());
        param.put("thumbnail", theme.getThumbnail());

        Number key = jdbcInsert.executeAndReturnKey(param);
        return theme.toEntity(key.longValue());
    }

    @Override
    public List<Theme> findAll() {
        String sql = generateFindAllQuery();
        return jdbcTemplate.query(sql, mapResultsToTheme());
    }

    private static String generateFindAllQuery() {
        return "SELECT id, name, description, thumbnail FROM theme";
    }

    @Override
    public Theme findById(Long id) {
        String whereClause = " WHERE id = ?";
        String sql = generateFindAllQuery() + whereClause;
        try {
            return jdbcTemplate.queryForObject(sql, mapResultsToTheme(), id);
        } catch (DataAccessException exception) {
            throw new ThemeNotExistException();
        }
    }

    @Override
    public List<Theme> findMostReservedThemesInPeriodWithLimit(LocalDate startDate, LocalDate endDate, int limitCount) {
        String sql = """
                SELECT
                  t.id,
                  t.name,
                  t.description,
                  t.thumbnail
                FROM
                  reservation as r
                  INNER JOIN theme as t ON r.theme_id = t.id
                WHERE
                   r.date >= ? AND r.date < ?
                GROUP BY
                  theme_id
                ORDER BY
                  COUNT(theme_id) DESC
                LIMIT ?;
            """;
        return jdbcTemplate.query(sql, mapResultsToTheme(), startDate, endDate, limitCount);
    }

    @Override
    public int deleteById(Long id) {
        try {
            String sql = "DELETE FROM theme WHERE id = ?";
            return jdbcTemplate.update(sql, id);
        } catch (DataIntegrityViolationException exception) {
            throw new AssociatedReservationExistsException("해당 테마에 이미 저장된 예약이 있으므로 삭제할 수 없다.");
        }
    }

    @Override
    public boolean existByName(String name) {
        String sql = "SELECT EXISTS(SELECT id FROM theme WHERE name = ?)";
        return jdbcTemplate.queryForObject(sql, Boolean.class, name);
    }

    private RowMapper<Theme> mapResultsToTheme() {
        return (rs, rowNum) -> new Theme(
            rs.getLong("id"),
            rs.getString("name"),
            rs.getString("description"),
            rs.getString("thumbnail"));
    }
}
