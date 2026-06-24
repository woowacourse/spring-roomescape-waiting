package roomescape.infrastructure.repository;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import roomescape.domain.Member;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationRepository;
import roomescape.domain.ReservationTime;
import roomescape.domain.Slot;
import roomescape.domain.Theme;
import roomescape.domain.exception.ConflictException;

@Repository
public class ReservationJdbcRepository implements ReservationRepository {

    private static final String ALREADY_EXISTS_RESERVATION = "해당 날짜와 시간, 테마에 이미 예약이 존재합니다.";

    private static final String SELECT_BASE = """
            SELECT 
                r.id as reservation_id, r.name, r.date,
                t.id as time_id, t.start_at as time_value,
                th.id as theme_id, th.name as theme_name,
                th.description as theme_description,
                th.thumbnail_image_url as theme_thumbnail
            FROM reservation as r
            INNER JOIN reservation_time as t ON r.time_id = t.id
            INNER JOIN theme as th ON r.theme_id = th.id
            """;

    private final JdbcTemplate jdbcTemplate;

    public ReservationJdbcRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<Reservation> reservationRowMapper = (rs, rowNum) -> {
        ReservationTime time = new ReservationTime(
                rs.getLong("time_id"),
                rs.getTime("time_value").toLocalTime()
        );
        Theme theme = new Theme(
                rs.getLong("theme_id"),
                rs.getString("theme_name"),
                rs.getString("theme_description"),
                rs.getString("theme_thumbnail")
        );
        Member reserver = new Member(
                rs.getString("name")
        );
        Slot slot = new Slot(
                rs.getDate("date").toLocalDate(),
                time,
                theme
        );

        return new Reservation(
                rs.getLong("reservation_id"),
                reserver,
                slot
        );
    };

    @Override
    public List<Reservation> findAll(int offset, int limit) {
        String sql = SELECT_BASE + " ORDER BY r.date DESC, time_value ASC LIMIT ? OFFSET ?";
        return jdbcTemplate.query(sql, reservationRowMapper, limit, offset);
    }

    @Override
    public long count() {
        String sql = "SELECT COUNT(*) FROM reservation";
        Long count = jdbcTemplate.queryForObject(sql, Long.class);
        return count != null ? count : 0L;
    }

    @Override
    public boolean existsByTimeId(Long timeId) {
        String sql = "SELECT COUNT(*) FROM reservation WHERE time_id = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, timeId);
        return count != null && count > 0;
    }

    @Override
    public Reservation save(Reservation reservation) {
        String sql = "INSERT INTO reservation (name, date, time_id, theme_id) VALUES (?, ?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();

        try {
            jdbcTemplate.update(connection -> {
                PreparedStatement ps = connection.prepareStatement(sql, new String[]{"id"});
                ps.setString(1, reservation.getReserver().name());
                ps.setDate(2, Date.valueOf(reservation.getSlot().date()));
                ps.setLong(3, reservation.getSlot().time().getId());
                ps.setLong(4, reservation.getSlot().theme().getId());
                return ps;
            }, keyHolder);
        } catch (DuplicateKeyException e) {
            throw new ConflictException(ALREADY_EXISTS_RESERVATION, e);
        }

        long id = keyHolder.getKey().longValue();
        return new Reservation(
                id,
                reservation.getReserver(),
                reservation.getSlot()
        );
    }

    @Override
    public Reservation update(Reservation reservation) {
        String sql = "UPDATE reservation SET date = ?, time_id = ?, theme_id = ? WHERE id = ?";
        try {
            jdbcTemplate.update(
                    sql,
                    Date.valueOf(reservation.getSlot().date()),
                    reservation.getSlot().time().getId(),
                    reservation.getSlot().theme().getId(),
                    reservation.getId()
            );
        } catch (DuplicateKeyException e) {
            throw new ConflictException(ALREADY_EXISTS_RESERVATION, e);
        }
        return reservation;
    }

    @Override
    public void deleteById(Long id) {
        String sql = "DELETE FROM reservation WHERE id = ?";
        jdbcTemplate.update(sql, id);
    }

    @Override
    public Optional<Reservation> findById(Long id) {
        return findById(id, "");
    }

    @Override
    public Optional<Reservation> findByIdForUpdate(Long id) {
        return findById(id, " FOR UPDATE");
    }

    private Optional<Reservation> findById(Long id, String lockClause) {
        String sql = SELECT_BASE + " WHERE r.id = ?" + lockClause;
        List<Reservation> results = jdbcTemplate.query(sql, reservationRowMapper, id);
        return results.stream().findFirst();
    }

    @Override
    public List<Long> findReservedTimeIdsByDateAndTheme(LocalDate date, Theme theme) {
        String sql = "SELECT time_id FROM reservation WHERE date = ? AND theme_id = ?";
        return jdbcTemplate.queryForList(sql, Long.class, date, theme.getId());
    }

    @Override
    public List<Reservation> findByMember(Member member) {
        String sql = SELECT_BASE + " WHERE r.name = ? ORDER BY r.date DESC, time_value ASC";
        return jdbcTemplate.query(sql, reservationRowMapper, member.name());
    }

    @Override
    public Optional<Reservation> findBySlot(Slot slot) {
        String sql = SELECT_BASE + " WHERE r.date = ? AND r.time_id = ? AND r.theme_id = ?";
        List<Reservation> results = jdbcTemplate.query(
                sql,
                reservationRowMapper,
                slot.date(),
                slot.time().getId(),
                slot.theme().getId()
        );
        return results.stream().findFirst();
    }
}
