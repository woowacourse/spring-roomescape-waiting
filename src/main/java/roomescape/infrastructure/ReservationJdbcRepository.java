package roomescape.infrastructure;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import roomescape.domain.member.Member;
import roomescape.domain.reservation.Reservation;
import roomescape.domain.reservation.ReservationRepository;
import roomescape.domain.reservation.ReservationTime;
import roomescape.domain.reservation.Theme;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
public class ReservationJdbcRepository implements ReservationRepository {
    private static final String TABLE_NAME = "reservation";

    private final JdbcTemplate jdbcTemplate;
    private final SimpleJdbcInsert simpleJdbcInsert;
    private final RowMapper<Reservation> rowMapper = (resultSet, rowNum) -> {
        ReservationTime reservationTime = new ReservationTime(resultSet.getLong("time_id"), resultSet.getString("start_at"));
        Theme theme = new Theme(resultSet.getLong("theme_id"), resultSet.getString("theme_name"), resultSet.getString("description"), resultSet.getString("thumbnail"));
        Member member = new Member(resultSet.getLong("member_id"), resultSet.getString("member_name"), resultSet.getString("email"), resultSet.getString("password"), resultSet.getString("role"));
        Reservation reservation = new Reservation(
                resultSet.getLong("reservation_id"),
                resultSet.getString("date"),
                member,
                reservationTime,
                theme);
        return reservation;
    };

    public ReservationJdbcRepository(final JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        this.simpleJdbcInsert = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName(TABLE_NAME)
                .usingGeneratedKeyColumns("id");
    }

    @Override
    public List<Reservation> findAll() {
        String sql = "SELECT r.id as reservation_id, r.date, "
                + "rt.id as time_id, rt.start_at, "
                + "t.id as theme_id, t.name as theme_name, t.description, t.thumbnail, "
                + "m.id as member_id, m.name as member_name, m.email, m.password, m.role "
                + "FROM reservation as r "
                + "inner join reservation_time as rt on r.time_id = rt.id "
                + "inner join theme as t on r.theme_id = t.id "
                + "inner join member as m on r.member_id = m.id";
        List<Reservation> reservations = jdbcTemplate.query(sql, rowMapper);
        return reservations;
    }

    @Override
    public Reservation save(final Reservation reservation) {
        Map<String, ?> params = Map.of(
                "date", reservation.getDate(),
                "member_id", reservation.getMember().getId(),
                "time_id", reservation.getReservationTime().getId(),
                "theme_id", reservation.getTheme().getId());
        long id = simpleJdbcInsert.executeAndReturnKey(params).longValue();
        return new Reservation(id, reservation);
    }

    @Override
    public void deleteById(final long id) {
        String sql = "DELETE FROM Reservation WHERE id = ?";
        jdbcTemplate.update(sql, id);
    }

    @Override
    public boolean existsByDateAndTimeAndTheme(String date, long timeId, long themeId) {
        String sql = "SELECT COUNT(*) FROM Reservation WHERE date = ? AND time_id = ? AND theme_id = ?";
        return jdbcTemplate.queryForObject(sql, Integer.class, date, timeId, themeId) > 0;
    }

    @Override
    public boolean existsByTimeId(long timeId) {
        String sql = "SELECT COUNT(*) FROM Reservation WHERE time_id = ?";
        return jdbcTemplate.queryForObject(sql, Integer.class, timeId) > 0;
    }

    @Override
    public boolean existsByThemeId(long themeId) {
        String sql = "SELECT COUNT(*) FROM Reservation WHERE theme_id = ?";
        return jdbcTemplate.queryForObject(sql, Integer.class, themeId) > 0;
    }

    @Override
    public boolean existsById(long id) {
        String sql = "SELECT COUNT(*) FROM Reservation WHERE id = ?";
        return jdbcTemplate.queryForObject(sql, Integer.class, id) > 0;
    }

    @Override
    public Optional<Reservation> findById(long id) {
        String sql = "SELECT r.id as reservation_id, r.date, "
                + "rt.id as time_id, rt.start_at, "
                + "t.id as theme_id, t.name as theme_name, t.description, t.thumbnail, "
                + "m.id as member_id, m.name as member_name, m.email, m.password, m.role "
                + "FROM reservation as r "
                + "inner join reservation_time as rt on r.time_id = rt.id "
                + "inner join theme as t on r.theme_id = t.id "
                + "inner join member as m on r.member_id = m.id "
                + "WHERE r.id = ?";
        try {
            return Optional.ofNullable(jdbcTemplate.queryForObject(sql, rowMapper, id));
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public List<Reservation> findBy(Long memberId, Long themeId, LocalDate dateFrom, LocalDate dateTo) {
        String sql = "SELECT r.id as reservation_id, r.date, "
                + "rt.id as time_id, rt.start_at, "
                + "t.id as theme_id, t.name as theme_name, t.description, t.thumbnail, "
                + "m.id as member_id, m.name as member_name, m.email, m.password, m.role "
                + "FROM reservation as r "
                + "inner join reservation_time as rt on r.time_id = rt.id "
                + "inner join theme as t on r.theme_id = t.id "
                + "inner join member as m on r.member_id = m.id "
                + "where 1 = 1";
        List<Object> params = new ArrayList<>();

        if (memberId != null) {
            sql += " and r.member_id = ?";
            params.add(memberId);
        }
        if (themeId != null) {
            sql += " and r.theme_id = ?";
            params.add(themeId);
        }
        if (dateFrom != null) {
            sql += " and r.date >= ?";
            params.add(dateFrom);
        }
        if (dateTo != null) {
            sql += " and r.date <= ?";
            params.add(dateTo);
        }

        List<Reservation> reservations = jdbcTemplate.query(sql, params.toArray(), rowMapper);
        return reservations;
    }
}
