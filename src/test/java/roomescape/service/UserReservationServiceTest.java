package roomescape.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationTime;
import roomescape.domain.Theme;
import roomescape.repository.ReservationRepository;
import roomescape.repository.ReservationTimeRepository;
import roomescape.service.dto.ReservationCreateCommand;
import roomescape.service.dto.ReservationResult;
import roomescape.service.dto.ReservationUpdateCommand;
import roomescape.service.dto.ReservationWithWaitingOrder;
import roomescape.service.exception.PastReservationException;
import roomescape.service.exception.ReservationConflictException;
import roomescape.service.exception.ReservationNotFoundException;
import roomescape.service.exception.ReservationTimeNotFoundException;
import roomescape.service.exception.UnauthorizedReservationException;

@ExtendWith(MockitoExtension.class)
class UserReservationServiceTest {

    private static final ReservationTime VALID_TIME = new ReservationTime(1L, LocalTime.of(10, 0));
    private static final ReservationTime ANOTHER_TIME = new ReservationTime(2L, LocalTime.of(11, 0));
    private static final Theme VALID_THEME = new Theme(
            1L,
            "무인도 탈출",
            "갯벌이 많은 무인도를 탈출하는 흥미진진 대탈출!",
            "https://picsum.photos/seed/roomescape1/800/600.jpg"
    );
    private static final LocalDate FUTURE_DATE = LocalDate.of(2099, 12, 31);
    private static final LocalDate ANOTHER_FUTURE_DATE = LocalDate.of(2099, 11, 30);
    private static final LocalDate PAST_DATE = LocalDate.of(2020, 1, 1);
    private static final String OWNER = "카프카";
    private static final String OTHER = "모아";

    @Mock
    private AdminReservationService reservationService;
    @Mock
    private ReservationRepository reservationRepository;
    @Mock
    private ReservationTimeRepository reservationTimeRepository;
    @InjectMocks
    private UserReservationService userReservationService;

    @Test
    @DisplayName("미래 시점에 예약하면 정상적으로 생성된다")
    void 미래_시점_예약은_정상_생성된다() {
        ReservationCreateCommand command = new ReservationCreateCommand(OWNER, FUTURE_DATE, 1L, 1L);
        given(reservationTimeRepository.findById(1L)).willReturn(Optional.of(VALID_TIME));

        assertDoesNotThrow(() -> userReservationService.create(command));

        verify(reservationTimeRepository, times(1)).findById(1L);
        verify(reservationService, times(1)).create(command);
        verifyNoInteractions(reservationRepository);
    }

    @Test
    @DisplayName("과거 날짜로 예약하면 PastReservationException이 발생한다")
    void 과거_날짜_예약시_예외가_발생한다() {
        ReservationCreateCommand command = new ReservationCreateCommand(OWNER, PAST_DATE, 1L, 1L);
        given(reservationTimeRepository.findById(1L)).willReturn(Optional.of(VALID_TIME));

        assertThrows(
                PastReservationException.class,
                () -> userReservationService.create(command)
        );

        verify(reservationTimeRepository, times(1)).findById(1L);
        verifyNoInteractions(reservationService, reservationRepository);
    }

    @Test
    @DisplayName("존재하지 않는 timeId로 예약하면 ReservationTimeNotFoundException이 발생한다")
    void 존재하지_않는_timeId로_예약시_예외가_발생한다() {
        ReservationCreateCommand command = new ReservationCreateCommand(OWNER, FUTURE_DATE, 1L, 1L);
        given(reservationTimeRepository.findById(1L)).willReturn(Optional.empty());

        assertThrows(
                ReservationTimeNotFoundException.class,
                () -> userReservationService.create(command)
        );

        verify(reservationTimeRepository, times(1)).findById(1L);
        verifyNoInteractions(reservationService, reservationRepository);
    }

    @Test
    @DisplayName("이름으로 예약 목록을 조회한다")
    void 이름으로_예약_목록을_조회한다() {
        ReservationWithWaitingOrder reservation = new ReservationWithWaitingOrder(
                1L, OWNER, FUTURE_DATE, VALID_TIME, VALID_THEME, 2L);
        given(reservationRepository.findByReserverName(OWNER)).willReturn(List.of(reservation));

        List<ReservationResult> results = userReservationService.findByReserverName(OWNER);

        assertThat(results).hasSize(1);
        assertThat(results.get(0).reserverName()).isEqualTo(OWNER);
        assertThat(results.get(0).waitingOrder()).isEqualTo(2L);
        verify(reservationRepository, times(1)).findByReserverName(OWNER);
        verifyNoInteractions(reservationService, reservationTimeRepository);
    }

