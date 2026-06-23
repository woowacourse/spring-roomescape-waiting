package roomescape.theme;

import java.sql.PreparedStatement;
import java.time.LocalDate;
import java.util.List;
import org.springframework.jdbc.core.DataClassRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import roomescape.theme.dto.PopularThemeProjection;

@Repository
public class ThemeDao {

    private final JdbcTemplate jdbcTemplate;

    public ThemeDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public Theme findById(Long id) {
        String sql = "SELECT * FROM theme WHERE id = ?";
        return jdbcTemplate.queryForObject(sql, new DataClassRowMapper<>(Theme.class), id);
    }

    public List<Theme> findAllThemes() {
        String sql = "SELECT * FROM theme";
        List<Theme> themes = jdbcTemplate.query(
                sql,
                new DataClassRowMapper<>(Theme.class)
        );
        return themes;
    }

    public Long insertTheme(String name, String description, String imgUrl) {
        String sql = "INSERT INTO theme (name, description, img_url) VALUES (?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement preparedStatement = connection.prepareStatement(
                    sql, new String[]{"id"}
            );
            preparedStatement.setString(1, name);
            preparedStatement.setString(2, description);
            preparedStatement.setString(3, imgUrl);
            return preparedStatement;
        }, keyHolder);
        return keyHolder.getKey().longValue();
    }

    public int delete(Long id) {
        return jdbcTemplate.update("delete from theme where id = ?", id);
    }

    public List<PopularThemeProjection> findPopularThemes(LocalDate from, LocalDate to) {
        String sql = """
                SELECT
                    ranked.theme_rank AS rank,
                    ranked.id,
                    ranked.name,
                    ranked.description,
                    ranked.img_url,
                    ranked.reservation_count
                FROM (
                    SELECT
                        ROW_NUMBER() OVER (ORDER BY COUNT(r.id) DESC, t.id ASC) AS theme_rank,
                        t.id,
                        t.name,
                        t.description,
                        t.img_url,
                        COUNT(r.id) AS reservation_count
                    FROM theme t
                    JOIN reservation r ON t.id = r.theme_id
                    WHERE r.date BETWEEN ? AND ?
                      AND r.status = 'CONFIRMED'
                    GROUP BY t.id, t.name, t.description, t.img_url
                ) ranked
                WHERE ranked.theme_rank <= 10
                ORDER BY ranked.theme_rank;
                """;
        List<PopularThemeProjection> popularThemeProjections = jdbcTemplate.query(sql,
                new DataClassRowMapper<>(PopularThemeProjection.class),
                from.toString(),
                to.toString());
        return popularThemeProjections;
    }
}
