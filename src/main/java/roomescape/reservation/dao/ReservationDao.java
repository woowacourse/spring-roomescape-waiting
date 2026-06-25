package roomescape.reservation.dao;

import java.sql.PreparedStatement;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;
import roomescape.payment.domain.PaymentStatus;
import roomescape.reservation.dao.dto.ReservationWithRank;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationStatus;
import roomescape.reservation.domain.ReservationTime;
import roomescape.theme.domain.Theme;

@Component
public class ReservationDao {

  private JdbcTemplate jdbcTemplate;

  public ReservationDao(JdbcTemplate jdbcTemplate) {
    this.jdbcTemplate = jdbcTemplate;
  }

  public Reservation insert(String name, LocalDate date, Long timeId, Long themeId,
      ReservationStatus status) {
    KeyHolder keyHolder = new GeneratedKeyHolder();

    String sql = "insert into reservation (name, date, time_id, theme_id, status) values (?, ?, ?, ?, ?)";
    jdbcTemplate.update(connection -> {
      PreparedStatement ps = connection.prepareStatement(sql, new String[]{"id"});
      ps.setString(1, name);
      ps.setObject(2, date);
      ps.setLong(3, timeId);
      ps.setLong(4, themeId);
      ps.setString(5, status.name());
      return ps;
    }, keyHolder);

    ReservationTime time = jdbcTemplate.queryForObject(
        "select id, start_at from reservation_time where id = ?",
        (resultSet, rowNum) -> ReservationTime.of(resultSet.getLong("id"),
            LocalTime.parse(resultSet.getString("start_at"))),
        timeId
    );

    Theme theme = jdbcTemplate.queryForObject(
        "select * from theme where id = ?",
        (resultSet, rowNum) -> Theme.of(resultSet.getLong("id"),
            resultSet.getString("name"), resultSet.getString("description"),
            resultSet.getString("image_url")),
        themeId
    );

    return Reservation.of(keyHolder.getKey().longValue(), name, date, time, theme, status);
  }

  private final RowMapper<Reservation> reservationRowMapper = (resultSet, rowNum) -> {
    ReservationTime time = ReservationTime.of(
        resultSet.getLong("time_id"),
        LocalTime.parse(resultSet.getString("start_at"))
    );
    Theme theme = Theme.of(
        resultSet.getLong("theme_id"),
        resultSet.getString("theme_name"),
        resultSet.getString("theme_description"),
        resultSet.getString("theme_image_url")
    );
    return Reservation.of(
        resultSet.getLong("id"),
        resultSet.getString("name"),
        LocalDate.parse(resultSet.getString("date")),
        time,
        theme,
        ReservationStatus.valueOf(resultSet.getString("status"))
    );
  };

  public List<Reservation> findAll() {
    String sql = "select r.id, r.name, r.date, r.status, "
        + "t.id as time_id, t.start_at, "
        + "th.id as theme_id, th.name as theme_name, "
        + "th.description as theme_description, th.image_url as theme_image_url "
        + "from reservation r "
        + "inner join reservation_time t on r.time_id = t.id "
        + "inner join theme th on r.theme_id = th.id "
        + "where r.status in ('RESERVED', 'WAITING')";

    return jdbcTemplate.query(sql, reservationRowMapper);
  }

  public void delete(Long id) {
    String sql = "update reservation set status = ? where id = ?";
    jdbcTemplate.update(sql, ReservationStatus.CANCELED.name(), id);
  }

  public boolean existsByTimeId(Long timeId) {
    String sql = "select count(*) from reservation where time_id = ?";
    Integer count = jdbcTemplate.queryForObject(sql, Integer.class, timeId);

    return count != null && count > 0;
  }

  public Reservation findById(Long id) {
    jdbcTemplate.queryForObject(
        "select id from reservation where id = ? for update",
        Long.class, id);

    String sql = "select r.id, r.name, r.date, r.status, "
        + "t.id as time_id, t.start_at, "
        + "th.id as theme_id, th.name as theme_name, "
        + "th.description as theme_description, th.image_url as theme_image_url "
        + "from reservation r "
        + "inner join reservation_time t on r.time_id = t.id "
        + "inner join theme th on r.theme_id = th.id "
        + "where r.id = ?";

    return jdbcTemplate.queryForObject(sql, reservationRowMapper, id);
  }