    @Test
    @DisplayName("본인 예약을 정상적으로 취소한다")
    void 본인_예약을_정상적으로_취소한다() {
        Reservation reservation = new Reservation(1L, OWNER, FUTURE_DATE, VALID_TIME, VALID_THEME);
        given(reservationRepository.findById(1L)).willReturn(Optional.of(reservation));

        userReservationService.cancel(1L, OWNER);

        verify(reservationRepository, times(1)).findById(1L);
        verify(reservationRepository, times(1)).deleteById(1L);
        verifyNoInteractions(reservationService, reservationTimeRepository);
    }

    @Test
    @DisplayName("존재하지 않는 예약을 취소하면 ReservationNotFoundException이 발생한다")
    void 존재하지_않는_예약_취소시_예외가_발생한다() {
        given(reservationRepository.findById(1L)).willReturn(Optional.empty());

        assertThrows(
                ReservationNotFoundException.class,
                () -> userReservationService.cancel(1L, OWNER)
        );

        verify(reservationRepository, times(1)).findById(1L);
        verifyNoInteractions(reservationService, reservationTimeRepository);
    }

    @Test
    @DisplayName("본인이 아닌 예약을 취소하면 UnauthorizedReservationException이 발생한다")
    void 본인이_아닌_예약_취소시_예외가_발생한다() {
        Reservation reservation = new Reservation(1L, OWNER, FUTURE_DATE, VALID_TIME, VALID_THEME);
        given(reservationRepository.findById(1L)).willReturn(Optional.of(reservation));

        assertThrows(
                UnauthorizedReservationException.class,
                () -> userReservationService.cancel(1L, OTHER)
        );

        verify(reservationRepository, times(1)).findById(1L);
        verifyNoInteractions(reservationService, reservationTimeRepository);
    }

    @Test
    @DisplayName("과거 예약을 취소하면 PastReservationException이 발생한다")
    void 과거_예약_취소시_예외가_발생한다() {
        Reservation reservation = new Reservation(1L, OWNER, PAST_DATE, VALID_TIME, VALID_THEME);
        given(reservationRepository.findById(1L)).willReturn(Optional.of(reservation));

        assertThrows(
                PastReservationException.class,
                () -> userReservationService.cancel(1L, OWNER)
        );

        verify(reservationRepository, times(1)).findById(1L);
        verifyNoInteractions(reservationService, reservationTimeRepository);
    }

    @Test
    @DisplayName("본인 예약을 정상적으로 변경한다")
    void 본인_예약을_정상적으로_변경한다() {
        Reservation reservation = new Reservation(1L, OWNER, FUTURE_DATE, VALID_TIME, VALID_THEME);
        ReservationUpdateCommand command = new ReservationUpdateCommand(1L, OWNER, ANOTHER_FUTURE_DATE, 2L);
        given(reservationRepository.findById(1L)).willReturn(Optional.of(reservation));
        given(reservationTimeRepository.findById(2L)).willReturn(Optional.of(ANOTHER_TIME));
        given(reservationRepository.existsByDateAndTimeIdAndThemeIdAndIdNot(
                ANOTHER_FUTURE_DATE, 2L, VALID_THEME.getId(), 1L)).willReturn(false);
        given(reservationRepository.update(any(Reservation.class))).willAnswer(inv -> {
            Reservation r = inv.getArgument(0);
            return new ReservationWithWaitingOrder(
                    r.getId(), r.getReserverName(), r.getDate(), r.getTime(), r.getTheme(), 0L);
        });

        ReservationResult result = userReservationService.update(command);

        assertThat(result.date()).isEqualTo(ANOTHER_FUTURE_DATE);
        assertThat(result.time().id()).isEqualTo(2L);
        verify(reservationRepository, times(1)).findById(1L);
        verify(reservationTimeRepository, times(1)).findById(2L);
        verify(reservationRepository, times(1)).existsByDateAndTimeIdAndThemeIdAndIdNot(
                ANOTHER_FUTURE_DATE, 2L, VALID_THEME.getId(), 1L);
        verify(reservationRepository, times(1)).update(any(Reservation.class));
        verifyNoInteractions(reservationService);
    }

