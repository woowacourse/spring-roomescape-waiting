package roomescape.reservation.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import roomescape.common.exception.DomainException;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.Status;
import roomescape.reservation.repository.JdbcReservationRepository;
import roomescape.reservation.repository.ReservationRepository;
import roomescape.reservation.repository.dto.ReservationWaitingDto;
import roomescape.reservation.service.dto.ReservationWaitingResult;
import roomescape.reservation.service.validator.ReservationValidator;
import roomescape.reservationtime.domain.ReservationTime;
import roomescape.reservationtime.repository.JdbcReservationTimeRepository;
import roomescape.reservationtime.repository.ReservationTimeRepository;
import roomescape.test_config.MutableClock;
import roomescape.test_config.TestClockConfig;
import roomescape.theme.domain.Theme;
import roomescape.theme.repository.JdbcThemeRepository;
import roomescape.theme.repository.ThemeRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import static org.assertj.core.api.Assertions.*;
import static roomescape.reservation.domain.Status.*;
import static roomescape.reservation.exception.ReservationErrorCode.*;
import static roomescape.reservationtime.exeption.ReservationTimeErrorCode.*;

@JdbcTest
@Import({
        TestClockConfig.class,
        ReservationService.class,
        JdbcReservationRepository.class,
        JdbcReservationTimeRepository.class,
        JdbcThemeRepository.class,
        ReservationValidator.class
})
class ReservationServiceTest {

    @Autowired
    ReservationService reservationService;

    @Autowired
    ReservationRepository reservationRepository;

    @Autowired
    ReservationTimeRepository reservationTimeRepository;

    @Autowired
    ThemeRepository themeRepository;

    @Autowired
    MutableClock clock;


    @Test
    @DisplayName("해당 날짜, 시간, 테마에 처음으로 예약을 추가하면 예약이 확정된다.")
    public void create_success1() {
        // given
        ReservationTime time = insertReservationTime(LocalTime.of(10, 0));
        Theme theme = insertTheme("레벨2 탈출", "우테코 레벨2를 탈출하는 내용입니다.", "https://example.com/theme.png");
        LocalDate date = LocalDate.of(2025, 5, 11);

        clock.setFixed(LocalDate.of(2025, 5, 10));

        // when
        ReservationWaitingResult reservationWaitingResult =
                reservationService.create("포비", date, time.getId(), theme.getId());

        // then
        assertThat(reservationWaitingResult.status()).isEqualTo(CONFIRMED);
    }

    @Test
    @DisplayName("예약이 존재하는 날짜, 시간, 테마로 새로운 예약을 추가하면 예약이 대기 상태로 들어간다.")
    public void create_success2() {
        // given
        clock.setFixed(LocalDate.of(2025, 5, 10));

        ReservationTime time = insertReservationTime(LocalTime.of(10, 0));
        Theme theme = insertTheme("레벨2 탈출", "우테코 레벨2를 탈출하는 내용입니다.", "https://example.com/theme.png");
        LocalDate date = LocalDate.of(2025, 5, 11);
        insertReservation("포비", date, time, theme, CONFIRMED);

        // when
        ReservationWaitingResult reservationWaitingResult =
                reservationService.create("브라운", date, time.getId(), theme.getId());

        // then
        assertThat(reservationWaitingResult.status()).isEqualTo(Status.WAITING);
    }

    @Test
    @DisplayName("이미 지난 날짜 및 시간으로 예약하려는 경우 예외가 발생한다.")
    public void create_fail2() {
        // given
        ReservationTime time = insertReservationTime(LocalTime.of(10, 0));
        Theme theme = insertTheme("레벨2 탈출", "우테코 레벨2를 탈출하는 내용입니다.", "https://example.com/theme.png");
        LocalDate pastDate = LocalDate.of(2023, 8, 5);
        LocalDate currentDate = LocalDate.of(2025, 5, 11);

        clock.setFixed(currentDate);

        // when, then
        assertThatThrownBy(() -> reservationService.create("포비", pastDate, time.getId(), theme.getId()))
                .isInstanceOf(DomainException.class)
                .hasMessage(PAST_RESERVATION_NOT_ALLOWED.message());
    }

