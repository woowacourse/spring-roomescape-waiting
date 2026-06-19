package roomescape.feature.reservation.repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import javax.sql.DataSource;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import roomescape.feature.reservation.domain.OrderStatus;
import roomescape.feature.reservation.domain.Reservation;
import roomescape.feature.reservation.domain.ReservationStatus;
import roomescape.feature.reservation.domain.ReserverName;
import roomescape.feature.reservation.domain.SlotKey;
import roomescape.feature.theme.domain.Theme;
import roomescape.feature.time.domain.Time;
import roomescape.global.domain.EntityStatus;

@Repository
public class JdbcReservationRepository implements ReservationRepository {

    private final NamedParameterJdbcTemplate jdbcTemplate;
    private final SimpleJdbcInsert simpleJdbcInsert;

    public JdbcReservationRepository(DataSource dataSource) {
        this.jdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
        this.simpleJdbcInsert = new SimpleJdbcInsert(dataSource)
                .withTableName("reservation")
                .usingColumns("name", "date", "time_id", "theme_id", "status")
                .usingGeneratedKeyColumns("id");
    }

    @Override
    public List<Reservation> findAllReservations() {
        return jdbcTemplate.query(
                """
                        SELECT r.id, r.name, r.date, r.status, r.order_status, r.version,
                               rt.id AS time_id, rt.start_at, rt.status AS time_status,
                               t.id AS theme_id, t.name AS theme_name, t.description, t.image_url,
                               t.status AS theme_status
                        FROM reservation r
                        JOIN reservation_time rt ON r.time_id = rt.id
                        JOIN theme t ON r.theme_id = t.id
                        ORDER BY r.id
                        """,
                (rs, rowNum) -> mapReservation(rs)
        );
    }

    @Override
    public Optional<Reservation> findLowestIdWaitingReservation(SlotKey slotKey) {
        String findSql = """
                SELECT r.id, r.name, r.date, r.status, r.order_status, r.version,
                       rt.id AS time_id, rt.start_at, rt.status AS time_status,
                       t.id AS theme_id, t.name AS theme_name, t.description, t.image_url,
                       t.status AS theme_status
                FROM reservation r
                JOIN reservation_time rt ON r.time_id = rt.id
                JOIN theme t ON r.theme_id = t.id
                WHERE r.status = 'WAITING' AND r.date = :date AND r.time_id = :timeId AND r.theme_id = :themeId
                ORDER BY id
                LIMIT 1
                """;
        SqlParameterSource parameters = new MapSqlParameterSource()
                .addValue("date", slotKey.date())
                .addValue("timeId", slotKey.timeId())
                .addValue("themeId", slotKey.themeId());

        try {
            Reservation reservation = jdbcTemplate.queryForObject(
                    findSql,
                    parameters,
                    (rs, rowNum) -> mapReservation(rs)
            );
            return Optional.ofNullable(reservation);
        } catch (EmptyResultDataAccessException exception) {
            return Optional.empty();
        }
    }

    @Override
    public boolean existsActiveReservation(SlotKey slotKey) {
        String sql = """
                SELECT EXISTS (
                    SELECT 1
                    FROM reservation
                    WHERE status = 'ACTIVE'
                      AND date = :date
                      AND time_id = :timeId
                      AND theme_id = :themeId
                )
                """;
        SqlParameterSource parameters = new MapSqlParameterSource()
                .addValue("date", slotKey.date())
                .addValue("timeId", slotKey.timeId())
                .addValue("themeId", slotKey.themeId());

        Boolean exists = jdbcTemplate.queryForObject(sql, parameters, Boolean.class);
        return Boolean.TRUE.equals(exists);
    }

    @Override
    public List<SlotKey> findDeadSlotKeys() {
        String sql = """
                SELECT DISTINCT w.date, w.time_id, w.theme_id
                FROM reservation w
                WHERE w.status = 'WAITING'
                  AND NOT EXISTS (
                      SELECT 1
                      FROM reservation a
                      WHERE a.status = 'ACTIVE'
                        AND a.date = w.date
                        AND a.time_id = w.time_id
                        AND a.theme_id = w.theme_id
                  )
                """;

        return jdbcTemplate.query(
                sql,
                (rs, rowNum) -> new SlotKey(
                        rs.getDate("date").toLocalDate(),
                        rs.getLong("time_id"),
                        rs.getLong("theme_id")
                )
        );
    }

    private Reservation mapReservation(ResultSet rs) throws SQLException {
        return Reservation.reconstruct(
                rs.getLong("id"),
                new ReserverName(rs.getString("name")),
                rs.getDate("date").toLocalDate(),
                mapTime(rs),
                mapTheme(rs),
                ReservationStatus.valueOf(rs.getString("status")),
                OrderStatus.valueOf(rs.getString("order_status")),
                rs.getLong("version")
        );
    }

