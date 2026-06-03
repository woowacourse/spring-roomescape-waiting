package roomescape.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationTime;
import roomescape.domain.Schedule;
import roomescape.domain.Theme;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@JdbcTest
@Import(JdbcReservationRepository.class)
class ReservationRepositoryTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private ReservationRepository reservationRepository;

    private ReservationTime time;
    private Theme theme;

    @BeforeEach
    void setUp() {
        jdbcTemplate.update("DELETE FROM waiting");
        jdbcTemplate.update("DELETE FROM reservation");
        jdbcTemplate.update("DELETE FROM reservation_time");
        jdbcTemplate.update("DELETE FROM theme");

        jdbcTemplate.update("INSERT INTO reservation_time (start_at) VALUES (?)", "10:00");
        jdbcTemplate.update("INSERT INTO theme (name, description, thumbnail) VALUES (?, ?, ?)",
                "공포방", "무서운방입니다.", "image-url");
        this.time = jdbcTemplate.queryForObject("SELECT * FROM reservation_time",
                (rs, rowNum) -> new ReservationTime(
                        rs.getLong("id"),
                        rs.getObject("start_at", LocalTime.class)));
        this.theme = jdbcTemplate.queryForObject("SELECT * FROM theme",
                (rs, rowNum) -> new Theme(
                        rs.getLong("id"),
                        rs.getString("name"),
                        rs.getString("description"),
                        rs.getString("thumbnail"))
        );
    }

    @Test
    void 예약_전체를_조회한다() {
        reservationRepository.save(new Reservation(
                null, "브라운", new Schedule(LocalDate.of(2026, 5, 10), time, theme)));

        List<Reservation> result = reservationRepository.findAll(0, 10);

        assertThat(result).hasSize(1);
        Reservation reservation = result.get(0);
        Schedule schedule = reservation.getSchedule();
        assertThat(reservation.getName()).isEqualTo("브라운");
        assertThat(schedule.getDate()).isEqualTo(LocalDate.of(2026, 5, 10));
        assertThat(schedule.getTime().getId()).isEqualTo(time.getId());
        assertThat(schedule.getTime().getStartAt()).isEqualTo(LocalTime.of(10, 0));
        assertThat(schedule.getTheme().getId()).isEqualTo(theme.getId());
        assertThat(schedule.getTheme().getName()).isEqualTo("공포방");
        assertThat(schedule.getTheme().getDescription()).isEqualTo("무서운방입니다.");
        assertThat(schedule.getTheme().getThumbnail()).isEqualTo("image-url");
    }

    @Test
    void 예약_목록을_페이지_단위로_조회한다() {
        reservationRepository.save(new Reservation(
                null, "브라운", new Schedule(LocalDate.of(2026, 5, 10), time, theme)));
        reservationRepository.save(new Reservation(
                null, "어셔", new Schedule(LocalDate.of(2026, 5, 11), time, theme)));
        reservationRepository.save(new Reservation(
                null, "레서", new Schedule(LocalDate.of(2026, 5, 12), time, theme)));

        List<Reservation> result = reservationRepository.findAll(1, 2);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("레서");
    }

    @Test
    void 이름으로_사용자_예약_목록을_페이지_단위로_조회한다() {
        reservationRepository.save(new Reservation(
                null, "브라운", new Schedule(LocalDate.of(2026, 5, 10), time, theme)));
        reservationRepository.save(new Reservation(
                null, "어셔", new Schedule(LocalDate.of(2026, 5, 11), time, theme)));
        reservationRepository.save(new Reservation(
                null, "브라운", new Schedule(LocalDate.of(2026, 5, 12), time, theme)));

        List<Reservation> result = reservationRepository.findUserReservations("브라운", 0, 10);

        assertThat(result).hasSize(2);
        assertThat(result)
                .extracting(Reservation::getName)
                .containsExactly("브라운", "브라운");
        assertThat(result)
                .extracting(reservation -> reservation.getSchedule().getDate())
                .containsExactly(LocalDate.of(2026, 5, 10), LocalDate.of(2026, 5, 12));
    }

    @Test
    void 이름으로_사용자_예약_목록을_조회할_때_페이지를_적용한다() {
        reservationRepository.save(new Reservation(
                null, "브라운", new Schedule(LocalDate.of(2026, 5, 10), time, theme)));
        reservationRepository.save(new Reservation(
                null, "브라운", new Schedule(LocalDate.of(2026, 5, 11), time, theme)));
        reservationRepository.save(new Reservation(
                null, "브라운", new Schedule(LocalDate.of(2026, 5, 12), time, theme)));

        List<Reservation> result = reservationRepository.findUserReservations("브라운", 1, 2);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getSchedule().getDate()).isEqualTo(LocalDate.of(2026, 5, 12));
    }

    @Test
    void 예약을_저장한다() {
        Reservation saved = reservationRepository.save(new Reservation(null, "브라운", new Schedule(LocalDate.of(2026, 5, 10), time, theme)));

        Integer count = jdbcTemplate.queryForObject(
                "SELECT count(*) FROM reservation WHERE id = ?", Integer.class, saved.getId());
        assertThat(count).isEqualTo(1);
    }

    @Test
    void id로_예약을_조회한다() {
        Reservation savedReservation = reservationRepository.save(new Reservation(
                null, "브라운", new Schedule(LocalDate.of(2026, 5, 10), time, theme)));

        Reservation result = reservationRepository.findById(savedReservation.getId()).get();

        Schedule schedule = result.getSchedule();
        assertThat(result.getId()).isEqualTo(savedReservation.getId());
        assertThat(result.getName()).isEqualTo("브라운");
        assertThat(schedule.getDate()).isEqualTo(LocalDate.of(2026, 5, 10));
        assertThat(schedule.getTime().getId()).isEqualTo(time.getId());
        assertThat(schedule.getTheme().getId()).isEqualTo(theme.getId());
    }

    @Test
    void 존재하지_않는_id로_조회하면_빈_Optional을_반환한다() {
        assertThat(reservationRepository.findById(999L)).isEmpty();
    }

    @Test
    void 예약의_날짜와_시간을_변경한다() {
        jdbcTemplate.update("INSERT INTO reservation_time (start_at) VALUES (?)", "12:00");
        ReservationTime newTime = jdbcTemplate.queryForObject(
                "SELECT * FROM reservation_time WHERE start_at = ?",
                (rs, rowNum) -> new ReservationTime(
                        rs.getLong("id"),
                        LocalTime.parse(rs.getString("start_at"))),
                "12:00"
        );
        Reservation savedReservation = reservationRepository.save(new Reservation(
                null, "브라운", new Schedule(LocalDate.of(2026, 5, 10), time, theme)));
        Reservation updated = new Reservation(
                savedReservation.getId(), "브라운", new Schedule(LocalDate.of(2026, 5, 11), newTime, theme));

        reservationRepository.update(updated);

        Reservation result = reservationRepository.findById(savedReservation.getId()).get();
        Schedule schedule = result.getSchedule();
        assertThat(schedule.getDate()).isEqualTo(LocalDate.of(2026, 5, 11));
        assertThat(schedule.getTime().getId()).isEqualTo(newTime.getId());
        assertThat(schedule.getTime().getStartAt()).isEqualTo(LocalTime.of(12, 0));
    }

    @Test
    void 같은_날짜_시간_테마의_예약을_조회한다() {
        Reservation first = reservationRepository.save(new Reservation(
                null, "브라운", new Schedule(LocalDate.of(2026, 5, 10), time, theme)));

        Optional<Reservation> result = reservationRepository.findBySchedule(first.getSchedule());

        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(first.getId());
    }

    @Test
    void 같은_날짜_시간_테마의_예약이_없으면_빈_Optional을_반환한다() {
        Reservation reservation = new Reservation(
                null, "브라운", new Schedule(LocalDate.of(2026, 5, 10), time, theme));

        Optional<Reservation> result = reservationRepository.findBySchedule(reservation.getSchedule());

        assertThat(result).isEmpty();
    }


    @Test
    void 사용자의_예약만_조회하고_예약대기는_제외한다() {
        reservationRepository.save(new Reservation(
                null, "브라운", new Schedule(LocalDate.of(2026, 5, 10), time, theme)));
        jdbcTemplate.update("INSERT INTO waiting (name, date, time_id, theme_id) VALUES (?, ?, ?, ?)",
                "브라운", LocalDate.of(2026, 5, 11).toString(), time.getId(), theme.getId());

        List<Reservation> result = reservationRepository.findUserReservations("브라운", 0, 10);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("브라운");
        assertThat(result.get(0).getSchedule().getDate()).isEqualTo(LocalDate.of(2026, 5, 10));
    }

    @Test
    void 예약을_삭제한다() {
        Reservation savedReservation = reservationRepository.save(new Reservation(
                null, "브라운", new Schedule(LocalDate.of(2026, 5, 10), time, theme)));

        reservationRepository.delete(savedReservation);

        assertThat(reservationRepository.findById(savedReservation.getId())).isEmpty();
    }
}
