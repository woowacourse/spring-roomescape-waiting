package roomescape.infrastructure.persistance;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import javax.sql.DataSource;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import roomescape.domain.member.Email;
import roomescape.domain.member.Member;
import roomescape.domain.member.Role;
import roomescape.domain.reservation.Reservation;
import roomescape.domain.reservation.ReservationRepository;
import roomescape.domain.reservation.ReservationTime;
import roomescape.domain.reservation.Theme;

@Repository
public class JdbcReservationRepository implements ReservationRepository {

    private static final RowMapper<Reservation> reservationRowMapper = (rs, rowNum) ->
            new Reservation(
                    rs.getLong("reservation_id"),
                    new Member(
                            rs.getLong("member_id"),
                            rs.getString("member_name"),
                            new Email(rs.getString("member_email")),
                            rs.getString("member_password"),
                            Role.valueOf(rs.getString("member_role"))
                    ),
                    rs.getDate("date").toLocalDate(),
                    new ReservationTime(
                            rs.getLong("time_id"),
                            rs.getTime("time_value").toLocalTime()
                    ),
                    new Theme(
                            rs.getLong("theme_id"),
                            rs.getString("theme_name"),
                            rs.getString("theme_description"),
                            rs.getString("theme_thumbnail")
                    )
            );

    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;
    private final SimpleJdbcInsert simpleJdbcInsert;

    public JdbcReservationRepository(NamedParameterJdbcTemplate namedParameterJdbcTemplate, DataSource dataSource) {
        this.namedParameterJdbcTemplate = namedParameterJdbcTemplate;
        this.simpleJdbcInsert = new SimpleJdbcInsert(dataSource)
                .withTableName("reservation")
                .usingGeneratedKeyColumns("id");
    }

    @Override
    public List<Reservation> findAll() {
        String sql = """
                SELECT
                    r.id as reservation_id,
                    m.id as member_id,
                    m.name as member_name,
                    m.email as member_email,
                    m.password as member_password,
                    r.date,
                    t.id as time_id,
                    t.start_at as time_value,
                    r.theme_id,
                    tm.name as theme_name,
                    tm.description as theme_description,
                    tm.thumbnail as theme_thumbnail,
                    m.id as member_id,
                    m.name as member_name,
                    m.email as member_email,
                    m.password as member_password,
                    m.role as member_role
                FROM reservation as r
                INNER JOIN reservation_time as t
                    ON r.time_id = t.id
                INNER JOIN theme as tm
                    ON r.theme_id = tm.id
                INNER JOIN member as m
                    ON r.member_id = m.id
                """;
        return namedParameterJdbcTemplate.query(sql, reservationRowMapper);
    }

    @Override
    public Long create(Reservation reservation) {
        SqlParameterSource params = new MapSqlParameterSource()
                .addValue("member_id", reservation.getMember().getId())
                .addValue("date", reservation.getDate())
                .addValue("time_id", reservation.getTime().id())
                .addValue("theme_id", reservation.getTheme().getId());
        return simpleJdbcInsert.executeAndReturnKey(params).longValue();
    }

    @Override
    public void deleteById(Long reservationId) {
        String sql = """
                DELETE FROM reservation
                WHERE id = :id
                """;
        namedParameterJdbcTemplate.update(sql, Map.of("id", reservationId));
    }

    @Override
    public Optional<Reservation> findById(Long reservationId) {
        try {
            String sql = """
                    SELECT
                        r.id as reservation_id,
                        r.date,
                        t.id as time_id,
                        t.start_at as time_value,
                        r.theme_id,
                        tm.name as theme_name,
                        tm.description as theme_description,
                        tm.thumbnail as theme_thumbnail,
                        m.id as member_id,
                        m.name as member_name,
                        m.email as member_email,
                        m.password as member_password,
                        m.role as member_role
                    FROM reservation as r
                    INNER JOIN reservation_time as t
                        ON r.time_id = t.id
                    INNER JOIN theme as tm
                        ON r.theme_id = tm.id
                    INNER JOIN member as m
                        ON r.member_id = m.id
                    WHERE r.id = :id
                    """;
            Reservation reservation = namedParameterJdbcTemplate.queryForObject(
                    sql,
                    Map.of("id", reservationId),
                    reservationRowMapper
            );
            return Optional.ofNullable(reservation);
        } catch (EmptyResultDataAccessException exception) {
            return Optional.empty();
        }
    }

