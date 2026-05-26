package roomescape.theme.infra;

import java.util.List;
import java.util.Optional;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import roomescape.theme.domain.Theme;
import roomescape.theme.domain.repository.ThemeRepository;

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
                        .build()
        );
    }

    @Override
    public Theme save(Theme theme) {
        SqlParameterSource params = new MapSqlParameterSource()
                .addValue("name", theme.getName())
                .addValue("description", theme.getDescription())
                .addValue("thumbnail_img_url", theme.getThumbnailImgUrl());

        Long id = jdbcInsert.executeAndReturnKey(params).longValue();
        return theme.withId(id);
    }

    @Override
    public Integer delete(long id) {
        return jdbcTemplate.update("DELETE FROM theme WHERE id = ?", id);
    }

    @Override
    public Boolean existsByNameAndDescription(Theme theme) {
        String sql = "SELECT EXISTS(SELECT 1 FROM theme WHERE name = ? AND description = ?)";

        return jdbcTemplate.queryForObject(sql, Boolean.class, theme.getName(), theme.getDescription());
    }
}