    @Test
    @DisplayName("본인이 아닌 예약을 변경하면 UnauthorizedReservationException이 발생한다")
    void 본인이_아닌_예약_변경시_예외가_발생한다() {
        Reservation reservation = new Reservation(1L, OWNER, FUTURE_DATE, VALID_TIME, VALID_THEME);
        ReservationUpdateCommand command = new ReservationUpdateCommand(1L, OTHER, ANOTHER_FUTURE_DATE, 2L);
        given(reservationRepository.findById(1L)).willReturn(Optional.of(reservation));

        assertThrows(
                UnauthorizedReservationException.class,
                () -> userReservationService.update(command)
        );

        verify(reservationRepository, times(1)).findById(1L);
        verifyNoInteractions(reservationService, reservationTimeRepository);
    }

    @Test
    @DisplayName("존재하지 않는 timeId로 변경하면 ReservationTimeNotFoundException이 발생한다")
    void 존재하지_않는_timeId로_변경시_예외가_발생한다() {
        Reservation reservation = new Reservation(1L, OWNER, FUTURE_DATE, VALID_TIME, VALID_THEME);
        ReservationUpdateCommand command = new ReservationUpdateCommand(1L, OWNER, ANOTHER_FUTURE_DATE, 99L);
        given(reservationRepository.findById(1L)).willReturn(Optional.of(reservation));
        given(reservationTimeRepository.findById(99L)).willReturn(Optional.empty());

        assertThrows(
                ReservationTimeNotFoundException.class,
                () -> userReservationService.update(command)
        );

        verify(reservationRepository, times(1)).findById(1L);
        verify(reservationTimeRepository, times(1)).findById(99L);
        verifyNoInteractions(reservationService);
    }

    @Test
    @DisplayName("변경 시점이 과거이면 PastReservationException이 발생한다")
    void 변경_시점이_과거이면_예외가_발생한다() {
        Reservation reservation = new Reservation(1L, OWNER, FUTURE_DATE, VALID_TIME, VALID_THEME);
        ReservationUpdateCommand command = new ReservationUpdateCommand(1L, OWNER, PAST_DATE, 2L);
        given(reservationRepository.findById(1L)).willReturn(Optional.of(reservation));
        given(reservationTimeRepository.findById(2L)).willReturn(Optional.of(ANOTHER_TIME));

        assertThrows(
                PastReservationException.class,
                () -> userReservationService.update(command)
        );

        verify(reservationRepository, times(1)).findById(1L);
        verify(reservationTimeRepository, times(1)).findById(2L);
        verifyNoInteractions(reservationService);
    }

    @Test
    @DisplayName("기존 예약이 과거이면 PastReservationException이 발생한다")
    void 기존_예약이_과거이면_변경할_수_없다() {
        Reservation reservation = new Reservation(1L, OWNER, PAST_DATE, VALID_TIME, VALID_THEME);
        ReservationUpdateCommand command = new ReservationUpdateCommand(1L, OWNER, ANOTHER_FUTURE_DATE, 2L);
        given(reservationRepository.findById(1L)).willReturn(Optional.of(reservation));

        assertThrows(
                PastReservationException.class,
                () -> userReservationService.update(command)
        );

        verify(reservationRepository, times(1)).findById(1L);
        verifyNoInteractions(reservationService, reservationTimeRepository);
    }

    @Test
    @DisplayName("변경하려는 시간이 이미 차 있으면 ReservationConflictException이 발생한다")
    void 변경_시간_충돌시_예외가_발생한다() {
        Reservation reservation = new Reservation(1L, OWNER, FUTURE_DATE, VALID_TIME, VALID_THEME);
        ReservationUpdateCommand command = new ReservationUpdateCommand(1L, OWNER, ANOTHER_FUTURE_DATE, 2L);
        given(reservationRepository.findById(1L)).willReturn(Optional.of(reservation));
        given(reservationTimeRepository.findById(2L)).willReturn(Optional.of(ANOTHER_TIME));
        given(reservationRepository.existsByDateAndTimeIdAndThemeIdAndIdNot(
                ANOTHER_FUTURE_DATE, 2L, VALID_THEME.getId(), 1L)).willReturn(true);

        assertThrows(
                ReservationConflictException.class,
                () -> userReservationService.update(command)
        );

        verify(reservationRepository, times(1)).findById(1L);
        verify(reservationTimeRepository, times(1)).findById(2L);
        verify(reservationRepository, times(1)).existsByDateAndTimeIdAndThemeIdAndIdNot(
                ANOTHER_FUTURE_DATE, 2L, VALID_THEME.getId(), 1L);
        verifyNoInteractions(reservationService);
    }
}