    @Test
    @DisplayName("예약을 취소할 때 확정된 상태의 예약을 취소하면 기존 대기 중인 예약 중 가장 우선순위 높은 예약이 확정 상태로 변한다. ")
    public void cancel_success() {
        // given
        Theme theme = insertTheme("레벨2 탈출", "우테코 레벨2를 탈출하는 내용입니다.", "https://example.com/theme.png");
        ReservationTime time = insertReservationTime(LocalTime.of(10, 0));
        LocalDate date = LocalDate.of(2023, 8, 10);

        Reservation cancel = insertReservation("브라운", date, time, theme, CONFIRMED);
        Reservation waiting1 = insertReservation("포비", date, time, theme, WAITING);
        Reservation waiting2 = insertReservation("브리", date, time, theme, WAITING);

        // when
        reservationService.cancel(cancel.getId());

        // then
        Reservation updatedWaiting = reservationRepository.findById(waiting1.getId()).get();
        assertThat(updatedWaiting.getStatus()).isEqualTo(CONFIRMED);
    }

    @Test
    @DisplayName("해당 예약이 존재하지 않으면 취소할 수 없기 때문에 예외가 발생한다.")
    public void cancel_fail() {
        // given
        Long id = 1L;

        // when, then
        assertThatThrownBy(() -> reservationService.cancel(id))
                .isInstanceOf(DomainException.class)
                .hasMessage(RESERVATION_NOT_FOUND.message());
    }

    @Test
    @DisplayName("본인의 예약을 취소하면 해당 예약의 상태가 취소됨으로 변경된다.")
    public void cancelMine_success1() {
        // given
        clock.setFixed(LocalDate.of(2023, 7, 6));

        ReservationTime time = insertReservationTime(LocalTime.of(10, 0));
        Theme theme = insertTheme("레벨2 탈출", "우테코 레벨2를 탈출하는 내용입니다.", "https://example.com/theme.png");
        Reservation reservation = insertReservation("브리", LocalDate.of(2023, 8, 10), time, theme, WAITING);

        // when
        reservationService.cancelMine(reservation.getId(), reservation.getGuestName());

        // then
        assertThat(reservationRepository.findById(reservation.getId()).get().getStatus()).isEqualTo(Status.CANCELED);
    }

    @Test
    @DisplayName("본인의 예약을 취소할 때 확정된 상태의 예약을 취소하면 기존 대기 중인 예약 중 가장 우선순위 높은 예약이 확정 상태로 변한다.")
    public void cancelMine_success2() {
        // given
        clock.setFixed(LocalDate.of(2023, 7, 6));
        Theme theme = insertTheme("레벨2 탈출", "우테코 레벨2를 탈출하는 내용입니다.", "https://example.com/theme.png");
        ReservationTime time = insertReservationTime(LocalTime.of(10, 0));
        LocalDate date = LocalDate.of(2023, 8, 10);

        Reservation cancel = insertReservation("브라운", date, time, theme, CONFIRMED);
        Reservation waiting1 = insertReservation("포비", date, time, theme, WAITING);
        Reservation waiting2 = insertReservation("브리", date, time, theme, WAITING);

        // when
        reservationService.cancelMine(cancel.getId(), cancel.getGuestName());

        // then
        Reservation updatedWaiting = reservationRepository.findById(waiting1.getId()).get();
        assertThat(updatedWaiting.getStatus()).isEqualTo(CONFIRMED);
    }


