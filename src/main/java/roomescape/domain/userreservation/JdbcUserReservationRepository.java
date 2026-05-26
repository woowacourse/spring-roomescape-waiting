package roomescape.domain.userreservation;

import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import roomescape.domain.reservation.Reservation;
import roomescape.domain.reservationdate.ReservationDate;
import roomescape.domain.reservationtime.ReservationTime;
import roomescape.domain.theme.Theme;
import roomescape.domain.user.User;

@Repository
@RequiredArgsConstructor
public class JdbcUserReservationRepository implements UserReservationRepository {

    private static final String COLUMN_ID = "user_reservation_id";
    private static final String COLUMN_USER_ID = "user_id";
    private static final String COLUMN_USER_NAME = "user_name";
    private static final String COLUMN_RESERVATION_ID = "reservation_id";
    private static final String COLUMN_DATE_ID = "date_id";
    private static final String COLUMN_DATE = "date";
    private static final String COLUMN_TIME_ID = "time_id";
    private static final String COLUMN_START_AT = "start_at";
    private static final String COLUMN_THEME_ID = "theme_id";
    private static final String COLUMN_THEME_NAME = "theme_name";
    private static final String COLUMN_THEME_CONTENT = "theme_content";
    private static final String COLUMN_THEME_URL = "theme_url";
    private static final String COLUMN_WAITING_NUMBER = "waiting_number";
    private static final String COLUMN_STATUS = "status";
    private static final String COLUMN_CREATED_AT = "created_at";
    private static final String COLUMN_UPDATED_AT = "updated_at";

    private static final String INSERT_SQL =
        """
            insert into user_reservation(reservation_id, user_id, waiting_number, status, created_at, updated_at)
            values (?, ?, ?, ?, ?, ?)
            """;
    private static final String FIND_ALL_SQL =
        """
            select ur.id as user_reservation_id,
                   ur.waiting_number,
                   ur.status,
                   u.id as user_id,
                   u.name as user_name,
                   r.id as reservation_id,
                   rd.id as date_id,
                   rd.date,
                   rt.id as time_id,
                   rt.start_at,
                   th.id as theme_id,
                   th.name as theme_name,
                   th.content as theme_content,
                   th.url as theme_url,
                   ur.created_at,
                   ur.updated_at
            from user_reservation ur
            join users u on ur.user_id = u.id
            join reservation r on ur.reservation_id = r.id
            join reservation_date rd on r.date_id = rd.id
            join reservation_time rt on r.time_id = rt.id
            join theme th on r.theme_id = th.id
            order by ur.id
            """;
    private static final String FIND_BY_ID_SQL =
        """
            select ur.id as user_reservation_id,
                   ur.waiting_number,
                   ur.status,
                   u.id as user_id,
                   u.name as user_name,
                   r.id as reservation_id,
                   rd.id as date_id,
                   rd.date,
                   rt.id as time_id,
                   rt.start_at,
                   th.id as theme_id,
                   th.name as theme_name,
                   th.content as theme_content,
                   th.url as theme_url,
                   ur.created_at,
                   ur.updated_at
            from user_reservation ur
            join users u on ur.user_id = u.id
            join reservation r on ur.reservation_id = r.id
            join reservation_date rd on r.date_id = rd.id
            join reservation_time rt on r.time_id = rt.id
            join theme th on r.theme_id = th.id
            where ur.id = ?
            """;
    private static final String FIND_BY_USER_ID_SQL =
        """
            select ur.id as user_reservation_id,
                   ur.waiting_number,
                   ur.status,
                   u.id as user_id,
                   u.name as user_name,
                   r.id as reservation_id,
                   rd.id as date_id,
                   rd.date,
                   rt.id as time_id,
                   rt.start_at,
                   th.id as theme_id,
                   th.name as theme_name,
                   th.content as theme_content,
                   th.url as theme_url,
                   ur.created_at,
                   ur.updated_at
            from user_reservation ur
            join users u on ur.user_id = u.id
            join reservation r on ur.reservation_id = r.id
            join reservation_date rd on r.date_id = rd.id
            join reservation_time rt on r.time_id = rt.id
            join theme th on r.theme_id = th.id
            where ur.user_id = ?
            order by ur.id desc
            """;
    private static final String FIND_ALL_BY_RESERVATION_ID_ORDER_SQL =
        """
            select ur.id as user_reservation_id,
                   ur.waiting_number,
                   ur.status,
                   u.id as user_id,
                   u.name as user_name,
                   r.id as reservation_id,
                   rd.id as date_id,
                   rd.date,
                   rt.id as time_id,
                   rt.start_at,
                   th.id as theme_id,
                   th.name as theme_name,
                   th.content as theme_content,
                   th.url as theme_url,
                   ur.created_at,
                   ur.updated_at
            from user_reservation ur
            join users u on ur.user_id = u.id
            join reservation r on ur.reservation_id = r.id
            join reservation_date rd on r.date_id = rd.id
            join reservation_time rt on r.time_id = rt.id
            join theme th on r.theme_id = th.id
            where ur.reservation_id = ?
              and ur.status = 'WAITING'
            order by ur.updated_at, ur.id
            """;
    private static final String COUNT_BY_RESERVATION_ID_SQL =
        """
            select count(*)
            from user_reservation
            where reservation_id = ?
            """;
    private static final String EXISTS_ACTIVE_BY_USER_ID_AND_RESERVATION_ID_SQL =
        """
            select exists(
                select 1
                from user_reservation
                where user_id = ?
                  and reservation_id = ?
                  and status <> 'CANCELED'
            )
            """;
    private static final String UPDATE_SQL =
        """
            update user_reservation
            set reservation_id = ?, user_id = ?, waiting_number = ?, status = ?, created_at = ?, updated_at = ?
            where id = ?
            """;
    private static final String UPDATE_WAITING_NUMBER_SQL =
        """
            update user_reservation
            set waiting_number = ?
            where id = ?
            """;
    private static final String UPDATE_STATUS_SQL =
        """
            update user_reservation
            set status = ?
            where id = ?
            """;