  public Optional<Reservation> findFirstWaitingByDateTimeTheme(LocalDate date, Long timeId, Long themeId) {
    String sql = "select r.id, r.name, r.date, r.status, "
        + "t.id as time_id, t.start_at, "
        + "th.id as theme_id, th.name as theme_name, "
        + "th.description as theme_description, th.image_url as theme_image_url "
        + "from reservation r "
        + "inner join reservation_time t on r.time_id = t.id "
        + "inner join theme th on r.theme_id = th.id "
        + "where r.date = ? and r.time_id = ? and r.theme_id = ? and r.status = ? "
        + "order by r.id asc limit 1 for update";

    List<Reservation> results = jdbcTemplate.query(sql, reservationRowMapper, date, timeId, themeId, ReservationStatus.WAITING.name());
    return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
  }

  public void updateStatus(Long id, ReservationStatus status) {
    String sql = "update reservation set status = ? where id = ?";
    jdbcTemplate.update(sql, status.name(), id);
  }

  public boolean findByDateTimeThemeStatus(String date, Long timeId, Long themeId) {
    String sql = "select count(*) from reservation where date = ? and time_id = ? and theme_id = ? and status = ?";
    Integer count = jdbcTemplate.queryForObject(sql, Integer.class, date, timeId, themeId, ReservationStatus.RESERVED.name());

    return count != null && count > 0;
  }

  public boolean findByNameAndDateAndTimeAndTheme(String name, String date, Long timeId, Long themeId) {
    String sql = "select count(*) from reservation where name = ? and date = ? and time_id = ? and theme_id = ? and status in ('RESERVED', 'WAITING')";
    Integer count = jdbcTemplate.queryForObject(sql, Integer.class, name, date, timeId, themeId);

    return count != null && count > 0;
  }

  public boolean existsByNameAndReservationId(String name, Long reservationId) {
    String sql = "select count(*) from reservation where name = ? and id = ?";
    Integer count = jdbcTemplate.queryForObject(sql, Integer.class, name, reservationId);

    return count != null && count > 0;
  }

  public void deleteByNameAndReservationId(String name, Long reservationId) {
    String sql = "delete from reservation where name = ? and id = ?";
    jdbcTemplate.update(sql, name, reservationId);
  }

  public void updateReservation(LocalDate date, Long timeId, String name, Long reservationId) {
    String sql = "update reservation set date = ?, time_id = ? "
        + "where name = ? and id = ?";

    jdbcTemplate.update(sql, date, timeId, name, reservationId);
  }

  public boolean existsByTimeIdAndThemeId(LocalDate date, Long timeId, Long themeId) {
    String sql = "select count(*) from reservation where date = ? and time_id = ? and theme_id = ?";
    Integer count = jdbcTemplate.queryForObject(sql, Integer.class, date, timeId, themeId);

    return count != null && count > 0;
  }

  public List<ReservationWithRank> findAllByName(String name) {
    String sql = "WITH waiting_rank AS ( "
        + "  SELECT id, RANK() OVER (PARTITION BY date, time_id, theme_id ORDER BY id) AS wait_rank "
        + "  FROM reservation WHERE status = 'WAITING' "
        + ") "
        + "SELECT r.id, r.name, r.date, r.status, "
        + "       t.id AS time_id, t.start_at, "
        + "       th.id AS theme_id, th.name AS theme_name, "
        + "       wr.wait_rank, "
        + "       o.order_id, "
        + "       p.payment_key, p.amount AS payment_amount, p.status AS payment_status "
        + "FROM reservation r "
        + "INNER JOIN reservation_time t ON r.time_id = t.id "
        + "INNER JOIN theme th           ON r.theme_id = th.id "
        + "LEFT  JOIN waiting_rank wr    ON r.id = wr.id "
        + "LEFT  JOIN orders o           ON r.id = o.reservation_id "
        + "LEFT  JOIN payment p          ON r.id = p.reservation_id "
        + "WHERE r.name = ?";

    return jdbcTemplate.query(sql, (rs, rowNum) -> new ReservationWithRank(
        rs.getLong("id"),
        rs.getString("name"),
        LocalDate.parse(rs.getString("date")),
        rs.getLong("time_id"),
        LocalTime.parse(rs.getString("start_at")),
        rs.getLong("theme_id"),
        rs.getString("theme_name"),
        ReservationStatus.valueOf(rs.getString("status")),
        rs.getObject("wait_rank", Long.class),
        rs.getString("order_id"),
        rs.getString("payment_key"),
        rs.getObject("payment_amount", Long.class),
        rs.getString("payment_status") != null
            ? PaymentStatus.from(rs.getString("payment_status"))
            : null
    ), name);
  }
}