    @Test
    @DisplayName("해당 예약이 존재하지 않으면 본인의 예약을 삭제할 수 없기 때문에 예외가 발생한다.")
    public void cancelMine_fail1() {
        // given
        Long id = 1L;

        // when, then
        assertThatThrownBy(() -> reservationService.cancelMine(id, "브라운"))
                .isInstanceOf(DomainException.class)
                .hasMessage(RESERVATION_NOT_FOUND.message());
    }

    @Test
    @DisplayName("이미 시작된 예약은 삭제할 수 없다.")
    public void cancelMine_fail2() {
        // given
        clock.setFixed(LocalDate.of(2023, 8, 11));

        ReservationTime time = insertReservationTime(LocalTime.of(10, 0));
        Theme theme = insertTheme("레벨2 탈출", "우테코 레벨2를 탈출하는 내용입니다.", "https://example.com/theme.png");
        Reservation reservation = insertReservation("브라운", LocalDate.of(2023, 8, 10), time, theme, WAITING);

        // when, then
        assertThatThrownBy(() -> reservationService.cancelMine(reservation.getId(), reservation.getGuestName()))
                .isInstanceOf(DomainException.class)
                .hasMessage(CANNOT_EDIT_ALREADY_STARTED_RESERVATION.message());
    }

    @Test
    @DisplayName("본인의 예약이 아니면 삭제할 수 없기 때문에 예외가 발생한다.")
    public void cancelMine_fail3() {
        // given
        clock.setFixed(LocalDate.of(2023, 7, 6));

        ReservationTime time = insertReservationTime(LocalTime.of(10, 0));
        Theme theme = insertTheme("레벨2 탈출", "우테코 레벨2를 탈출하는 내용입니다.", "https://example.com/theme.png");
        Reservation reservation = insertReservation("브라운", LocalDate.of(2023, 8, 10), time, theme, WAITING);

        // when, then
        assertThatThrownBy(() -> reservationService.cancelMine(reservation.getId(), "포비"))
                .isInstanceOf(DomainException.class)
                .hasMessage(CANNOT_EDIT_OTHER_GUEST_RESERVATION.message());
    }

    @Test
    @DisplayName("예약의 날짜 및 시간을 수정한다.")
    public void editDateTime_success1() {
        // given
        ReservationTime existTime = insertReservationTime(LocalTime.of(10, 0));
        LocalDate existDate = LocalDate.of(2023, 8, 5);
        Theme theme = insertTheme("레벨2 탈출", "우테코 레벨2를 탈출하는 내용입니다.", "https://example.com/theme.png");
        Reservation reservation = insertReservation("브라운", existDate, existTime, theme, WAITING);

        LocalDate editedDate = LocalDate.of(2023, 8, 10);
        ReservationTime editedTime = insertReservationTime(LocalTime.of(12, 0));

        clock.setFixed(LocalDate.of(2023, 7, 20));

        // when
        reservationService.editDateTime(reservation.getId(), editedDate, editedTime.getId(), reservation.getGuestName());

        // then
        ReservationWaitingDto reservationWaitingDto = reservationRepository.findWaitingById(reservation.getId()).get();

        assertThat(reservationWaitingDto)
                .extracting(ReservationWaitingDto::date, r -> r.time().getId())
                .containsExactly(editedDate, editedTime.getId());
    }

    @Test
    @DisplayName("확정된 상태의 예약을 수정하면 기존 대기 중인 예약 중 가장 우선순위 높은 예약이 확정 상태로 변한다.")
    public void editDateTime_success2() {
        // given
        clock.setFixed(LocalDate.of(2023, 7, 6));
        Theme theme = insertTheme("레벨2 탈출", "우테코 레벨2를 탈출하는 내용입니다.", "https://example.com/theme.png");
        ReservationTime time = insertReservationTime(LocalTime.of(10, 0));
        LocalDate date = LocalDate.of(2023, 8, 10);

        Reservation reservation = insertReservation("브라운", date, time, theme, CONFIRMED);
        Reservation waiting1 = insertReservation("포비", date, time, theme, WAITING);
        Reservation waiting2 = insertReservation("브리", date, time, theme, WAITING);


        // when
        reservationService.editDateTime(
                reservation.getId(), LocalDate.of(2023, 8, 12), time.getId(), "브라운");

        // then
        Reservation updatedWaiting = reservationRepository.findById(waiting1.getId()).get();
        assertThat(updatedWaiting.getStatus()).isEqualTo(CONFIRMED);
    }