    private final JdbcTemplate jdbcTemplate;

    @Override
    public UserReservation save(UserReservation userReservation) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(INSERT_SQL, new String[]{"id"});
            ps.setLong(1, userReservation.getReservation().getId());
            ps.setLong(2, userReservation.getUser().getId());
            if (userReservation.getWaitingNumber() == null) {
                ps.setObject(3, null);
            } else {
                ps.setLong(3, userReservation.getWaitingNumber());
            }
            ps.setString(4, userReservation.getStatus().name());
            ps.setTimestamp(5, Timestamp.valueOf(userReservation.getCreatedAt()));
            ps.setTimestamp(6, Timestamp.valueOf(userReservation.getUpdatedAt()));
            return ps;
        }, keyHolder);
        long id = extractId(keyHolder);
        return UserReservation.createWithId(id, userReservation);
    }

    @Override
    public List<UserReservation> findAll() {
        return jdbcTemplate.query(FIND_ALL_SQL, userReservationRowMapper());
    }

    @Override
    public Optional<UserReservation> findById(Long id) {
        List<UserReservation> result = jdbcTemplate.query(FIND_BY_ID_SQL, userReservationRowMapper(), id);
        return result.stream().findFirst();
    }

    @Override
    public List<UserReservation> findByUserId(Long userId) {
        return jdbcTemplate.query(FIND_BY_USER_ID_SQL, userReservationRowMapper(), userId);
    }

    @Override
    public Long countByReservationId(Long reservationId) {
        Long count = jdbcTemplate.queryForObject(COUNT_BY_RESERVATION_ID_SQL, Long.class, reservationId);
        if (count == null) {
            return 0L;
        }
        return count;
    }

    @Override
    public List<UserReservation> findAllByReservationIdOrder(Long reservationId) {
        return jdbcTemplate.query(FIND_ALL_BY_RESERVATION_ID_ORDER_SQL, userReservationRowMapper(), reservationId);
    }

    @Override
    public Optional<UserReservation> update(Long id, UserReservation userReservation) {
        int updatedCount = jdbcTemplate.update(
            UPDATE_SQL,
            userReservation.getReservation().getId(),
            userReservation.getUser().getId(),
            userReservation.getWaitingNumber(),
            userReservation.getStatus().name(),
            Timestamp.valueOf(userReservation.getCreatedAt()),
            Timestamp.valueOf(userReservation.getUpdatedAt()),
            id
        );
        if (updatedCount == 0) {
            return Optional.empty();
        }
        return findById(id);
    }

    @Override
    public void updateStatus(Long id, WaitingStatus status) {
        jdbcTemplate.update(UPDATE_STATUS_SQL, status.name(), id);
    }

    @Override
    public boolean existsActiveByUserIdAndReservationId(Long userId, Long reservationId) {
        Boolean exists = jdbcTemplate.queryForObject(
            EXISTS_ACTIVE_BY_USER_ID_AND_RESERVATION_ID_SQL,
            Boolean.class,
            userId,
            reservationId
        );
        return exists != null && exists;
    }

    @Override
    public void updateWaitingNumbers(List<UserReservation> userReservations) {
        AtomicLong waitingNumber = new AtomicLong(0L);
        jdbcTemplate.batchUpdate(
            UPDATE_WAITING_NUMBER_SQL,
            userReservations,
            userReservations.size(),
            (ps, userReservation) -> {
                ps.setLong(1, waitingNumber.getAndIncrement());
                ps.setLong(2, userReservation.getId());
            }
        );
    }

    private RowMapper<UserReservation> userReservationRowMapper() {
        return (rs, rowNum) -> UserReservation.of(
            rs.getLong(COLUMN_ID),
            Reservation.of(
                rs.getLong(COLUMN_RESERVATION_ID),
                ReservationDate.of(
                    rs.getLong(COLUMN_DATE_ID),
                    rs.getDate(COLUMN_DATE).toLocalDate()
                ),
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
            User.of(
                rs.getLong(COLUMN_USER_ID),
                rs.getString(COLUMN_USER_NAME)
            ),
            rs.getObject(COLUMN_WAITING_NUMBER, Long.class),
            WaitingStatus.valueOf(rs.getString(COLUMN_STATUS)),
            rs.getTimestamp(COLUMN_CREATED_AT).toLocalDateTime(),
            rs.getTimestamp(COLUMN_UPDATED_AT).toLocalDateTime()
        );
    }

    private long extractId(KeyHolder keyHolder) {
        if (keyHolder.getKey() == null) {
            throw new IllegalStateException("생성 키를 조회할 수 없습니다.");
        }
        return keyHolder.getKey().longValue();
    }
}
