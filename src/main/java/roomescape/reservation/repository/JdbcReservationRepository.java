package roomescape.reservation.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import roomescape.common.exception.RoomEscapeException;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationStatus;
import roomescape.reservation.domain.Slot;
import roomescape.reservation.dto.WaitingRank;
import roomescape.reservation.exception.ReservationErrorCode;
import roomescape.reservationtime.domain.ReservationTime;
import roomescape.theme.domain.Theme;

@Repository
public class JdbcReservationRepository implements ReservationRepository {

    private static final RowMapper<Reservation> RESERVATION_ROW_MAPPER = reservationRowMapper();
    private static final RowMapper<WaitingRank> WAITING_RANK_ROW_MAPPER = waitingRankRowMapper();

    private final NamedParameterJdbcTemplate jdbcTemplate;
    private final SimpleJdbcInsert reservationInsert;

    public JdbcReservationRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = new NamedParameterJdbcTemplate(jdbcTemplate);
        this.reservationInsert = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("reservation")
                .usingGeneratedKeyColumns("id")
                .usingColumns("slot_id", "name", "status", "created_at");
    }

    private static RowMapper<Reservation> reservationRowMapper() {
        return (rs, rowNum) -> Reservation.of(
                rs.getLong("reservation_id"),
                mapSlot(rs),
                rs.getString("reservation_name"),
                ReservationStatus.valueOf(rs.getString("reservation_status")),
                rs.getTimestamp("reservation_created_at").toLocalDateTime()
        );
    }

    private static RowMapper<WaitingRank> waitingRankRowMapper() {
        return (rs, rowNum) -> {
            Reservation reservation = Reservation.of(
                    rs.getLong("reservation_id"),
                    mapSlot(rs),
                    rs.getString("reservation_name"),
                    ReservationStatus.WAITING,
                    rs.getTimestamp("reservation_created_at").toLocalDateTime()
            );

            return new WaitingRank(
                    reservation,
                    rs.getLong("waiting_number")
            );
        };
    }

    private static Slot mapSlot(java.sql.ResultSet rs) throws java.sql.SQLException {
        ReservationTime time = ReservationTime.of(
                rs.getLong("time_id"),
                rs.getTime("start_at").toLocalTime()
        );
        Theme theme = Theme.of(
                rs.getLong("theme_id"),
                rs.getString("theme_name"),
                rs.getString("theme_description"),
                rs.getString("theme_image_url"),
                rs.getLong("theme_running_time")
        );

        return Slot.of(
                rs.getLong("slot_id"),
                rs.getDate("slot_date").toLocalDate(),
                time,
                theme
        );
    }

    @Override
    public Reservation save(Reservation reservation) {
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("slot_id", reservation.getSlotId())
                .addValue("name", reservation.getName())
                .addValue("status", reservation.getStatus().name())
                .addValue("created_at", reservation.getCreatedAt());
        long id = reservationInsert.executeAndReturnKey(params).longValue();

        return Reservation.of(
                id,
                reservation.getSlot(),
                reservation.getName(),
                reservation.getStatus(),
                reservation.getCreatedAt()
        );
    }

    @Override
    public Optional<Reservation> findById(Long id) {
        String sql = """
                SELECT r.id           AS reservation_id,
                       r.name         AS reservation_name,
                       r.status       AS reservation_status,
                       r.created_at   AS reservation_created_at,
                       s.id           AS slot_id,
                       s.date         AS slot_date,
                       t.id           AS time_id,
                       t.start_at     AS start_at,
                       th.id          AS theme_id,
                       th.name        AS theme_name,
                       th.description AS theme_description,
                       th.image_url   AS theme_image_url,
                       th.running_time AS theme_running_time
                FROM reservation AS r
                INNER JOIN slot AS s ON r.slot_id = s.id
                INNER JOIN reservation_time AS t ON s.time_id = t.id
                INNER JOIN theme AS th ON s.theme_id = th.id
                WHERE r.id = :id
                """;

        List<Reservation> results = jdbcTemplate.query(
                sql,
                new MapSqlParameterSource("id", id),
                RESERVATION_ROW_MAPPER
        );

        return results.stream()
                .findFirst();
    }

    @Override
    public List<Reservation> findAll() {
        String sql = """
                SELECT r.id           AS reservation_id,
                       r.name         AS reservation_name,
                       r.status       AS reservation_status,
                       r.created_at   AS reservation_created_at,
                       s.id           AS slot_id,
                       s.date         AS slot_date,
                       t.id           AS time_id,
                       t.start_at     AS start_at,
                       th.id          AS theme_id,
                       th.name        AS theme_name,
                       th.description AS theme_description,
                       th.image_url   AS theme_image_url,
                       th.running_time AS theme_running_time
                FROM reservation AS r
                INNER JOIN slot AS s ON r.slot_id = s.id
                INNER JOIN reservation_time AS t ON s.time_id = t.id
                INNER JOIN theme AS th ON s.theme_id = th.id
                """;

        return jdbcTemplate.query(
                sql,
                RESERVATION_ROW_MAPPER
        );
    }

    @Override
    public int delete(Long id) {
        String sql = """
                DELETE FROM reservation
                WHERE id = :id
                """;

        return jdbcTemplate.update(
                sql,
                new MapSqlParameterSource("id", id)
        );
    }

    @Override
    public boolean existsOccupiedBySlotId(Long slotId) {
        String sql = """
                SELECT EXISTS (
                  SELECT 1 FROM reservation
                  WHERE slot_id = :slot_id AND status IN ('CONFIRMED', 'PENDING')
                )
                """;
        return Boolean.TRUE.equals(
                jdbcTemplate.queryForObject(
                        sql,
                        new MapSqlParameterSource("slot_id", slotId),
                        Boolean.class
                )
        );
    }

    @Override
    public Optional<Reservation> findFirstWaitingBySlotId(Long slotId) {
        String sql = """
                SELECT r.id           AS reservation_id,
                       r.name         AS reservation_name,
                       r.status       AS reservation_status,
                       r.created_at   AS reservation_created_at,
                       s.id           AS slot_id,
                       s.date         AS slot_date,
                       t.id           AS time_id,
                       t.start_at     AS start_at,
                       th.id          AS theme_id,
                       th.name        AS theme_name,
                       th.description AS theme_description,
                       th.image_url   AS theme_image_url,
                       th.running_time AS theme_running_time
                FROM reservation AS r
                INNER JOIN slot AS s ON r.slot_id = s.id
                INNER JOIN reservation_time AS t ON s.time_id = t.id
                INNER JOIN theme AS th ON s.theme_id = th.id
                WHERE s.id = :slot_id AND r.status = 'WAITING'
                ORDER BY r.created_at, r.id
                LIMIT 1
                """;
        List<Reservation> results = jdbcTemplate.query(
                sql,
                new MapSqlParameterSource("slot_id", slotId),
                RESERVATION_ROW_MAPPER
        );

        return results.stream()
                .findFirst();
    }

    @Override
    public Reservation updateStatus(Reservation reservation) {
        String sql = """
                UPDATE reservation
                SET status = :status
                WHERE id = :id
                """;
        jdbcTemplate.update(
                sql,
                new MapSqlParameterSource()
                        .addValue("status", reservation.getStatus().name())
                        .addValue("id", reservation.getId())
        );

        return findById(reservation.getId())
                .orElseThrow(() -> new RoomEscapeException(ReservationErrorCode.RESERVATION_NOT_FOUND));
    }

    @Override
    public List<Reservation> findReservedByName(String name) {
        String sql = """
                SELECT r.id           AS reservation_id,
                       r.name         AS reservation_name,
                       r.status       AS reservation_status,
                       r.created_at   AS reservation_created_at,
                       s.id           AS slot_id,
                       s.date         AS slot_date,
                       t.id           AS time_id,
                       t.start_at     AS start_at,
                       th.id          AS theme_id,
                       th.name        AS theme_name,
                       th.description AS theme_description,
                       th.image_url   AS theme_image_url,
                       th.running_time AS theme_running_time
                FROM reservation AS r
                INNER JOIN slot AS s ON r.slot_id = s.id
                INNER JOIN reservation_time AS t ON s.time_id = t.id
                INNER JOIN theme AS th ON s.theme_id = th.id
                WHERE r.name = :name AND r.status IN ('CONFIRMED', 'PENDING')
                """;

        return jdbcTemplate.query(
                sql,
                new MapSqlParameterSource("name", name),
                RESERVATION_ROW_MAPPER
        );
    }

    @Override
    public List<WaitingRank> findWaitingRanksByName(String name) {
        String sql = """
                SELECT ranked.reservation_id,
                       ranked.reservation_name,
                       ranked.reservation_created_at,
                       ranked.waiting_number,
                       s.id           AS slot_id,
                       s.date         AS slot_date,
                       t.id           AS time_id,
                       t.start_at     AS start_at,
                       th.id          AS theme_id,
                       th.name        AS theme_name,
                       th.description AS theme_description,
                       th.image_url   AS theme_image_url,
                       th.running_time AS theme_running_time
                FROM (
                    SELECT r.id         AS reservation_id,
                           r.name       AS reservation_name,
                           r.created_at AS reservation_created_at,
                           r.slot_id    AS slot_id,
                           ROW_NUMBER() OVER (PARTITION BY r.slot_id ORDER BY r.created_at, r.id) AS waiting_number
                    FROM reservation AS r
                    WHERE r.status = 'WAITING'
                ) AS ranked
                INNER JOIN slot AS s ON ranked.slot_id = s.id
                INNER JOIN reservation_time AS t ON s.time_id = t.id
                INNER JOIN theme AS th ON s.theme_id = th.id
                WHERE ranked.reservation_name = :name
                """;

        return jdbcTemplate.query(
                sql,
                new MapSqlParameterSource("name", name),
                WAITING_RANK_ROW_MAPPER
        );
    }

    @Override
    public boolean existsByTimeId(Long timeId) {
        String sql = """
                SELECT EXISTS (
                  SELECT 1 FROM reservation AS r
                  INNER JOIN slot AS s ON r.slot_id = s.id
                  WHERE s.time_id = :time_id
                )
                """;

        return Boolean.TRUE.equals(
                jdbcTemplate.queryForObject(
                        sql,
                        new MapSqlParameterSource("time_id", timeId),
                        Boolean.class
                )
        );
    }

    @Override
    public boolean existsByThemeId(Long themeId) {
        String sql = """
                SELECT EXISTS (
                  SELECT 1 FROM reservation AS r
                  INNER JOIN slot AS s ON r.slot_id = s.id
                  WHERE s.theme_id = :theme_id
                )
                """;

        return Boolean.TRUE.equals(
                jdbcTemplate.queryForObject(
                        sql,
                        new MapSqlParameterSource("theme_id", themeId),
                        Boolean.class
                )
        );
    }
}
