package roomescape.domain.userreservation;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class JdbcUserReservationRepository implements UserReservationRepository {

    private static final String COLUMN_ID = "id";
    private static final String COLUMN_RESERVATION_ID = "reservation_id";
    private static final String COLUMN_USER_ID = "user_id";
    private static final String COLUMN_WAITING_NUMBER = "waiting_number";
    private static final String COLUMN_STATUS = "status";

    private static final String INSERT_SQL =
        """
            insert into user_reservation(reservation_id, user_id, waiting_number, status)
            values (?, ?, ?, ?)
            """;
    private static final String FIND_ALL_SQL =
        """
            select id, reservation_id, user_id, waiting_number, status
            from user_reservation
            order by id
            """;
    private static final String FIND_BY_ID_SQL =
        """
            select id, reservation_id, user_id, waiting_number, status
            from user_reservation
            where id = ?
            """;
    private static final String FIND_BY_USER_ID_SQL =
        """
            select id, reservation_id, user_id, waiting_number, status
            from user_reservation
            where user_id = ?
            order by id desc
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

    private final JdbcTemplate jdbcTemplate;

    @Override
    public UserReservation save(UserReservation userReservation) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(INSERT_SQL, Statement.RETURN_GENERATED_KEYS);
            ps.setLong(1, userReservation.getReservationId());
            ps.setLong(2, userReservation.getUserId());
            if (userReservation.getWaitingNumber() == null) {
                ps.setObject(3, null);
            } else {
                ps.setLong(3, userReservation.getWaitingNumber());
            }
            ps.setString(4, userReservation.getStatus().name());
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
    public boolean existsActiveByUserIdAndReservationId(Long userId, Long reservationId) {
        Boolean exists = jdbcTemplate.queryForObject(
            EXISTS_ACTIVE_BY_USER_ID_AND_RESERVATION_ID_SQL,
            Boolean.class,
            userId,
            reservationId
        );
        return exists != null && exists;
    }

    private RowMapper<UserReservation> userReservationRowMapper() {
        return (rs, rowNum) -> UserReservation.of(
            rs.getLong(COLUMN_ID),
            rs.getLong(COLUMN_RESERVATION_ID),
            rs.getLong(COLUMN_USER_ID),
            rs.getObject(COLUMN_WAITING_NUMBER, Long.class),
            WaitingStatus.valueOf(rs.getString(COLUMN_STATUS))
        );
    }

    private long extractId(KeyHolder keyHolder) {
        if (keyHolder.getKey() == null) {
            throw new IllegalStateException("생성 키를 조회할 수 없습니다.");
        }
        return keyHolder.getKey().longValue();
    }
}
