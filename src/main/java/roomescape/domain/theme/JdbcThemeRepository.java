package roomescape.domain.theme;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import roomescape.domain.theme.dto.ThemeRankResult;
import roomescape.support.exception.InternalServerException;
import roomescape.support.exception.errors.RoomescapeErrors;

@Repository
@RequiredArgsConstructor
public class JdbcThemeRepository implements ThemeRepository {

    private static final String COLUMN_ID = "id";
    private static final String COLUMN_NAME = "name";
    private static final String COLUMN_CONTENT = "content";
    private static final String COLUMN_URL = "url";

    private static final String FIND_ALL_SQL = "select id, name, content, url from theme order by id";
    private static final String FIND_BY_ID_SQL = "select id, name, content, url from theme where id = ?";
    private static final String INSERT_SQL = "insert into theme(name, content, url) values (?, ?, ?)";
    private static final String DELETE_BY_ID_SQL = "delete from theme where id = ?";
    private static final String FIND_ALL_WITH_RANK_SQL = """
        select
            t.id,
            t.name,
            t.url,
            rank() over (order by count(r.id) desc) as rank
        from theme t
        join reservation_slot rs on rs.theme_id = t.id
        join reservation r on r.reservation_slot_id = rs.id
        join reservation_date rd on rd.id = rs.date_id
        where rd.date between ? and ?
          and r.status <> 'CANCELED'
        group by t.id, t.name, t.url
        order by rank, t.id
        limit ?
        """;

    private final JdbcTemplate jdbcTemplate;

    @Override
    public Optional<Theme> findById(Long id) {
        List<Theme> result = jdbcTemplate.query(FIND_BY_ID_SQL, themeRowMapper(), id);
        return result.stream().findFirst();
    }

    @Override
    public List<Theme> findAll() {
        return jdbcTemplate.query(FIND_ALL_SQL, themeRowMapper());
    }

    @Override
    public List<ThemeRankResult> findPopularThemes(int rankLimit, LocalDate startDay, LocalDate today) {
        return jdbcTemplate.query(
            FIND_ALL_WITH_RANK_SQL,
            themeRankRowMapper(),
            startDay,
            today,
            rankLimit
        );
    }

    @Override
    public Theme save(Theme theme) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(INSERT_SQL, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, theme.getName());
            ps.setString(2, theme.getContent());
            ps.setString(3, theme.getUrl());
            return ps;
        }, keyHolder);
        long id = extractId(keyHolder);
        return Theme.of(
            id,
            theme.getName(),
            theme.getContent(),
            theme.getUrl()
        );
    }

    @Override
    public int deleteById(Long id) {
        return jdbcTemplate.update(DELETE_BY_ID_SQL, id);
    }

    private RowMapper<Theme> themeRowMapper() {
        return ((rs, rowNum) -> Theme.of(
            rs.getLong(COLUMN_ID),
            rs.getString(COLUMN_NAME),
            rs.getString(COLUMN_CONTENT),
            rs.getString(COLUMN_URL)
        ));
    }

    private RowMapper<ThemeRankResult> themeRankRowMapper() {
        return ((rs, rowNum) -> ThemeRankResult.of(
            rs.getLong(COLUMN_ID),
            rs.getString(COLUMN_NAME),
            rs.getString(COLUMN_URL),
            rs.getInt("rank")
        ));
    }

    private long extractId(KeyHolder keyHolder) {
        if (keyHolder.getKey() == null) {
            throw new InternalServerException(RoomescapeErrors.INVALID_GENERATED_KEY);
        }
        return keyHolder.getKey().longValue();
    }
}
