package roomescape.infra.reservation;

import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import roomescape.domain.reservation.Reservation;
import roomescape.domain.reservation.ReservationRepository;
import roomescape.domain.reservation.ReservationSlot;
import roomescape.domain.reservation.ReservationStatus;
import roomescape.domain.reservation.ReservationTime;
import roomescape.domain.theme.Theme;
import roomescape.domain.user.User;

@Repository
public class JdbcReservationRepository implements ReservationRepository {

    private static final String COLUMN_ID = "user_reservation_id";
    private static final String COLUMN_USER_ID = "user_id";
    private static final String COLUMN_USER_NAME = "user_name";
    private static final String COLUMN_RESERVATION_SLOT_ID = "reservation_slot_id";
    private static final String COLUMN_DATE = "date";
    private static final String COLUMN_TIME_ID = "time_id";
    private static final String COLUMN_START_AT = "start_at";
    private static final String COLUMN_THEME_ID = "theme_id";
    private static final String COLUMN_THEME_NAME = "theme_name";
    private static final String COLUMN_THEME_CONTENT = "theme_content";
    private static final String COLUMN_THEME_URL = "theme_url";
    private static final String COLUMN_WAITING_NUMBER = "waiting_number";
    private static final String COLUMN_STATUS = "status";
    private static final String COLUMN_RESERVED_AT = "reserved_at";

    private static final String FIND_ALL_SQL = """
        select r.id as user_reservation_id,
               r.waiting_number,
               r.status,
               u.id as user_id,
               u.name as user_name,
               rs.id as reservation_slot_id,
               rs.date,
               rt.id as time_id,
               rt.start_at,
               th.id as theme_id,
               th.name as theme_name,
               th.content as theme_content,
               th.url as theme_url,
               r.reserved_at
        from reservation r
        join users u on r.user_id = u.id
        join reservation_slot rs on r.reservation_slot_id = rs.id
        join reservation_time rt on rs.time_id = rt.id
        join theme th on rs.theme_id = th.id
        order by rs.date desc, rt.start_at desc, r.id;
        """;

    private static final String FIND_BY_ID_SQL = """
        select r.id as user_reservation_id,
               r.waiting_number,
               r.status,
               u.id as user_id,
               u.name as user_name,
               rs.id as reservation_slot_id,
               rs.date,
               rt.id as time_id,
               rt.start_at,
               th.id as theme_id,
               th.name as theme_name,
               th.content as theme_content,
               th.url as theme_url,
               r.reserved_at
        from reservation r
        join users u on r.user_id = u.id
        join reservation_slot rs on r.reservation_slot_id = rs.id
        join reservation_time rt on rs.time_id = rt.id
        join theme th on rs.theme_id = th.id
        where r.id = :id
        """;

    private static final String FIND_ALL_BY_USER_ID_SQL = """
        select r.id as user_reservation_id,
               r.waiting_number,
               r.status,
               u.id as user_id,
               u.name as user_name,
               rs.id as reservation_slot_id,
               rs.date,
               rt.id as time_id,
               rt.start_at,
               th.id as theme_id,
               th.name as theme_name,
               th.content as theme_content,
               th.url as theme_url,
               r.reserved_at
        from reservation r
        join users u on r.user_id = u.id
        join reservation_slot rs on r.reservation_slot_id = rs.id
        join reservation_time rt on rs.time_id = rt.id
        join theme th on rs.theme_id = th.id
        where u.id = :userId
        order by rs.date desc, rt.start_at desc, r.id;
        """;

    private static final String FIND_ALL_BY_SLOT_ID_ORDER_SQL = """
        select r.id as user_reservation_id,
               r.waiting_number,
               r.status,
               u.id as user_id,
               u.name as user_name,
               rs.id as reservation_slot_id,
               rs.date,
               rt.id as time_id,
               rt.start_at,
               th.id as theme_id,
               th.name as theme_name,
               th.content as theme_content,
               th.url as theme_url,
               r.reserved_at
        from reservation r
        join users u on r.user_id = u.id
        join reservation_slot rs on r.reservation_slot_id = rs.id
        join reservation_time rt on rs.time_id = rt.id
        join theme th on rs.theme_id = th.id
        where r.reservation_slot_id = :slotId
        order by r.reserved_at, r.id
        """;

