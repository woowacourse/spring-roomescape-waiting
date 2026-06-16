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
import roomescape.domain.member.Member;
import roomescape.domain.member.MemberRole;
import roomescape.domain.vo.Slot;
import roomescape.domain.store.Store;
import roomescape.domain.theme.Theme;
import roomescape.domain.time.Time;
import roomescape.domain.waiting.Waiting;
import roomescape.domain.waiting.Waitings;
import roomescape.domain.vo.Name;

@Repository
public class WaitingJdbcDao implements WaitingDao {
    private static final RowMapper<Member> MEMBER_ROW_MAPPER = (rs, rowNum) -> {
        Long memberStoreId = rs.getObject("member_store_id", Long.class);
        Store memberStore = memberStoreId == null ? null
                : new Store(memberStoreId, rs.getString("member_store_name"));
        return new Member(
                rs.getLong("member_id"),
                rs.getString("member_name"),
                rs.getString("member_email"),
                rs.getString("member_password"),
                MemberRole.valueOf(rs.getString("member_role")),
                memberStore
        );
    };
    private static final RowMapper<Time> TIME_ROW_MAPPER = (rs, rowNum) -> new Time(
            rs.getLong("time_id"),
            LocalTime.parse(rs.getString("time_start_at"))
    );
    private static final RowMapper<Theme> THEME_ROW_MAPPER = (rs, rowNum) -> new Theme(
            rs.getLong("theme_id"),
            new Name(rs.getString("theme_name")),
            rs.getString("theme_thumbnail_url"),
            rs.getString("theme_description"),
            rs.getLong("theme_price")
    );
    private static final RowMapper<Waiting> ROW_MAPPER = (rs, rowNum) -> {
        Long waitingStoreId = rs.getObject("waiting_store_id", Long.class);
        Store waitingStore = waitingStoreId == null ? null
                : new Store(waitingStoreId, rs.getString("waiting_store_name"));
        return Waiting.reconstruct(
                rs.getLong("id"),
                MEMBER_ROW_MAPPER.mapRow(rs, rowNum),
                LocalDate.parse(rs.getString("date")),
                TIME_ROW_MAPPER.mapRow(rs, rowNum),
                THEME_ROW_MAPPER.mapRow(rs, rowNum),
                waitingStore
        );
    };
    private static final String BASE_SELECT = """
            SELECT
                    w.id,
                    w.date,
                    ws.id AS waiting_store_id,
                    ws.name AS waiting_store_name,
                    m.id AS member_id,
                    m.name AS member_name,
                    m.email AS member_email,
                    m.password AS member_password,
                    m.role AS member_role,
                    ms.id AS member_store_id,
                    ms.name AS member_store_name,
                    t.id AS time_id,
                    t.start_at AS time_start_at,
                    th.id AS theme_id,
                    th.name AS theme_name,
                    th.thumbnail_url AS theme_thumbnail_url,
                    th.description AS theme_description,
                    th.price AS theme_price
                FROM waitings w
                INNER JOIN members m ON w.member_id = m.id
                INNER JOIN times t ON w.time_id = t.id
                INNER JOIN themes th ON w.theme_id = th.id
                LEFT JOIN stores ws ON w.store_id = ws.id
                LEFT JOIN stores ms ON m.store_id = ms.id
            """;
    private static final String DEFAULT_ORDER = "ORDER BY w.date, w.time_id, w.theme_id, w.store_id, w.id";

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
        return waiting.withId(id);
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
    public Waitings findQueueBySlot(Slot slot) {
        return findQueueBySlot(slot, "");
    }

    @Override
    public Waitings findQueueBySlotForUpdate(Slot slot) {
        return findQueueBySlot(slot, "FOR UPDATE");
    }

    private Waitings findQueueBySlot(Slot slot, String lockClause) {
        String sql = BASE_SELECT + """
                WHERE w.date = :date AND w.time_id = :timeId AND w.theme_id = :themeId AND w.store_id = :storeId
                ORDER BY w.id
                """ + lockClause;
        SqlParameterSource params = new MapSqlParameterSource("date", slot.getDate())
                .addValue("timeId", slot.getTime().getId())
                .addValue("themeId", slot.getTheme().getId())
                .addValue("storeId", slot.getStoreId());

        return new Waitings(slot, jdbcTemplate.query(sql, params, ROW_MAPPER));
    }

    @Override
    public Optional<Waiting> findFirstBySlotKeyForUpdate(Long themeId, Long timeId, LocalDate date, Long storeId) {
        String sql = BASE_SELECT + """
                WHERE w.date = :date AND w.time_id = :timeId AND w.theme_id = :themeId AND w.store_id = :storeId
                ORDER BY w.id
                LIMIT 1
                FOR UPDATE
                """;
        SqlParameterSource params = new MapSqlParameterSource("date", date)
                .addValue("timeId", timeId)
                .addValue("themeId", themeId)
                .addValue("storeId", storeId);
        return jdbcTemplate.query(sql, params, ROW_MAPPER).stream().findFirst();
    }

    @Override
    public List<Waitings> findAllQueues() {
        String sql = BASE_SELECT + DEFAULT_ORDER;
        return toQueues(jdbcTemplate.query(sql, ROW_MAPPER));
    }

    @Override
    public List<Waitings> findQueuesContainingMember(Long memberId) {
        String sql = BASE_SELECT + """
                WHERE EXISTS (
                    SELECT 1
                    FROM waitings mine
                    WHERE mine.member_id = :memberId
                    AND mine.date = w.date
                    AND mine.time_id = w.time_id
                    AND mine.theme_id = w.theme_id
                    AND mine.store_id = w.store_id
                )
                """ + DEFAULT_ORDER;

        SqlParameterSource params = new MapSqlParameterSource("memberId", memberId);

        return toQueues(jdbcTemplate.query(sql, params, ROW_MAPPER));
    }

    @Override
    public List<Waitings> findQueuesByStoreId(Long storeId) {
        String sql = BASE_SELECT + """
                WHERE w.store_id = :storeId
                """ + DEFAULT_ORDER;

        SqlParameterSource params = new MapSqlParameterSource("storeId", storeId);

        return toQueues(jdbcTemplate.query(sql, params, ROW_MAPPER));
    }

    private List<Waitings> toQueues(List<Waiting> waitings) {
        return waitings.stream()
                .collect(java.util.stream.Collectors.groupingBy(
                        Waiting::getSlot,
                        java.util.LinkedHashMap::new,
                        java.util.stream.Collectors.toList()
                ))
                .entrySet()
                .stream()
                .map(entry -> new Waitings(entry.getKey(), entry.getValue()))
                .toList();
    }
}
