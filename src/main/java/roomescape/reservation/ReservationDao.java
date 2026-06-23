package roomescape.reservation;

import java.sql.PreparedStatement;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import roomescape.reservationtime.ReservationTime;

@Repository
public class ReservationDao {

    private final JdbcTemplate jdbcTemplate;

    public ReservationDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<Reservation> findAllReservations() {
        String sql = """
                SELECT
                    r.id as reservation_id,
                    r.member_id,
                    r.date,
                    t.id as time_id,
                    t.start_at,
                    r.theme_id,
                    r.store_id,
                    r.status
                FROM reservation as r
                INNER JOIN reservation_time as t
                  ON r.time_id = t.id
                """;
        List<Reservation> reservations = jdbcTemplate.query(
                sql,
                reservationRowMapper());
        return reservations;
    }

    public List<Reservation> findAllReservationsByMemberId(Long memberId) {
        String sql = """
                SELECT
                    r.id as reservation_id,
                    r.member_id,
                    r.date,
                    t.id as time_id,
                    t.start_at,
                    r.theme_id,
                    r.store_id,
                    r.status
                FROM reservation as r
                INNER JOIN reservation_time as t
                  ON r.time_id = t.id
                WHERE r.member_id = ?
                  AND r.status = 'CONFIRMED'
                """;
        List<Reservation> reservations = jdbcTemplate.query(
                sql,
                reservationRowMapper(),
                memberId
        );
        return reservations;
    }

    public List<Reservation> findReservationsByStoreId(Long storeId) {
        String sql = """
                SELECT
                    r.id as reservation_id,
                    r.member_id,
                    r.date,
                    t.id as time_id,
                    t.start_at,
                    r.theme_id,
                    r.store_id,
                    r.status
                FROM reservation as r
                INNER JOIN reservation_time as t
                  ON r.time_id = t.id
                WHERE r.store_id = ?
                  AND r.status = 'CONFIRMED'
                """;
        List<Reservation> reservations = jdbcTemplate.query(
                sql,
                reservationRowMapper(),
                storeId
        );
        return reservations;
    }

    public Reservation findReservationByIdForUpdate(Long id) {
        String sql = """
                SELECT
                    r.id as reservation_id,
                    r.member_id,
                    r.date,
                    t.id as time_id,
                    t.start_at,
                    r.theme_id,
                    r.store_id,
                    r.status
                FROM reservation as r
                INNER JOIN reservation_time as t
                  ON r.time_id = t.id
                WHERE r.id = ?
                FOR UPDATE
                """;
        Reservation reservation = jdbcTemplate.queryForObject(
                sql,
                reservationRowMapper(), id);
        return reservation;
    }

    public Reservation insert(Reservation reservation) {
        String sql = "insert into reservation (member_id, date, time_id, theme_id, store_id, status) values (?, ?, ?, ?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(
                    sql,
                    new String[]{"id"}
            );
            ps.setLong(1, reservation.getMemberId());
            ps.setString(2, reservation.getDate().toString());
            ps.setLong(3, reservation.getTime().getId());
            ps.setLong(4, reservation.getThemeId());
            ps.setLong(5, reservation.getStoreId());
            ps.setString(6, reservation.getStatus().name());
            return ps;
        }, keyHolder);

        Long id = keyHolder.getKey().longValue();
        return new Reservation(
                id,
                reservation.getMemberId(),
                reservation.getDate(),
                reservation.getTime(),
                reservation.getThemeId(),
                reservation.getStoreId(),
                reservation.getStatus()
        );
    }

    public Reservation update(Reservation reservation) {
        String sql = "update reservation set member_id = ?, date = ?, time_id = ?, theme_id = ?, store_id = ?, status = ? where id = ?";
        jdbcTemplate.update(
                sql,
                reservation.getMemberId(),
                reservation.getDate().toString(),
                reservation.getTime().getId(),
                reservation.getThemeId(),
                reservation.getStoreId(),
                reservation.getStatus().name(),
                reservation.getId()
        );
        return reservation;
    }

    public int delete(Long id) {
        return jdbcTemplate.update("delete from reservation where id = ?", id);
    }

    private RowMapper<Reservation> reservationRowMapper() {
        return (resultSet, rowNum) -> {
            Reservation newReservation = new Reservation(
                    resultSet.getLong("reservation_id"),
                    resultSet.getLong("member_id"),
                    LocalDate.parse(resultSet.getString("date")),
                    new ReservationTime(
                            resultSet.getLong("time_id"),
                            LocalTime.parse(resultSet.getString("start_at"))
                    ),
                    resultSet.getLong("theme_id"),
                    resultSet.getLong("store_id"),
                    ReservationStatus.valueOf(resultSet.getString("status"))
            );
            return newReservation;
        };
    }

}
