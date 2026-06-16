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
import roomescape.domain.reservationStatus.ConfirmedStatus;
import roomescape.domain.reservationStatus.PendingStatus;
import roomescape.infra.JdbcReservationRepository;

@JdbcTest
@Sql({"/schema.sql", "/test-data.sql"})
class JdbcReservationRepositoryTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private JdbcReservationRepository jdbcReservationRepository;

    private static final Theme THEME_1 = new Theme(1L, "테스트테마1", "테스트용 첫 번째 테마 설명", "https://test.com/thumb1.jpg", 10000L);
    private static final Theme THEME_2 = new Theme(2L, "테스트테마2", "테스트용 두 번째 테마 설명", "https://test.com/thumb2.jpg", 10000L);
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
        Reservation reservation = new Reservation("브라운", themeSlot.getId(), themeSlot.getDate(), themeSlot.getTime(), themeSlot.getTheme());
        Reservation savedReservation = jdbcReservationRepository.save(reservation);
        assertThat(savedReservation.getId()).isPositive();
    }

    @Test
    @DisplayName("식별자로 예약 객체를 조회한다.")
    void findById() {
        ThemeSlot themeSlot = saveThemeSlot(THEME_2, LocalDate.now(), TIME_14, false);
        Reservation savedReservation = jdbcReservationRepository.save(new Reservation("브라운", themeSlot.getId(), themeSlot.getDate(), themeSlot.getTime(), themeSlot.getTheme()));
        Reservation foundReservation = jdbcReservationRepository.findById(savedReservation.getId()).get();
        assertThat(foundReservation.getName()).isEqualTo("브라운");
    }

    @Test
    @DisplayName("모든 예약 객체 목록을 조회한다.")
    void findAll() {
        ThemeSlot themeSlot = saveThemeSlot(THEME_1, LocalDate.now(), TIME_18, false);
        jdbcReservationRepository.save(new Reservation("브라운", themeSlot.getId(), themeSlot.getDate(), themeSlot.getTime(), themeSlot.getTheme()));
        List<Reservation> reservations = jdbcReservationRepository.findAll();
        assertThat(reservations).hasSize(1);
    }

    @Test
    @DisplayName("식별자로 예약을 삭제한다.")
    void deleteById() {
        ThemeSlot themeSlot = saveThemeSlot(THEME_2, LocalDate.now(), TIME_10, false);
        Reservation savedReservation = jdbcReservationRepository.save(new Reservation("브라운", themeSlot.getId(), themeSlot.getDate(), themeSlot.getTime(), themeSlot.getTheme()));
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
    @DisplayName("같은 슬롯에 대한 대기 예약은 신청 순서대로 가져와진다")
    void assignWaitingOrderByApplicationOrder() {
        ThemeSlot themeSlot = saveThemeSlot(THEME_1, LocalDate.now(), TIME_10, false);
        jdbcReservationRepository.save(new Reservation(1L, "브라운", themeSlot.getId(), themeSlot.getDate(), themeSlot.getTime(), themeSlot.getTheme(), ConfirmedStatus.getInstance()));
        jdbcReservationRepository.save(new Reservation("브라운1", themeSlot.getId(), themeSlot.getDate(), themeSlot.getTime(), themeSlot.getTheme()));
        jdbcReservationRepository.save(new Reservation("브라운2", themeSlot.getId(), themeSlot.getDate(), themeSlot.getTime(), themeSlot.getTheme()));
        jdbcReservationRepository.save(new Reservation("브라운3", themeSlot.getId(), themeSlot.getDate(), themeSlot.getTime(), themeSlot.getTheme()));

        List<Reservation> pendingReservations = jdbcReservationRepository.findByThemeSlotAndPending(
                themeSlot.getId());

        assertThat(pendingReservations.get(0).getName()).isEqualTo("브라운1");
        assertThat(pendingReservations.get(1).getName()).isEqualTo("브라운2");
        assertThat(pendingReservations.get(2).getName()).isEqualTo("브라운3");
    }

    @Test
    @DisplayName("같은 슬롯에 대한 대기 예약은 전부 PENDING 상태다")
    void findByThemeSlotAndPendingMustBePending(){
        ThemeSlot themeSlot = saveThemeSlot(THEME_1, LocalDate.now(), TIME_10, false);
        jdbcReservationRepository.save(new Reservation(1L, "브라운", themeSlot.getId(), themeSlot.getDate(), themeSlot.getTime(), themeSlot.getTheme(), ConfirmedStatus.getInstance()));
        jdbcReservationRepository.save(new Reservation("브라운1", themeSlot.getId(), themeSlot.getDate(), themeSlot.getTime(), themeSlot.getTheme()));
        jdbcReservationRepository.save(new Reservation("브라운2", themeSlot.getId(), themeSlot.getDate(), themeSlot.getTime(), themeSlot.getTheme()));
        jdbcReservationRepository.save(new Reservation("브라운3", themeSlot.getId(), themeSlot.getDate(), themeSlot.getTime(), themeSlot.getTheme()));

        List<Reservation> pendingReservations = jdbcReservationRepository.findByThemeSlotAndPending(
                themeSlot.getId());

        assertThat(pendingReservations.get(0).getReservationStatus()).isEqualTo(PendingStatus.getInstance());
        assertThat(pendingReservations.get(1).getReservationStatus()).isEqualTo(PendingStatus.getInstance());
        assertThat(pendingReservations.get(2).getReservationStatus()).isEqualTo(PendingStatus.getInstance());
    }

    @Test
    @DisplayName("같은 테마 슬롯 조건의 PENDING 예약을 ID순으로 가장 먼저 조회한다.")
    void findFirstPendingReservationByThemeSlotOrderByIdAsc() {
        ThemeSlot themeSlot = saveThemeSlot(THEME_1, LocalDate.now(), TIME_10, false);
        jdbcReservationRepository.save(new Reservation(1L, "브라운", themeSlot.getId(), themeSlot.getDate(), themeSlot.getTime(), themeSlot.getTheme(), ConfirmedStatus.getInstance()));
        jdbcReservationRepository.save(new Reservation("브라운1", themeSlot.getId(), themeSlot.getDate(), themeSlot.getTime(), themeSlot.getTheme()));
        jdbcReservationRepository.save(new Reservation("브라운2", themeSlot.getId(), themeSlot.getDate(), themeSlot.getTime(), themeSlot.getTheme()));
        jdbcReservationRepository.save(new Reservation("브라운3", themeSlot.getId(), themeSlot.getDate(), themeSlot.getTime(), themeSlot.getTheme()));

        Optional<Reservation> reservation = jdbcReservationRepository.findRecentReservationByThemeSlot(
                themeSlot.getId());
        assertThat(reservation).isNotEmpty();
        assertThat(reservation.get().getName()).isEqualTo("브라운1");

    }
}
