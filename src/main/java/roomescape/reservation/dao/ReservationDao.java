package roomescape.reservation.dao;

import java.sql.PreparedStatement;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationStatus;
import roomescape.reservation.domain.ReservationTime;
import roomescape.reservation.dto.response.MyReservationResponse;
import roomescape.reservation.dto.response.ThemeSimpleResponse;
import roomescape.reservation.dto.response.TimeResponse;
import roomescape.theme.domain.Theme;

@Repository
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

  public List<Reservation> findAll() {
    String sql = "select r.id, r.name, r.date, r.status, "
        + "t.id as time_id, t.start_at, "
        + "th.id as theme_id, th.name as theme_name, "
        + "th.description as theme_description, th.image_url as theme_image_url "
        + "from reservation r "
        + "inner join reservation_time t on r.time_id = t.id "
        + "inner join theme th on r.theme_id = th.id";

    RowMapper<Reservation> rowMapper = (resultSet, rowNum) -> {
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

    return jdbcTemplate.query(sql, rowMapper);
  }

  public void delete(Long id) {
    String sql = "delete from reservation where id = ?";
    jdbcTemplate.update(sql, id);
  }

  public void deleteByIdAndName(Long id, String name) {
    String sql = "delete from reservation where id = ? and name = ?";
    jdbcTemplate.update(sql, id, name);
  }

  public boolean existsByTimeId(Long timeId) {
    String sql = "select count(*) from reservation where time_id = ?";
    Integer count = jdbcTemplate.queryForObject(sql, Integer.class, timeId);

    return count != null && count > 0;
  }

  public Reservation findById(Long id) {
    String sql = "select r.id, r.name, r.date, r.status, "
        + "t.id as time_id, t.start_at, "
        + "th.id as theme_id, th.name as theme_name, "
        + "th.description as theme_description, th.image_url as theme_image_url "
        + "from reservation r "
        + "inner join reservation_time t on r.time_id = t.id "
        + "inner join theme th on r.theme_id = th.id "
        + "where r.id = ?";

    RowMapper<Reservation> rowMapper = (resultSet, rowNum) -> {
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

    return jdbcTemplate.queryForObject(sql, rowMapper, id);
  }

  public boolean findByDateTimeTheme(String date, Long timeId, Long themeId) {
    String sql = "select count(*) from reservation where date = ? and time_id = ? and theme_id = ?";
    Integer count = jdbcTemplate.queryForObject(sql, Integer.class, date, timeId, themeId);

    return count != null && count > 0;
  }

  public boolean findByNameAndDateAndTimeAndTheme(String name, String date, Long timeId, Long themeId) {
    String sql = "select count(*) from reservation where name = ? and date = ? and time_id = ? and theme_id = ?";
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

  public List<MyReservationResponse> findAllByName(String name) {
    String sql = "with waiting_rank as ( "
        + "  select id, rank() over (partition by date, time_id, theme_id order by id) as wait_rank "
        + "  from reservation "
        + "  where status = 'WAITING' "
        + ") "
        + "select r.id, r.name, r.date, r.status, "
        + "       t.id as time_id, t.start_at, "
        + "       th.id as theme_id, th.name as theme_name, "
        + "       wr.wait_rank "
        + "from reservation r "
        + "inner join reservation_time t on r.time_id = t.id "
        + "inner join theme th           on r.theme_id = th.id "
        + "left  join waiting_rank wr    on r.id = wr.id "
        + "where r.name = ?";

    return jdbcTemplate.query(sql, (rs, rowNum) -> new MyReservationResponse(
        rs.getLong("id"),
        rs.getString("name"),
        LocalDate.parse(rs.getString("date")),
        new TimeResponse(rs.getLong("time_id"), LocalTime.parse(rs.getString("start_at"))),
        new ThemeSimpleResponse(rs.getLong("theme_id"), rs.getString("theme_name")),
        ReservationStatus.valueOf(rs.getString("status")),
        rs.getObject("wait_rank", Long.class)
    ), name);
  }
}
