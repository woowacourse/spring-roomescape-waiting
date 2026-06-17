package roomescape.infra;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import roomescape.config.TestClockConfig;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationTime;
import roomescape.domain.Slot;
import roomescape.domain.Theme;
import roomescape.repository.ReservationRepository;
import roomescape.repository.ReservationTimeRepository;
import roomescape.repository.SlotRepository;
import roomescape.repository.ThemeRepository;

@DataJpaTest
@Import({
    JdbcReservationRepository.class,
    JdbcSlotRepository.class,
    TestClockConfig.class
})
class JdbcReservationRepositoryTest {

    private static final LocalDate FUTURE_SECOND_DATE = LocalDate.now().plusDays(2);
    private static final LocalDate FUTURE_THIRD_DATE = LocalDate.now().plusDays(3);
    private static final LocalTime TEN = LocalTime.of(10, 0);
    private static final LocalTime TWELVE = LocalTime.of(12, 0);

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private ReservationTimeRepository timeRepository;

    @Autowired
    private ThemeRepository themeRepository;

    @Autowired
    private SlotRepository slotRepository;

    @Test
    void 예약을_저장한다() {
        ReservationTime reservationTime = createReservationTime(LocalTime.of(10, 0));
        Theme theme = createTheme();

        Long saveId = reservationRepository.save(createReservation(
            "브라운", FUTURE_THIRD_DATE, reservationTime, theme));

        Optional<Reservation> reservation = reservationRepository.findById(saveId);

        assertThat(reservation).isPresent();

        assertThat(reservation.get().getId()).isNotNull();
        assertThat(reservation.get().getName()).isEqualTo("브라운");
        assertThat(reservation.get().getDate()).isEqualTo(FUTURE_THIRD_DATE);
        assertThat(reservation.get().getTime().getId()).isEqualTo(reservationTime.getId());
        assertThat(reservation.get().getTheme().getId()).isEqualTo(theme.getId());
    }

    @Test
    void 모든_예약을_날짜_내림차순_시간_오름차순으로_조회한다() {
        ReservationTime tenClock = createReservationTime(TEN);
        ReservationTime twelveClock = createReservationTime(TWELVE);
        Theme theme = createTheme();

        reservationRepository.save(createReservation(
            "브리", FUTURE_SECOND_DATE, tenClock, theme));
        reservationRepository.save(createReservation(
            "브라운", FUTURE_THIRD_DATE, twelveClock, theme));
        reservationRepository.save(createReservation(
            "브리", FUTURE_THIRD_DATE, tenClock, theme));

        List<Reservation> reservations = reservationRepository.findAll();

        assertThat(reservations).hasSize(3);

        Reservation firstResult = reservations.getFirst();
        assertThat(firstResult.getName()).isEqualTo("브리");
        assertThat(firstResult.getDate()).isEqualTo(FUTURE_THIRD_DATE);
        assertThat(firstResult.getTime().getId()).isEqualTo(tenClock.getId());
        assertThat(firstResult.getTheme().getId()).isEqualTo(theme.getId());
    }

    @Test
    void 예약_id에_맞는_예약을_조회한다() {
        ReservationTime tenClock = createReservationTime(TEN);
        ReservationTime twelveClock = createReservationTime(TWELVE);
        Theme theme = createTheme();

        reservationRepository.save(createReservation(
            "브라운", FUTURE_THIRD_DATE, twelveClock, theme));
        Long findId = reservationRepository.save(createReservation(
            "브리", FUTURE_THIRD_DATE, tenClock, theme));

        Optional<Reservation> reservation = reservationRepository.findById(findId);

        assertThat(reservation).isPresent();

        assertThat(reservation.get().getId()).isNotNull();
        assertThat(reservation.get().getName()).isEqualTo("브리");
        assertThat(reservation.get().getDate()).isEqualTo(FUTURE_THIRD_DATE);
        assertThat(reservation.get().getTime().getId()).isEqualTo(tenClock.getId());
        assertThat(reservation.get().getTheme().getId()).isEqualTo(theme.getId());
    }