    @Test
    @DisplayName("수정하려는 예약이 존재하지 않으면 예외가 발생한다.")
    public void editDateTime_fail1() {
        // given
        Long reservationId = 1L;
        LocalDate editedDate = LocalDate.of(2023, 8, 10);
        ReservationTime editedTime = insertReservationTime(LocalTime.of(12, 0));

        // when then
        assertThatThrownBy(() -> reservationService.editDateTime(reservationId, editedDate, editedTime.getId(), "브라운"))
                .isInstanceOf(DomainException.class)
                .hasMessage(RESERVATION_NOT_FOUND.message());
    }

    @Test
    @DisplayName("수정하려는 예약 시간이 존재하지 않으면 예외가 발생한다.")
    public void editDateTime_fail2() {
        // given
        clock.setFixed(LocalDate.of(2023, 7, 20));

        ReservationTime existTime = insertReservationTime(LocalTime.of(10, 0));
        LocalDate existDate = LocalDate.of(2023, 8, 5);
        Theme theme = insertTheme("레벨2 탈출", "우테코 레벨2를 탈출하는 내용입니다.", "https://example.com/theme.png");
        Reservation reservation = insertReservation("브라운", existDate, existTime, theme, WAITING);

        LocalDate editedDate = LocalDate.of(2023, 8, 10);
        Long editedTimeId = 999L;

        // when then
        assertThatThrownBy(() -> reservationService.editDateTime(reservation.getId(), editedDate, editedTimeId, reservation.getGuestName()))
                .isInstanceOf(DomainException.class)
                .hasMessage(RESERVATION_TIME_NOT_FOUND.message());
    }

    @Test
    @DisplayName("이미 시작된 예약은 수정할 수 없다.")
    public void editDateTime_fail3() {
        // given
        clock.setFixed(LocalDate.of(2023, 8, 6));

        ReservationTime existTime = insertReservationTime(LocalTime.of(10, 0));
        LocalDate existDate = LocalDate.of(2023, 8, 5);
        Theme theme = insertTheme("레벨2 탈출", "우테코 레벨2를 탈출하는 내용입니다.", "https://example.com/theme.png");
        Reservation reservation = insertReservation("브라운", existDate, existTime, theme, WAITING);

        LocalDate editedDate = LocalDate.of(2023, 8, 10);
        ReservationTime editedTime = insertReservationTime(LocalTime.of(12, 0));

        // when then
        assertThatThrownBy(() -> reservationService.editDateTime(reservation.getId(), editedDate, editedTime.getId(), reservation.getGuestName()))
                .isInstanceOf(DomainException.class)
                .hasMessage(CANNOT_EDIT_ALREADY_STARTED_RESERVATION.message());
    }

    @Test
    @DisplayName("수정하려는 날짜 및 시간에 예약이 존재하면 대기 상태가 된다.")
    public void editDateTime_fail4() {
        // given
        clock.setFixed(LocalDate.of(2023, 7, 6));

        Theme theme = insertTheme("레벨2 탈출", "우테코 레벨2를 탈출하는 내용입니다.", "https://example.com/theme.png");

        LocalDate editedDate = LocalDate.of(2023, 8, 10);
        ReservationTime editedTime = insertReservationTime(LocalTime.of(10, 0));

        insertReservation("브라운", editedDate, editedTime, theme, CONFIRMED);

        LocalDate existDate = LocalDate.of(2023, 8, 6);
        ReservationTime existTime = insertReservationTime(LocalTime.of(12, 0));
        Reservation reservation = insertReservation("포비", existDate, existTime, theme, CONFIRMED);

        reservationService.editDateTime(reservation.getId(), editedDate, editedTime.getId(), "포비");

        // then
        assertThat(reservationRepository.findById(reservation.getId()).get().getStatus())
                .isEqualTo(Status.WAITING);
    }

