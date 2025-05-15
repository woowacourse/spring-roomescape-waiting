package roomescape.reservation.repository.jdbc;

import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import roomescape.exception.DomainValidationException;
import roomescape.member.domain.Member;
import roomescape.member.domain.MemberRole;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.repository.ReservationRepository;
import roomescape.reservationtime.domain.ReservationTime;
import roomescape.theme.domain.Theme;

@Repository
@ConditionalOnProperty(name = "repository.strategy", havingValue = "jdbc")
public class JdbcReservationRepository implements ReservationRepository {
    private final JdbcTemplate jdbcTemplate;
    private final SimpleJdbcInsert simpleJdbcInsert;

    @Autowired
    public JdbcReservationRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        this.simpleJdbcInsert = new SimpleJdbcInsert(jdbcTemplate)
            .withTableName("reservation")
            .usingGeneratedKeyColumns("id");
    }

    @Override
    public Reservation save(Reservation reservation) {
        Map<String, Object> parameters = Map.ofEntries(
            Map.entry("date", reservation.getDate()),
            Map.entry("time_id", reservation.getTime().getId()),
            Map.entry("theme_id", reservation.getTheme().getId()),
            Map.entry("member_id", reservation.getMember().getId())
        );

        Long generatedKey = simpleJdbcInsert.executeAndReturnKey(parameters).longValue();

        return Reservation.generateWithPrimaryKey(reservation, generatedKey);
    }

    @Override
    public List<Reservation> findAll() {
        final String query = """
            SELECT
                r.id as reservation_id,
                r.date as reservation_date,
                t.id as time_id,
                t.start_at as time_start_at,
                m.id as member_id,
                m.name as member_name,
                m.email as member_email,
                m.role as role,
                m.password as member_password,
                th.id as theme_id,
                th.name as theme_name,
                th.description as theme_description,
                th.thumbnail as theme_thumbnail 
            FROM reservation as r
            INNER JOIN reservation_time as t
            ON r.time_id = t.id
            INNER JOIN theme as th
            ON r.theme_id = th.id
            INNER JOIN member as m
            ON r.member_id = m.id
            """;

        return jdbcTemplate.query(
            query,
            (resultSet, rowNum) -> new Reservation(
                resultSet.getLong("reservation_id"),
                resultSet.getDate("reservation_date").toLocalDate(),
                new ReservationTime(
                    resultSet.getLong("time_id"),
                    resultSet.getTime("time_start_at").toLocalTime()
                ),
                new Theme(
                    resultSet.getLong("theme_id"),
                    resultSet.getString("theme_name"),
                    resultSet.getString("theme_description"),
                    resultSet.getString("theme_thumbnail")
                ),
                new Member(
                    resultSet.getLong("member_id"),
                    resultSet.getString("member_name"),
                    resultSet.getString("member_email"),
                    MemberRole.valueOf(resultSet.getString("role")),
                    resultSet.getString("member_password")
                )
            )
        );
    }

    @Override
    public List<Reservation> findByMemberAndThemeAndVisitDateBetween(Long themeId, Long memberId, String dateFrom,
                                                                     String dateTo) {
        String query = """
            SELECT
                r.id as reservation_id,
                r.date as reservation_date,
                t.id as time_id,
                t.start_at as time_start_at,
                m.id as member_id,
                m.name as member_name,
                m.email as member_email,
                m.role as role,
                m.password as member_password,
                th.id as theme_id,
                th.name as theme_name,
                th.description as theme_description,
                th.thumbnail as theme_thumbnail 
            FROM reservation as r
            INNER JOIN reservation_time as t
                ON r.time_id = t.id
            INNER JOIN theme as th
                ON r.theme_id = th.id
            INNER JOIN member as m
                ON r.member_id = m.id
            WHERE th.id = ?
              AND m.id = ?
              AND r.date BETWEEN ? AND ?
            """;

        return jdbcTemplate.query(
            query,
            (resultSet, rowNum) -> new Reservation(
                resultSet.getLong("reservation_id"),
                resultSet.getDate("reservation_date").toLocalDate(),
                new ReservationTime(
                    resultSet.getLong("time_id"),
                    resultSet.getTime("time_start_at").toLocalTime()
                ),
                new Theme(
                    resultSet.getLong("theme_id"),
                    resultSet.getString("theme_name"),
                    resultSet.getString("theme_description"),
                    resultSet.getString("theme_thumbnail")
                ),
                new Member(
                    resultSet.getLong("member_id"),
                    resultSet.getString("member_name"),
                    resultSet.getString("member_email"),
                    MemberRole.valueOf(resultSet.getString("role")),
                    resultSet.getString("member_password")
                )
            ),
            themeId, memberId, dateFrom, dateTo
        );
    }

    @Override
    public boolean existsByTimeId(Long timeId) {
        final String query = """
            SELECT EXISTS (
                SELECT 1
                FROM reservation
                WHERE time_id = ?
            )
            """;

        return jdbcTemplate.queryForObject(query, Boolean.class, timeId);
    }

    @Override
    public void deleteById(Long id) {
        final String query = "DELETE FROM reservation WHERE id = ?";
        int affectedRows = jdbcTemplate.update(query, id);
        if (affectedRows == 0) {
            throw new DomainValidationException("예약을 삭제할 수 없습니다. 존재하지 않는 예약입니다.");
        }
    }
}
