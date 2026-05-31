package roomescape.theme.dao;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import roomescape.theme.domain.Theme;
import roomescape.theme.dto.request.ThemeCreateRequest;
import roomescape.theme.dto.response.ReservedThemeResponse;

@Repository
public class ThemeDao {

    private final JdbcTemplate jdbcTemplate;
    private final SimpleJdbcInsert simpleJdbcInsert;

    public ThemeDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        this.simpleJdbcInsert = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("theme")
                .usingGeneratedKeyColumns("id");
    }

    public Theme insert(ThemeCreateRequest themeCreateRequest) {
        Long id = simpleJdbcInsert.executeAndReturnKey(Map.of(
                "name", themeCreateRequest.name(),
                "description", themeCreateRequest.description(),
                "image_url", themeCreateRequest.imageUrl()
        )).longValue();

        return Theme.of(id, themeCreateRequest.name(), themeCreateRequest.description(), themeCreateRequest.imageUrl());
    }

    public List<Theme> findAll() {
        String sql = "SELECT id, name, description, image_url FROM theme ORDER BY id";
        return jdbcTemplate.query(sql, themeRowMapper());
    }

    public Theme findById(Long id) {
        try {
            String sql = "SELECT id, name, description, image_url FROM theme WHERE id = ?";
            return jdbcTemplate.queryForObject(sql, themeRowMapper(), id);
        } catch (EmptyResultDataAccessException e) {
            throw new IllegalArgumentException("존재하지 않는 테마입니다. id = " + id);
        }
    }

    public boolean delete(Long id) {
        try {
            String sql = "DELETE FROM theme WHERE id = ?";
            int deletedRowCount = jdbcTemplate.update(sql, id);
            return deletedRowCount > 0;
        } catch (DataIntegrityViolationException e) {
            throw new IllegalArgumentException("사용 중인 테마는 삭제할 수 없습니다. id = " + id);
        }
    }

    public List<ReservedThemeResponse> findMostReserved(long limit, LocalDate startDate, LocalDate endDate) {
        String sql = "SELECT t.id, t.name, t.description, t.image_url, count(r.id) AS reservation_count"
                + " FROM theme t"
                + " LEFT OUTER JOIN reservation r ON t.id = r.theme_id"
                + " AND (? IS NULL OR r.date >= ?) AND r.date <= ?"
                + " GROUP BY t.id"
                + " ORDER BY reservation_count DESC"
                + " LIMIT ?";

        return jdbcTemplate.query(sql, reservedThemeRowMapper(), startDate, startDate, endDate, limit);
    }

    private RowMapper<Theme> themeRowMapper() {
        return (resultSet, rowNum) -> Theme.of(
                resultSet.getLong("id"),
                resultSet.getString("name"),
                resultSet.getString("description"),
                resultSet.getString("image_url")
        );
    }

    private RowMapper<ReservedThemeResponse> reservedThemeRowMapper() {
        return (resultSet, rowNum) -> new ReservedThemeResponse(
                resultSet.getLong("id"),
                resultSet.getString("name"),
                resultSet.getString("description"),
                resultSet.getString("image_url"),
                resultSet.getInt("reservation_count")
        );
    }
}
