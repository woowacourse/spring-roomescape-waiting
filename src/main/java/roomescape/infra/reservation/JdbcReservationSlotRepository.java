package roomescape.infra.reservation;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import roomescape.domain.reservation.ReservationSlot;
import roomescape.domain.reservation.ReservationSlotRepository;
import roomescape.domain.reservation.ReservationTime;
import roomescape.domain.theme.Theme;

@Repository
public class JdbcReservationSlotRepository implements ReservationSlotRepository {

    private static final String TABLE_NAME = "reservation_slot";
    private static final String COLUMN_ID = "id";
    private static final String COLUMN_DATE = "date";
    private static final String COLUMN_TIME_ID = "time_id";
    private static final String COLUMN_START_AT = "start_at";
    private static final String COLUMN_THEME_ID = "theme_id";
    private static final String COLUMN_THEME_NAME = "theme_name";
    private static final String COLUMN_THEME_CONTENT = "theme_content";
    private static final String COLUMN_THEME_URL = "theme_url";
    private static final String PARAM_TIME_ID = "timeId";
    private static final String PARAM_THEME_ID = "themeId";
    private static final String FIND_ALL_SQL = """
            select rs.id,
                   rs.date,
                   rt.id as time_id, rt.start_at,
                   th.id as theme_id, th.name as theme_name, th.content as theme_content, th.url as theme_url
            from reservation_slot rs
            join reservation_time rt on rs.time_id = rt.id
            join theme th on rs.theme_id = th.id
            order by rs.date, rt.start_at, rs.id
            """;
    private static final String COUNT_BY_TIME_ID_SQL = """
            select count(*)
            from reservation_slot
            where time_id = :timeId
            """;
    private static final String DELETE_BY_ID_SQL = "delete from reservation_slot where id = :id";
    private static final String COUNT_BY_THEME_ID_SQL = """
            select count(*)
            from reservation_slot
            where theme_id = :themeId
            """;
    private static final String FIND_BY_SCHEDULE_SQL = """
            select rs.id,
                   rs.date,
                   rt.id as time_id, rt.start_at,
                   th.id as theme_id, th.name as theme_name, th.content as theme_content, th.url as theme_url
            from reservation_slot rs
            join reservation_time rt on rs.time_id = rt.id
            join theme th on rs.theme_id = th.id
            where rs.time_id = :timeId and rs.date = :date and rs.theme_id = :themeId
            """;
    private static final String FIND_BY_SCHEDULE_FOR_UPDATE_SQL = """
            select rs.id,
                   rs.date,
                   rt.id as time_id, rt.start_at,
                   th.id as theme_id, th.name as theme_name, th.content as theme_content, th.url as theme_url
            from reservation_slot rs
            join reservation_time rt on rs.time_id = rt.id
            join theme th on rs.theme_id = th.id
            where rs.time_id = :timeId and rs.date = :date and rs.theme_id = :themeId
            for update
            """;
    private static final String FIND_BY_ID_SQL = """
            select rs.id,
                   rs.date,
                   rt.id as time_id, rt.start_at,
                   th.id as theme_id, th.name as theme_name, th.content as theme_content, th.url as theme_url
            from reservation_slot rs
            join reservation_time rt on rs.time_id = rt.id
            join theme th on rs.theme_id = th.id
            where rs.id = :id
            """;
    private static final RowMapper<ReservationSlot> RESERVATION_SLOT_ROW_MAPPER = (rs, rowNum) -> ReservationSlot.of(
            rs.getLong(COLUMN_ID),
            rs.getDate(COLUMN_DATE).toLocalDate(),
            ReservationTime.of(
                    rs.getLong(COLUMN_TIME_ID),
                    rs.getTime(COLUMN_START_AT).toLocalTime()
            ),
            Theme.of(
                    rs.getLong(COLUMN_THEME_ID),
                    rs.getString(COLUMN_THEME_NAME),
                    rs.getString(COLUMN_THEME_CONTENT),
                    rs.getString(COLUMN_THEME_URL)
            )
    );

    private final NamedParameterJdbcTemplate jdbcTemplate;
    private final SimpleJdbcInsert simpleJdbcInsert;

    public JdbcReservationSlotRepository(NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        this.simpleJdbcInsert = new SimpleJdbcInsert(jdbcTemplate.getJdbcTemplate())
                .withTableName(TABLE_NAME)
                .usingGeneratedKeyColumns(COLUMN_ID);
    }

    @Override
    public ReservationSlot save(ReservationSlot reservation) {
        Number key = simpleJdbcInsert.executeAndReturnKey(new MapSqlParameterSource()
                .addValue(COLUMN_DATE, reservation.getDate())
                .addValue(COLUMN_TIME_ID, reservation.getTime().getId())
                .addValue(COLUMN_THEME_ID, reservation.getTheme().getId()));
        return ReservationSlot.of(extractId(key), reservation.getDate(), reservation.getTime(), reservation.getTheme());
    }

    public List<ReservationSlot> findAll() {
        return jdbcTemplate.query(FIND_ALL_SQL, new MapSqlParameterSource(), RESERVATION_SLOT_ROW_MAPPER);
    }

    public int deleteById(Long id) {
        return jdbcTemplate.update(DELETE_BY_ID_SQL, new MapSqlParameterSource().addValue(COLUMN_ID, id));
    }

    public int countByTimeId(Long timeId) {
        Integer count = jdbcTemplate.queryForObject(
                COUNT_BY_TIME_ID_SQL,
                new MapSqlParameterSource().addValue(PARAM_TIME_ID, timeId),
                Integer.class
        );
        if (count == null) {
            return 0;
        }
        return count;
    }

    public int countByThemeId(Long themeId) {
        Integer count = jdbcTemplate.queryForObject(
                COUNT_BY_THEME_ID_SQL,
                new MapSqlParameterSource().addValue(PARAM_THEME_ID, themeId),
                Integer.class
        );
        if (count == null) {
            return 0;
        }
        return count;
    }

    @Override
    public boolean existsByTimeId(Long id) {
        return countByTimeId(id) > 0;
    }

    @Override
    public boolean existsByThemeId(Long id) {
        return countByThemeId(id) > 0;
    }

    @Override
    public Optional<ReservationSlot> findBySchedule(Long timeId, LocalDate date, Long themeId) {
        return findBySchedule(FIND_BY_SCHEDULE_SQL, timeId, date, themeId);
    }

    @Override
    public Optional<ReservationSlot> findByScheduleForUpdate(Long timeId, LocalDate date, Long themeId) {
        return findBySchedule(FIND_BY_SCHEDULE_FOR_UPDATE_SQL, timeId, date, themeId);
    }

    public Optional<ReservationSlot> findById(Long id) {
        List<ReservationSlot> result = jdbcTemplate.query(
                FIND_BY_ID_SQL,
                new MapSqlParameterSource().addValue(COLUMN_ID, id),
                RESERVATION_SLOT_ROW_MAPPER
        );
        return result.stream().findFirst();
    }

    private long extractId(Number key) {
        if (key == null) {
            throw new IllegalStateException("생성 키를 조회할 수 없습니다.");
        }
        return key.longValue();
    }

    private Optional<ReservationSlot> findBySchedule(String sql, Long timeId, LocalDate date, Long themeId) {
        List<ReservationSlot> result = jdbcTemplate.query(
                sql,
                new MapSqlParameterSource()
                        .addValue(PARAM_TIME_ID, timeId)
                        .addValue(COLUMN_DATE, date)
                        .addValue(PARAM_THEME_ID, themeId),
                RESERVATION_SLOT_ROW_MAPPER
        );
        return result.stream().findFirst();
    }
}
