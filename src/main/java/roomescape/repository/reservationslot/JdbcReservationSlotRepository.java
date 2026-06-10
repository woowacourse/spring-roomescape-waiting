package roomescape.repository.reservationslot;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import roomescape.domain.reservationslot.ReservationSlot;
import roomescape.domain.reservationtime.ReservationTime;
import roomescape.domain.theme.Theme;
import roomescape.repository.PersistenceConflictException;

@Repository
public class JdbcReservationSlotRepository implements ReservationSlotRepository {

    private static final String RESERVATION_SLOT_BASE_SELECT = """
            SELECT  rs.id,
                    rs.date,
                    rs.time_id,
                    rt.start_at,
                    rs.theme_id,
                    t.name AS theme_name,
                    t.description,
                    t.thumbnail_url
            FROM    reservation_slot AS rs
            INNER JOIN reservation_time AS rt ON rs.time_id = rt.id
            INNER JOIN theme AS t ON rs.theme_id = t.id
            """;

    private static final RowMapper<ReservationSlot> reservationSlotRowMapper = (resultSet, rowNum) -> {
        Theme theme = Theme.of(
                resultSet.getLong("theme_id"),
                resultSet.getString("theme_name"),
                resultSet.getString("description"),
                resultSet.getString("thumbnail_url")
        );

        ReservationTime time = ReservationTime.of(
                resultSet.getLong("time_id"),
                resultSet.getTime("start_at").toLocalTime()
        );

        return new ReservationSlot(
                resultSet.getLong("id"),
                resultSet.getDate("date").toLocalDate(),
                theme,
                time
            );
    };

    private final JdbcTemplate jdbcTemplate;

    public JdbcReservationSlotRepository(final JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public List<ReservationSlot> findAll() {
        return jdbcTemplate.query(RESERVATION_SLOT_BASE_SELECT, reservationSlotRowMapper);
    }

    @Override
    public Optional<ReservationSlot> findById(final long slotId) {
        String sql = RESERVATION_SLOT_BASE_SELECT + " WHERE rs.id = ?";
        return jdbcTemplate.query(sql, reservationSlotRowMapper, slotId)
                .stream()
                .findFirst();
    }

    @Override
    public Optional<ReservationSlot> findBySlot(ReservationSlot reservationSlot) {
        String sql = RESERVATION_SLOT_BASE_SELECT + " WHERE rs.date = ? AND rs.time_id = ? AND rs.theme_id = ? ";
        return jdbcTemplate.query(sql,
                reservationSlotRowMapper,
                Date.valueOf(reservationSlot.getDate()),
                reservationSlot.getTime().getId(),
                reservationSlot.getTheme().getId())
                .stream()
                .findFirst();
    }

    @Override
    public List<ReservationSlot> findByDateAndTheme(final LocalDate date, final Theme theme) {
        String sql = RESERVATION_SLOT_BASE_SELECT + " WHERE rs.date = ? AND rs.theme_id = ? ";

        return jdbcTemplate.query(
                sql,
                reservationSlotRowMapper,
                Date.valueOf(date),
                theme.getId()
        );
    }

    @Override
    public ReservationSlot save(final ReservationSlot reservationSlot) {
        String sql = "INSERT INTO reservation_slot (date, theme_id, time_id) VALUES (?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();

        try {
            jdbcTemplate.update(connection -> {
                PreparedStatement preparedStatement = connection.prepareStatement(sql, new String[]{"id"});
                preparedStatement.setDate(1, Date.valueOf(reservationSlot.getDate()));
                preparedStatement.setLong(2, reservationSlot.getTheme().getId());
                preparedStatement.setLong(3, reservationSlot.getTime().getId());
                return preparedStatement;
            }, keyHolder);
        } catch (DataIntegrityViolationException exception) {
            throw new PersistenceConflictException(exception);
        }

        Number key = keyHolder.getKey();
        if (key == null) {
            throw new IllegalStateException("[ERROR] 예약 슬롯 ID를 생성하지 못했습니다.");
        }

        return new ReservationSlot(
                key.longValue(),
                reservationSlot.getDate(),
                reservationSlot.getTheme(),
                reservationSlot.getTime()
        );
    }
}
