package roomescape.reservation.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import roomescape.global.exception.ConflictException;
import roomescape.global.exception.InvalidRequestException;
import roomescape.global.exception.NotFoundException;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.repository.ReservationRepository;
import roomescape.reservationtime.domain.ReservationTime;
import roomescape.reservationtime.repository.ReservationTimeRepository;
import roomescape.theme.domain.Theme;
import roomescape.theme.repository.ThemeRepository;

import java.time.LocalDate;
import java.time.LocalTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Transactional
class ReservationServiceTest {

    private static final String NAME = "브라운";
    private static final String OTHER_NAME = "레아";
    private static final LocalDate TODAY = LocalDate.now();
    private static final LocalDate FUTURE_DATE = TODAY.plusDays(1);
    private static final LocalDate NEXT_FUTURE_DATE = TODAY.plusDays(2);
    private static final LocalDate PAST_DATE = TODAY.minusDays(1);
    private static final long NOT_FOUND_ID = 37L;

    @Autowired
    private ReservationService reservationService;

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private ReservationTimeRepository reservationTimeRepository;

    @Autowired
    private ThemeRepository themeRepository;

    @Test
    @DisplayName("예약을 생성한다.")
    public void create_success() {
        // given
        ReservationTime time = saveReservationTime(10);
        Theme theme = saveTheme();

        // when
        Reservation reservation = createReservation(NAME, FUTURE_DATE, time, theme);

        // then
        assertThat(reservationService.findAll()).containsExactly(reservation);
    }

    @Test
    @DisplayName("이미 예약된 날짜, 시간, 테마로 예약하면 예외가 발생한다.")
    public void create_fail_whenDuplicatedReservation() {
        // given
        ReservationTime time = saveReservationTime(10);
        Theme theme = saveTheme();

        createReservation(NAME, FUTURE_DATE, time, theme);

        // when, then
        assertThatThrownBy(() -> createReservation(NAME, FUTURE_DATE, time, theme))
                .isInstanceOf(ConflictException.class);
    }

    @Test
    @DisplayName("현재 시각보다 이전 날짜와 시간으로 예약하면 예외가 발생한다.")
    public void create_fail_whenPastDateTime() {
        // given
        ReservationTime time = saveReservationTime(14);
        Theme theme = saveTheme();

        // when, then
        assertThatThrownBy(() -> createReservation(NAME, PAST_DATE, time, theme))
                .isInstanceOf(InvalidRequestException.class);

        assertThat(reservationService.findAll()).isEmpty();
    }

