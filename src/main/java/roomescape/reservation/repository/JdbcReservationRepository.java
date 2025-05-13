package roomescape.reservation.repository;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import roomescape.common.exception.DataExistException;
import roomescape.common.exception.SaveException;
import roomescape.member.domain.Member;
import roomescape.member.domain.Role;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationTime;
import roomescape.theme.domain.Theme;

@Repository
@RequiredArgsConstructor
public class JdbcReservationRepository implements ReservationRepository {

    private static final RowMapper<Reservation> RESERVATION_ROW_MAPPER = (rs, rowNum) ->
            new Reservation(
                    rs.getLong("id"),
                    new Member(
                            rs.getLong("member_id"),
                            rs.getString("member_name"),
                            rs.getString("email"),
                            rs.getString("password"),
                            Role.valueOf(rs.getString("role"))
                    ),
                    rs.getDate("date").toLocalDate(),
                    new ReservationTime(
                            rs.getLong("time_id"),
                            rs.getTime("start_at").toLocalTime()
                    ),
                    new Theme(
                            rs.getLong("theme_id"),
                            rs.getString("theme_name"),
                            rs.getString("description"),
                            rs.getString("thumbnail")
                    )
            );

    private final JdbcTemplate jdbcTemplate;

    @Override
    public Long save(final Reservation reservation) {
        String sql = "INSERT INTO reservations (member_id, date, time_id, theme_id) VALUES (?, ?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();

        final int rowAffected = jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, new String[]{"id"});
            ps.setLong(1, reservation.getMember().getId());
            ps.setDate(2, Date.valueOf(reservation.getDate()));
            ps.setLong(3, reservation.getTime().getId());
            ps.setLong(4, reservation.getTheme().getId());
            return ps;
        }, keyHolder);

        if (rowAffected != 1) {
            throw new SaveException("예약 정보 저장에 실패했습니다.");
        }

        final Number key = keyHolder.getKey();

        return key.longValue();
    }

    @Override
    public void deleteById(Long id) {
        final String sql = "DELETE FROM reservations WHERE id = ?";
        try {
            jdbcTemplate.update(sql, id);
        } catch (final DataIntegrityViolationException e) {
            throw new DataExistException("데이터 무결성 제약으로 인해 삭제할 수 없습니다." + e);
        }
    }

    @Override
    public boolean existsByDateAndStartAtAndThemeId(final LocalDate date, final LocalTime startAt,
                                                    final Long themeId) {
        final String sql = """
                SELECT EXISTS(
                    SELECT 1
                    FROM reservations AS r
                    INNER JOIN reservation_times AS rt
                    ON r.time_id = rt.id
                    INNER JOIN themes AS th
                    ON r.theme_id = th.id
                    WHERE r.date = ? AND rt.start_at = ? AND th.id = ?
                )
                """;

        return jdbcTemplate.queryForObject(sql, Boolean.class, date, startAt, themeId);
    }

    @Override
    public boolean existsByDateAndTimeIdAndThemeId(final LocalDate date, final Long timeId, final Long themeId) {
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
    public Optional<Reservation> findById(Long id) {
        final String sql = """
                SELECT 
                    r.id AS id,
                    r.date AS date,
                    m.id AS member_id,
                    m.name AS member_name,
                    m.email AS email,
                    m.password AS password,
                    m.role AS role,
                    rt.id AS time_id,
                    rt.start_at AS start_at,
                    th.id AS theme_id,
                    th.name AS theme_name,
                    th.description AS description,
                    th.thumbnail AS thumbnail
                FROM reservations AS r
                INNER JOIN reservation_times AS rt
                ON r.time_id = rt.id
                INNER JOIN themes AS th
                ON r.theme_id = th.id
                INNER JOIN members AS m
                ON r.member_id = m.id
                WHERE r.id = ?
                """;
        final List<Reservation> reservations = jdbcTemplate.query(sql, RESERVATION_ROW_MAPPER, id);
        if (!reservations.isEmpty()) {
            return Optional.of(reservations.getFirst());
        }
        return Optional.empty();
    }

    @Override
    public List<Reservation> findAll() {
        final String sql = """
                SELECT 
                    r.id AS id,
                    r.date AS date,
                    m.id AS member_id,
                    m.name AS member_name,
                    m.email AS email,
                    m.password AS password,
                    m.role AS role,
                    rt.id AS time_id,
                    rt.start_at AS start_at,
                    th.id AS theme_id,
                    th.name AS theme_name,
                    th.description AS description,
                    th.thumbnail AS thumbnail
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
    public List<Reservation> findByInFromTo(final Long themeId, final Long memberId, final LocalDate dateFrom,
                                            final LocalDate dateTo) {
        final String sql = """
                SELECT 
                    r.id AS id,
                    r.date AS date,
                    m.id AS member_id,
                    m.name AS member_name,
                    m.email AS email,
                    m.password AS password,
                    m.role AS role,
                    rt.id AS time_id,
                    rt.start_at AS start_at,
                    th.id AS theme_id,
                    th.name AS theme_name,
                    th.description AS description,
                    th.thumbnail AS thumbnail
                FROM reservations AS r
                INNER JOIN reservation_times AS rt
                ON r.time_id = rt.id
                INNER JOIN themes AS th
                ON r.theme_id = th.id
                INNER JOIN members AS m
                ON r.member_id = m.id
                WHERE ((? IS NULL OR ? IS NULL) OR (r.date BETWEEN ? AND ?))
                AND (th.id = ? OR ? IS NULL)
                AND (m.id = ? OR ? IS NULL)
                """;

        return jdbcTemplate.query(sql, RESERVATION_ROW_MAPPER, dateFrom, dateTo, dateFrom, dateTo, themeId, themeId,
                memberId, memberId);
    }
}
