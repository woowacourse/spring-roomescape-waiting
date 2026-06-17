package roomescape.theme.infra;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import roomescape.global.NotFoundException;
import roomescape.theme.domain.Theme;
import roomescape.theme.domain.repository.ThemeRepository;
import roomescape.theme.exception.ThemeErrorMessage;

@Repository
public class JdbcThemeRepository implements ThemeRepository {

    private final JdbcTemplate jdbcTemplate;
    private final SimpleJdbcInsert jdbcInsert;

    public JdbcThemeRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        this.jdbcInsert = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("theme")
                .usingGeneratedKeyColumns("id");
    }

    @Override
    public Optional<Theme> findById(Long id) {
        return jdbcTemplate.query("SELECT * from theme WHERE id = ?",
                (rs, rowNum) ->
                        Theme.builder()
                                .id(rs.getLong("id"))
                                .name(rs.getString("name"))
                                .description(rs.getString("description"))
                                .thumbnailImgUrl(rs.getString("thumbnail_img_url"))
                                .price(rs.getLong("price"))
                                .build()
                , id).stream().findFirst();
    }

    @Override
    public List<Theme> findAll() {
        return jdbcTemplate.query("SELECT * FROM theme ORDER BY id ASC",
                (rs, rw) -> Theme.builder()
                        .id(rs.getLong("id"))
                        .name(rs.getString("name"))
                        .description(rs.getString("description"))
                        .thumbnailImgUrl(rs.getString("thumbnail_img_url"))
                        .price(rs.getLong("price"))
                        .build()
        );
    }

    @Override
    public List<Theme> findSortedPopularThemes(LocalDate from, LocalDate to, int limit) {
        String sql = """
                SELECT t.id, t.name, t.description, t.thumbnail_img_url, t.price
                FROM theme t
                JOIN reservation r ON t.id = r.theme_id
                WHERE r.date BETWEEN ? AND ?
                GROUP BY t.id, t.name, t.description, t.thumbnail_img_url, t.price
                ORDER BY COUNT(r.id) DESC
                LIMIT ?
                """;

        return jdbcTemplate.query(sql,
                (rs, rowNum) -> Theme.builder()
                        .id(rs.getLong("id"))
                        .name(rs.getString("name"))
                        .description(rs.getString("description"))
                        .thumbnailImgUrl(rs.getString("thumbnail_img_url"))
                        .price(rs.getLong("price"))
                        .build(),
                from, to, limit);
    }

    @Override
    public Theme save(Theme theme) {
        SqlParameterSource params = new MapSqlParameterSource()
                .addValue("name", theme.getName())
                .addValue("description", theme.getDescription())
                .addValue("thumbnail_img_url", theme.getThumbnailImgUrl())
                .addValue("price", theme.getPrice());

        Long id = jdbcInsert.executeAndReturnKey(params).longValue();
        return theme.withId(id);
    }

    @Override
    public void delete(long id) {
        int count = jdbcTemplate.update("DELETE FROM theme WHERE id = ?", id);
        if (count == 0) {
            throw new NotFoundException(ThemeErrorMessage.THEME_NOT_FOUND, id);
        }
    }

    @Override
    public Boolean existsByNameAndDescription(Theme theme) {
        String sql = "SELECT EXISTS(SELECT 1 FROM theme WHERE name = ? AND description = ?)";

        return jdbcTemplate.queryForObject(sql, Boolean.class, theme.getName(), theme.getDescription());
    }
}
