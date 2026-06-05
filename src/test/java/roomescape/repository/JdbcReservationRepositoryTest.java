package roomescape.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.jdbc.Sql;
import roomescape.domain.Reservation;
import roomescape.domain.Theme;
import roomescape.domain.ThemeSlot;
import roomescape.domain.Time;
import roomescape.domain.WaitingReservation;
import roomescape.domain.reservationStatus.CancelledStatus;
import roomescape.domain.reservationStatus.ConfirmedStatus;

@JdbcTest
@Sql({"/schema.sql", "/test-data.sql"})
class JdbcReservationRepositoryTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private JdbcReservationRepository jdbcReservationRepository;

    private static final Theme THEME_1 = new Theme(1L, "테스트테마1", "테스트용 첫 번째 테마 설명", "https://test.com/thumb1.jpg");
    private static final Theme THEME_2 = new Theme(2L, "테스트테마2", "테스트용 두 번째 테마 설명", "https://test.com/thumb2.jpg");
    private static final Time TIME_10 = new Time(1L, LocalTime.of(10, 0));
    private static final Time TIME_14 = new Time(2L, LocalTime.of(14, 0));
    private static final Time TIME_18 = new Time(3L, LocalTime.of(18, 0));

    @BeforeEach
    void setUp() {
        jdbcReservationRepository = new JdbcReservationRepository(jdbcTemplate);
    }

    @Test
    @DisplayName("예약을 저장하고 영속화된 객체를 반환한다.")
    void save() {
        ThemeSlot themeSlot = saveThemeSlot(THEME_1, LocalDate.now(), TIME_10, false);
        Reservation reservation = new Reservation("브라운", themeSlot);
        Reservation savedReservation = jdbcReservationRepository.save(reservation);
        assertThat(savedReservation.getId()).isPositive();
    }

    @Test
    @DisplayName("식별자로 예약 객체를 조회한다.")
    void findById() {
        ThemeSlot themeSlot = saveThemeSlot(THEME_2, LocalDate.now(), TIME_14, false);
        Reservation savedReservation = jdbcReservationRepository.save(new Reservation("브라운", themeSlot));
        Reservation foundReservation = jdbcReservationRepository.findById(savedReservation.getId()).get();
        assertThat(foundReservation.getName()).isEqualTo("브라운");
    }

    @Test
    @DisplayName("식별자로 예약 row를 잠그고 예약 객체를 조회한다.")
    void findByIdForUpdate() {
        ThemeSlot themeSlot = saveThemeSlot(THEME_2, LocalDate.now(), TIME_14, false);
        Reservation savedReservation = jdbcReservationRepository.save(new Reservation("브라운", themeSlot));
        Reservation foundReservation = jdbcReservationRepository.findByIdForUpdate(savedReservation.getId()).get();
        assertThat(foundReservation.getThemeSlotId()).isEqualTo(themeSlot.getId());
    }

    @Test
    @DisplayName("모든 예약 객체 목록을 조회한다.")
    void findAll() {
        ThemeSlot themeSlot = saveThemeSlot(THEME_1, LocalDate.now(), TIME_18, false);
        jdbcReservationRepository.save(new Reservation("브라운", themeSlot));
        List<Reservation> reservations = jdbcReservationRepository.findAll();
        assertThat(reservations).hasSize(1);
    }

    @Test
    @DisplayName("식별자로 예약을 삭제한다.")
    void deleteById() {
        ThemeSlot themeSlot = saveThemeSlot(THEME_2, LocalDate.now(), TIME_10, false);
        Reservation savedReservation = jdbcReservationRepository.save(new Reservation("브라운", themeSlot));
        jdbcReservationRepository.deleteById(savedReservation.getId());
        assertThat(jdbcReservationRepository.findAll()).isEmpty();
    }

    private ThemeSlot saveThemeSlot(Theme theme, LocalDate date, Time time, boolean isReserved) {
        jdbcTemplate.update(
                "INSERT INTO theme_slot (theme_id, date, time_id, is_reserved) VALUES (?, ?, ?, ?)",
                theme.getId(),
                date,
                time.getId(),
                isReserved
        );
        Long id = jdbcTemplate.queryForObject(
                "SELECT id FROM theme_slot WHERE theme_id = ? AND date = ? AND time_id = ?",
                Long.class,
                theme.getId(),
                date,
                time.getId()
        );
        return new ThemeSlot(id, theme, date, time, isReserved);
    }

    @Test
    @DisplayName("같은 테마 슬롯 조건의 PENDING 예약을 ID순으로 가장 먼저 조회한다.")
    void findFirstPendingReservationByThemeSlotOrderByIdAsc() {
        ThemeSlot themeSlot = saveThemeSlot(THEME_1, LocalDate.now(), TIME_10, false);
        jdbcReservationRepository.save(new Reservation(1L, "브라운", themeSlot, ConfirmedStatus.getInstance()));
        jdbcReservationRepository.save(new Reservation("브라운1", themeSlot));
        jdbcReservationRepository.save(new Reservation("브라운2", themeSlot));
        jdbcReservationRepository.save(new Reservation("브라운3", themeSlot));

        Optional<Reservation> reservation = jdbcReservationRepository.findFirstPendingByThemeSlotId(
                themeSlot.getId());
        assertThat(reservation).isNotEmpty();
        assertThat(reservation.get().getName()).isEqualTo("브라운1");

    }

    @Test
    @DisplayName("사용자의 PENDING 예약을 슬롯별 대기 순번과 함께 조회한다.")
    void findWaitingReservationsWithOrderByName() {
        ThemeSlot firstThemeSlot = saveThemeSlot(THEME_1, LocalDate.now(), TIME_10, false);
        ThemeSlot secondThemeSlot = saveThemeSlot(THEME_2, LocalDate.now().plusDays(1), TIME_14, false);
        jdbcReservationRepository.save(new Reservation(1L, "브라운", firstThemeSlot, ConfirmedStatus.getInstance()));
        jdbcReservationRepository.save(new Reservation("김대기1", firstThemeSlot));
        jdbcReservationRepository.save(new Reservation("김대기2", firstThemeSlot));
        jdbcReservationRepository.save(new Reservation(1L, "브라운", secondThemeSlot, ConfirmedStatus.getInstance()));
        jdbcReservationRepository.save(new Reservation("김대기2", secondThemeSlot));
        jdbcReservationRepository.save(new Reservation("김대기1", secondThemeSlot));

        List<WaitingReservation> waitingReservations = jdbcReservationRepository.findWaitingReservationsWithOrderByName("김대기2");

        assertThat(waitingReservations)
                .extracting(WaitingReservation::waitingOrder)
                .containsExactly(2, 1);
    }

    @Test
    @DisplayName("기대 상태와 현재 상태가 같을 때만 상태를 변경한다.")
    void updateStatus() {
        ThemeSlot themeSlot = saveThemeSlot(THEME_1, LocalDate.now(), TIME_10, false);
        Reservation pendingReservation = jdbcReservationRepository.save(new Reservation("김대기", themeSlot));
        pendingReservation.confirm();

        boolean updated = jdbcReservationRepository.updateStatus(pendingReservation, "PENDING");

        Reservation reservation = jdbcReservationRepository.findById(pendingReservation.getId()).orElseThrow();
        assertThat(updated).isTrue();
        assertThat(reservation.getReservationStatus()).isEqualTo(ConfirmedStatus.getInstance());
    }

    @Test
    @DisplayName("기대 상태와 현재 상태가 다르면 상태 변경에서 제외한다.")
    void updateStatusWhenExpectedStatusIsDifferent() {
        ThemeSlot themeSlot = saveThemeSlot(THEME_1, LocalDate.now(), TIME_10, false);
        Reservation reservation = jdbcReservationRepository.save(new Reservation("김대기", themeSlot));
        Reservation cancelledReservation = new Reservation(
                reservation.getId(),
                reservation.getName(),
                reservation.getThemeSlot(),
                CancelledStatus.getInstance()
        );
        jdbcReservationRepository.updateStatus(cancelledReservation, "PENDING");
        Reservation confirmedReservation = new Reservation(
                reservation.getId(),
                reservation.getName(),
                reservation.getThemeSlot(),
                ConfirmedStatus.getInstance()
        );

        boolean updated = jdbcReservationRepository.updateStatus(confirmedReservation, "PENDING");

        Reservation foundReservation = jdbcReservationRepository.findById(reservation.getId()).orElseThrow();
        assertThat(updated).isFalse();
        assertThat(foundReservation.getReservationStatus()).isEqualTo(CancelledStatus.getInstance());
    }
}