    private Time mapTime(ResultSet rs) throws SQLException {
        return Time.reconstruct(
                rs.getLong("time_id"),
                rs.getTime("start_at").toLocalTime(),
                EntityStatus.valueOf(rs.getString("time_status"))
        );
    }

    private Theme mapTheme(ResultSet rs) throws SQLException {
        return Theme.reconstruct(
                rs.getLong("theme_id"),
                rs.getString("theme_name"),
                rs.getString("description"),
                rs.getString("image_url"),
                EntityStatus.valueOf(rs.getString("theme_status"))
        );
    }

    @Override
    public List<Reservation> findReservationsByNameAndNotDeleted(ReserverName name) {
        String sql = """
                SELECT r.id, r.name, r.date, r.status, r.order_status, r.version,
                       rt.id AS time_id, rt.start_at, rt.status AS time_status,
                       t.id AS theme_id, t.name AS theme_name, t.description, t.image_url,
                       t.status AS theme_status
                FROM reservation r
                JOIN reservation_time rt ON r.time_id = rt.id
                JOIN theme t ON r.theme_id = t.id
                WHERE r.name = :name
                  AND r.status <> 'DELETED'
                ORDER BY r.date, rt.start_at
                """;
        SqlParameterSource parameters = new MapSqlParameterSource("name", name.value());

        return jdbcTemplate.query(
                sql,
                parameters,
                (rs, rowNum) -> mapReservation(rs)
        );
    }

    @Override
    public Optional<Reservation> findReservationByIdAndNotDeleted(Long id) {
        String sql = """
                SELECT r.id, r.name, r.date, r.status, r.order_status, r.version,
                       rt.id AS time_id, rt.start_at, rt.status AS time_status,
                       t.id AS theme_id, t.name AS theme_name, t.description, t.image_url,
                       t.status AS theme_status
                FROM reservation r
                JOIN reservation_time rt ON r.time_id = rt.id
                JOIN theme t ON r.theme_id = t.id
                WHERE r.id = :id
                  AND r.status <> 'DELETED'
                """;
        SqlParameterSource parameters = new MapSqlParameterSource("id", id);
        List<Reservation> reservations = jdbcTemplate.query(
                sql,
                parameters,
                (rs, rowNum) -> mapReservation(rs)
        );

        return reservations.stream().findFirst();
    }

    @Override
    public List<Long> findTimeIdsByDateAndThemeIdAndNotDeleted(LocalDate date, Long themeId) {
        String sql = """
                SELECT r.time_id
                FROM reservation r
                JOIN reservation_time rt ON r.time_id = rt.id
                JOIN theme t ON r.theme_id = t.id
                WHERE r.date = :date
                  AND r.theme_id = :themeId
                  AND r.status = 'ACTIVE'
                  AND rt.status = 'ACTIVE'
                  AND t.status = 'ACTIVE'
                """;
        SqlParameterSource parameters = new MapSqlParameterSource(Map.of(
                "date", date,
                "themeId", themeId
        ));

        return jdbcTemplate.query(
                sql,
                parameters,
                (resultSet, rowNum) -> resultSet.getLong("time_id"));
    }

    @Override
    public Reservation save(Reservation reservation) {
        Map<String, Object> args = Map.of(
                "name", reservation.getName().value(),
                "date", reservation.getDate(),
                "time_id", reservation.getTime().getId(),
                "theme_id", reservation.getTheme().getId(),
                "status", reservation.getStatus().name()
        );
        Long generatedKey = simpleJdbcInsert.executeAndReturnKey(args).longValue();

        return Reservation.reconstruct(generatedKey, reservation.getName(), reservation.getDate(),
                reservation.getTime(), reservation.getTheme(), reservation.getStatus());
    }

    @Override
    public Reservation update(Reservation reservation) {
        String sql = """
                UPDATE reservation
                SET name = :name,
                    date = :date,
                    time_id = :timeId,
                    theme_id = :themeId,
                    status = :status,
                    version = version + 1
                WHERE id = :id
                  AND version = :version
                """;
        SqlParameterSource parameters = new MapSqlParameterSource()
                .addValue("id", reservation.getId())
                .addValue("name", reservation.getName().value())
                .addValue("date", reservation.getDate())
                .addValue("timeId", reservation.getTime().getId())
                .addValue("themeId", reservation.getTheme().getId())
                .addValue("status", reservation.getStatus().name())
                .addValue("version", reservation.getVersion());

        int updatedRowCount = jdbcTemplate.update(sql, parameters);
        if (updatedRowCount == 0) {
            throw new OptimisticLockingFailureException(
                    "예약이 다른 요청에 의해 먼저 변경되었습니다."
                            + " id: " + reservation.getId()
                            + " version: " + reservation.getVersion());
        }

        return Reservation.reconstruct(reservation.getId(), reservation.getName(), reservation.getDate(),
                reservation.getTime(), reservation.getTheme(), reservation.getStatus(),
                reservation.getOrderStatus(), reservation.getVersion() + 1);
    }

