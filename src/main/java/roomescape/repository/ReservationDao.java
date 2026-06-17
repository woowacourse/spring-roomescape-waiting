package roomescape.repository;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import roomescape.domain.Member;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationStatus;
import roomescape.domain.ReservationTime;
import roomescape.domain.Slot;
import roomescape.domain.Theme;
import roomescape.exception.ResourceNotFoundException;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
public class ReservationDao {

    private static final String SELECT_BASE = """
            SELECT
                reservation.id as reservation_id,
                reservation.name,
                reservation.date,
                reservation.status,
                time.id as time_id,
                time.start_at as time_value,
                theme.id as theme_id,
                theme.name as theme_name,
                theme.thumbnail_url as thumbnail_url,
                theme.description as theme_description
            FROM reservation as reservation
            INNER JOIN reservation_time as time ON reservation.time_id = time.id
            INNER JOIN theme as theme ON reservation.theme_id = theme.id
            """;

    private final NamedParameterJdbcTemplate jdbcTemplate;
    private final SimpleJdbcInsert insertExecutor;
    private final RowMapper<Reservation> rowMapper = (rs, rowNum) -> {
        Theme theme = Theme.create(
                rs.getLong("theme_id"),
                rs.getString("theme_name"),
                rs.getString("thumbnail_url"),
                rs.getString("theme_description")
        );

        ReservationTime reservationTime = ReservationTime.create(
                rs.getLong("time_id"),
                rs.getObject("time_value", LocalTime.class)
        );

        Slot slot = new Slot(rs.getObject("date", LocalDate.class), reservationTime, theme);

        return Reservation.create(
                rs.getLong("reservation_id"),
                new Member(rs.getString("name")),
                slot,
                ReservationStatus.valueOf(rs.getString("status"))
        );
    };

    public ReservationDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = new NamedParameterJdbcTemplate(jdbcTemplate);
        this.insertExecutor = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("reservation")
                .usingGeneratedKeyColumns("id");
    }

    public Reservation save(Reservation reservation) {
        Slot slot = reservation.slot();
        SqlParameterSource params = new MapSqlParameterSource()
                .addValue("name", reservation.owner().name())
                .addValue("date", slot.date())
                .addValue("time_id", slot.time().id())
                .addValue("theme_id", slot.theme().id())
                .addValue("status", reservation.status().name());

        long id = insertExecutor.executeAndReturnKey(params).longValue();

        return Reservation.create(id, reservation.owner(), slot, reservation.status());
    }

    public Reservation update(Reservation reservation) {
        Slot slot = reservation.slot();
        String sql = """
                UPDATE reservation
                SET name = :name, date = :date, time_id = :timeId, theme_id = :themeId, status = :status
                WHERE id = :id
                """;
        SqlParameterSource params = new MapSqlParameterSource()
                .addValue("name", reservation.owner().name())
                .addValue("date", slot.date())
                .addValue("timeId", slot.time().id())
                .addValue("themeId", slot.theme().id())
                .addValue("status", reservation.status().name())
                .addValue("id", reservation.id());
        int affected = jdbcTemplate.update(sql, params);

        if (affected == 0) {
            throw new ResourceNotFoundException("요청한 예약을 찾을 수 없습니다.");
        }
        return reservation;
    }

    public void deleteById(long id) {
        String sql = "DELETE FROM reservation WHERE id = :id";
        int affected = jdbcTemplate.update(sql, Map.of("id", id));

        if (affected == 0) {
            throw new ResourceNotFoundException("요청한 예약을 찾을 수 없습니다.");
        }
    }

    public void deletePendingPaymentById(long id) {
        String sql = "DELETE FROM reservation WHERE id = :id AND status = :status";
        jdbcTemplate.update(sql, Map.of(
                "id", id,
                "status", ReservationStatus.PENDING_PAYMENT.name()
        ));
    }

    public List<Reservation> findAll() {
        return jdbcTemplate.query(SELECT_BASE, Map.of(), rowMapper);
    }

    public Optional<Reservation> findById(long id) {
        String sql = SELECT_BASE + " WHERE reservation.id = :id";
        return jdbcTemplate.query(sql, Map.of("id", id), rowMapper)
                .stream()
                .findFirst();
    }

    public List<Reservation> findAllByName(Member member) {
        String sql = SELECT_BASE + " WHERE reservation.name = :name";
        return jdbcTemplate.query(sql, Map.of("name", member.name()), rowMapper);
    }

    public Optional<Reservation> findBySlot(Slot slot) {
        String sql = SELECT_BASE + " WHERE reservation.date = :date AND reservation.time_id = :timeId AND reservation.theme_id = :themeId";
        SqlParameterSource params = slotParams(slot);
        return jdbcTemplate.query(sql, params, rowMapper)
                .stream()
                .findFirst();
    }

    public Optional<Reservation> findBySlotForUpdate(Slot slot) {
        String sql = SELECT_BASE + " WHERE reservation.date = :date AND reservation.time_id = :timeId AND reservation.theme_id = :themeId FOR UPDATE";
        SqlParameterSource params = slotParams(slot);
        return jdbcTemplate.query(sql, params, rowMapper)
                .stream()
                .findFirst();
    }

    public Optional<Reservation> findConfirmedBySlotForUpdate(Slot slot) {
        String sql = SELECT_BASE + """
                 WHERE reservation.date = :date
                   AND reservation.time_id = :timeId
                   AND reservation.theme_id = :themeId
                   AND reservation.status = :status
                 FOR UPDATE
                """;
        SqlParameterSource params = new MapSqlParameterSource()
                .addValue("date", slot.date())
                .addValue("timeId", slot.time().id())
                .addValue("themeId", slot.theme().id())
                .addValue("status", ReservationStatus.CONFIRMED.name());
        return jdbcTemplate.query(sql, params, rowMapper)
                .stream()
                .findFirst();
    }

    public Optional<Reservation> findByIdForUpdate(long id) {
        String sql = SELECT_BASE + " WHERE reservation.id = :id FOR UPDATE";
        return jdbcTemplate.query(sql, Map.of("id", id), rowMapper)
                .stream()
                .findFirst();
    }

    public boolean existsBySlot(Slot slot) {
        String sql = """
        SELECT EXISTS (
            SELECT 1 FROM reservation
            WHERE date = :date AND time_id = :timeId AND theme_id = :themeId
        )
        """;
        return Boolean.TRUE.equals(
                jdbcTemplate.queryForObject(sql, slotParams(slot), Boolean.class)
        );
    }

    public boolean existsPendingPaymentByOwner(Member member) {
        String sql = """
                SELECT EXISTS (
                    SELECT 1 FROM reservation
                    WHERE name = :name AND status = :status
                )
                """;
        return Boolean.TRUE.equals(jdbcTemplate.queryForObject(sql, Map.of(
                "name", member.name(),
                "status", ReservationStatus.PENDING_PAYMENT.name()
        ), Boolean.class));
    }

    public void confirm(long id) {
        String sql = "UPDATE reservation SET status = :status WHERE id = :id";
        int affected = jdbcTemplate.update(sql, Map.of(
                "id", id,
                "status", ReservationStatus.CONFIRMED.name()
        ));

        if (affected == 0) {
            throw new ResourceNotFoundException("요청한 예약을 찾을 수 없습니다.");
        }
    }

    private SqlParameterSource slotParams(Slot slot) {
        return new MapSqlParameterSource()
                .addValue("date", slot.date())
                .addValue("timeId", slot.time().id())
                .addValue("themeId", slot.theme().id());
    }

}
