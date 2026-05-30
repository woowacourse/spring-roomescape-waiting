package roomescape.dao.jdbc;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import roomescape.dao.WaitingDao;
import roomescape.domain.Member;
import roomescape.domain.MemberRole;
import roomescape.domain.Theme;
import roomescape.domain.Time;
import roomescape.domain.Waiting;
import roomescape.domain.vo.Name;

@Repository
public class WaitingJdbcDao implements WaitingDao {
    private static final RowMapper<Member> MEMBER_ROW_MAPPER = (rs, rowNum) -> new Member(
            rs.getLong("member_id"),
            rs.getString("member_name"),
            rs.getString("member_email"),
            rs.getString("member_password"),
            MemberRole.valueOf(rs.getString("member_role")),
            rs.getObject("member_store_id", Long.class)
    );
    private static final RowMapper<Time> TIME_ROW_MAPPER = (rs, rowNum) -> new Time(
            rs.getLong("time_id"),
            LocalTime.parse(rs.getString("time_start_at"))
    );
    private static final RowMapper<Theme> THEME_ROW_MAPPER = (rs, rowNum) -> new Theme(
            rs.getLong("theme_id"),
            new Name(rs.getString("theme_name")),
            rs.getString("theme_thumbnail_url"),
            rs.getString("theme_description")
    );
    private static final RowMapper<Waiting> ROW_MAPPER = (rs, rowNum) -> Waiting.reconstruct(
            rs.getLong("id"),
            MEMBER_ROW_MAPPER.mapRow(rs, rowNum),
            LocalDate.parse(rs.getString("date")),
            TIME_ROW_MAPPER.mapRow(rs, rowNum),
            THEME_ROW_MAPPER.mapRow(rs, rowNum),
            rs.getObject("store_id", Long.class),
            rs.getLong("rank")
    );
    private static final String BASE_SELECT = """
            SELECT
                    w.id,
                    w.date,
                    w.store_id,
                    m.id AS member_id,
                    m.name AS member_name,
                    m.email AS member_email,
                    m.password AS member_password,
                    m.role AS member_role,
                    m.store_id AS member_store_id,
                    t.id AS time_id,
                    t.start_at AS time_start_at,
                    th.id AS theme_id,
                    th.name AS theme_name,
                    th.thumbnail_url AS theme_thumbnail_url,
                    th.description AS theme_description,
                    (SELECT COUNT(*)+1
                        FROM waitings w2
                        WHERE w2.date = w.date
                        AND w2.time_id = w.time_id
                        AND w2.theme_id = w.theme_id
                        AND w2.store_id = w.store_id
                        AND w2.id < w.id) AS rank
                FROM waitings w
                INNER JOIN members m ON w.member_id = m.id
                INNER JOIN times t ON w.time_id = t.id
                INNER JOIN themes th ON w.theme_id = th.id
            """;

    private final NamedParameterJdbcTemplate jdbcTemplate;
    private final SimpleJdbcInsert simpleJdbcInsert;

