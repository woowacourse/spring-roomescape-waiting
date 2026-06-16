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
    private final RowMapper<Theme> themeRowMapper = (rs, rowNum) -> new Theme(
            rs.getLong("id"),
            rs.getString("name"),
            rs.getString("description"),
            rs.getString("thumbnail_url"),
            rs.getLong("price")
    );
    private final RowMapper<PopularTheme> popularThemeRowMapper = (rs, rowNum) -> new PopularTheme(
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
                "SELECT id, name, description, thumbnail_url, price FROM theme WHERE is_deleted = FALSE",
                themeRowMapper
        );
    }

    @Override
    public Optional<Theme> findById(Long id) {
        List<Theme> result = jdbcTemplate.query(
                "SELECT id, name, description, thumbnail_url, price FROM theme WHERE id = ? AND is_deleted = FALSE",
                themeRowMapper,
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
        jdbcTemplate.update("UPDATE theme SET is_deleted = TRUE WHERE id = ?", id);
    }

    @Override
    public boolean existsById(Long id) {
        Boolean exists = jdbcTemplate.queryForObject(
                "SELECT EXISTS (SELECT 1 FROM theme WHERE id = ? AND is_deleted = FALSE)",
                Boolean.class,
                id
        );
        return Boolean.TRUE.equals(exists);
    }

    @Override
    public boolean existsByName(String name) {
        Boolean exists = jdbcTemplate.queryForObject(
                "SELECT EXISTS (SELECT 1 FROM theme WHERE name = ? AND is_deleted = FALSE)",
                Boolean.class,
                name
        );
        return Boolean.TRUE.equals(exists);
    }

    @Override
    public List<PopularTheme> findPopular() {
        String sql = """
                SELECT t.id, t.name, t.description, t.thumbnail_url,
                       COUNT(r.id) AS reservation_count
                FROM theme t
                INNER JOIN reservation_slot s ON t.id = s.theme_id
                INNER JOIN reservation r ON s.id = r.slot_id
                INNER JOIN reservation_date d ON s.date_id = d.id
                WHERE d.date >= DATEADD('DAY', -7, CURRENT_DATE)
                  AND d.date <  CURRENT_DATE
                  AND t.is_deleted = FALSE
                GROUP BY t.id, t.name, t.description, t.thumbnail_url
                ORDER BY reservation_count DESC
                LIMIT 10
                """;
        return jdbcTemplate.query(sql, popularThemeRowMapper);
    }
}