    @Override
    public void changeStatus(Long id, long version, ReservationStatus from, ReservationStatus to) {
        String sql = """
                UPDATE reservation
                SET status = :to,
                    version = version + 1
                WHERE id = :id
                  AND status = :from
                  AND version = :version
                """;
        SqlParameterSource parameters = new MapSqlParameterSource()
                .addValue("id", id)
                .addValue("version", version)
                .addValue("from", from.name())
                .addValue("to", to.name());

        int updatedRowCount = jdbcTemplate.update(sql, parameters);
        if (updatedRowCount == 0) {
            throw new OptimisticLockingFailureException(
                    "예약 상태가 다른 요청에 의해 먼저 변경되었습니다."
                            + " id: " + id
                            + " version: " + version);
        }
    }

    @Override
    public void changeOrderStatus(Long id, long version, OrderStatus from, OrderStatus to) {
        String sql = """
                UPDATE reservation
                SET order_status = :to,
                    version = version + 1
                WHERE id = :id
                  AND order_status = :from
                  AND version = :version
                """;
        SqlParameterSource parameters = new MapSqlParameterSource()
                .addValue("id", id)
                .addValue("version", version)
                .addValue("from", from.name())
                .addValue("to", to.name());

        int updatedRowCount = jdbcTemplate.update(sql, parameters);
        if (updatedRowCount == 0) {
            throw new OptimisticLockingFailureException(
                    "예약 주문 상태가 다른 요청에 의해 먼저 변경되었습니다."
                            + " id: " + id
                            + " version: " + version);
        }
    }

    @Override
    public int countByIdLessThanEqualAndSlot(Long reservationId, SlotKey slotKey) {
        String countSql = """
                SELECT COUNT(*)
                FROM reservation
                WHERE id <= :id
                  AND date = :date
                  AND time_id = :timeId
                  AND theme_id = :themeId
                  AND status = 'WAITING'
                """;
        SqlParameterSource parameters = new MapSqlParameterSource()
                .addValue("id", reservationId)
                .addValue("date", slotKey.date())
                .addValue("timeId", slotKey.timeId())
                .addValue("themeId", slotKey.themeId());

        return jdbcTemplate.queryForObject(countSql, parameters, Integer.class);
    }

    @Override
    public boolean existsReservationByIdAndNotDeleted(Long id) {
        String sql = """
                SELECT EXISTS (
                    SELECT 1
                    FROM reservation
                    WHERE id = :id
                      AND status <> 'DELETED'
                )
                """;

        SqlParameterSource parameters = new MapSqlParameterSource("id", id);
        Boolean exists = jdbcTemplate.queryForObject(sql, parameters, Boolean.class);
        return Boolean.TRUE.equals(exists);
    }

    @Override
    public boolean existsReservationAndStatus(Reservation reservation, ReservationStatus status) {
        String sql = """
                SELECT EXISTS (
                    SELECT 1
                    FROM reservation
                    WHERE date = :date
                      AND name = :name
                      AND time_id = :timeId
                      AND theme_id = :themeId
                      AND status = :status
                )
                """;

        SqlParameterSource parameters = new MapSqlParameterSource(Map.of(
                "date", reservation.getDate(),
                "name", reservation.getName().value(),
                "timeId", reservation.getTime().getId(),
                "themeId", reservation.getTheme().getId(),
                "status", status.name()
        ));

        Boolean exists = jdbcTemplate.queryForObject(sql, parameters, Boolean.class);
        return Boolean.TRUE.equals(exists);
    }

    @Override
    public boolean existsActiveOrWaitingReservation(SlotKey slotKey) {
        String existsSql = """
                SELECT EXISTS (
                    SELECT 1
                    FROM reservation
                    WHERE date = :date
                      AND time_id = :timeId
                      AND theme_id = :themeId
                      AND status IN ('ACTIVE', 'WAITING')
                )
                """;
        SqlParameterSource parameters = new MapSqlParameterSource(Map.of(
                "date", slotKey.date(),
                "timeId", slotKey.timeId(),
                "themeId", slotKey.themeId()
        ));

        Boolean exists = jdbcTemplate.queryForObject(existsSql, parameters, Boolean.class);
        return Boolean.TRUE.equals(exists);
    }
}
