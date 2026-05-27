package roomescape.dao;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationTime;

import java.sql.PreparedStatement;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

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
                    r.store_id
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
                    r.store_id
                FROM reservation as r
                INNER JOIN reservation_time as t
                  ON r.time_id = t.id
                WHERE r.member_id = ?
                """;
        List<Reservation> reservations = jdbcTemplate.query(
                sql,
                reservationRowMapper(),
                memberId
        );
        return reservations;
    }

    public List<Reservation> findByStoreId(Long storeId) {
        String sql = """
                SELECT
                    r.id as reservation_id,
                    r.member_id,
                    r.date,
                    t.id as time_id,
                    t.start_at,
                    r.theme_id,
                    r.store_id
                FROM reservation as r
                INNER JOIN reservation_time as t
                  ON r.time_id = t.id
                WHERE r.store_id = ?
                """;
        List<Reservation> reservations = jdbcTemplate.query(
                sql,
                reservationRowMapper(),
                storeId
        );
        return reservations;
    }

    public Reservation findReservationById(Long id) {
        String sql = """
                SELECT
                    r.id as reservation_id,
                    r.member_id,
                    r.date,
                    t.id as time_id,
                    t.start_at,
                    r.theme_id,
                    r.store_id
                FROM reservation as r
                INNER JOIN reservation_time as t
                  ON r.time_id = t.id
                WHERE r.id = ?
                """;
        Reservation reservation = jdbcTemplate.queryForObject(
                sql,
                reservationRowMapper(), id);
        return reservation;
    }

    public int updateById(Long id, LocalDate date, Long timeId) {
        String sql = "update reservation set date = ?, time_id = ? where id = ?";
        return jdbcTemplate.update(sql, date.toString(), timeId, id);
    }

    public Long insertWithKeyHolder(Long memberId, LocalDate date, Long timeId, Long themeId, Long storeId) {
        String sql = "insert into reservation (member_id, date, time_id, theme_id, store_id) values (?, ?, ?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(
                    sql,
                    new String[]{"id"}
            );
            ps.setLong(1, memberId);
            ps.setString(2, date.toString());
            ps.setLong(3, timeId);
            ps.setLong(4, themeId);
            ps.setLong(5, storeId);
            return ps;
        }, keyHolder);

        return keyHolder.getKey().longValue();
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
                    resultSet.getLong("store_id")
            );
            return newReservation;
        };
    }
}