    @Test
    @DisplayName("존재하지 않는 예약 시간으로 예약하면 예외가 발생한다.")
    public void create_fail_whenReservationTimeNotFound() {
        // given
        Theme theme = saveTheme();

        // when, then
        assertThatThrownBy(() -> reservationService.create(NAME, FUTURE_DATE, NOT_FOUND_ID, theme.getId()))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    @DisplayName("존재하지 않는 테마로 예약하면 예외가 발생한다.")
    public void create_fail_whenThemeNotFound() {
        // given
        ReservationTime time = saveReservationTime(10);

        // when, then
        assertThatThrownBy(() -> reservationService.create(NAME, FUTURE_DATE, time.getId(), NOT_FOUND_ID))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    @DisplayName("예약을 삭제한다.")
    public void delete_success() {
        // given
        Reservation reservation = createDefaultReservation();

        // when
        reservationService.delete(reservation.getId());

        // then
        assertThat(reservationService.findAll()).isEmpty();
    }

    @Test
    @DisplayName("존재하지 않는 예약 삭제를 요청해도 성공한다.")
    public void delete_success_whenReservationNotFound() {
        // when
        reservationService.delete(NOT_FOUND_ID);

        // then
        assertThat(reservationService.findAll()).isEmpty();
    }

    @Test
    @DisplayName("사용자가 본인의 예약을 취소한다.")
    public void cancel_success() {
        // given
        Reservation reservation = createDefaultReservation();

        // when
        reservationService.cancel(reservation.getId(), NAME);

        // then
        assertThat(reservationService.findAll()).isEmpty();
    }

    @Test
    @DisplayName("해당 이름으로 예약을 찾을 수 없으면 예약 취소 시 예외가 발생한다.")
    public void cancel_fail_whenReservationNotFoundByName() {
        // given
        Reservation reservation = createDefaultReservation();

        // when, then
        assertThatThrownBy(() -> reservationService.cancel(reservation.getId(), OTHER_NAME))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    @DisplayName("존재하지 않는 예약 취소를 요청해도 성공한다.")
    public void cancel_success_whenReservationNotFound() {
        // when
        reservationService.cancel(NOT_FOUND_ID, NAME);

        // then
        assertThat(reservationService.findAll()).isEmpty();
    }

    @Test
    @DisplayName("이미 지난 예약을 취소하면 예외가 발생한다.")
    public void cancel_fail_whenPastReservation() {
        // given
        Reservation reservation = savePastReservation();

        // when, then
        assertThatThrownBy(() -> reservationService.cancel(reservation.getId(), NAME))
                .isInstanceOf(InvalidRequestException.class);
    }

    @Test
    @DisplayName("사용자가 본인 예약의 날짜와 시간을 변경한다.")
    public void updateDateTime_success() {
        // given
        Reservation reservation = createDefaultReservation();
        ReservationTime newTime = saveReservationTime(11);

        // when
        Reservation updatedReservation = reservationService.updateDateTime(
                reservation.getId(),
                NAME,
                NEXT_FUTURE_DATE,
                newTime.getId()
        );

        // then
        assertThat(updatedReservation.getDate()).isEqualTo(NEXT_FUTURE_DATE);
        assertThat(updatedReservation.getTime()).isEqualTo(newTime);

        Reservation savedReservation = reservationService.findAll().get(0);
        assertThat(savedReservation.getDate()).isEqualTo(NEXT_FUTURE_DATE);
        assertThat(savedReservation.getTime()).isEqualTo(newTime);
    }

    @Test
    @DisplayName("이미 예약된 날짜, 시간, 테마로 예약을 변경하면 예외가 발생한다.")
    public void updateDateTime_fail_whenDuplicatedReservation() {
        // given
        ReservationTime time = saveReservationTime(10);
        ReservationTime occupiedTime = saveReservationTime(11);
        Theme theme = saveTheme();

        Reservation reservation = createReservation(NAME, FUTURE_DATE, time, theme);
        createReservation(OTHER_NAME, NEXT_FUTURE_DATE, occupiedTime, theme);

        // when, then
        assertThatThrownBy(() -> reservationService.updateDateTime(
                reservation.getId(),
                NAME,
                NEXT_FUTURE_DATE,
                occupiedTime.getId()
        ))
                .isInstanceOf(ConflictException.class);
    }

    @Test
    @DisplayName("현재 예약과 같은 날짜와 시간으로 변경하면 예외가 발생한다.")
    public void updateDateTime_fail_whenSameSchedule() {
        // given
        ReservationTime time = saveReservationTime(10);
        Theme theme = saveTheme();
        Reservation reservation = createReservation(NAME, FUTURE_DATE, time, theme);

        // when, then
        assertThatThrownBy(() -> reservationService.updateDateTime(
                reservation.getId(),
                NAME,
                FUTURE_DATE,
                time.getId()
        ))
                .isInstanceOf(InvalidRequestException.class);
    }

    @Test
    @DisplayName("현재 시각보다 이전 날짜와 시간으로 예약을 변경하면 예외가 발생한다.")
    public void updateDateTime_fail_whenPastDateTime() {
        // given
        Reservation reservation = createDefaultReservation();
        ReservationTime pastTime = saveReservationTime(14);

        // when, then
        assertThatThrownBy(() -> reservationService.updateDateTime(
                reservation.getId(),
                NAME,
                PAST_DATE,
                pastTime.getId()
        ))
                .isInstanceOf(InvalidRequestException.class);
    }

    @Test
    @DisplayName("이미 지난 예약을 변경하면 예외가 발생한다.")
    public void updateDateTime_fail_whenPastReservation() {
        // given
        Reservation reservation = savePastReservation();
        ReservationTime newTime = saveReservationTime(11);

        // when, then
        assertThatThrownBy(() -> reservationService.updateDateTime(
                reservation.getId(),
                NAME,
                FUTURE_DATE,
                newTime.getId()
        ))
                .isInstanceOf(InvalidRequestException.class);
    }

    private Reservation createDefaultReservation() {
        return createReservation(NAME, FUTURE_DATE, saveReservationTime(10), saveTheme());
    }

    private Reservation createReservation(String name, LocalDate date, ReservationTime time, Theme theme) {
        return reservationService.create(name, date, time.getId(), theme.getId());
    }

    private Reservation savePastReservation() {
        return reservationRepository.save(new Reservation(NAME, PAST_DATE, saveReservationTime(14), saveTheme()));
    }

    private ReservationTime saveReservationTime(int hour) {
        return reservationTimeRepository.save(new ReservationTime(LocalTime.of(hour, 0)));
    }

    private Theme saveTheme() {
        return themeRepository.save(new Theme(
                "레벨2 탈출",
                "우테코 레벨2를 탈출하는 내용입니다.",
                "https://example.com/theme.png"
        ));
    }
}
