package roomescape.reservation.repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import roomescape.member.domain.Member;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationStatus;
import roomescape.reservationtime.domain.ReservationTime;
import roomescape.theme.domain.Theme;

@Repository
public class JdbcReservationRepository implements ReservationRepository {

    private final JdbcTemplate jdbcTemplate;
    private final SimpleJdbcInsert simpleJdbcInsert;

    private final RowMapper<Reservation> rowMapper = (resultSet, rowNum) -> Reservation.restore(
            resultSet.getLong("reservation_id"),
            Member.restore(
                    resultSet.getLong("member_id"),
                    resultSet.getString("member_name"),
                    resultSet.getString("member_email"),
                    resultSet.getString("member_password"),
                    roomescape.member.domain.Role.valueOf(resultSet.getString("member_role"))
            ),
            resultSet.getDate("date").toLocalDate(),
            ReservationTime.restore(
                    resultSet.getLong("time_id"),
                    resultSet.getTime("time_start_at").toLocalTime(),
                    resultSet.getTime("time_finish_at").toLocalTime()
            ),
            Theme.restore(
                    resultSet.getLong("theme_id"),
                    resultSet.getString("theme_name"),
                    resultSet.getString("theme_description"),
                    resultSet.getString("theme_image_url"),
                    resultSet.getLong("theme_price")
            ),
            ReservationStatus.valueOf(resultSet.getString("status"))
    );

    private final RowMapper<Long> idMapper = (resultSet, rowNum) -> resultSet.getLong("id");

    public JdbcReservationRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        this.simpleJdbcInsert = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("reservation")
                .usingColumns("member_id", "date", "time_id", "theme_id", "status")
                .usingGeneratedKeyColumns("id");
    }

    @Override
    public Reservation save(Reservation reservation) {
        SqlParameterSource parameters = new MapSqlParameterSource()
                .addValue("member_id", reservation.getMember().getId())
                .addValue("date", reservation.getDate())
                .addValue("time_id", reservation.getTime().getId())
                .addValue("theme_id", reservation.getTheme().getId())
                .addValue("status", reservation.getStatus().name());
        Long id = simpleJdbcInsert.executeAndReturnKey(parameters).longValue();
        return Reservation.restore(id, reservation.getMember(), reservation.getDate(), reservation.getTime(),
                reservation.getTheme(), reservation.getStatus());
    }

    @Override
    public Optional<Reservation> findById(Long id) {
        String query = """
                SELECT r.id AS reservation_id, r.date, r.status,
                       m.id AS member_id, m.name AS member_name, m.email AS member_email, m.password AS member_password, m.role AS member_role,
                       rt.id AS time_id, rt.start_at AS time_start_at, rt.finish_at AS time_finish_at,
                       t.id AS theme_id, t.name AS theme_name, t.description AS theme_description, t.image_url AS theme_image_url, t.price AS theme_price
                FROM reservation r
                JOIN member m ON r.member_id = m.id
                JOIN reservation_time rt ON r.time_id = rt.id
                JOIN theme t ON r.theme_id = t.id
                WHERE r.id = ?
                """;
        return jdbcTemplate.query(query, rowMapper, id).stream().findFirst();
    }

    @Override
    public List<Reservation> findByMemberId(Long memberId) {
        String query = """
                SELECT r.id AS reservation_id, r.date, r.status,
                       m.id AS member_id, m.name AS member_name, m.email AS member_email, m.password AS member_password, m.role AS member_role,
                       rt.id AS time_id, rt.start_at AS time_start_at, rt.finish_at AS time_finish_at,
                       t.id AS theme_id, t.name AS theme_name, t.description AS theme_description, t.image_url AS theme_image_url, t.price AS theme_price
                FROM reservation r
                JOIN member m ON r.member_id = m.id
                JOIN reservation_time rt ON r.time_id = rt.id
                JOIN theme t ON r.theme_id = t.id
                WHERE r.member_id = ?
                ORDER BY r.date DESC, rt.start_at DESC
                """;
        return jdbcTemplate.query(query, rowMapper, memberId);
    }

    @Override
    public List<Reservation> findPendingCreatedBefore(LocalDateTime cutoff) {
        String query = """
                SELECT r.id AS reservation_id, r.date, r.status,
                       m.id AS member_id, m.name AS member_name, m.email AS member_email, m.password AS member_password, m.role AS member_role,
                       rt.id AS time_id, rt.start_at AS time_start_at, rt.finish_at AS time_finish_at,
                       t.id AS theme_id, t.name AS theme_name, t.description AS theme_description, t.image_url AS theme_image_url, t.price AS theme_price
                FROM reservation r
                JOIN member m ON r.member_id = m.id
                JOIN reservation_time rt ON r.time_id = rt.id
                JOIN theme t ON r.theme_id = t.id
                WHERE r.status = 'PENDING' AND r.created_at < ?
                """;
        return jdbcTemplate.query(query, rowMapper, cutoff);
    }

    @Override
    public void update(Long id, LocalDate date, Long timeId) {
        String query = "UPDATE reservation SET date = ?, time_id = ? WHERE id = ?";
        jdbcTemplate.update(query, date, timeId, id);
    }

    @Override
    public void confirm(Long id) {
        String query = "UPDATE reservation SET status = 'CONFIRMED' WHERE id = ?";
        jdbcTemplate.update(query, id);
    }

    @Override
    public boolean existsByDateAndTimeIdAndThemeId(LocalDate date, Long timeId, Long themeId) {
        String query = "SELECT COUNT(*) FROM reservation WHERE date = ? AND time_id = ? AND theme_id = ?";
        Integer count = jdbcTemplate.queryForObject(query, Integer.class, date, timeId, themeId);
        return count != null && count > 0;
    }

    @Override
    public boolean existsByMemberIdAndDateAndTimeIdAndThemeId(Long memberId, LocalDate date, Long timeId, Long themeId) {
        String query = "SELECT COUNT(*) FROM reservation WHERE member_id = ? AND date = ? AND time_id = ? AND theme_id = ?";
        Integer count = jdbcTemplate.queryForObject(query, Integer.class, memberId, date, timeId, themeId);
        return count != null && count > 0;
    }

    @Override
    public void lockSlot(LocalDate date, Long timeId, Long themeId) {
        String query = "SELECT id FROM reservation WHERE date = ? AND time_id = ? AND theme_id = ? FOR UPDATE";
        jdbcTemplate.query(query, idMapper, date, timeId, themeId);
    }

    @Override
    public void deleteById(Long id) {
        String query = "DELETE FROM reservation WHERE id = ?";
        jdbcTemplate.update(query, id);
    }

}
