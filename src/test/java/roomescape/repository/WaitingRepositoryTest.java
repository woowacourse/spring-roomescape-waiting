package roomescape.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import roomescape.domain.ReservationTime;
import roomescape.domain.Schedule;
import roomescape.domain.Theme;
import roomescape.domain.Waiting;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@JdbcTest
@Import(JdbcWaitingRepository.class)
class WaitingRepositoryTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private WaitingRepository waitingRepository;

    private ReservationTime time;
    private Theme theme;
    private final LocalDate date = LocalDate.of(2026, 5, 10);

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
                        rs.getString("thumbnail")));
    }

    @Test
    void 예약_대기를_저장한다() {
        Waiting saved = waitingRepository.save(new Waiting(null, "레서", new Schedule(date, time, theme)));

        Integer count = jdbcTemplate.queryForObject(
                "SELECT count(*) FROM waiting WHERE id = ?", Integer.class, saved.getId());
        assertThat(count).isEqualTo(1);
        assertThat(saved.getId()).isNotNull();
    }

    @Test
    void id로_예약_대기를_조회한다() {
        Waiting saved = waitingRepository.save(new Waiting(null, "레서", new Schedule(date, time, theme)));

        Waiting result = waitingRepository.findById(saved.getId()).get();

        Schedule schedule = result.getSchedule();
        assertThat(result.getId()).isEqualTo(saved.getId());
        assertThat(result.getName()).isEqualTo("레서");
        assertThat(schedule.getDate()).isEqualTo(date);
        assertThat(schedule.getTime().getId()).isEqualTo(time.getId());
        assertThat(schedule.getTime().getStartAt()).isEqualTo(LocalTime.of(10, 0));
        assertThat(schedule.getTheme().getId()).isEqualTo(theme.getId());
        assertThat(schedule.getTheme().getName()).isEqualTo("공포방");
    }

    @Test
    void 존재하지_않는_id로_조회하면_빈_Optional을_반환한다() {
        assertThat(waitingRepository.findById(999L)).isEmpty();
    }

    @Test
    void 동일한_일정과_이름의_예약_대기를_조회한다() {
        waitingRepository.save(new Waiting(null, "레서", new Schedule(date, time, theme)));

        Optional<Waiting> result = waitingRepository.findByScheduleAndName(
                new Waiting(null, "레서", new Schedule(date, time, theme)));

        assertThat(result).isPresent();
        assertThat(result.get().getName()).isEqualTo("레서");
    }

    @Test
    void 동일한_일정과_이름의_예약_대기가_없으면_빈_Optional을_반환한다() {
        waitingRepository.save(new Waiting(null, "레서", new Schedule(date, time, theme)));

        Optional<Waiting> result = waitingRepository.findByScheduleAndName(
                new Waiting(null, "밍구", new Schedule(date, time, theme)));

        assertThat(result).isEmpty();
    }

    @Test
    void 같은_일정에서_예약_대기_순번을_구한다() {
        Waiting first = waitingRepository.save(new Waiting(null, "레서", new Schedule(date, time, theme)));
        waitingRepository.save(new Waiting(null, "밍구", new Schedule(date, time, theme)));
        Waiting third = waitingRepository.save(new Waiting(null, "브라운", new Schedule(date, time, theme)));

        Schedule schedule = new Schedule(date, time, theme);
        Long firstOrder = waitingRepository.findWaitingOrder(first);
        Long thirdOrder = waitingRepository.findWaitingOrder(third);

        assertThat(firstOrder).isEqualTo(1L);
        assertThat(thirdOrder).isEqualTo(3L);
    }

    @Test
    void 예약_대기를_삭제한다() {
        Waiting saved = waitingRepository.save(new Waiting(null, "레서", new Schedule(date, time, theme)));

        waitingRepository.delete(saved);

        assertThat(waitingRepository.findById(saved.getId())).isEmpty();
    }
}
