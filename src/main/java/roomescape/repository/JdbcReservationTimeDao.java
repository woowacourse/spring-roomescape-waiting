//package roomescape.repository;
//
//import org.springframework.dao.DataIntegrityViolationException;
//import org.springframework.dao.DuplicateKeyException;
//import org.springframework.dao.EmptyResultDataAccessException;
//import org.springframework.jdbc.core.JdbcTemplate;
//import org.springframework.jdbc.core.RowMapper;
//import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
//import org.springframework.jdbc.core.namedparam.SqlParameterSource;
//import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
//import org.springframework.stereotype.Repository;
//import roomescape.domain.ReservationTime;
//
//import javax.sql.DataSource;
//import java.time.LocalDate;
//import java.time.LocalTime;
//import java.util.List;
//import java.util.Optional;
//
//@Repository
//public class JdbcReservationTimeDao implements ReservationTimeRepository {
//
//    private static final RowMapper<ReservationTime> rowMapper = ((rs, rowNum) -> {
//        String startAt = rs.getString("start_at");
//        LocalTime reservationStartAt = LocalTime.parse(startAt);
//        ReservationTime reservationTime = new ReservationTime(rs.getLong("id"), reservationStartAt);
//        return reservationTime;
//    });
//
//    private final JdbcTemplate jdbcTemplate;
//    private final SimpleJdbcInsert jdbcInsert;
//
//    public JdbcReservationTimeDao(JdbcTemplate jdbcTemplate, DataSource source) {
//        this.jdbcTemplate = jdbcTemplate;
//        this.jdbcInsert = new SimpleJdbcInsert(source)
//                .withTableName("reservation_time")
//                .usingGeneratedKeyColumns("id");
//    }
//
//    @Override
//    public ReservationTime save(ReservationTime reservationTime) {
//        try {
//            LocalTime startTime = reservationTime.startAt();
//            SqlParameterSource params = new MapSqlParameterSource()
//                    .addValue("start_at", startTime.toString());
//            long id = jdbcInsert.executeAndReturnKey(params).longValue();
//            return new ReservationTime(id, reservationTime.startAt());
//        } catch (DuplicateKeyException e) {
//            throw new IllegalStateException("[ERROR] 이미 등록된 예약 시간 입니다.");
//        }
//    }
//
//    @Override
//    public List<ReservationTime> findAll() {
//        String sql = "select * from reservation_time";
//
//        return jdbcTemplate.query(sql, rowMapper);
//    }
//
//    @Override
//    public Optional<ReservationTime> findById(long id) {
//        String sql = "select * from reservation_time where id = ?";
//        try {
//            return Optional.of(jdbcTemplate.queryForObject(sql, rowMapper, id));
//        } catch (EmptyResultDataAccessException e) {
//            return Optional.empty();
//        }
//    }
//
//    @Override
//    public int deleteById(long id) {
//        String sql = "delete from reservation_time where id = ?";
//        return jdbcTemplate.update(sql, id);
//    }
//}
