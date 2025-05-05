package roomescape.reservation.infrastructure;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import roomescape.auth.domain.AuthRole;
import roomescape.exception.resource.InCorrectResultSizeException;
import roomescape.exception.resource.ResourceNotFoundException;
import roomescape.member.domain.Member;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationCommandRepository;
import roomescape.reservation.domain.ReservationQueryRepository;
import roomescape.reservation.domain.ReservationTime;
import roomescape.theme.domain.Theme;

@Repository
@RequiredArgsConstructor
public class JdbcReservationRepository implements ReservationCommandRepository, ReservationQueryRepository {

    private static final RowMapper<Reservation> RESERVATION_ROW_MAPPER = (rs, rowNum) ->
            new Reservation(
                    rs.getLong("id"),
                    rs.getDate("date").toLocalDate(),
                    new ReservationTime(
                            rs.getLong("time_id"),
                            rs.getTime("start_at").toLocalTime()
                    ),
                    new Theme(
                            rs.getLong("theme_id"),
                            rs.getString("theme_name"),
                            rs.getString("theme_description"),
                            rs.getString("theme_thumbnail")
                    ),
                    new Member(
                            rs.getLong("member_id"),
                            rs.getString("member_name"),
                            rs.getString("member_email"),
                            rs.getString("member_password"),
                            AuthRole.from(rs.getString("member_role"))
                    )
            );

    private final JdbcTemplate jdbcTemplate;