    @Test
    @DisplayName("수정하려는 날짜 및 시간에 예약이 존재는 하는데 그게 본인의 예약인 경우 예외가 발생한다.")
    public void editDateTime_fail4_2() {
        // given
        clock.setFixed(LocalDate.of(2023, 7, 6));

        Theme theme = insertTheme("레벨2 탈출", "우테코 레벨2를 탈출하는 내용입니다.", "https://example.com/theme.png");
        LocalDate date = LocalDate.of(2023, 8, 10);
        ReservationTime time = insertReservationTime(LocalTime.of(10, 0));

        Reservation reservation = insertReservation("브라운", date, time, theme, Status.WAITING);

        // when then
        assertThatThrownBy(() -> reservationService.editDateTime(reservation.getId(), date, time.getId(), reservation.getGuestName()))
                .isInstanceOf(DomainException.class)
                .hasMessage(RESERVATION_ALREADY_EXISTS.message());
    }

    @ParameterizedTest
    @CsvSource({
            "2023-07-05, 10:00", // 날짜가 지난 경우
            "2023-07-06, 09:59", // 시간이 지난 경우
    })
    @DisplayName("이미 지난 날짜 및 시간으로 예약을 수정하려는 경우 예외가 발생한다.")
    public void editDateTime_fail5(LocalDate ed, LocalTime et) {
        // given
        clock.setFixed(LocalDateTime.of(2023, 7, 6, 10, 0));
        LocalDate existDate = LocalDate.of(2023, 8, 6);
        ReservationTime existTime = insertReservationTime(LocalTime.of(12, 0));
        Theme theme = insertTheme("레벨2 탈출", "우테코 레벨2를 탈출하는 내용입니다.", "https://example.com/theme.png");
        Reservation reservation = insertReservation("브라운", existDate, existTime, theme, WAITING);

        ReservationTime editedTime = insertReservationTime(et);

        // when then
        assertThatThrownBy(() -> reservationService.editDateTime(reservation.getId(), ed, editedTime.getId(), reservation.getGuestName()))
                .isInstanceOf(DomainException.class)
                .hasMessage(PAST_RESERVATION_NOT_ALLOWED.message());
    }

    @Test
    @DisplayName("본인의 예약이 아니면 예외가 발생한다.")
    public void editDateTime_fail6() {
        // given
        clock.setFixed(LocalDate.of(2023, 7, 6));

        Theme theme = insertTheme("레벨2 탈출", "우테코 레벨2를 탈출하는 내용입니다.", "https://example.com/theme.png");

        ReservationTime time = insertReservationTime(LocalTime.of(10, 0));

        Reservation reservation = insertReservation("브라운", LocalDate.of(2023, 8, 10), time, theme, CONFIRMED);

        // when then
        assertThatThrownBy(() -> reservationService.editDateTime(
                reservation.getId(), reservation.getDate(), time.getId(), "other_guest"))
                .isInstanceOf(DomainException.class)
                .hasMessage(CANNOT_EDIT_OTHER_GUEST_RESERVATION.message());
    }


    private ReservationTime insertReservationTime(LocalTime startAt) {
        return reservationTimeRepository.save(ReservationTime.create(startAt));
    }

    private Theme insertTheme(String name, String description, String thumbnail) {
        return themeRepository.save(Theme.create(name, description, thumbnail));
    }

    private Reservation insertReservation(String name, LocalDate date, ReservationTime time, Theme theme, Status status) {
        return reservationRepository.save(Reservation.create(name, date, time, theme, status, LocalDateTime.now(clock)));
    }
}
