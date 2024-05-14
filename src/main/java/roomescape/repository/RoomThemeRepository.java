package roomescape.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import roomescape.domain.RoomTheme;
import roomescape.exception.BadRequestException;

@Repository
public class RoomThemeRepository {
    private final JdbcTemplate jdbcTemplate;
    private final SimpleJdbcInsert simpleJdbcInsert;

    public RoomThemeRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        this.simpleJdbcInsert = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("theme")
                .usingGeneratedKeyColumns("id");
    }

    public List<RoomTheme> findAll() {
        return jdbcTemplate.query("SELECT * FROM theme",
                (rs, rowNum) -> new RoomTheme(
                        rs.getLong("id"),
                        rs.getString("name"),
                        rs.getString("description"),
                        rs.getString("thumbnail")
                ));
    }

    public List<RoomTheme> findAllRanking() {
        return jdbcTemplate.query("""
                select t.id, t.name, t.description, t.thumbnail from theme as t
                inner join reservation as r on r.theme_id = t.id
                WHERE (NOW() -  8) < r.date and r.date < NOW()
                group by t.id
                order by count(t.id) desc
                limit 10
                """, (rs, rowNum) -> new RoomTheme(
                rs.getLong("id"),
                rs.getString("name"),
                rs.getString("description"),
                rs.getString("thumbnail")
        ));
    }

    public Optional<RoomTheme> findById(Long id) {
        if (id == null) {
            throw new BadRequestException("id가 빈값일 수 없습니다.");
        }

        try {
            return Optional.ofNullable(jdbcTemplate.queryForObject("SELECT * FROM theme WHERE id = ?",
                    (rs, rowNum) -> new RoomTheme(
                            rs.getLong("id"),
                            rs.getString("name"),
                            rs.getString("description"),
                            rs.getString("thumbnail")
                    ), id));
        } catch (EmptyResultDataAccessException exception) {
            return Optional.empty();
        }
    }

    public RoomTheme save(RoomTheme roomTheme) {
        if (roomTheme == null) {
            throw new BadRequestException("테마가 빈값일 수 없습니다.");
        }
        SqlParameterSource parameterSource = new MapSqlParameterSource()
                .addValue("name", roomTheme.getName())
                .addValue("description", roomTheme.getDescription())
                .addValue("thumbnail", roomTheme.getThumbnail());

        long id = simpleJdbcInsert.executeAndReturnKey(parameterSource).longValue();
        return roomTheme.withId(id);
    }

    public boolean deleteById(Long id) {
        if (id == null) {
            throw new BadRequestException("id가 빈값일 수 없습니다.");
        }
        return jdbcTemplate.update("DELETE FROM theme WHERE id = ?", id) > 0;
    }
}