    @Test
    void 날짜_테마에_따른_예약_id_조회() {
        LocalDate findDate = FUTURE_THIRD_DATE;
        ReservationTime tenClock = createReservationTime(TEN);
        ReservationTime twelveClock = createReservationTime(TWELVE);
        Theme findTheme = createTheme();

        Long firstSaveId = reservationRepository.save(createReservation(
            "브라운", findDate, twelveClock, findTheme));
        Long secondSaveId = reservationRepository.save(createReservation(
            "브리", findDate, tenClock, findTheme));
        Long thirdSaveId = reservationRepository.save(createReservation(
            "브리", FUTURE_SECOND_DATE, tenClock, findTheme));

        Set<Long> findReservationsId = reservationRepository.findReservedTimeIdsByDateAndThemeId(findDate,
            findTheme.getId());

        assertThat(findReservationsId).hasSize(2);
        assertThat(findReservationsId.contains(firstSaveId)).isTrue();
        assertThat(findReservationsId.contains(secondSaveId)).isTrue();
        assertThat(findReservationsId.contains(thirdSaveId)).isFalse();
    }

    @Test
    void 시간_id_존재여부() {
        ReservationTime reservationTime = createReservationTime(TEN);
        Theme theme = createTheme();

        reservationRepository.save(createReservation(
            "브라운", FUTURE_THIRD_DATE, reservationTime, theme));

        assertThat(reservationRepository.existsByTimeId(reservationTime.getId())).isTrue();
        assertThat(reservationRepository.existsByTimeId(2L)).isFalse();
    }

    @Test
    void 테마_id_존재여부() {
        ReservationTime reservationTime = createReservationTime(TEN);
        Theme theme = createTheme();

        reservationRepository.save(createReservation(
            "브라운", FUTURE_THIRD_DATE, reservationTime, theme));

        assertThat(reservationRepository.existsByThemeId(theme.getId())).isTrue();
        assertThat(reservationRepository.existsByThemeId(2L)).isFalse();
    }

    @Test
    void 날짜_시간_테마_id가_존재한다() {
        LocalDate findDate = FUTURE_THIRD_DATE;
        ReservationTime reservationTime = createReservationTime(TEN);
        Theme theme = createTheme();

        Reservation reservation = createReservation("브라운", findDate, reservationTime, theme);
        reservationRepository.save(reservation);

        assertThat(reservationRepository.existsBy(reservation)).isTrue();
    }

    @Test
    void 예약을_삭제한다() {
        ReservationTime reservationTime = createReservationTime(TEN);
        Theme theme = createTheme();

        Long saveId = reservationRepository.save(createReservation(
            "브라운", FUTURE_SECOND_DATE, reservationTime, theme));
        reservationRepository.deleteById(saveId);

        assertThat(reservationRepository.findById(saveId)).isEmpty();
    }

    @Test
    void 이름으로_예약을_조회한다() {
        ReservationTime reservationTime = createReservationTime(TEN);
        ReservationTime anotherTime = createReservationTime(TWELVE);
        Theme theme = createTheme();

        reservationRepository.save(createReservation("브리", FUTURE_SECOND_DATE, reservationTime, theme));
        reservationRepository.save(createReservation("네오", FUTURE_SECOND_DATE, anotherTime, theme));
        reservationRepository.save(createReservation("네오", FUTURE_THIRD_DATE, reservationTime, theme));

        List<Reservation> neoReservations = reservationRepository.findByName("네오");

        assertThat(neoReservations).hasSize(2);
        assertThat(neoReservations)
            .extracting(Reservation::getName)
            .containsOnly("네오");
    }

    private ReservationTime createReservationTime(LocalTime time) {
        ReservationTime reservationTime = new ReservationTime(time);
        Long id = timeRepository.save(reservationTime).getId();
        return new ReservationTime(id, reservationTime.getStartAt());
    }

    private Theme createTheme() {
        Theme theme = new Theme("방탈출 제목", "방탈출 설명", "thumbnail.png");
        Long id = themeRepository.save(theme).getId();
        return new Theme(
            id,
            theme.getName(),
            theme.getDescription(),
            theme.getThumbnailImageUrl()
        );
    }

    private Reservation createReservation(String name, LocalDate date, ReservationTime time, Theme theme) {
        Slot slot = slotRepository.getOrCreate(Slot.of(date, time, theme));
        return new Reservation(name, slot);
    }
}
