package roomescape.domain.reservation;

import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import roomescape.domain.reservation.dto.ReservationCountResult;
import roomescape.domain.reservation.dto.ReservationWithWaitingNumber;
import roomescape.domain.reservationdate.ReservationDate;
import roomescape.domain.reservationslot.ReservationSlot;
import roomescape.domain.reservationtime.ReservationTime;
import roomescape.domain.theme.Theme;
import roomescape.domain.user.User;

@Repository
@RequiredArgsConstructor
public class JdbcReservationRepository implements ReservationRepository {

    private static final String COLUMN_ID = "user_reservation_id";
    private static final String COLUMN_USER_ID = "user_id";
    private static final String COLUMN_USER_NAME = "user_name";
    private static final String COLUMN_RESERVATION_SLOT_ID = "reservation_slot_id";
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
            insert into reservation(reservation_slot_id, user_id, status, created_at, updated_at)
            values (?, ?, ?, ?, ?)
            """;
    private static final String FIND_ALL_WITH_ORDER_AND_STATUS_SQL =
        """
            with ranked_reservation as (
                select r.*,
                       row_number() over (
                           partition by r.reservation_slot_id, r.status
                           order by r.updated_at, r.id
                       ) as waiting_order
                from reservation r
                where r.status <> 'CANCELED'
            )
            select r.id as user_reservation_id,
                   case when r.status = 'WAITING'
                        then r.waiting_order
                        else null
                   end as waiting_number,
                   r.status,
                   u.id as user_id,
                   u.name as user_name,
                   rs.id as reservation_slot_id,
                   rd.id as date_id,
                   rd.date,
                   rt.id as time_id,
                   rt.start_at,
                   th.id as theme_id,
                   th.name as theme_name,
                   th.content as theme_content,
                   th.url as theme_url,
                   r.created_at,
                   r.updated_at
            from ranked_reservation r
            join users u on r.user_id = u.id
            join reservation_slot rs on r.reservation_slot_id = rs.id
            join reservation_date rd on rs.date_id = rd.id
            join reservation_time rt on rs.time_id = rt.id
            join theme th on rs.theme_id = th.id
            order by rd.date desc, rt.start_at desc, r.id;
            """;
    private static final String FIND_BY_ID_SQL =
        """
            select r.id as user_reservation_id,
                   r.status,
                   u.id as user_id,
                   u.name as user_name,
                   rs.id as reservation_slot_id,
                   rd.id as date_id,
                   rd.date,
                   rt.id as time_id,
                   rt.start_at,
                   th.id as theme_id,
                   th.name as theme_name,
                   th.content as theme_content,
                   th.url as theme_url,
                   r.created_at,
                   r.updated_at
            from reservation r
            join users u on r.user_id = u.id
            join reservation_slot rs on r.reservation_slot_id = rs.id
            join reservation_date rd on rs.date_id = rd.id
            join reservation_time rt on rs.time_id = rt.id
            join theme th on rs.theme_id = th.id
            where r.id = ?
              and r.status <> 'CANCELED'
            """;
    private static final String FIND_ALL_BY_USERNAME_SQL =
        """
            with ranked_reservation as (
                select r.*,
                       row_number() over (
                           partition by r.reservation_slot_id, r.status
                           order by r.updated_at, r.id
                       ) as waiting_order
                from reservation r
                where r.status <> 'CANCELED'
            )
            select r.id as user_reservation_id,
                   case when r.status = 'WAITING'
                        then r.waiting_order
                        else null
                   end as waiting_number,
                   r.status,
                   u.id as user_id,
                   u.name as user_name,
                   rs.id as reservation_slot_id,
                   rd.id as date_id,
                   rd.date,
                   rt.id as time_id,
                   rt.start_at,
                   th.id as theme_id,
                   th.name as theme_name,
                   th.content as theme_content,
                   th.url as theme_url,
                   r.created_at,
                   r.updated_at
            from ranked_reservation r
            join users u on r.user_id = u.id
            join reservation_slot rs on r.reservation_slot_id = rs.id
            join reservation_date rd on rs.date_id = rd.id
            join reservation_time rt on rs.time_id = rt.id
            join theme th on rs.theme_id = th.id
            where u.name = ?
            order by rd.date desc, rt.start_at desc, r.id;
            """;
    private static final String FIND_ALL_BY_RESERVATION_ID_ORDER_SQL =
        """
            select r.id as user_reservation_id,
                   r.status,
                   u.id as user_id,
                   u.name as user_name,
                   rs.id as reservation_slot_id,
                   rd.id as date_id,
                   rd.date,
                   rt.id as time_id,
                   rt.start_at,
                   th.id as theme_id,
                   th.name as theme_name,
                   th.content as theme_content,
                   th.url as theme_url,
                   r.created_at,
                   r.updated_at
            from reservation r
            join users u on r.user_id = u.id
            join reservation_slot rs on r.reservation_slot_id = rs.id
            join reservation_date rd on rs.date_id = rd.id
            join reservation_time rt on rs.time_id = rt.id
            join theme th on rs.theme_id = th.id
            where r.reservation_slot_id = ?
              and r.status <> 'CANCELED'
            order by r.updated_at, r.id
            """;
    private static final String COUNT_BY_RESERVATION_SLOT_ID_SQL =
        """
            select count(*)
            from reservation
            where reservation_slot_id = ?
              and status <> 'CANCELED'
            """;
    private static final String EXISTS_ACTIVE_BY_USER_ID_AND_RESERVATION_ID_SQL =
        """
            select exists(
                select 1
                from reservation
                where user_id = ?
                  and reservation_slot_id = ?
                  and status <> 'CANCELED'
            )
            """;
    private static final String UPDATE_SQL =
        """
            update reservation
            set reservation_slot_id = ?, user_id = ?, status = ?, created_at = ?, updated_at = ?
            where id = ?
            """;
    private static final String DELETE_BY_ID_SQL = "delete from reservation where id = ?";
    private static final String UPDATE_STATUS_SQL = """
        update reservation
        set status = ?
        where id = ?
        """;
    private static final String COUNT_RESERVATION_BY_THEME_AND_DATE =
        """
            select rt.id as time_id,
            rt.start_at,
            count(r.id) as reservation_count
            from reservation_time rt
            left join reservation_slot rs
            on rs.time_id = rt.id
            and rs.date_id = ?
            and rs.theme_id = ?
            left join reservation r
            on r.reservation_slot_id = rs.id
            and r.status <> 'CANCELED'
            group by rt.id, rt.start_at
            order by rt.start_at;
            """;

    private final JdbcTemplate jdbcTemplate;

    @Override
    public Reservation save(Reservation userReservation) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(INSERT_SQL, new String[]{"id"});
            ps.setLong(1, userReservation.getReservationSlot().getId());
            ps.setLong(2, userReservation.getUser().getId());
            ps.setString(3, userReservation.getStatus().name());
            ps.setTimestamp(4, Timestamp.valueOf(userReservation.getCreatedAt()));
            ps.setTimestamp(5, Timestamp.valueOf(userReservation.getUpdatedAt()));
            return ps;
        }, keyHolder);
        long id = extractId(keyHolder);
        return Reservation.createWithId(id, userReservation);
    }

    @Override
    public List<ReservationWithWaitingNumber> findAll() {
        return jdbcTemplate.query(FIND_ALL_WITH_ORDER_AND_STATUS_SQL, reservationWithWaitingNumberRowMapper());
    }

    @Override
    public Optional<Reservation> findById(Long id) {
        List<Reservation> result = jdbcTemplate.query(FIND_BY_ID_SQL, userReservationRowMapper(), id);
        return result.stream().findFirst();
    }

    @Override
    public List<ReservationWithWaitingNumber> findReservations(String username) {
        return jdbcTemplate.query(FIND_ALL_BY_USERNAME_SQL, reservationWithWaitingNumberRowMapper(), username);
    }

    @Override
    public Long countByReservationSlotId(Long reservationSlotId) {
        Long count = jdbcTemplate.queryForObject(COUNT_BY_RESERVATION_SLOT_ID_SQL, Long.class, reservationSlotId);
        if (count == null) {
            return 0L;
        }
        return count;
    }

    @Override
    public List<Reservation> findAllByReservationIdOrder(Long reservationId) {
        return jdbcTemplate.query(FIND_ALL_BY_RESERVATION_ID_ORDER_SQL, userReservationRowMapper(), reservationId);
    }

    @Override
    public void update(Long id, Reservation updatedReservation) {
        jdbcTemplate.update(
            UPDATE_SQL,
            updatedReservation.getReservationSlot().getId(),
            updatedReservation.getUser().getId(),
            updatedReservation.getStatus().name(),
            Timestamp.valueOf(updatedReservation.getCreatedAt()),
            Timestamp.valueOf(updatedReservation.getUpdatedAt()),
            id
        );
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
    public void deleteById(Long id) {
        jdbcTemplate.update(DELETE_BY_ID_SQL, id);
    }

    @Override
    public void updateStatus(ReservationStatus changeStatus, Long id) {
        jdbcTemplate.update(UPDATE_STATUS_SQL, changeStatus.name(), id);
    }

    @Override
    public List<ReservationCountResult> countReservation(Long themeId, Long dateId) {
        return jdbcTemplate.query(
            COUNT_RESERVATION_BY_THEME_AND_DATE,
            reservationCountResultRowMapper(),
            dateId,
            themeId
        );
    }

    private RowMapper<ReservationCountResult> reservationCountResultRowMapper() {
        return (rs, rowNum) -> ReservationCountResult.of(
            rs.getLong(COLUMN_TIME_ID),
            rs.getTime(COLUMN_START_AT).toLocalTime(),
            rs.getLong("reservation_count")
        );
    }

    private RowMapper<Reservation> userReservationRowMapper() {
        return (rs, rowNum) -> Reservation.of(
            rs.getLong(COLUMN_ID),
            ReservationSlot.of(
                rs.getLong(COLUMN_RESERVATION_SLOT_ID),
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
            ReservationStatus.valueOf(rs.getString(COLUMN_STATUS)),
            rs.getTimestamp(COLUMN_CREATED_AT).toLocalDateTime(),
            rs.getTimestamp(COLUMN_UPDATED_AT).toLocalDateTime()
        );
    }

    private RowMapper<ReservationWithWaitingNumber> reservationWithWaitingNumberRowMapper() {
        return (rs, rowNum) -> new ReservationWithWaitingNumber(
            userReservationRowMapper().mapRow(rs, rowNum),
            rs.getObject(COLUMN_WAITING_NUMBER, Long.class)
        );
    }

    private long extractId(KeyHolder keyHolder) {
        if (keyHolder.getKey() == null) {
            throw new IllegalStateException("생성 키를 조회할 수 없습니다.");
        }
        return keyHolder.getKey().longValue();
    }
}
