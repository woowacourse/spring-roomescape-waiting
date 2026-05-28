package roomescape.reservationWaiting.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import roomescape.global.exception.ForbiddenException;
import roomescape.global.exception.NotFoundException;
import roomescape.global.exception.InvalidRequestValueException;
import roomescape.global.exception.BadRequestException;
import roomescape.global.exception.DuplicateException;
import roomescape.reservationWaiting.exception.ReservationWaitingErrorCode;

import roomescape.reservation.domain.Reservation;
import roomescape.reservation.repository.ReservationRepository;
import roomescape.reservationWaiting.domain.ReservationWaiting;
import roomescape.reservationWaiting.repository.ReservationWaitingRepository;
import roomescape.reservationWaiting.service.dto.ReservationWaitingCommand;
import roomescape.reservationWaiting.service.dto.ReservationWaitingResult;
import roomescape.theme.domain.Theme;
import roomescape.time.domain.ReservationTime;

@ExtendWith(MockitoExtension.class)
class ReservationWaitingServiceTest {

    @Mock
    ReservationWaitingRepository reservationWaitingRepository;

    @Mock
    ReservationRepository reservationRepository;

    @Mock
    Clock clock;

    @InjectMocks
    ReservationWaitingService reservationWaitingService;

    @Test
    @DisplayName("새로운 에약 대기를 신청한다.")
    void save_validCommand_returnsReservationWaiting() {
        //given
        given(reservationWaitingRepository.existsByDateAndTimeIdAndThemeIdAndName(any(), any(), any(), anyString()))
                .willReturn(false);

        ReservationTime time = new ReservationTime(1L, LocalTime.of(10, 0));
        Theme theme = new Theme(1L, "이름", "설명", "thumbnailUrl");

        given(clock.instant()).willReturn(
                LocalDate.of(2026, 5, 14)
                        .atStartOfDay(ZoneId.systemDefault())
                        .toInstant()
        );
        given(clock.getZone()).willReturn(ZoneId.systemDefault());

        given(reservationRepository.findByDateAndTimeIdAndThemeId(
                any(), any(), any())
        ).willReturn(Optional.of(new Reservation(1L, "포비", LocalDate.of(2026, 5, 15), time, theme)));

        given(reservationWaitingRepository.save(any()))
                .willReturn(new ReservationWaiting(1L, "브라운", LocalDate.of(2026, 5, 15), time, theme));

        //when
        ReservationWaitingResult reservationWaiting = reservationWaitingService.save(
                new ReservationWaitingCommand(
                        "브라운", LocalDate.of(2026, 5, 15), 1L, 1L
                )
        );

        //then
        assertThat(reservationWaiting.id()).isNotNull();
    }

    @Test
    @DisplayName("예약 대기 생성 시, 기존에 이미 동일한 예약 대기가 있으면 예외가 발생한다.")
    void save_duplicateWaiting_throwsDuplicateException() {
        //given
        given(reservationWaitingRepository.existsByDateAndTimeIdAndThemeIdAndName(any(), any(), any(), anyString()))
                .willReturn(true);

        //when & then
        assertThatThrownBy(() -> reservationWaitingService.save(
                new ReservationWaitingCommand(
                        "브라운", LocalDate.of(2026, 5, 15), 1L, 1L
                )
        )).isInstanceOf(DuplicateException.class)
                .hasMessage(ReservationWaitingErrorCode.DUPLICATE_WAITING.getMessage());
    }

    @Test
    @DisplayName("예약 대기 생성 시, 해당 슬롯의 예약이 없으면 예외가 발생한다.")
    void save_noTargetReservation_throwsNotFoundException() {
        //given
        given(reservationWaitingRepository.existsByDateAndTimeIdAndThemeIdAndName(any(), any(), any(), anyString()))
                .willReturn(false);

        given(reservationRepository.findByDateAndTimeIdAndThemeId(
                any(), any(), any())
        ).willReturn(Optional.empty());

        //when & then
        assertThatThrownBy(() -> reservationWaitingService.save(
                new ReservationWaitingCommand(
                        "브라운", LocalDate.of(2026, 5, 15), 1L, 1L
                )
        )).isInstanceOf(NotFoundException.class)
                .hasMessage(ReservationWaitingErrorCode.TARGET_RESERVATION_NOT_FOUND.getMessage());
    }

