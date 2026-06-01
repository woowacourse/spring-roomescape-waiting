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
import roomescape.domain.Member;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationTime;
import roomescape.domain.ReservationWaiting;
import roomescape.domain.Theme;
import roomescape.domain.exception.ConflictException;
import roomescape.domain.exception.NotFoundException;
import roomescape.domain.projection.ReservationWaitingWithOrder;
import roomescape.repository.ReservationWaitingJdbcRepository;
import roomescape.repository.ReservationWaitingQueryJdbcRepository;
import roomescape.repository.ReservationWaitingQueryRepository;

@JdbcTest
@Import({
        ReservationWaitingJdbcRepository.class,
        ReservationWaitingQueryJdbcRepository.class
})
class ReservationWaitingJdbcRepositoryTest {

    private static final String THEME_NAME = "공포";
    private static final String THEME_DESCRIPTION = "무서운 테마";
    private static final String THEME_THUMBNAIL_IMAGE_URL = "https://example.com/horror.jpg";
    private static final LocalDate RESERVATION_DATE = LocalDate.of(2026, 8, 5);
    private static final LocalTime RESERVATION_START_AT = LocalTime.of(10, 0);
    private static final LocalDateTime WAITING_CREATED_AT = LocalDateTime.of(2026, 8, 1, 10, 0);

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
                THEME_NAME, THEME_DESCRIPTION, THEME_THUMBNAIL_IMAGE_URL
        );
        Long themeId = jdbcTemplate.queryForObject("SELECT id FROM theme LIMIT 1", Long.class);

        jdbcTemplate.update(
                "INSERT INTO reservation (name, date, time_id, theme_id) VALUES (?, ?, ?, ?)",
                "티뉴", RESERVATION_DATE, timeId, themeId
        );
        Long reservationId = jdbcTemplate.queryForObject("SELECT id FROM reservation LIMIT 1", Long.class);

        ReservationTime time = new ReservationTime(timeId, RESERVATION_START_AT);
        Theme theme = new Theme(themeId, THEME_NAME, THEME_DESCRIPTION, THEME_THUMBNAIL_IMAGE_URL);
        reservation = new Reservation(reservationId, "티뉴", RESERVATION_DATE, time, theme);
    }

    @Test
    void save는_생성된_id를_부여해_반환한다() {
        ReservationWaiting waiting = new ReservationWaiting(
                "민욱",
                WAITING_CREATED_AT,
                reservation.getSlot()
        );

        ReservationWaiting saved = repository.save(waiting, reservation.getId());

        assertThat(saved.getId()).isNotNull();
    }

    @Test
    void 같은_예약에_같은_이름으로_대기를_저장하면_ConflictException을_던진다() {
        repository.save(
                new ReservationWaiting(
                        "민욱",
                        WAITING_CREATED_AT,
                        reservation.getSlot()
                ),
                reservation.getId()
        );

        ReservationWaiting duplicated = new ReservationWaiting(
                "민욱",
                WAITING_CREATED_AT.plusSeconds(1),
                reservation.getSlot()
        );

        assertThatThrownBy(() -> repository.save(duplicated, reservation.getId()))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("이미 대기");
    }

    @Test
    void 존재하지_않는_예약에_대기를_저장하면_NotFoundException을_던진다() {
        ReservationWaiting waiting = new ReservationWaiting(
                "민욱",
                WAITING_CREATED_AT,
                reservation.getSlot()
        );

        assertThatThrownBy(() -> repository.save(waiting, 9999L))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("예약을 찾을 수 없습니다");
    }

    @Test
    void 같은_예약에_먼저_신청한_대기가_있으면_다음_순번을_부여한다() {
        repository.save(
                new ReservationWaiting("민욱", WAITING_CREATED_AT, reservation.getSlot()),
                reservation.getId()
        );

        ReservationWaiting second = repository.save(
                new ReservationWaiting("브라운", WAITING_CREATED_AT, reservation.getSlot()),
                reservation.getId()
        );
        ReservationWaitingWithOrder found = queryRepository.findById(second.getId()).orElseThrow();

        assertThat(found.order()).isEqualTo(2);
    }

    @Test
    void existBy는_같은_이름과_예약의_대기가_있으면_true를_반환한다() {
        repository.save(
                new ReservationWaiting("민욱", WAITING_CREATED_AT, reservation.getSlot()),
                reservation.getId()
        );

        assertThat(repository.existBy(new Member("민욱"), reservation.getId())).isTrue();
    }

    @Test
    void existBy는_일치하는_대기가_없으면_false를_반환한다() {
        assertThat(repository.existBy(new Member("민욱"), reservation.getId())).isFalse();
    }

    @Test
    void findById는_저장된_대기를_반환한다() {
        ReservationWaiting saved = repository.save(
                new ReservationWaiting("민욱", WAITING_CREATED_AT, reservation.getSlot()),
                reservation.getId()
        );

        Optional<ReservationWaiting> found = repository.findById(saved.getId());

        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("민욱");
        assertThat(found.get().getSlot()).isEqualTo(reservation.getSlot());
    }

    @Test
    void findById는_존재하지_않으면_빈_Optional을_반환한다() {
        assertThat(repository.findById(999L)).isEmpty();
    }

    @Test
    void findByName은_같은_예약의_대기_순번을_계산해_반환한다() {
        repository.save(
                new ReservationWaiting("브라운", WAITING_CREATED_AT, reservation.getSlot()),
                reservation.getId()
        );
        repository.save(
                new ReservationWaiting("민욱", WAITING_CREATED_AT.plusMinutes(1), reservation.getSlot()),
                reservation.getId()
        );

        List<ReservationWaitingWithOrder> found = queryRepository.findByMember(new Member("민욱"));

        assertThat(found).hasSize(1);
        assertThat(found.get(0).order()).isEqualTo(2);
    }

    @Test
    void deleteById_이후_findById는_빈_Optional을_반환한다() {
        ReservationWaiting saved = repository.save(
                new ReservationWaiting("민욱", WAITING_CREATED_AT, reservation.getSlot()),
                reservation.getId()
        );

        repository.deleteById(saved.getId());

        assertThat(repository.findById(saved.getId())).isEmpty();
    }
}
