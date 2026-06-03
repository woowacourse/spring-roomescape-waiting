package roomescape.service;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import roomescape.domain.Reservation;
import roomescape.domain.Theme;
import roomescape.domain.TimeSlot;
import roomescape.domain.Waiting;
import roomescape.exception.DuplicateException;
import roomescape.exception.NotFoundException;
import roomescape.exception.PastTimeException;
import roomescape.repository.ReservationRepository;
import roomescape.repository.WaitingRepository;

@ExtendWith(MockitoExtension.class)
class WaitingServiceTest {

    @Mock
    private WaitingRepository waitingRepository;

    @Mock
    private ReservationRepository reservationRepository;

    private WaitingService waitingService;

    @BeforeEach
    void setUp() {
        waitingService = new WaitingService(waitingRepository, reservationRepository);
    }

    @Test
    @DisplayName("존재하는 예약 대기를 삭제한다.")
    void 대기_삭제() {
        Waiting waiting = createWaitingEntity();
        given(waitingRepository.findById(1L)).willReturn(Optional.of(waiting));

        assertThatCode(() -> waitingService.removeWaiting(1L, "브라운")).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("존재하지 않는 예약 대기를 삭제하면, 예외가 발생한다.")
    void 존재하지_않는_대기_삭제_예외_발생() {
        given(waitingRepository.findById(1L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> waitingService.removeWaiting(1L, "브라운"))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("해당하는 예약 대기 정보를 찾을 수 없습니다.");
    }

    @Test
    @DisplayName("존재하는 예약 대기를 중복해서 저장하면, 예외가 발생한다.")
    void 중복_대기_예외_발생() {
        Reservation reservation = createReservation("네오", LocalDate.now().plusDays(1), LocalTime.of(10, 0));
        given(reservationRepository.findByDateAndTimeIdAndThemeId(reservation.getDate(), 1L, 1L))
                .willReturn(Optional.of(reservation));
        given(waitingRepository.exists("브라운", reservation.getDate(), 1L, 1L)).willReturn(true);

        assertThatThrownBy(() -> waitingService.saveWaiting("브라운", reservation.getDate(), 1L, 1L))
                .isInstanceOf(DuplicateException.class)
                .hasMessage("해당 날짜의 시간과 테마는 이미 예약 대기되어 있습니다.");
    }

    @Test
    @DisplayName("이미 본인이 예약한 슬롯에 대기를 추가하면, 예외가 발생한다.")
    void 예약된_시간_대기_예외_발생() {
        Reservation reservation = createReservation("브라운", LocalDate.now().plusDays(1), LocalTime.of(10, 0));
        given(reservationRepository.findByDateAndTimeIdAndThemeId(reservation.getDate(), 1L, 1L))
                .willReturn(Optional.of(reservation));

        assertThatThrownBy(() -> waitingService.saveWaiting("브라운", reservation.getDate(), 1L, 1L))
                .isInstanceOf(DuplicateException.class)
                .hasMessage("이미 예약된 시간입니다. 다른 날짜 혹은 테마를 선택해주세요.");
    }

    @Test
    @DisplayName("예약이 없는 슬롯에 대기를 추가하면, 예외가 발생한다.")
    void 예약_없는_슬롯_대기_예외_발생() {
        LocalDate date = LocalDate.now().plusDays(1);
        given(reservationRepository.findByDateAndTimeIdAndThemeId(date, 1L, 1L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> waitingService.saveWaiting("브라운", date, 1L, 1L))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("예약되지 않은 슬롯입니다.");
    }

    @Test
    @DisplayName("이미 지난 시간으로 대기를 추가하면, 예외가 발생한다.")
    void 지난_시간_대기_예외_발생() {
        Reservation reservation = createReservation("네오", LocalDate.now(), LocalTime.of(0, 0));
        given(reservationRepository.findByDateAndTimeIdAndThemeId(reservation.getDate(), 1L, 1L))
                .willReturn(Optional.of(reservation));

        assertThatThrownBy(() -> waitingService.saveWaiting("브라운", LocalDate.now(), 1L, 1L))
                .isInstanceOf(PastTimeException.class);
    }

    private Waiting createWaitingEntity() {
        return new Waiting(1L, "브라운", LocalDate.now().plusDays(1), createTimeSlot(), createTheme(),
                LocalDateTime.now());
    }

    private Reservation createReservation(String name, LocalDate date, LocalTime time) {
        return new Reservation(1L, name, date, new TimeSlot(1L, time), createTheme(), date.minusDays(1).atStartOfDay());
    }

    private TimeSlot createTimeSlot() {
        return new TimeSlot(1L, LocalTime.of(10, 0));
    }

    private Theme createTheme() {
        return new Theme(1L, "테마", "설명", "thumbnail.png");
    }
}
