package roomescape.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static roomescape.fixture.ReservationFixture.member;
import static roomescape.fixture.ReservationFixture.reservation;
import static roomescape.fixture.ReservationFixture.slot;
import static roomescape.fixture.ReservationFixture.waiting;

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
import roomescape.domain.ReservationWaitingQueryRepository;
import roomescape.domain.Theme;
import roomescape.domain.exception.ConflictException;
import roomescape.domain.projection.ReservationWaitingWithOrder;
import roomescape.infrastructure.repository.ReservationWaitingJdbcRepository;
import roomescape.infrastructure.repository.ReservationWaitingQueryJdbcRepository;

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
        reservation = reservation(reservationId, "티뉴", slot(RESERVATION_DATE, time, theme));
    }

    @Test
    void save는_생성된_id를_부여해_반환한다() {
        ReservationWaiting waiting = waiting("민욱", reservation.getSlot(), WAITING_CREATED_AT);

        ReservationWaiting saved = repository.save(waiting);

        assertThat(saved.getId()).isNotNull();
    }

    @Test
    void 같은_예약에_같은_이름으로_대기를_저장하면_ConflictException을_던진다() {
        repository.save(waiting("민욱", reservation.getSlot(), WAITING_CREATED_AT));

        ReservationWaiting duplicated = waiting("민욱", reservation.getSlot(), WAITING_CREATED_AT.plusSeconds(1));

        assertThatThrownBy(() -> repository.save(duplicated))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("이미 대기");
    }

    @Test
    void 같은_슬롯에_먼저_신청한_대기가_있으면_다음_순번을_부여한다() {
        repository.save(waiting("민욱", reservation.getSlot(), WAITING_CREATED_AT));

        ReservationWaiting second = repository.save(waiting("브라운", reservation.getSlot(), WAITING_CREATED_AT));
        ReservationWaitingWithOrder found = queryRepository.findById(second.getId()).orElseThrow();

        assertThat(found.order()).isEqualTo(2);
    }

    @Test
    void existsBy는_같은_이름과_슬롯의_대기가_있으면_true를_반환한다() {
        repository.save(waiting("민욱", reservation.getSlot(), WAITING_CREATED_AT));

        assertThat(repository.existsBy(member("민욱"), reservation.getSlot())).isTrue();
    }

    @Test
    void existsBy는_일치하는_대기가_없으면_false를_반환한다() {
        assertThat(repository.existsBy(member("민욱"), reservation.getSlot())).isFalse();
    }

    @Test
    void findById는_저장된_대기를_반환한다() {
        ReservationWaiting saved = repository.save(waiting("민욱", reservation.getSlot(), WAITING_CREATED_AT));

        Optional<ReservationWaiting> found = repository.findById(saved.getId());

        assertThat(found).isPresent();
        assertThat(found.get().getWaiter()).isEqualTo(member("민욱"));
        assertThat(found.get().getSlot()).isEqualTo(reservation.getSlot());
    }

    @Test
    void findById는_존재하지_않으면_빈_Optional을_반환한다() {
        assertThat(repository.findById(999L)).isEmpty();
    }

    @Test
    void findFirstBySlot은_생성_ID가_가장_작은_대기를_반환한다() {
        repository.save(waiting("브라운", reservation.getSlot(), WAITING_CREATED_AT.plusMinutes(1)));
        repository.save(waiting("밀란", reservation.getSlot(), WAITING_CREATED_AT));

        Optional<ReservationWaiting> found = repository.findFirstBySlot(reservation.getSlot());

        assertThat(found).isPresent();
        assertThat(found.get().getWaiter()).isEqualTo(member("브라운"));
    }

    @Test
    void findByName은_같은_슬롯의_대기_순번을_계산해_반환한다() {
        repository.save(waiting("브라운", reservation.getSlot(), WAITING_CREATED_AT));
        repository.save(waiting("민욱", reservation.getSlot(), WAITING_CREATED_AT.plusMinutes(1)));

        List<ReservationWaitingWithOrder> found = queryRepository.findByMember(member("민욱"));

        assertThat(found).hasSize(1);
        assertThat(found.getFirst().order()).isEqualTo(2);
    }

    @Test
    void deleteById_이후_findById는_빈_Optional을_반환한다() {
        ReservationWaiting saved = repository.save(waiting("민욱", reservation.getSlot(), WAITING_CREATED_AT));

        repository.deleteById(saved.getId());

        assertThat(repository.findById(saved.getId())).isEmpty();
    }
}
