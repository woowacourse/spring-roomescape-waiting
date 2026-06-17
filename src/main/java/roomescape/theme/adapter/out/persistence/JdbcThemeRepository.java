package roomescape.theme.adapter.out.persistence;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import roomescape.theme.application.port.out.ThemeRepository;
import roomescape.theme.domain.Theme;

@Repository
@RequiredArgsConstructor
public class JdbcThemeRepository implements ThemeRepository {
    private final NamedParameterJdbcTemplate template;
    private final RowMapper<Theme> themeRowMapper = (resultSet, rowNum) ->
            new Theme(
                    resultSet.getLong("id"),
                    resultSet.getString("name"),
                    resultSet.getString("description"),
                    resultSet.getString("thumbnail_url"),
                    resultSet.getInt("price")
            );

    @Override
    public Theme save(Theme theme) {
        String sql = """
                INSERT INTO theme(name, description, thumbnail_url, price)
                VALUES (:name, :description, :thumbnail_url, :price)
                """;

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("name", theme.getName())
                .addValue("description", theme.getDescription())
                .addValue("thumbnail_url", theme.getThumbnailUrl())
                .addValue("price", theme.getPrice());

        KeyHolder keyHolder = new GeneratedKeyHolder();
        template.update(sql, params, keyHolder);

        Number id = keyHolder.getKey();
        if (id == null) {
            throw new IllegalStateException("theme 저장 후 생성된 ID를 반환받지 못했습니다.");
        }

        return new Theme(
                keyHolder.getKey().longValue(),
                theme.getName(),
                theme.getDescription(),
                theme.getThumbnailUrl(),
                theme.getPrice()
        );
    }

    @Override
    public void deleteById(long id) {
        String sql = "DELETE FROM theme WHERE id = :id";
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("id", id);

        template.update(sql, params);
    }

    @Override
    public List<Theme> findThemesBySlotDate(LocalDate date) {
        String sql = "SELECT DISTINCT t.id, t.name, t.description, t.thumbnail_url, t.price " +
                "FROM theme t " +
                "JOIN slot s ON t.id = s.theme_id " +
                "WHERE s.date = :date " +
                "ORDER BY t.id ASC";

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("date", date);

        return template.query(sql, params, themeRowMapper);
    }

    @Override
    public List<Theme> findPopularThemeByCurrentDate(LocalDate currentDate) {
        LocalDate startDate = currentDate.minusDays(7);

        String sql = "SELECT t.id, t.name, t.description, t.thumbnail_url, t.price " +
                "FROM theme t " +
                "JOIN slot s ON t.id = s.theme_id " +
                "JOIN reservation r ON s.id = r.slot_id " +
                "WHERE s.date >= :startDate " +
                "AND s.date < :currentDate " +
                "AND r.status = 'CONFIRMED' " +
                "GROUP BY t.id, t.name, t.description, t.thumbnail_url, t.price " +
                "ORDER BY COUNT(r.id) DESC, t.id ASC " +
                "LIMIT :limit";

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("startDate", startDate)
                .addValue("currentDate", currentDate)
                .addValue("limit", 10);

        return template.query(sql, params, themeRowMapper);
    }

    @Override
    public Optional<Theme> findById(long id) {
        String sql = "SELECT * FROM theme WHERE id = :id";

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("id", id);

        List<Theme> result = template.query(sql, params, themeRowMapper);

        return result.stream().findFirst();
    }

    @Override
    public List<Theme> findAll() {
        String sql = "SELECT * FROM theme";

        return template.query(sql, themeRowMapper);
    }

    @Override
    public boolean existsAlreadyTheme(String themeName) {
        String sql = "SELECT EXISTS (SELECT 1 FROM theme WHERE name = :themeName)";

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("themeName", themeName);

        return Boolean.TRUE.equals(template.queryForObject(sql, params, Boolean.class));
    }
}
