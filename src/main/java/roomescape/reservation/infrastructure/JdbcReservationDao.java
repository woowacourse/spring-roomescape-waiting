package roomescape.reservation.infrastructure;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Repository;
import roomescape.member.business.domain.Member;
import roomescape.member.business.domain.Role;
import roomescape.reservation.business.domain.Reservation;
import roomescape.reservation.business.domain.ReservationTime;
import roomescape.reservation.business.repository.ReservationDao;
import roomescape.theme.business.domain.Theme;

@Repository
public class JdbcReservationDao implements ReservationDao {

    private static final RowMapper<Reservation> ROW_MAPPER = (rs, rowNum) -> {
        ReservationTime reservationTime = new ReservationTime(
                rs.getLong("reservation_time.id"),
                rs.getTime("reservation_time.start_at").toLocalTime()
        );
        Theme theme = new Theme(
                rs.getLong("reservation.theme_id"),
                rs.getString("theme.name"),
                rs.getString("theme.description"),
                rs.getString("theme.thumbnail")
        );
        Member member = new Member(
                rs.getLong("member.id"),
                rs.getString("member.name"),
                rs.getString("member.email"),
                rs.getString("member.password"),
                Role.valueOf(rs.getString("role"))
        );
        return new Reservation(
                rs.getLong("reservation.id"),
                rs.getDate("reservation.date").toLocalDate(),
                reservationTime,
                theme,
                member);
    };

    private final NamedParameterJdbcTemplate jdbcTemplate;

    public JdbcReservationDao(final NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public List<Reservation> findAll() {
        final String sql = """
                SELECT * FROM reservation r 
                JOIN reservation_time rt ON r.time_id = rt.id
                JOIN theme t ON r.theme_id = t.id
                JOIN member m ON r.member_id = m.id
                """;

        return jdbcTemplate.query(sql, ROW_MAPPER);
    }

    @Override
    public Reservation save(final Reservation reservation) {
        final String sql = "INSERT INTO reservation (date, time_id, theme_id, member_id) VALUES (:date, :time_id, :theme_id, :member_id)";
        final GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();

        final MapSqlParameterSource mapSqlParameterSource = new MapSqlParameterSource()
                .addValue("date", reservation.getDate())
                .addValue("time_id", reservation.getTime().getId())
                .addValue("theme_id", reservation.getTheme().getId())
                .addValue("member_id", reservation.getMember().getId());
        jdbcTemplate.update(sql, mapSqlParameterSource, keyHolder);

        final Number key = keyHolder.getKey();
        return new Reservation(
                key.longValue(),
                reservation.getDate(),
                reservation.getTime(),
                reservation.getTheme(),
                reservation.getMember()
        );
    }

    @Override
    public int deleteById(final Long id) {
        final String sql = "DELETE FROM reservation WHERE id = :id";
        return jdbcTemplate.update(sql, new MapSqlParameterSource("id", id));
    }

    @Override
    public Optional<Reservation> findById(final Long id) {
        final String sql = """
                SELECT * FROM reservation r
                JOIN reservation_time rt ON r.time_id = rt.id
                JOIN theme t ON r.theme_id = t.id
                JOIN member m ON r.member_id = m.id
                WHERE r.id = :id
                """;
        try {
            final Reservation reservation = jdbcTemplate.queryForObject(sql, new MapSqlParameterSource("id", id),
                    ROW_MAPPER);
            return Optional.of(reservation);
        } catch (final EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public boolean existByTimeId(final Long timeId) {
        final String sql = """
                SELECT EXISTS 
                    (SELECT 1 
                     FROM reservation 
                     WHERE time_id = :time_id
                    )
                """;

        return Boolean.TRUE == jdbcTemplate.queryForObject(
                sql, new MapSqlParameterSource("time_id", timeId), Boolean.class);
    }

    @Override
    public boolean existByThemeId(final Long themeId) {
        final String sql = """
                SELECT EXISTS 
                    (SELECT 1 
                     FROM reservation 
                     WHERE theme_id = :theme_id
                    )
                """;

        return Boolean.TRUE == jdbcTemplate.queryForObject(
                sql, new MapSqlParameterSource("theme_id", themeId), Boolean.class);
    }

    @Override
    public List<Reservation> findByDateAndThemeId(final LocalDate date, final Long themeId) {
        final String sql = """
                SELECT * FROM reservation r
                JOIN reservation_time rt ON r.time_id = rt.id
                JOIN theme t ON r.theme_id = t.id
                JOIN member m ON r.member_id = m.id
                WHERE r.date = :date AND t.id = :theme_id
                """;

        final MapSqlParameterSource mapSqlParameterSource = new MapSqlParameterSource()
                .addValue("theme_id", themeId)
                .addValue("date", date);

        return jdbcTemplate.query(sql, mapSqlParameterSource, ROW_MAPPER);
    }

    @Override
    public List<Reservation> findReservationByThemeIdAndMemberIdInDuration(final long themeId, final long memberId,
                                                                           final LocalDate start,
                                                                           final LocalDate end) {
        final String sql = """
                    SELECT * FROM reservation r
                    JOIN reservation_time rt ON r.time_id = rt.id
                    JOIN theme t ON r.theme_id = t.id
                    JOIN member m ON r.member_id = m.id
                    WHERE r.theme_id = :theme_id
                      AND r.member_id = :member_id
                      AND r.date BETWEEN :start AND :end
                """;

        final MapSqlParameterSource mapSqlParameterSource = new MapSqlParameterSource()
                .addValue("theme_id", themeId)
                .addValue("member_id", memberId)
                .addValue("start", start)
                .addValue("end", end);

        return jdbcTemplate.query(sql, mapSqlParameterSource, ROW_MAPPER);
    }
}
