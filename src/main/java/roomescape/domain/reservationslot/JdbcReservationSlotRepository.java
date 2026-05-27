package roomescape.domain.reservationslot;

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
import roomescape.domain.reservationdate.ReservationDate;
import roomescape.domain.reservationtime.ReservationTime;
import roomescape.domain.theme.Theme;

@Repository
@RequiredArgsConstructor
public class JdbcReservationSlotRepository implements ReservationSlotRepository {

    private static final String COLUMN_ID = "id";
    private static final String COLUMN_NAME = "name";
    private static final String COLUMN_DATE_ID = "date_id";
    private static final String COLUMN_DATE = "date";
    private static final String COLUMN_TIME_ID = "time_id";
    private static final String COLUMN_START_AT = "start_at";
    private static final String COLUMN_THEME_ID = "theme_id";
    private static final String COLUMN_THEME_NAME = "theme_name";
    private static final String COLUMN_THEME_CONTENT = "theme_content";
    private static final String COLUMN_THEME_URL = "theme_url";
    private static final String COLUMN_CONTENT = "content";
    private static final String COLUMN_URL = "url";

    private static final String INSERT_SQL = "insert into reservation_slot(date_id, time_id, theme_id) values (?, ?, ?)";
    private static final String FIND_ALL_SQL =
        """
            select rs.id,
                   rd.id as date_id, rd.date,
                   rt.id as time_id, rt.start_at,
                   th.id as theme_id, th.name as theme_name, th.content as theme_content, th.url as theme_url
            from reservation_slot rs
            join reservation_date rd on rs.date_id = rd.id
            join reservation_time rt on rs.time_id = rt.id
            join theme th on rs.theme_id = th.id
            order by rs.id
            """;
    private static final String COUNT_BY_TIME_ID_SQL =
        """
            select count(*)
            from reservation_slot
            where time_id = ?
            """;
    private static final String COUNT_BY_RESERVATION_DATE_ID_SQL =
        """
            select count(*)
            from reservation_slot
            where date_id = ?
            """;
    private static final String DELETE_BY_ID_SQL = "delete from reservation_slot where id = ?";
    private static final String FIND_BY_THEME_AND_DATE_SQL =
        """
            select distinct rs.time_id
            from reservation_slot rs
            join reservation r on r.reservation_slot_id = rs.id
            where rs.theme_id = ? and rs.date_id = ?
            """;

    private static final String FIND_POPULAR_THEME_SQL =
        """
            select
                th.id,
                th.name,
                th.content,
                th.url
            from reservation r
            join reservation_slot rs on r.reservation_slot_id = rs.id
            join reservation_date rd on rs.date_id = rd.id
            join theme th on rs.theme_id = th.id
            where rd.date between ? and ?
              and r.status = 'CONFIRMED'
            group by th.id, th.name, th.content, th.url
            order by count(r.id) desc, th.id asc
            limit ?
            """;
    private static final String COUNT_BY_THEME_ID_SQL =
        """
            select count(*)
            from reservation_slot
            where theme_id = ?
            """;
    private static final String EXISTS_RESERVATION_BY_TIME_AND_DATE_AND_THEME_SQL =
        """
            select exists(
                select 1
                from reservation_slot r
                where time_id = ? and date_id = ? and theme_id = ?
            )
            """;
    private static final String FIND_BY_SCHEDULE_SQL =
        """
            select rs.id,
                   rd.id as date_id, rd.date,
                   rt.id as time_id, rt.start_at,
                   th.id as theme_id, th.name as theme_name, th.content as theme_content, th.url as theme_url
            from reservation_slot rs
            join reservation_date rd on rs.date_id = rd.id
            join reservation_time rt on rs.time_id = rt.id
            join theme th on rs.theme_id = th.id
            where rs.time_id = ? and rs.date_id = ? and rs.theme_id = ?
            """;
    private static final String FIND_BY_ID_SQL =
        """
            select rs.id,
                   rd.id as date_id, rd.date,
                   rt.id as time_id, rt.start_at,
                   th.id as theme_id, th.name as theme_name, th.content as theme_content, th.url as theme_url
            from reservation_slot rs
            join reservation_date rd on rs.date_id = rd.id
            join reservation_time rt on rs.time_id = rt.id
            join theme th on rs.theme_id = th.id
            where rs.id = ?
            """;
    private static final String UPDATE_SQL =
        """
            update reservation_slot
            set date_id = ?, time_id = ?, theme_id = ?
            where id = ?
            """;

    private final JdbcTemplate jdbcTemplate;

