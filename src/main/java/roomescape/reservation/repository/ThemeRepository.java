package roomescape.reservation.repository;

import java.sql.PreparedStatement;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import roomescape.reservation.domain.Description;
import roomescape.reservation.domain.Theme;
import roomescape.reservation.domain.ThemeName;

@Repository
public class ThemeRepository {

    private final JdbcTemplate jdbcTemplate;

    public ThemeRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public Long save(Theme theme) {
        String sql = "insert into theme(name, description, thumbnail) values (?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(con -> {
            PreparedStatement ps = con.prepareStatement(
                    sql, new String[]{"id"}
            );
            ps.setString(1, theme.getName());
            ps.setString(2, theme.getDescription());
            ps.setString(3, theme.getThumbnail());
            return ps;
        }, keyHolder);

        return Objects.requireNonNull(keyHolder.getKey()).longValue();
    }

    public Optional<Theme> findById(Long id) {
        String sql = "select * from theme where id = ?";
        try {
            return Optional.of(jdbcTemplate.queryForObject(sql, createThemeRowMapper(), id));
        } catch (DataAccessException exception) {
            return Optional.empty();
        }
    }

    public Optional<Theme> findByName(String name) {
        String sql = "select * from theme where name = ?";
        try {
            return Optional.of(jdbcTemplate.queryForObject(sql, createThemeRowMapper(), name));
        } catch (DataAccessException exception) {
            return Optional.empty();
        }
    }

    public List<Theme> findAll() {
        String sql = "select * from theme";

        return jdbcTemplate.query(sql, createThemeRowMapper());
    }

    public List<Theme> findThemesThatReservationReferById(Long id) {
        String sql = """
                select *
                from theme t
                join reservation r
                on r.theme_id = t.id
                where t.id = ?
                """;

        return jdbcTemplate.query(sql, createThemeRowMapper(), id);
    }

    public List<Theme> findPopularThemesDescOfLastWeekForLimit(int limitCount) {
        String sql = """
                select t.id, t.name, t.description, t.thumbnail, count(*) as cnt
                from theme t
                join reservation r
                on r.theme_id = t.id
                where r.date between timestampadd(week, -1, current_timestamp()) and current_timestamp()
                group by t.id
                order by cnt desc
                limit ?;
                """;

        return jdbcTemplate.query(sql, createThemeRowMapper(), limitCount);
    }

    public void delete(Long id) {
        String sql = "delete from theme where id = ?";
        jdbcTemplate.update(sql, id);
    }

    private RowMapper<Theme> createThemeRowMapper() {
        return (rs, rowNum) -> new Theme(
                rs.getLong("id"),
                new ThemeName(rs.getString("name")),
                new Description(rs.getString("description")),
                rs.getString("thumbnail")
        );
    }
}
