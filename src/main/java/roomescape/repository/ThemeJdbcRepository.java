package roomescape.repository;

import java.sql.PreparedStatement;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import roomescape.domain.PopularTheme;
import roomescape.domain.Theme;

@Repository
public class ThemeJdbcRepository implements ThemeRepository {

    private final JdbcTemplate jdbcTemplate;

    private final RowMapper<Theme> rowMapper = (resultSet, rowNum) -> {
        Theme theme = new Theme(
                resultSet.getLong("id"),
                resultSet.getString("name"),
                resultSet.getString("description"),
                resultSet.getString("thumbnail_image_url")
        );
        return theme;
    };

    public ThemeJdbcRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public List<Theme> findAll(int limit, int offset) {
        String sql = """
                select id, name, description, thumbnail_image_url
                from theme
                order by id
                limit ? offset ?
                """;
        return jdbcTemplate.query(sql, rowMapper, limit, offset);
    }

    @Override
    public Optional<Theme> findById(Long id) {
        String sql = "select id, name, description, thumbnail_image_url from theme where id = ?";
        try {
            return Optional.ofNullable(jdbcTemplate.queryForObject(sql, rowMapper, id));
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public Long save(Theme theme) {
        String sql = "insert into theme(name, description, thumbnail_image_url) values(?, ?, ?)";

        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, new String[]{"id"});
            ps.setString(1, theme.getName());
            ps.setString(2, theme.getDescription());
            ps.setString(3, theme.getThumbnailImageUrl());
            return ps;
        }, keyHolder);

        return keyHolder.getKey().longValue();
    }

    @Override
    public int deleteById(Long id) {
        String sql = "delete from theme where id = ?";
        return jdbcTemplate.update(sql, id);
    }

    @Override
    public List<PopularTheme> findPopularThemes(LocalDate startDate, LocalDate endDate, Integer limit) {
        String sql = """
                select t.id, t.name, t.description, t.thumbnail_image_url, count(r.id) as reservation_count
                from theme t
                join reservation_slot rs on t.id = rs.theme_id and rs.date >= ? and rs.date <= ?
                join reservation r on r.slot_id = rs.id
                group by t.id
                order by reservation_count desc, t.id asc
                limit ?
                """;

        RowMapper<PopularTheme> popularThemeRowMapper = (resultSet, rowNum) -> new PopularTheme(
                rowMapper.mapRow(resultSet, rowNum),
                resultSet.getLong("reservation_count")
        );

        return jdbcTemplate.query(sql, popularThemeRowMapper, startDate, endDate, limit);
    }
}