    public WaitingJdbcDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = new NamedParameterJdbcTemplate(jdbcTemplate);
        this.simpleJdbcInsert = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("waitings")
                .usingGeneratedKeyColumns("id")
                .usingColumns("member_id", "date", "time_id", "theme_id", "store_id");
    }

    @Override
    public List<Waiting> findAll() {
        return jdbcTemplate.query(BASE_SELECT, ROW_MAPPER);
    }

    @Override
    public Optional<Waiting> findById(Long id) {
        String sql = BASE_SELECT + "WHERE w.id = :id";
        MapSqlParameterSource parameterSource = new MapSqlParameterSource("id", id);

        return jdbcTemplate.query(sql, parameterSource, ROW_MAPPER)
                .stream().findFirst();
    }

    @Override
    public Waiting insert(Waiting waiting) {
        SqlParameterSource params = new MapSqlParameterSource()
                .addValue("member_id", waiting.getMember().getId())
                .addValue("date", waiting.getDate())
                .addValue("time_id", waiting.getTime().getId())
                .addValue("theme_id", waiting.getTheme().getId())
                .addValue("store_id", waiting.getStoreId());

        long id = simpleJdbcInsert.executeAndReturnKey(params).longValue();

        return findById(id)
                .orElseThrow(() -> new IllegalStateException("Waiting insert는 성공했지만, 조회는 실피했습니다"));
    }

    @Override
    public Waiting update(Waiting waiting) {
        String sql = """
                UPDATE waitings
                SET member_id = :memberId, date = :date, time_id = :timeId, theme_id = :themeId, store_id = :storeId
                WHERE id = :id
                """;
        SqlParameterSource params = new MapSqlParameterSource("id", waiting.getId())
                .addValue("memberId", waiting.getMember().getId())
                .addValue("date", waiting.getDate())
                .addValue("timeId", waiting.getTime().getId())
                .addValue("themeId", waiting.getTheme().getId())
                .addValue("storeId", waiting.getStoreId());

        jdbcTemplate.update(sql, params);
        return waiting;
    }

    @Override
    public boolean delete(Long id) {
        String sql = """
                DELETE FROM waitings
                WHERE id = :id
                """;
        SqlParameterSource params = new MapSqlParameterSource("id", id);
        return jdbcTemplate.update(sql, params) > 0;
    }

    @Override
    public boolean existsById(Long id) {
        String sql = """
                SELECT EXISTS(SELECT 1 FROM waitings WHERE id = :id);
                """;
        SqlParameterSource params = new MapSqlParameterSource("id", id);
        return Boolean.TRUE.equals(jdbcTemplate.queryForObject(sql, params, Boolean.class));
    }

    @Override
    public boolean existsByMemberIdAndDateAndTimeIdAndThemeIdAndStoreId(
            Long memberId, LocalDate date, Long timeId, Long themeId, Long storeId) {
        String sql = """
                SELECT EXISTS(
                    SELECT 1 FROM waitings
                    WHERE member_id = :memberId
                    AND date = :date
                    AND time_id = :timeId
                    AND theme_id = :themeId
                    AND store_id = :storeId
                );
                """;
        SqlParameterSource params = new MapSqlParameterSource()
                .addValue("memberId", memberId)
                .addValue("date", date)
                .addValue("timeId", timeId)
                .addValue("themeId", themeId)
                .addValue("storeId", storeId);
        return Boolean.TRUE.equals(jdbcTemplate.queryForObject(sql, params, Boolean.class));
    }

    @Override
    public Optional<Waiting> findFirst(LocalDate date, Long timeId, Long themeId, Long storeId) {
        String sql = BASE_SELECT + """
                WHERE w.date = :date AND w.time_id = :timeId AND w.theme_id = :themeId AND w.store_id = :storeId
                ORDER BY w.id LIMIT 1
                """;
        SqlParameterSource params = new MapSqlParameterSource("date", date)
                .addValue("timeId", timeId)
                .addValue("themeId", themeId)
                .addValue("storeId", storeId);

        return jdbcTemplate.query(sql, params, ROW_MAPPER)
                .stream().findFirst();
    }

    @Override
    public List<Waiting> findAllByMemberId(Long memberId) {
        String sql = BASE_SELECT + """
                WHERE w.member_id = :memberId
                ORDER BY w.date, w.time_id
                """;

        SqlParameterSource params = new MapSqlParameterSource("memberId", memberId);

        return jdbcTemplate.query(sql, params, ROW_MAPPER);
    }

    @Override
    public List<Waiting> findAllByStoreId(Long storeId) {
        String sql = BASE_SELECT + """
                WHERE w.store_id = :storeId
                ORDER BY w.date, w.time_id, w.id
                """;

        SqlParameterSource params = new MapSqlParameterSource("storeId", storeId);

        return jdbcTemplate.query(sql, params, ROW_MAPPER);
    }
}
