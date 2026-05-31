package roomescape.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationTime;
import roomescape.domain.ReservationWaiting;
import roomescape.domain.Theme;
import roomescape.exception.BusinessRuleViolationException;
import roomescape.exception.NotFoundException;
import roomescape.projection.ReservationWaitingWithOrder;
import roomescape.repository.ReservationWaitingJdbcRepository;
import roomescape.repository.ReservationWaitingQueryJdbcRepository;
import roomescape.repository.ReservationWaitingQueryRepository;

@JdbcTest
@Import({ReservationWaitingJdbcRepository.class, ReservationWaitingQueryJdbcRepository.class})
class ReservationWaitingJdbcRepositoryTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private ReservationWaitingJdbcRepository repository;

    @Autowired
    private ReservationWaitingQueryRepository queryRepository;

    private Reservation reservation;

    @BeforeEach
    void setUp() {
        jdbcTemplate.update("INSERT INTO reservation_time (start_at) VALUES ('10:00')");
        Long timeId = jdbcTemplate.queryForObject("SELECT id FROM reservation_time LIMIT 1", Long.class);

        jdbcTemplate.update(
                "INSERT INTO theme (name, description, thumbnail_image_url) VALUES (?, ?, ?)",
                "공포", "무서운 테마", "https://example.com/horror.jpg"
        );
        Long themeId = jdbcTemplate.queryForObject("SELECT id FROM theme LIMIT 1", Long.class);

        jdbcTemplate.update(
                "INSERT INTO reservation (name, date, time_id, theme_id) VALUES (?, ?, ?, ?)",
                "티뉴", LocalDate.of(2026, 8, 5), timeId, themeId
        );
        Long reservationId = jdbcTemplate.queryForObject("SELECT id FROM reservation LIMIT 1", Long.class);

        ReservationTime time = new ReservationTime(timeId, LocalTime.of(10, 0));
        Theme theme = new Theme(themeId, "공포", "무서운 테마", "https://example.com/horror.jpg");
        reservation = new Reservation(reservationId, "티뉴", LocalDate.of(2026, 8, 5), time, theme);
    }

    @Test
    void save는_생성된_id를_부여해_반환한다() {
        ReservationWaiting waiting = new ReservationWaiting(
                "민욱", LocalDateTime.of(2026, 8, 1, 10, 0, 0), reservation);

        ReservationWaiting saved = repository.save(waiting);

        assertThat(saved.getId()).isNotNull();
    }

    @Test
    void 같은_예약에_같은_이름으로_대기를_저장하면_BusinessRuleViolationException을_던진다() {
        repository.save(new ReservationWaiting(
                "민욱", LocalDateTime.of(2026, 8, 1, 10, 0, 0), reservation));

        ReservationWaiting duplicated = new ReservationWaiting(
                "민욱", LocalDateTime.of(2026, 8, 1, 10, 0, 1), reservation);

        assertThatThrownBy(() -> repository.save(duplicated))
                .isInstanceOf(BusinessRuleViolationException.class)
                .hasMessageContaining("이미 대기");
    }

    @Test
    void 존재하지_않는_예약에_대기를_저장하면_NotFoundException을_던진다() {
        Reservation missingReservation = new Reservation(
                9999L,
                "티뉴",
                reservation.getDate(),
                reservation.getTime(),
                reservation.getTheme()
        );
        ReservationWaiting waiting = new ReservationWaiting(
                "민욱", LocalDateTime.of(2026, 8, 1, 10, 0, 0), missingReservation);

        assertThatThrownBy(() -> repository.save(waiting))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("예약을 찾을 수 없습니다");
    }

    @Test
    void 같은_예약에_먼저_신청한_대기가_있으면_다음_순번을_부여한다() {
        LocalDateTime waitingTime = LocalDateTime.of(2026, 8, 1, 10, 0, 0);
        repository.save(new ReservationWaiting(
                "민욱", waitingTime, reservation));

        ReservationWaiting second = repository.save(new ReservationWaiting(
                "브라운", waitingTime, reservation));
        ReservationWaitingWithOrder found = queryRepository.findById(second.getId()).orElseThrow();

        assertThat(found.order()).isEqualTo(2);
    }

    @Test
    void existBy는_같은_이름과_예약의_대기가_있으면_true를_반환한다() {
        repository.save(new ReservationWaiting(
                "민욱", LocalDateTime.of(2026, 8, 1, 10, 0, 0), reservation));

        assertThat(repository.existBy("민욱", reservation.getId())).isTrue();
    }

    @Test
    void existBy는_일치하는_대기가_없으면_false를_반환한다() {
        assertThat(repository.existBy("민욱", reservation.getId())).isFalse();
    }

    @Test
    void findById는_저장된_대기를_반환한다() {
        ReservationWaiting saved = repository.save(new ReservationWaiting(
                "민욱", LocalDateTime.of(2026, 8, 1, 10, 0, 0), reservation));

        Optional<ReservationWaiting> found = repository.findById(saved.getId());

        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("민욱");
        assertThat(found.get().getReservation().getId()).isEqualTo(reservation.getId());
    }

    @Test
    void findById는_존재하지_않으면_빈_Optional을_반환한다() {
        assertThat(repository.findById(999L)).isEmpty();
    }

    @Test
    void findByName은_같은_예약의_대기_순번을_계산해_반환한다() {
        LocalDateTime waitingTime = LocalDateTime.of(2026, 8, 1, 10, 0, 0);
        repository.save(new ReservationWaiting("브라운", waitingTime, reservation));
        repository.save(new ReservationWaiting("민욱", waitingTime.plusMinutes(1), reservation));

        List<ReservationWaitingWithOrder> found = queryRepository.findByName("민욱");

        assertThat(found).hasSize(1);
        assertThat(found.get(0).order()).isEqualTo(2);
    }

    @Test
    void deleteById_이후_findById는_빈_Optional을_반환한다() {
        ReservationWaiting saved = repository.save(new ReservationWaiting(
                "민욱", LocalDateTime.of(2026, 8, 1, 10, 0, 0), reservation));

        repository.deleteById(saved.getId());

        assertThat(repository.findById(saved.getId())).isEmpty();
    }
}