    @Override
    public Long save(Reservation reservation) {
        String sql = "INSERT INTO reservations (date, time_id, theme_id, member_id) VALUES (?, ?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, new String[]{"id"});
            ps.setDate(1, Date.valueOf(reservation.getDate()));
            ps.setLong(2, reservation.getTime().getId());
            ps.setLong(3, reservation.getTheme().getId());
            ps.setLong(4, reservation.getMember().getId());
            return ps;
        }, keyHolder);

        return Optional.of(keyHolder.getKey())
                .map(Number::longValue)
                .orElseThrow(() -> new IllegalStateException("예약 추가에 실패했습니다."));
    }

    @Override
    public void deleteById(Long id) {
        final String sql = "DELETE FROM reservations WHERE id = ?";
        jdbcTemplate.update(sql, id);
    }

    @Override
    public Boolean existsByDateAndTimeIdAndThemeId(final LocalDate date, final Long timeId, final Long themeId) {
        final String sql = """
                SELECT EXISTS(
                    SELECT 1
                    FROM reservations AS r
                    WHERE r.date = ? AND r.time_id = ? AND r.theme_id = ?
                    LIMIT 1
                )
                """;

        return jdbcTemplate.queryForObject(sql, Boolean.class, date, timeId, themeId);
    }

    @Override
    public Optional<Reservation> findById(final Long id) {
        final String sql = """
                SELECT 
                    r.id AS id,
                    r.date AS date,
                    rt.id AS time_id,
                    rt.start_at AS start_at,
                    th.id AS theme_id,
                    th.name AS theme_name,
                    th.description AS theme_description,
                    th.thumbnail AS theme_thumbnail,
                    m.id AS member_id,
                    m.name AS member_name,
                    m.email AS member_email,
                    m.password AS member_password,
                    m.role AS member_role
                FROM reservations AS r
                INNER JOIN reservation_times AS rt
                ON r.time_id = rt.id
                INNER JOIN themes AS th
                ON r.theme_id = th.id
                INNER JOIN members AS m
                ON r.member_id = m.id
                WHERE r.id = ?
                """;

        try {
            return Optional.of(jdbcTemplate.queryForObject(sql, RESERVATION_ROW_MAPPER, id));
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        } catch (IncorrectResultSizeDataAccessException e) {
            throw new InCorrectResultSizeException("예약이 여러 개 존재합니다.");
        }
    }

    @Override
    public Reservation getById(Long id) {
        final String sql = """
                SELECT 
                    r.id AS id,
                    r.date AS date,
                    rt.id AS time_id,
                    rt.start_at AS start_at,
                    th.id AS theme_id,
                    th.name AS theme_name,
                    th.description AS theme_description,
                    th.thumbnail AS theme_thumbnail,
                    m.id AS member_id,
                    m.name AS member_name,
                    m.email AS member_email,
                    m.password AS member_password,
                    m.role AS member_role
                FROM reservations AS r
                INNER JOIN reservation_times AS rt
                ON r.time_id = rt.id
                INNER JOIN themes AS th
                ON r.theme_id = th.id
                INNER JOIN members AS m
                ON r.member_id = m.id
                WHERE r.id = ?
                """;

        try {
            return jdbcTemplate.queryForObject(sql, RESERVATION_ROW_MAPPER, id);
        } catch (EmptyResultDataAccessException e) {
            throw new ResourceNotFoundException("예약이 존재하지 않습니다.");
        } catch (IncorrectResultSizeDataAccessException e) {
            throw new InCorrectResultSizeException("예약이 여러 개 존재합니다.");
        }
    }

    @Override
    public List<Reservation> findAll() {
        final String sql = """
                SELECT 
                    r.id AS id,                
                    r.date AS date,
                    rt.id AS time_id,
                    rt.start_at AS start_at,
                    th.id AS theme_id,
                    th.name AS theme_name,
                    th.description AS theme_description,
                    th.thumbnail AS theme_thumbnail,
                    m.id AS member_id,
                    m.name AS member_name,
                    m.email AS member_email,
                    m.password AS member_password,
                    m.role AS member_role
                FROM reservations AS r
                INNER JOIN reservation_times AS rt
                ON r.time_id = rt.id
                INNER JOIN themes AS th
                ON r.theme_id = th.id
                INNER JOIN members AS m
                ON r.member_id = m.id
                """;

        return jdbcTemplate.query(sql, RESERVATION_ROW_MAPPER);
    }

    @Override
    public List<Reservation> findAllByThemeIdAndMemberIdAndDateRange(final Long themeId, final Long memberId,
                                                                     final LocalDate dateFrom, final LocalDate dateTo) {
        final String sql = """
                SELECT 
                    r.id AS id,                
                    r.date AS date,
                    rt.id AS time_id,
                    rt.start_at AS start_at,
                    th.id AS theme_id,
                    th.name AS theme_name,
                    th.description AS theme_description,
                    th.thumbnail AS theme_thumbnail,
                    m.id AS member_id,
                    m.name AS member_name,
                    m.email AS member_email,
                    m.password AS member_password,
                    m.role AS member_role
                FROM reservations AS r
                INNER JOIN reservation_times AS rt
                ON r.time_id = rt.id
                INNER JOIN themes AS th
                ON r.theme_id = th.id
                INNER JOIN members AS m
                ON r.member_id = m.id
                WHERE th.id = ? AND m.id = ? AND r.date BETWEEN ? AND ?
                """;

        return jdbcTemplate.query(sql, RESERVATION_ROW_MAPPER, themeId, memberId, dateFrom, dateTo);
    }

    @Override
    public List<Reservation> findReservationsByDateAndThemeId(final LocalDate date, final Long themeId) {
        final String sql = """
                SELECT
                    r.id AS id,
                    r.date AS date,
                    rt.id AS time_id,
                    rt.start_at AS start_at,
                    th.id AS theme_id,
                    th.name AS theme_name,
                    th.description AS theme_description,
                    th.thumbnail AS theme_thumbnail,
                    m.id AS member_id,
                    m.name AS member_name,
                    m.email AS member_email,
                    m.password AS member_password,
                    m.role AS member_role
                FROM reservations AS r
                INNER JOIN reservation_times AS rt
                ON r.time_id = rt.id
                INNER JOIN themes AS th
                ON r.theme_id = th.id
                INNER JOIN members AS m
                ON r.member_id = m.id
                WHERE r.date = ? AND th.id = ?
                """;

        return jdbcTemplate.query(sql, RESERVATION_ROW_MAPPER, date, themeId);
    }
}
