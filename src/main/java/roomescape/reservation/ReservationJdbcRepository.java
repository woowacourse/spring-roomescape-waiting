package roomescape.reservation;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import roomescape.member.Member;
import roomescape.member.MemberRole;
import roomescape.reservationtime.ReservationTime;
import roomescape.theme.Theme;

@Repository
public class ReservationJdbcRepository implements ReservationRepository {

    private final JdbcTemplate jdbcTemplate;
    private final SimpleJdbcInsert simpleJdbcInsert;

    public ReservationJdbcRepository(
            @Autowired final JdbcTemplate jdbcTemplate
    ) {
        this.jdbcTemplate = jdbcTemplate;
        this.simpleJdbcInsert = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("reservation")
                .usingGeneratedKeyColumns("id");
    }

    @Override
    public Long save(
            final Reservation reservation,
            final Long reservationTimeId,
            final Long themeId,
            final Long memberId
    ) {
        final Map<String, Object> reservationParameter = Map.of(
                "date", reservation.getDate(),
                "member_id", memberId,
                "time_id", reservationTimeId,
                "theme_id", themeId
        );
        return simpleJdbcInsert
                .executeAndReturnKey(reservationParameter)
                .longValue();
    }

    @Override
    public List<Reservation> findAll() {
        final String sql = """
                SELECT r.id AS id, 
                       r.date AS date,
                       m.id AS member_id,
                       m.email AS member_email,
                       m.password AS member_password,
                       m.name AS member_name, 
                       m.role AS member_role,
                       t.id AS time_id, 
                       t.start_at AS time_value, 
                       th.id AS theme_id, 
                       th.name AS theme_name, 
                       th.description AS theme_description, 
                       th.thumbnail AS theme_thumbnail 
                FROM reservation AS r INNER JOIN reservation_time AS t INNER JOIN theme AS th INNER JOIN member AS m
                    ON r.time_id = t.id AND r.theme_id = th.id  AND r.member_id = m.id
                """;
        return jdbcTemplate.query(sql, getRowMapper());
    }

    @Override
    public List<Reservation> findAllByThemeIdAndDate(final Long themeId, final LocalDate date) {
        final String sql = """
                SELECT r.id AS id, 
                       r.date AS date, 
                       m.id AS member_id,
                       m.email AS member_email,
                       m.password AS member_password,
                       m.name AS member_name, 
                       m.role AS member_role, 
                       t.id AS time_id, 
                       t.start_at AS time_value, 
                       th.id AS theme_id, 
                       th.name AS theme_name, 
                       th.description AS theme_description, 
                       th.thumbnail AS theme_thumbnail 
                FROM reservation AS r INNER JOIN reservation_time AS t INNER JOIN theme AS th INNER JOIN member AS m
                    ON r.time_id = t.id AND r.theme_id = th.id AND r.member_id = m.id 
                WHERE
                    r.theme_id=? AND r.date=?
                """;
        return jdbcTemplate.query(sql, getRowMapper(), themeId, date);
    }

    @Override
    public List<Reservation> findAllByMemberIdAndThemeIdAndDateRange(
            final Long memberId,
            final Long themeId,
            final LocalDate from,
            final LocalDate to
    ) {
        final String sql = """
                SELECT r.id AS id, 
                       r.date AS date, 
                       m.id AS member_id,
                       m.email AS member_email,
                       m.password AS member_password,
                       m.name AS member_name, 
                       m.role AS member_role, 
                       t.id AS time_id, 
                       t.start_at AS time_value, 
                       th.id AS theme_id, 
                       th.name AS theme_name, 
                       th.description AS theme_description, 
                       th.thumbnail AS theme_thumbnail 
                FROM reservation AS r 
                    INNER JOIN reservation_time AS t ON r.time_id=t.id 
                    INNER JOIN theme AS th ON r.theme_id=th.id
                    INNER JOIN member AS m ON r.member_id=m.id 
                WHERE
                    (r.member_id=? OR ? IS NULL) 
                AND (r.theme_id=? OR ? IS NULL)
                AND ((? IS NULL OR ? IS NULL) OR (r.date BETWEEN ? AND ?));
                """;
        return jdbcTemplate.query(sql, getRowMapper(),
                memberId, memberId,
                themeId, themeId,
                from, to, from, to);
    }

    @Override
    public Reservation findById(Long id) {
        final String sql = """ 
                SELECT r.id AS id, 
                       r.date AS date, 
                       m.id AS member_id,
                       m.email AS member_email,
                       m.password AS member_password,
                       m.name AS member_name, 
                       m.role AS member_role, 
                       t.id AS time_id, 
                       t.start_at AS time_value, 
                       th.id AS theme_id, 
                       th.name AS theme_name, 
                       th.description AS theme_description, 
                       th.thumbnail AS theme_thumbnail 
                FROM reservation AS r 
                    INNER JOIN reservation_time AS t 
                    INNER JOIN theme AS th
                    INNER JOIN member AS m                                            
                    ON r.time_id = t.id AND r.theme_id = th.id AND r.member_id = m.id  
                WHERE r.id=? 
                """;
        return jdbcTemplate.queryForObject(sql, getRowMapper(), id);
    }

    @Override
    public void delete(final Long id) {
        final String sql = "DELETE FROM reservation WHERE id=?";
        jdbcTemplate.update(sql, id);
    }

    @Override
    public Boolean existsById(final Long id) {
        final String sql = "SELECT EXISTS (SELECT 1 FROM reservation WHERE id=?)";
        return jdbcTemplate.queryForObject(sql, Boolean.class, id);
    }

    @Override
    public Boolean existsByReservationTime(final Long reservationTimeId) {
        final String sql = "SELECT EXISTS (SELECT 1 FROM reservation WHERE time_id=?)";
        return jdbcTemplate.queryForObject(sql, Boolean.class, reservationTimeId);
    }

    @Override
    public Boolean existsByTheme(final Long themeId) {
        final String sql = "SELECT EXISTS (SELECT 1 FROM reservation WHERE theme_id=?)";
        return jdbcTemplate.queryForObject(sql, Boolean.class, themeId);
    }

    @Override
    public Boolean existsByReservationTimeIdAndDateAndThemeId(
            final Long reservationTimeId,
            final LocalDate date,
            final Long themeId
    ) {
        final String sql = "SELECT EXISTS (SELECT 1 FROM reservation WHERE time_id=? AND date=? AND theme_id=?)";
        return jdbcTemplate.queryForObject(sql, Boolean.class, reservationTimeId, date, themeId);
    }

    private RowMapper<Reservation> getRowMapper() {
        return (resultSet, rowNum) -> {
            final ReservationTime time = new ReservationTime(
                    resultSet.getLong("time_id"),
                    resultSet.getTime("time_value").toLocalTime()
            );
            final Theme theme = new Theme(
                    resultSet.getLong("theme_id"),
                    resultSet.getString("theme_name"),
                    resultSet.getString("theme_description"),
                    resultSet.getString("theme_thumbnail")
            );
            final Member member = new Member(
                    resultSet.getLong("member_id"),
                    resultSet.getString("member_email"),
                    resultSet.getString("member_password"),
                    resultSet.getString("member_name"),
                    MemberRole.valueOf(resultSet.getString("member_role"))
            );

            return new Reservation(
                    resultSet.getLong("id"),
                    resultSet.getDate("date").toLocalDate(),
                    member,
                    time,
                    theme
            );
        };
    }
}
