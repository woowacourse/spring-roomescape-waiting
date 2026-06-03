package roomescape.domain.reservation;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.LocalTime;
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
public class JdbcReservationRepository implements ReservationRepository {

    private static final String INSERT_SQL = "insert into reservation(name, date_id, time_id, theme_id) values (?, ?, ?, ?)";
    private static final String FIND_ALL_SQL =
        """
            select r.id, r.name,
                   rd.id as date_id, rd.play_day,
                   rt.id as time_id, rt.start_at,
                   th.id as theme_id, th.name as theme_name, th.content as theme_content, th.url as theme_url
            from reservation r
            join reservation_date rd on r.date_id = rd.id
            join reservation_time rt on r.time_id = rt.id
            join theme th on r.theme_id = th.id
            order by r.id
            """;
    private static final String COUNT_BY_TIME_ID_SQL =
        """
            select count(*)
            from reservation
            where time_id = ?
            """;
    private static final String COUNT_BY_RESERVATION_DATE_ID_SQL =
        """
            select count(*)
            from reservation
            where date_id = ?
            """;
    private static final String DELETE_BY_ID_SQL = "delete from reservation where id = ?";
    private static final String FIND_BY_THEME_AND_DATE_SQL =
        """
            select time_id
            from reservation
            where theme_id = ? and date_id = ?
            """;

    private static final String COUNT_BY_THEME_ID_SQL =
        """
            select count(*)
            from reservation
            where theme_id = ?
            """;
    ;

    private static final String FIND_BY_NAME_SQL =
        """
            select r.id, r.name,
                   rd.id as date_id, rd.play_day,
                   rt.id as time_id, rt.start_at,
                   th.id as theme_id, th.name as theme_name, th.content as theme_content, th.url as theme_url
            from reservation r
            join reservation_date rd on r.date_id = rd.id
            join reservation_time rt on r.time_id = rt.id
            join theme th on r.theme_id = th.id
            where r.name  = ?
            order by rd.play_day
            """;

    private static final String FIND_UPCOMING_BY_NAME_SQL =
        """
            select r.id, r.name,
                   rd.id as date_id, rd.play_day,
                   rt.id as time_id, rt.start_at,
                   th.id as theme_id, th.name as theme_name, th.content as theme_content, th.url as theme_url
            from reservation r
            join reservation_date rd on r.date_id = rd.id
            join reservation_time rt on r.time_id = rt.id
            join theme th on r.theme_id = th.id
            where r.name = ?
              and (rd.play_day > ? or (rd.play_day = ? and rt.start_at > ?))
            order by rd.play_day, rt.start_at
            """;

    private static final String FIND_BY_ID_SQL =
        """
            select r.id, r.name,
                   rd.id as date_id, rd.play_day,
                   rt.id as time_id, rt.start_at,
                   th.id as theme_id, th.name as theme_name, th.content as theme_content, th.url as theme_url
            from reservation r
            join reservation_date rd on r.date_id = rd.id
            join reservation_time rt on r.time_id = rt.id
            join theme th on r.theme_id = th.id
            where r.id  = ?
            """;

    private static final String UPDATE_DATE_TIME_SQL =
        """
            update reservation
            set date_id = ?, time_id = ?
            where id = ?
            """;

    private static final String EXIST_BY_DATE_TIME_THEME_SQL =
        """
            select exists(
            select 1
            from reservation
            where date_id = ? and time_id = ? and theme_id = ?
            );
            """;

    private final JdbcTemplate jdbcTemplate;

    @Override
    public Reservation save(Reservation reservation) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(INSERT_SQL, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, reservation.getName());
            ps.setLong(2, reservation.getDate().getId());
            ps.setLong(3, reservation.getTime().getId());
            ps.setLong(4, reservation.getTheme().getId());
            return ps;
        }, keyHolder);
        long id = extractId(keyHolder);
        return Reservation.of(
            id,
            reservation.getName(),
            reservation.getDate(),
            reservation.getTime(),
            reservation.getTheme()
        );
    }

    @Override
    public List<Reservation> findAll() {
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
    public int countByThemeId(Long themeId) {
        Integer count = jdbcTemplate.queryForObject(COUNT_BY_THEME_ID_SQL, Integer.class, themeId);
        if (count == null) {
            return 0;
        }
        return count;
    }

    @Override
    public List<Reservation> findByName(String name) {
        return jdbcTemplate.query(FIND_BY_NAME_SQL, reservationRowMapper(), name);
    }

    @Override
    public List<Reservation> findUpcomingByName(String name, LocalDate currentDate, LocalTime currentTime) {
        String currentDateValue = currentDate.toString();
        return jdbcTemplate.query(
            FIND_UPCOMING_BY_NAME_SQL,
            reservationRowMapper(),
            name,
            currentDateValue,
            currentDateValue,
            currentTime.toString()
        );
    }

    @Override
    public Optional<Reservation> findById(Long id) {
        return jdbcTemplate.query(FIND_BY_ID_SQL, reservationRowMapper(), id)
            .stream()
            .findFirst();
    }

    @Override
    public int updateReservation(Long id, Long dateId, Long timeId) {
        return jdbcTemplate.update(UPDATE_DATE_TIME_SQL, dateId, timeId, id);
    }

    @Override
    public boolean existsByDateIdAndTimeIdAndThemeId(Long dateId, Long timeId, Long themeId) {
        return jdbcTemplate.queryForObject(EXIST_BY_DATE_TIME_THEME_SQL, Boolean.class, dateId, timeId, themeId);
    }

    private RowMapper<Reservation> reservationRowMapper() {
        return (rs, rowNum) -> Reservation.of(
            rs.getLong("id"),
            rs.getString("name"),
            ReservationDate.of(
                rs.getLong("date_id"),
                LocalDate.parse(rs.getString("play_day"))),
            ReservationTime.of(
                rs.getLong("time_id"),
                LocalTime.parse(rs.getString("start_at"))
            ),
            Theme.of(
                rs.getLong("theme_id"),
                rs.getString("theme_name"),
                rs.getString("theme_content"),
                rs.getString("theme_url")
            )
        );
    }

    private RowMapper<Long> reservationTimeIdRowMapper() {
        return (rs, rowNum) -> rs.getLong("time_id");
    }

    private long extractId(KeyHolder keyHolder) {
        if (keyHolder.getKey() == null) {
            throw new IllegalStateException("생성 키를 조회할 수 없습니다.");
        }
        return keyHolder.getKey().longValue();
    }
}
