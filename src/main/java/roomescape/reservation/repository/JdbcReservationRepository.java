package roomescape.reservation.repository;

import java.time.LocalDate;
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
import roomescape.reservation.dto.ReservationIdResponse;
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
                    resultSet.getString("member_password")
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
                    resultSet.getString("theme_image_url")
            )
    );

    private final RowMapper<Long> idMapper = (resultSet, rowNum) -> resultSet.getLong("id");

    public JdbcReservationRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        this.simpleJdbcInsert = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("reservation")
                .usingGeneratedKeyColumns("id");
    }

    @Override
    public Reservation save(Reservation reservation) {
        SqlParameterSource parameters = new MapSqlParameterSource()
                .addValue("member_id", reservation.getMember().getId())
                .addValue("date", reservation.getDate())
                .addValue("time_id", reservation.getTime().getId())
                .addValue("theme_id", reservation.getTheme().getId());
        Long id = simpleJdbcInsert.executeAndReturnKey(parameters).longValue();
        return Reservation.restore(id, reservation.getMember(), reservation.getDate(), reservation.getTime(),
                reservation.getTheme());
    }

    @Override
    public Optional<Reservation> findById(Long id) {
        String query = """
                SELECT r.id as reservation_id, r.date,
                       m.id as member_id, m.name as member_name, m.email as member_email, m.password as member_password,
                       rt.id as time_id, rt.start_at as time_start_at, rt.finish_at as time_finish_at,
                       t.id as theme_id, t.name as theme_name, t.description as theme_description, t.image_url as theme_image_url
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
                SELECT r.id as reservation_id, r.date,
                       m.id as member_id, m.name as member_name, m.email as member_email, m.password as member_password,
                       rt.id as time_id, rt.start_at as time_start_at, rt.finish_at as time_finish_at,
                       t.id as theme_id, t.name as theme_name, t.description as theme_description, t.image_url as theme_image_url
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
    public void update(Long id, LocalDate date, Long timeId) {
        String query = "UPDATE reservation SET date = ?, time_id = ? WHERE id = ?";
        jdbcTemplate.update(query, date, timeId, id);
    }

    @Override
    public boolean existsByDateAndTimeIdAndThemeId(LocalDate date, Long timeId, Long themeId) {
        String query = "SELECT count(*) FROM reservation WHERE date = ? AND time_id = ? AND theme_id = ?";
        Integer count = jdbcTemplate.queryForObject(query, Integer.class, date, timeId, themeId);
        return count != null && count > 0;
    }

    @Override
    public boolean existsByMemberIdAndDateAndTimeIdAndThemeId(Long memberId, LocalDate date, Long timeId, Long themeId) {
        String query = "SELECT count(*) FROM reservation WHERE member_id = ? AND date = ? AND time_id = ? AND theme_id = ?";
        Integer count = jdbcTemplate.queryForObject(query, Integer.class, memberId, date, timeId, themeId);
        return count != null && count > 0;
    }

    @Override
    public void deleteById(Long id) {
        String query = "DELETE FROM reservation WHERE id = ?";
        jdbcTemplate.update(query, id);
    }

    @Override
    public ReservationIdResponse findReservationId(LocalDate date, Long themeId, Long timeId) {
        String query = "SELECT id FROM reservation WHERE date = ? AND theme_id = ? AND time_id = ?";
        return ReservationIdResponse.from(jdbcTemplate.query(query, idMapper, date, themeId, timeId).getFirst());
    }
}