    private static final String EXISTS_BY_SLOT_ID_AND_USER_ID_SQL = """
        select exists(
            select 1
            from reservation
            where reservation_slot_id = :slotId
              and user_id = :userId
        )
        """;
    private static final String UPDATE_SQL = """
        update reservation
        set reservation_slot_id = :slotId,
            user_id = :userId,
            waiting_number = :waitingNumber,
            status = :status,
            reserved_at = :reservedAt
        where id = :id
        """;
    private static final String DELETE_BY_ID_SQL = "delete from reservation where id = :id";
    private static final RowMapper<Reservation> RESERVATION_ROW_MAPPER = (rs, rowNum) -> Reservation.of(
            rs.getLong(COLUMN_ID),
            User.of(
                    rs.getLong(COLUMN_USER_ID),
                    rs.getString(COLUMN_USER_NAME)
            ),
            ReservationSlot.of(
                    rs.getLong(COLUMN_RESERVATION_SLOT_ID),
                    rs.getDate(COLUMN_DATE).toLocalDate(),
                    ReservationTime.of(
                            rs.getLong(COLUMN_TIME_ID),
                            rs.getTime(COLUMN_START_AT).toLocalTime()
                    ),
                    Theme.of(
                            rs.getLong(COLUMN_THEME_ID),
                            rs.getString(COLUMN_THEME_NAME),
                            rs.getString(COLUMN_THEME_CONTENT),
                            rs.getString(COLUMN_THEME_URL)
                    )
            ),
            rs.getObject(COLUMN_WAITING_NUMBER, Integer.class),
            ReservationStatus.valueOf(rs.getString(COLUMN_STATUS)),
            rs.getTimestamp(COLUMN_RESERVED_AT).toLocalDateTime()
    );

    private final NamedParameterJdbcTemplate jdbcTemplate;
    private final SimpleJdbcInsert simpleJdbcInsert;

    public JdbcReservationRepository(NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        this.simpleJdbcInsert = new SimpleJdbcInsert(jdbcTemplate.getJdbcTemplate())
                .withTableName("reservation")
                .usingGeneratedKeyColumns(COLUMN_ID);
    }

    @Override
    public Reservation save(Reservation reservation) {
        Number key = simpleJdbcInsert.executeAndReturnKey(new MapSqlParameterSource()
                .addValue("reservation_slot_id", reservation.getSlot().getId())
                .addValue("user_id", reservation.getUser().getId())
                .addValue("waiting_number", reservation.getWaitingNumber())
                .addValue("status", reservation.getStatus().name())
                .addValue("reserved_at", Timestamp.valueOf(reservation.getReservedAt())));

        return Reservation.of(
                extractId(key),
                reservation.getUser(),
                reservation.getSlot(),
                reservation.getWaitingNumber(),
                reservation.getStatus(),
                reservation.getReservedAt()
        );
    }

    @Override
    public List<Reservation> findAll() {
        return jdbcTemplate.query(FIND_ALL_SQL, new MapSqlParameterSource(), RESERVATION_ROW_MAPPER);
    }

    @Override
    public Optional<Reservation> findById(Long id) {
        List<Reservation> result = jdbcTemplate.query(
                FIND_BY_ID_SQL,
                new MapSqlParameterSource().addValue("id", id),
                RESERVATION_ROW_MAPPER
        );
        return result.stream().findFirst();
    }

    @Override
    public List<Reservation> findAllReservationsByUserId(Long userId) {
        return jdbcTemplate.query(
                FIND_ALL_BY_USER_ID_SQL,
                new MapSqlParameterSource().addValue("userId", userId),
                RESERVATION_ROW_MAPPER
        );
    }

    public List<Reservation> findAllBySlotIdOrderByReservedAt(Long slotId) {
        return jdbcTemplate.query(
                FIND_ALL_BY_SLOT_ID_ORDER_SQL,
                new MapSqlParameterSource().addValue("slotId", slotId),
                RESERVATION_ROW_MAPPER
        );
    }

    @Override
    public boolean existsBySlotIdAndUserId(Long slotId, Long userId) {
        Boolean exists = jdbcTemplate.queryForObject(
                EXISTS_BY_SLOT_ID_AND_USER_ID_SQL,
                new MapSqlParameterSource()
                        .addValue("slotId", slotId)
                        .addValue("userId", userId),
                Boolean.class
        );
        return exists != null && exists;
    }

    @Override
    public int deleteById(Long id) {
        return jdbcTemplate.update(DELETE_BY_ID_SQL, new MapSqlParameterSource().addValue("id", id));
    }

    @Override
    public void batchUpdate(List<Reservation> reservations) {
        if (reservations.isEmpty()) {
            return;
        }

        SqlParameterSource[] batch = reservations.stream()
                .map(reservation -> new MapSqlParameterSource()
                        .addValue("id", reservation.getId())
                        .addValue("slotId", reservation.getSlot().getId())
                        .addValue("userId", reservation.getUser().getId())
                        .addValue("waitingNumber", reservation.getWaitingNumber())
                        .addValue("status", reservation.getStatus().name())
                        .addValue("reservedAt", Timestamp.valueOf(reservation.getReservedAt())))
                .toArray(SqlParameterSource[]::new);

        jdbcTemplate.batchUpdate(UPDATE_SQL, batch);
    }

    private long extractId(Number key) {
        if (key == null) {
            throw new IllegalStateException("생성 키를 조회할 수 없습니다.");
        }
        return key.longValue();
    }
}
