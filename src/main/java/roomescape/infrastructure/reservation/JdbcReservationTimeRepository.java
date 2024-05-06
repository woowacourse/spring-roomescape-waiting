package roomescape.infrastructure.reservation;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import roomescape.domain.reservation.ReservationTime;
import roomescape.domain.reservation.ReservationTimeRepository;
import roomescape.domain.reservation.TimeSlot;
import roomescape.infrastructure.reservation.rowmapper.ReservationTimeRowMapper;

@Repository
public class JdbcReservationTimeRepository implements ReservationTimeRepository {
    private final JdbcTemplate jdbcTemplate;
    private final SimpleJdbcInsert simpleJdbcInsert;

    public JdbcReservationTimeRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        this.simpleJdbcInsert = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("reservation_time")
                .usingGeneratedKeyColumns("id");
    }

    @Override
    public Optional<ReservationTime> findById(long id) {
        String sql = "select id, start_at from reservation_time where id = ?";
        try {
            ReservationTime findReservationTime = jdbcTemplate.queryForObject(
                    sql, (rs, rowNum) -> ReservationTimeRowMapper.mapRow(rs), id
            );
            return Optional.ofNullable(findReservationTime);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public ReservationTime create(ReservationTime reservationTime) {
        LocalTime startAt = reservationTime.getStartAt();
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("start_at", startAt);
        long id = simpleJdbcInsert.executeAndReturnKey(params).longValue();
        return new ReservationTime(id, startAt);
    }

    @Override
    public List<ReservationTime> findAll() {
        String sql = "select id, start_at from reservation_time";
        return jdbcTemplate.query(sql, (rs, rowNum) -> new ReservationTime(
                rs.getLong("id"),
                rs.getTime("start_at").toLocalTime()
        ));
    }

    @Override
    public void deleteById(long id) {
        String sql = "delete from reservation_time where id = ?";
        jdbcTemplate.update(sql, id);
    }

    @Override
    public boolean existsByStartAt(LocalTime startAt) {
        String sql = "select exists(select 1 from reservation_time where start_at = ?)";
        Boolean result = jdbcTemplate.queryForObject(sql, Boolean.class, startAt);
        return Boolean.TRUE.equals(result);
    }

    @Override
    public List<TimeSlot> getReservationTimeAvailabilities(LocalDate date, long themeId) {
        String sql = """
                select rt.id, rt.start_at, count(r.id) > 0 as is_booked
                from reservation_time as rt left join reservation as r
                on rt.id = r.time_id and r.date = ? and r.theme_id = ?
                group by rt.id, rt.start_at
                """;
        return jdbcTemplate.query(sql, (rs, rowNum) -> new TimeSlot(
                ReservationTimeRowMapper.mapRow(rs),
                rs.getBoolean("is_booked")
        ), date, themeId);
    }
}