    @Test
    @DisplayName("예약 대기 생성 시, 해당 슬롯으로 자신이 예약을 했었다면 예외가 발생한다.")
    void save_alreadyReservedByRequester_throwsBadRequestException() {
        //given
        given(reservationWaitingRepository.existsByDateAndTimeIdAndThemeIdAndName(any(), any(), any(), anyString()))
                .willReturn(false);

        ReservationTime time = new ReservationTime(1L, LocalTime.of(10, 0));
        Theme theme = new Theme(1L, "이름", "설명", "thumbnailUrl");

        given(reservationRepository.findByDateAndTimeIdAndThemeId(
                any(), any(), any())
        ).willReturn(Optional.of(new Reservation(1L, "브라운", LocalDate.of(2026, 5, 15), time, theme)));

        //when & then
        assertThatThrownBy(() -> reservationWaitingService.save(
                new ReservationWaitingCommand(
                        "브라운", LocalDate.of(2026, 5, 15), 1L, 1L
                )
        )).isInstanceOf(BadRequestException.class)
                .hasMessage(ReservationWaitingErrorCode.ALREADY_RESERVED.getMessage());
    }

    @Test
    @DisplayName("예약 대기를 아이디 기반으로 잘 삭제한다")
    void deleteReservationWaiting_success() {
        // given
        ReservationTime time = new ReservationTime(1L, LocalTime.of(10, 0));
        Theme theme = new Theme(1L, "이름", "설명", "thumbnailUrl");

        given(reservationWaitingRepository.findById(any())).willReturn(
                Optional.of(new ReservationWaiting(
                        1L, "브라운", LocalDate.of(2026, 5, 1), time, theme
                )));
        given(reservationWaitingRepository.deleteById(any())).willReturn(1);
        given(clock.instant()).willReturn(
                LocalDate.of(2026, 5, 1)
                        .atStartOfDay(ZoneId.systemDefault())
                        .toInstant()
        );
        given(clock.getZone()).willReturn(ZoneId.systemDefault());

        // when, then
        assertDoesNotThrow(
                () -> reservationWaitingService.delete(1L, "브라운")
        );
    }

    @Test
    @DisplayName("예약 대기 삭제 대상이 없는 경우 예외가 발생한다")
    void deleteReservationWaiting_fail_not_exist() {
        // given
        given(reservationWaitingRepository.findById(any())).willReturn(
                Optional.empty());

        // when, then
        assertThatThrownBy(() -> reservationWaitingService.delete(1L, "브라운")).isInstanceOf(
                NotFoundException.class);
    }

    @Test
    @DisplayName("예약 대기가 유효한 지 검증한다-과거이면 오류 발생")
    void deleteReservationWaiting_past() {
        // given
        ReservationTime time = new ReservationTime(1L, LocalTime.of(10, 0));
        Theme theme = new Theme(1L, "이름", "설명", "thumbnailUrl");
        given(reservationWaitingRepository.findById(any())).willReturn(
                Optional.of(new ReservationWaiting(
                        1L, "브라운", LocalDate.of(2026, 5, 1), time, theme
                )));

        given(clock.instant()).willReturn(
                LocalDate.of(2026, 5, 14)
                        .atStartOfDay(ZoneId.systemDefault())
                        .toInstant()
        );
        given(clock.getZone()).willReturn(ZoneId.systemDefault());

        // when,then
        assertThatThrownBy(() -> reservationWaitingService.delete(1L, "브라운")).isInstanceOf(
                InvalidRequestValueException.class);
    }

    @Test
    @DisplayName("예약 대기가 유효한 지 검증한다- 미래 오류 발생 X")
    void deleteReservationWaiting_future() {
        // given
        ReservationTime time = new ReservationTime(1L, LocalTime.of(10, 0));
        Theme theme = new Theme(1L, "이름", "설명", "thumbnailUrl");
        given(reservationWaitingRepository.findById(any())).willReturn(
                Optional.of(new ReservationWaiting(
                        1L, "브라운", LocalDate.of(2026, 5, 14), time, theme
                )));

        given(clock.instant()).willReturn(
                LocalDate.of(2026, 5, 1)
                        .atStartOfDay(ZoneId.systemDefault())
                        .toInstant()
        );
        given(clock.getZone()).willReturn(ZoneId.systemDefault());
        given(reservationWaitingRepository.deleteById(any())).willReturn(1);

        // when,then
        assertDoesNotThrow(
                () -> reservationWaitingService.delete(1L, "브라운")
        );
    }

    @Test
    @DisplayName("예약 대기자와 삭제 신청자 이름이 다르면 오류가 발생한다")
    void deleteReservationWaiting_not_authorized() {
        // given
        ReservationTime time = new ReservationTime(1L, LocalTime.of(10, 0));
        Theme theme = new Theme(1L, "이름", "설명", "thumbnailUrl");
        given(reservationWaitingRepository.findById(any())).willReturn(
                Optional.of(new ReservationWaiting(
                        1L, "브라운", LocalDate.of(2026, 5, 14), time, theme
                )));

        given(clock.instant()).willReturn(
                LocalDate.of(2026, 5, 1)
                        .atStartOfDay(ZoneId.systemDefault())
                        .toInstant()
        );
        given(clock.getZone()).willReturn(ZoneId.systemDefault());

        // when,then
        assertThatThrownBy(() -> reservationWaitingService.delete(1L, "검프")).isInstanceOf(
                ForbiddenException.class);
    }
}