    @Override
    public boolean existByTimeId(final Long reservationTimeId) {
        String sql = """
                SELECT COUNT(*)
                FROM reservation
                WHERE time_id = :timeId
                """;
        Long count = namedParameterJdbcTemplate.queryForObject(sql, Map.of("timeId", reservationTimeId), Long.class);
        return count != null && count > 0;
    }

    @Override
    public boolean existByDateAndTimeId(final LocalDate reservationDate, final Long timeId) {
        String sql = """
                SELECT COUNT(*)
                FROM reservation
                WHERE date = :date AND time_id = :timeId
                """;
        SqlParameterSource param = new MapSqlParameterSource()
                .addValue("date", reservationDate)
                .addValue("timeId", timeId);
        Long count = namedParameterJdbcTemplate.queryForObject(sql, param, Long.class);
        return count != null && count > 0;
    }

    @Override
    public boolean existByThemeId(final Long themeId) {
        String sql = """
                SELECT COUNT(*)
                FROM reservation
                WHERE theme_id = :themeId
                """;
        Long count = namedParameterJdbcTemplate.queryForObject(sql, Map.of("themeId", themeId), Long.class);
        return count != null && count > 0;
    }

    @Override
    public List<Reservation> findByThemeIdAndReservationDate(final Long themeId, final LocalDate reservationDate) {
        String sql = """
                SELECT
                    r.id as reservation_id,
                    r.date,
                    t.id as time_id,
                    t.start_at as time_value,
                    r.theme_id,
                    tm.name as theme_name,
                    tm.description as theme_description,
                    tm.thumbnail as theme_thumbnail,
                    m.id as member_id,
                    m.name as member_name,
                    m.email as member_email,
                    m.password as member_password,
                    m.role as member_role
                FROM reservation as r
                INNER JOIN reservation_time as t
                    ON r.time_id = t.id
                INNER JOIN theme as tm
                    ON r.theme_id = tm.id
                INNER JOIN member as m
                    ON r.member_id = m.id
                WHERE r.theme_id = :themeId
                  AND r.date = :date
                """;
        SqlParameterSource param = new MapSqlParameterSource()
                .addValue("themeId", themeId)
                .addValue("date", reservationDate);
        return namedParameterJdbcTemplate.query(sql, param, reservationRowMapper);
    }

    @Override
    public List<Reservation> findByThemeIdAndMemberIdBetweenDate(Long themeId,
                                                                 Long memberId,
                                                                 LocalDate from,
                                                                 LocalDate to) {
        String sql = """
                SELECT
                    r.id as reservation_id,
                    r.date,
                    t.id as time_id,
                    t.start_at as time_value,
                    r.theme_id,
                    tm.name as theme_name,
                    tm.description as theme_description,
                    tm.thumbnail as theme_thumbnail,
                    m.id as member_id,
                    m.name as member_name,
                    m.email as member_email,
                    m.password as member_password,
                    m.role as member_role
                FROM reservation as r
                INNER JOIN reservation_time as t
                    ON r.time_id = t.id
                INNER JOIN theme as tm
                    ON r.theme_id = tm.id
                INNER JOIN member as m
                    ON r.member_id = m.id
                WHERE r.theme_id = :themeId
                  AND m.id = :memberId
                  AND r.date BETWEEN :from AND :to
                """;
        SqlParameterSource param = new MapSqlParameterSource()
                .addValue("themeId", themeId)
                .addValue("memberId", memberId)
                .addValue("from", from)
                .addValue("to", to);
        return namedParameterJdbcTemplate.query(sql, param, reservationRowMapper);
    }
}
