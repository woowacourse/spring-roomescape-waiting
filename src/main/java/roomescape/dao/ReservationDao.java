package roomescape.dao;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationTime;
import roomescape.domain.Store;
import roomescape.domain.Theme;
import roomescape.dto.projection.MemberSummaryProjection;
import roomescape.dto.result.ReservationResult;
import roomescape.dto.result.StoreReservationResult;

import java.sql.PreparedStatement;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Repository
public class ReservationDao {

    private static final String RESERVATION_DETAIL_SELECT = """
            SELECT
                r.id as reservation_id,
                r.member_id,
                r.date,
                t.id as time_id,
                t.start_at,
                th.id as theme_id,
                th.name as theme_name,
                th.description as theme_description,
                th.img_url as theme_img_url,
                s.id as store_id,
                s.name as store_name
            FROM reservation as r
            INNER JOIN reservation_time as t ON r.time_id = t.id
            INNER JOIN theme as th ON r.theme_id = th.id
            INNER JOIN store as s ON r.store_id = s.id
            """;

    private static final String STORE_RESERVATION_DETAIL_SELECT = """
            SELECT
                r.id as reservation_id,
                r.member_id,
                r.date,
                t.id as time_id,
                t.start_at,
                th.id as theme_id,
                th.name as theme_name,
                th.description as theme_description,
                th.img_url as theme_img_url,
                s.id as store_id,
                s.name as store_name,
                m.email as member_email,
                m.name as member_name
            FROM reservation as r
            INNER JOIN reservation_time as t ON r.time_id = t.id
            INNER JOIN theme as th ON r.theme_id = th.id
            INNER JOIN store as s ON r.store_id = s.id
            INNER JOIN member as m ON r.member_id = m.id
            """;

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

    public List<ReservationResult> findAllReservationsByMemberId(Long memberId) {
        String sql = RESERVATION_DETAIL_SELECT + "WHERE r.member_id = ?";
        return jdbcTemplate.query(
                sql,
                reservationResultRowMapper(),
                memberId
        );
    }

    public List<StoreReservationResult> findByStoreId(Long storeId) {
        String sql = STORE_RESERVATION_DETAIL_SELECT + "WHERE r.store_id = ?";
        return jdbcTemplate.query(
                sql,
                storeReservationResultRowMapper(),
                storeId
        );
    }

    public ReservationResult findReservationResultById(Long id) {
        String sql = RESERVATION_DETAIL_SELECT + "WHERE r.id = ?";
        return jdbcTemplate.queryForObject(
                sql,
                reservationResultRowMapper(),
                id
        );
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

    public void lockById(Long id) {
        jdbcTemplate.query(
                "SELECT id FROM reservation WHERE id = ? FOR UPDATE",
                (rs, rowNum) -> rs.getLong("id"),
                id);
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

    private RowMapper<ReservationResult> reservationResultRowMapper() {
        return (resultSet, rowNum) -> {
            Reservation reservation = new Reservation(
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
            Theme theme = new Theme(
                    resultSet.getLong("theme_id"),
                    resultSet.getString("theme_name"),
                    resultSet.getString("theme_description"),
                    resultSet.getString("theme_img_url")
            );
            Store store = new Store(
                    resultSet.getLong("store_id"),
                    resultSet.getString("store_name")
            );
            return new ReservationResult(reservation, theme, store);
        };
    }

    private RowMapper<StoreReservationResult> storeReservationResultRowMapper() {
        return (resultSet, rowNum) -> {
            Reservation reservation = new Reservation(
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
            Theme theme = new Theme(
                    resultSet.getLong("theme_id"),
                    resultSet.getString("theme_name"),
                    resultSet.getString("theme_description"),
                    resultSet.getString("theme_img_url")
            );
            Store store = new Store(
                    resultSet.getLong("store_id"),
                    resultSet.getString("store_name")
            );
            MemberSummaryProjection member = new MemberSummaryProjection(
                    resultSet.getLong("member_id"),
                    resultSet.getString("member_email"),
                    resultSet.getString("member_name")
            );
            return new StoreReservationResult(reservation, theme, store, member);
        };
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

    public void updateMemberId(Long reservationId, Long memberId) {
        String sql = "update reservation set member_id = ? where id = ?";
        jdbcTemplate.update(sql, memberId, reservationId);
    }
}
