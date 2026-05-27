package roomescape.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationTime;
import roomescape.domain.Theme;
import roomescape.dto.WaitingRequestDTO;
import roomescape.exception.RoomEscapeException;
import roomescape.exception.WaitingErrorCode;
import roomescape.repository.ReservationRepository;
import roomescape.repository.ReservationTimeRepository;
import roomescape.repository.ThemeRepository;
import roomescape.repository.WaitingRepository;

@ExtendWith(MockitoExtension.class)
class WaitingServiceTest {

    @InjectMocks
    WaitingService waitingService;
    @Mock
    WaitingRepository waitingRepository;
    @Mock
    ReservationRepository reservationRepository;
    @Mock
    ReservationTimeRepository reservationTimeRepository;
    @Mock
    ThemeRepository themeRepository;

    @Test
    void 예약이_없는_날짜_시간_테마에는_대기할_수_없다() {
        // given
        String name = "namu";
        LocalDate date = LocalDate.parse("2026-11-11");
        ReservationTime reservationTime = ReservationTime.of(1L, LocalTime.parse("10:00"));
        Theme theme = Theme.of(1L, "공포의 병원", "버려진 정신병원에서 탈출해야 합니다.",
                "https://picsum.photos/200/300");

        given(reservationTimeRepository.findById(1L))
                .willReturn(Optional.of(reservationTime));
        given(themeRepository.findById(1L)).willReturn(Optional.of(theme));
        given(reservationRepository.findByDateAndTimeAndTheme(date, reservationTime, theme))
                .willReturn(Optional.empty());

        WaitingRequestDTO waitingRequestDTO = new WaitingRequestDTO(
                name,
                date,
                reservationTime.getId(),
                theme.getId()
        );

        // when & then
        assertThatThrownBy(() -> waitingService.addWaiting(waitingRequestDTO))
                .isInstanceOf(RoomEscapeException.class)
                .extracting("errorCode")
                .isEqualTo(WaitingErrorCode.IMMEDIATE_RESERVATION_AVAILABLE);
    }

    @Test
    void 같은_사용자가_같은_슬롯에_중복_대기할_수_없다() {
        // given
        String name = "namu";
        LocalDate date = LocalDate.parse("2026-11-11");
        ReservationTime reservationTime = ReservationTime.of(1L, LocalTime.parse("10:00"));
        Theme theme = Theme.of(1L, "공포의 병원", "버려진 정신병원에서 탈출해야 합니다.",
                "https://picsum.photos/200/300");
        given(reservationTimeRepository.findById(1L))
                .willReturn(Optional.of(reservationTime));
        given(themeRepository.findById(1L))
                .willReturn(Optional.of(theme));
        given(reservationRepository.findByDateAndTimeAndTheme(date, reservationTime, theme))
                .willReturn(Optional.of(Reservation.of(1L, "name", date, reservationTime, theme)));

        given(waitingRepository.existsByNameAndDateAndTimeAndTheme(
                name,
                date,
                reservationTime,
                theme
        ))
                .willReturn(true);

        WaitingRequestDTO waitingRequestDTO = new WaitingRequestDTO(
                name,
                date,
                reservationTime.getId(),
                theme.getId()
        );

        // when & then
        assertThatThrownBy(() -> waitingService.addWaiting(waitingRequestDTO))
                .isInstanceOf(RoomEscapeException.class)
                .extracting("errorCode")
                .isEqualTo(WaitingErrorCode.WAITING_DUPLICATE);
    }

    @Test
    void 본인의_확정된_예약_슬롯에_대기를_신청할_수_없다() {
        // given
        String name = "namu";
        LocalDate date = LocalDate.parse("2026-11-11");
        ReservationTime reservationTime = ReservationTime.of(1L, LocalTime.parse("10:00"));
        Theme theme = Theme.of(1L, "공포의 병원", "버려진 정신병원에서 탈출해야 합니다.",
                "https://picsum.photos/200/300");

        given(reservationTimeRepository.findById(1L))
                .willReturn(Optional.of(reservationTime));
        given(themeRepository.findById(1L)).willReturn(Optional.of(theme));
        given(reservationRepository.findByDateAndTimeAndTheme(date, reservationTime, theme))
                .willReturn(Optional.of(Reservation.of(1L, name, date, reservationTime, theme)));

        WaitingRequestDTO waitingRequestDTO = new WaitingRequestDTO(
                name,
                date,
                reservationTime.getId(),
                theme.getId()
        );

        // when & then
        assertThatThrownBy(() -> waitingService.addWaiting(waitingRequestDTO))
                .isInstanceOf(RoomEscapeException.class)
                .extracting("errorCode")
                .isEqualTo(WaitingErrorCode.CANNOT_WAITLIST_CONFIRMED_SLOT);

    }
}
