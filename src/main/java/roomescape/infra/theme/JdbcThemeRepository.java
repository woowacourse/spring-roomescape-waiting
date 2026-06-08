package roomescape.infra.theme;

import java.time.LocalDate;
import java.util.List;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import roomescape.domain.theme.Theme;
import roomescape.domain.theme.ThemeRankResult;
import roomescape.domain.theme.ThemeRepository;

@Repository
public class JdbcThemeRepository implements ThemeRepository {

    private static final String TABLE_NAME = "theme";
    private static final String COLUMN_ID = "id";
    private static final String COLUMN_NAME = "name";
    private static final String COLUMN_CONTENT = "content";
    private static final String COLUMN_URL = "url";

    private static final String FIND_ALL_SQL = "select id, name, content, url from theme order by id";
    private static final String DELETE_BY_ID_SQL = "delete from theme where id = :id";
    private static final String FIND_ALL_WITH_RANK_SQL = """
            select
                t.id,
                t.name,
                t.content,
                t.url,
                rank() over (order by count(r.id) desc) as rank
            from theme t
            join reservation_slot rs on rs.theme_id = t.id
            join reservation r on r.reservation_slot_id = rs.id
            where rs.date between :startDay and :today
            group by t.id, t.name, t.content, t.url
            order by rank, t.id
            limit :rankLimit
            """;
    private static final RowMapper<Theme> THEME_ROW_MAPPER = (rs, rowNum) -> Theme.of(
            rs.getLong(COLUMN_ID),
            rs.getString(COLUMN_NAME),
            rs.getString(COLUMN_CONTENT),
            rs.getString(COLUMN_URL)
    );
    private static final RowMapper<ThemeRankResult> THEME_RANK_ROW_MAPPER = (rs, rowNum) -> ThemeRankResult.of(
            Theme.of(
                    rs.getLong(COLUMN_ID),
                    rs.getString(COLUMN_NAME),
                    rs.getString(COLUMN_CONTENT),
                    rs.getString(COLUMN_URL)
            ),
            rs.getInt("rank")
    );

    private final NamedParameterJdbcTemplate jdbcTemplate;
    private final SimpleJdbcInsert simpleJdbcInsert;

    public JdbcThemeRepository(NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        this.simpleJdbcInsert = new SimpleJdbcInsert(jdbcTemplate.getJdbcTemplate())
                .withTableName(TABLE_NAME)
                .usingGeneratedKeyColumns(COLUMN_ID);
    }

    @Override
    public List<Theme> findAll() {
        return jdbcTemplate.query(FIND_ALL_SQL, new MapSqlParameterSource(), THEME_ROW_MAPPER);
    }

    @Override
    public List<ThemeRankResult> findPopularThemes(int rankLimit, LocalDate startDay, LocalDate today) {
        return jdbcTemplate.query(
                FIND_ALL_WITH_RANK_SQL,
                new MapSqlParameterSource()
                        .addValue("startDay", startDay)
                        .addValue("today", today)
                        .addValue("rankLimit", rankLimit),
                THEME_RANK_ROW_MAPPER
        );
    }

    @Override
    public Theme save(Theme theme) {
        Number key = simpleJdbcInsert.executeAndReturnKey(new MapSqlParameterSource()
                .addValue(COLUMN_NAME, theme.getName())
                .addValue(COLUMN_CONTENT, theme.getDescription())
                .addValue(COLUMN_URL, theme.getThumbnailUrl()));
        return Theme.of(extractId(key), theme.getName(), theme.getDescription(), theme.getThumbnailUrl());
    }

    @Override
    public int deleteById(Long id) {
        return jdbcTemplate.update(DELETE_BY_ID_SQL, new MapSqlParameterSource().addValue(COLUMN_ID, id));
    }

    private long extractId(Number key) {
        if (key == null) {
            throw new IllegalStateException("생성 키를 조회할 수 없습니다.");
        }
        return key.longValue();
    }
}