    @Override
    public ReservationSlot save(ReservationSlot reservation) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(INSERT_SQL, Statement.RETURN_GENERATED_KEYS);
            ps.setLong(1, reservation.getDate().getId());
            ps.setLong(2, reservation.getTime().getId());
            ps.setLong(3, reservation.getTheme().getId());
            return ps;
        }, keyHolder);
        long id = extractId(keyHolder);
        return ReservationSlot.createWithId(id, reservation);
    }

    @Override
    public List<ReservationSlot> findAll() {
        return jdbcTemplate.query(FIND_ALL_SQL, reservationRowMapper());
    }

    @Override
    public int deleteById(Long id) {
        return jdbcTemplate.update(DELETE_BY_ID_SQL, id);
    }

    @Override
    public int countByTimeId(Long timeId) {
        Integer count = jdbcTemplate.queryForObject(COUNT_BY_TIME_ID_SQL, Integer.class, timeId);
        if (count == null) {
            return 0;
        }
        return count;
    }

    @Override
    public int countByReservationDateId(Long dateId) {
        Integer count = jdbcTemplate.queryForObject(COUNT_BY_RESERVATION_DATE_ID_SQL, Integer.class, dateId);
        if (count == null) {
            return 0;
        }
        return count;
    }

    @Override
    public List<Long> findReservedTimes(Long themeId, Long dateId) {
        return jdbcTemplate.query(FIND_BY_THEME_AND_DATE_SQL, reservationTimeIdRowMapper(), themeId, dateId);
    }

    @Override
    public List<Theme> findPopularThemes(int rankLimit, LocalDate startDay, LocalDate endDay) {
        return jdbcTemplate.query(FIND_POPULAR_THEME_SQL, popularThemeRowMapper(), startDay, endDay, rankLimit);
    }

    @Override
    public int countByThemeId(Long themeId) {
        Integer count = jdbcTemplate.queryForObject(COUNT_BY_THEME_ID_SQL, Integer.class, themeId);
        if (count == null) {
            return 0;
        }
        return count;
    }

    @Override
    public boolean existsReservation(Long timeId, Long dateId, Long themeId) {
        return existsBySchedule(timeId, dateId, themeId);
    }

    @Override
    public boolean existsBySchedule(Long timeId, Long dateId, Long themeId) {
        Boolean exists = jdbcTemplate.queryForObject(
            EXISTS_RESERVATION_BY_TIME_AND_DATE_AND_THEME_SQL,
            Boolean.class,
            timeId,
            dateId,
            themeId
        );
        return exists != null && exists;
    }

    @Override
    public Optional<ReservationSlot> findBySchedule(Long timeId, Long dateId, Long themeId) {
        List<ReservationSlot> result = jdbcTemplate.query(
            FIND_BY_SCHEDULE_SQL,
            reservationRowMapper(),
            timeId,
            dateId,
            themeId
        );
        return result.stream().findFirst();
    }

    @Override
    public Optional<ReservationSlot> findById(Long id) {
        List<ReservationSlot> result = jdbcTemplate.query(FIND_BY_ID_SQL, reservationRowMapper(), id);
        return result.stream().findFirst();
    }

    @Override
    public Optional<ReservationSlot> update(Long id, ReservationSlot withoutId) {
        int updatedCount = jdbcTemplate.update(
            UPDATE_SQL,
            withoutId.getDate().getId(),
            withoutId.getTime().getId(),
            withoutId.getTheme().getId(),
            id
        );
        if (updatedCount == 0) {
            return Optional.empty();
        }
        return findById(id);
    }

    private RowMapper<ReservationSlot> reservationRowMapper() {
        return (rs, rowNum) -> ReservationSlot.of(
            rs.getLong(COLUMN_ID),
            ReservationDate.of(
                rs.getLong(COLUMN_DATE_ID),
                rs.getDate(COLUMN_DATE).toLocalDate()
            ),
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
    }

    private RowMapper<Long> reservationTimeIdRowMapper() {
        return (rs, rowNum) -> rs.getLong(COLUMN_TIME_ID);
    }

    private RowMapper<Theme> popularThemeRowMapper() {
        return (rs, rowNum) -> Theme.of(
            rs.getLong(COLUMN_ID),
            rs.getString(COLUMN_NAME),
            rs.getString(COLUMN_CONTENT),
            rs.getString(COLUMN_URL)
        );
    }

    private long extractId(KeyHolder keyHolder) {
        if (keyHolder.getKey() == null) {
            throw new IllegalStateException("생성 키를 조회할 수 없습니다.");
        }
        return keyHolder.getKey().longValue();
    }
}
