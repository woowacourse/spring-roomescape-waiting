package roomescape.reservationwaiting.repository;

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
import roomescape.reservationtime.domain.ReservationTime;
import roomescape.reservationwaiting.domain.ReservationWaiting;
import roomescape.theme.domain.Theme;

@Repository
public class JdbcReservationWaitingRepository implements ReservationWaitingRepository {

    private final JdbcTemplate jdbcTemplate;
    private final SimpleJdbcInsert simpleJdbcInsert;

    private final RowMapper<ReservationWaiting> rowMapper = (resultSet, rowNum) -> ReservationWaiting.restore(
            resultSet.getLong("waiting_id"),
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
            )
    );

    private final RowMapper<WaitingWithTurn> turnRowMapper = (resultSet, rowNum) ->
            new WaitingWithTurn(rowMapper.mapRow(resultSet, rowNum), resultSet.getLong("turn"));

    public JdbcReservationWaitingRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        this.simpleJdbcInsert = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("reservation_waiting")
                .usingGeneratedKeyColumns("id")
                .usingColumns("member_id", "date", "time_id", "theme_id");
    }

    @Override
    public ReservationWaiting save(ReservationWaiting waiting) {
        SqlParameterSource parameters = new MapSqlParameterSource()
                .addValue("member_id", waiting.getMember().getId())
                .addValue("date", waiting.getDate())
                .addValue("time_id", waiting.getTime().getId())
                .addValue("theme_id", waiting.getTheme().getId());
        Long id = simpleJdbcInsert.executeAndReturnKey(parameters).longValue();
        return ReservationWaiting.restore(id, waiting.getMember(), waiting.getDate(), waiting.getTime(),
                waiting.getTheme());
    }

    @Override
    public void deleteById(Long id) {
        String query = "DELETE FROM reservation_waiting WHERE id = ?";
        jdbcTemplate.update(query, id);
    }

    @Override
    public List<ReservationWaiting> findByMemberId(Long memberId) {
        String query = """
                SELECT rw.id AS waiting_id, rw.date,
                       m.id AS member_id, m.name AS member_name, m.email AS member_email, m.password AS member_password, m.role AS member_role,
                       rt.id AS time_id, rt.start_at AS time_start_at, rt.finish_at AS time_finish_at,
                       t.id AS theme_id, t.name AS theme_name, t.description AS theme_description, t.image_url AS theme_image_url, t.price AS theme_price
                FROM reservation_waiting rw
                JOIN member m ON rw.member_id = m.id
                JOIN reservation_time rt ON rw.time_id = rt.id
                JOIN theme t ON rw.theme_id = t.id
                WHERE rw.member_id = ?
                ORDER BY rw.id
                """;
        return jdbcTemplate.query(query, rowMapper, memberId);
    }

    @Override
    public Optional<ReservationWaiting> findById(Long id) {
        String query = """
                SELECT rw.id AS waiting_id, rw.date,
                       m.id AS member_id, m.name AS member_name, m.email AS member_email, m.password AS member_password, m.role AS member_role,
                       rt.id AS time_id, rt.start_at AS time_start_at, rt.finish_at AS time_finish_at,
                       t.id AS theme_id, t.name AS theme_name, t.description AS theme_description, t.image_url AS theme_image_url, t.price AS theme_price
                FROM reservation_waiting rw
                JOIN member m ON rw.member_id = m.id
                JOIN reservation_time rt ON rw.time_id = rt.id
                JOIN theme t ON rw.theme_id = t.id
                WHERE rw.id = ?
                """;
        return jdbcTemplate.query(query, rowMapper, id).stream().findFirst();
    }

    @Override
    public Optional<ReservationWaiting> findFirstByDateAndTimeIdAndThemeId(LocalDate date, Long timeId, Long themeId) {
        String query = """
                SELECT rw.id AS waiting_id, rw.date,
                       m.id AS member_id, m.name AS member_name, m.email AS member_email, m.password AS member_password, m.role AS member_role,
                       rt.id AS time_id, rt.start_at AS time_start_at, rt.finish_at AS time_finish_at,
                       t.id AS theme_id, t.name AS theme_name, t.description AS theme_description, t.image_url AS theme_image_url, t.price AS theme_price
                FROM reservation_waiting rw
                JOIN member m ON rw.member_id = m.id
                JOIN reservation_time rt ON rw.time_id = rt.id
                JOIN theme t ON rw.theme_id = t.id
                WHERE rw.date = ? AND rw.time_id = ? AND rw.theme_id = ?
                ORDER BY rw.created_at, rw.id
                LIMIT 1
                """;
        return jdbcTemplate.query(query, rowMapper, date, timeId, themeId).stream().findFirst();
    }

    @Override
    public boolean existsByMemberIdAndDateAndTimeIdAndThemeId(Long memberId, LocalDate date, Long timeId,
                                                              Long themeId) {
        String query = """
                SELECT COUNT(*) FROM reservation_waiting
                WHERE member_id = ? AND date = ? AND time_id = ? AND theme_id = ?
                """;
        Integer count = jdbcTemplate.queryForObject(query, Integer.class, memberId, date, timeId, themeId);
        return count != null && count > 0;
    }

    @Override
    public List<WaitingWithTurn> findWithTurnByMemberId(Long memberId) {
        String query = """
                SELECT rw.id AS waiting_id, rw.date,
                       m.id AS member_id, m.name AS member_name, m.email AS member_email, m.password AS member_password, m.role AS member_role,
                       rt.id AS time_id, rt.start_at AS time_start_at, rt.finish_at AS time_finish_at,
                       t.id AS theme_id, t.name AS theme_name, t.description AS theme_description, t.image_url AS theme_image_url, t.price AS theme_price,
                       sub.turn AS turn
                FROM (
                    SELECT id, ROW_NUMBER() OVER (PARTITION BY date, time_id, theme_id ORDER BY created_at, id) AS turn
                    FROM reservation_waiting
                ) sub
                JOIN reservation_waiting rw ON rw.id = sub.id
                JOIN member m ON rw.member_id = m.id
                JOIN reservation_time rt ON rw.time_id = rt.id
                JOIN theme t ON rw.theme_id = t.id
                WHERE rw.member_id = ?
                ORDER BY rw.id
                """;
        return jdbcTemplate.query(query, turnRowMapper, memberId);
    }
}
