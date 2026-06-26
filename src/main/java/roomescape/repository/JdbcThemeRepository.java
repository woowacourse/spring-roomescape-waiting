package roomescape.repository;

import java.sql.PreparedStatement;
import java.util.List;
import java.util.Optional;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import roomescape.domain.Theme;
import roomescape.service.dto.PopularTheme;

@Repository
public class JdbcThemeRepository implements ThemeRepository {

    private final JdbcTemplate jdbcTemplate;

    private static final RowMapper<Theme> ROW_MAPPER = (rs, rowNum) -> new Theme(
            rs.getLong("id"),
            rs.getString("name"),
            rs.getString("description"),
            rs.getString("thumbnail_url")
    );

    private static final RowMapper<PopularTheme> POPULAR_ROW_MAPPER = (rs, rowNum) -> new PopularTheme(
            new Theme(
                    rs.getLong("id"),
                    rs.getString("name"),
                    rs.getString("description"),
                    rs.getString("thumbnail_url")
            ),
            rs.getLong("reservation_count")
    );


    public JdbcThemeRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public List<Theme> findAll() {
        return jdbcTemplate.query(
                "SELECT id, name, description, thumbnail_url FROM theme",
                ROW_MAPPER
        );
    }

    @Override
    public Optional<Theme> findById(Long id) {
        List<Theme> result = jdbcTemplate.query(
                "SELECT id, name, description, thumbnail_url FROM theme WHERE id = ?",
                ROW_MAPPER,
                id
        );
        return result.stream().findFirst();
    }

    @Override
    public Theme save(Theme theme) {
        String sql = "INSERT INTO theme (name, description, thumbnail_url) VALUES (?, ?, ?)";
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

    @Override
    public void deleteById(Long id) {
        jdbcTemplate.update("DELETE FROM theme WHERE id = ?", id);
    }

    @Override
    public boolean existsById(Long id) {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM theme WHERE id = ?",
                Integer.class,
                id
        );
        return count != null && count > 0;
    }

    @Override
    public boolean existsByName(String name) {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM theme WHERE name = ?",
                Integer.class,
                name
        );
        return count != null && count > 0;
    }

    @Override
    public List<PopularTheme> findPopular() {
        String sql = """
                SELECT t.id, t.name, t.description, t.thumbnail_url,
                       COUNT(r.id) AS reservation_count
                FROM theme t
                INNER JOIN reservation r ON t.id = r.theme_id
                WHERE r.date >= DATEADD('DAY', -7, CURRENT_DATE)
                  AND r.date <  CURRENT_DATE
                  AND r.status <> 'CANCELED'
                GROUP BY t.id, t.name, t.description, t.thumbnail_url
                ORDER BY reservation_count DESC
                LIMIT 10
                """;
        return jdbcTemplate.query(sql, POPULAR_ROW_MAPPER);
    }

}
