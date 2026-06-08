package roomescape.service;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationStatus;
import roomescape.domain.ReservationTime;
import roomescape.domain.Theme;
import roomescape.domain.Waiting;
import roomescape.domain.exception.DomainConflictException;
import roomescape.repository.ReservationRepository;
import roomescape.repository.ReservationTimeRepository;
import roomescape.repository.ThemeRepository;
import roomescape.repository.UserReservationRepository;
import roomescape.service.dto.UserReservation;
import roomescape.service.exception.BusinessConflictException;
import roomescape.service.exception.BusinessException;
import roomescape.service.exception.ErrorCode;
import roomescape.service.exception.ResourceNotFoundException;

import java.time.*;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReservationServiceTest {

    @Mock
    private WaitingService waitingService;

    @Mock
    private ReservationRepository reservationRepository;

    @Mock
    private UserReservationRepository userReservationRepository;

    @Mock
    private ReservationTimeRepository reservationTimeRepository;

    @Mock
    private ThemeRepository themeRepository;

    private ReservationService reservationService;


    @BeforeEach
    void setUp() {
        Clock fixedClock = Clock.fixed(Instant.parse("2026-05-01T00:00:00Z"), ZoneOffset.UTC);
        reservationService = new ReservationService(
                waitingService, reservationRepository, userReservationRepository, reservationTimeRepository,
                themeRepository, fixedClock
        );
    }

    @Test
    void 예약_날짜가_과거인_경우_도메인_충돌_예외가_발생한다() {
        ReservationTime newTime = new ReservationTime(1L, LocalTime.of(12, 0));
        Theme theme = new Theme(1L, "공포방", "무서운방입니다.", "image-url");

        when(reservationTimeRepository.findById(anyLong())).thenReturn(Optional.of(newTime));
        when(themeRepository.findById(anyLong())).thenReturn(Optional.of(theme));

        assertThatThrownBy(() -> reservationService.createReservation("레서", LocalDate.of(2026, 4, 1), 1L, 1L))
                .isInstanceOf(DomainConflictException.class);

        verify(reservationRepository, never()).save(any(Reservation.class));
    }

    @Test
    void 이미_예약된_예약_시간인_경우_예외가_발생한다() {
        ReservationTime newTime = new ReservationTime(2L, LocalTime.of(12, 0));
        Theme theme = new Theme(1L, "공포방", "무서운방입니다.", "image-url");
        when(reservationTimeRepository.findById(anyLong())).thenReturn(Optional.of(newTime));
        when(themeRepository.findById(anyLong())).thenReturn(Optional.of(theme));
        when(reservationRepository.findBySchedule(any(LocalDate.class), anyLong(), anyLong()))
                .thenReturn(Optional.of(new Reservation(
                        1L, "브라운", LocalDate.of(2026, 5, 13), newTime, theme)));

        assertThatThrownBy(() -> reservationService.createReservation("레서", LocalDate.of(2026, 5, 13), 1L, 1L))
                .isInstanceOf(BusinessConflictException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(ErrorCode.DUPLICATE_RESERVATION);
    }

    @Test
    void 존재하지_않는_시간으로_예약을_생성하면_예외가_발생하고_예약을_저장하지_않는다() {
        when(reservationTimeRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> reservationService.createReservation(
                "브라운", LocalDate.of(2026, 5, 10), 999L, 2L))
                .isInstanceOf(ResourceNotFoundException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(ErrorCode.RESERVATION_TIME_NOT_FOUND);

        verify(themeRepository, never()).findById(2L);
        verify(reservationRepository, never()).save(any(Reservation.class));
    }

    @Test
    void 존재하지_않는_테마로_예약을_생성하면_예외가_발생하고_예약을_저장하지_않는다() {
        ReservationTime time = new ReservationTime(1L, LocalTime.of(12, 0));
        when(reservationTimeRepository.findById(anyLong())).thenReturn(Optional.of(time));
        when(themeRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> reservationService.createReservation(
                "브라운", LocalDate.of(2026, 5, 10), 1L, 999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(ErrorCode.THEME_NOT_FOUND);

        verify(reservationRepository, never()).save(any(Reservation.class));
    }

    @Test
    void 존재하지_않는_예약을_변경하면_예외가_발생한다() {
        when(reservationRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> reservationService.updateReservation(999L, "브라운", LocalDate.of(2026, 5, 11), 2L))
                .isInstanceOf(ResourceNotFoundException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(ErrorCode.RESERVATION_NOT_FOUND);

        verify(reservationTimeRepository, never()).findById(anyLong());
        verify(reservationRepository, never()).update(any(Reservation.class));
    }

    @Test
    void 본인의_예약이_아닌_경우_변경하면_도메인_충돌_예외가_발생한다() {
        ReservationTime originalTime = new ReservationTime(1L, LocalTime.of(10, 0));
        ReservationTime newTime = new ReservationTime(2L, LocalTime.of(12, 0));
        Theme theme = new Theme(1L, "공포방", "무서운방입니다.", "image-url");
        Reservation reservation = new Reservation(7L, "브라운", LocalDate.of(2026, 5, 10), originalTime, theme);

        when(reservationRepository.findById(7L)).thenReturn(Optional.of(reservation));
        when(reservationTimeRepository.findById(2L)).thenReturn(Optional.of(newTime));

        assertThatThrownBy(() -> reservationService.updateReservation(7L, "어셔", LocalDate.of(2026, 5, 11), 2L))
                .isInstanceOf(DomainConflictException.class);

        verify(reservationRepository, never()).update(any(Reservation.class));
    }

    @Test
    void 존재하지_않는_시간으로_예약을_변경하면_예외가_발생한다() {
        Reservation reservation = new Reservation(
                7L,
                "브라운",
                LocalDate.of(2026, 5, 10),
                new ReservationTime(1L, LocalTime.of(10, 0)),
                new Theme(1L, "공포방", "무서운방입니다.", "image-url")
        );
        when(reservationRepository.findById(7L)).thenReturn(Optional.of(reservation));
        when(reservationTimeRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> reservationService.updateReservation(7L, "브라운", LocalDate.of(2026, 5, 11), 999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(ErrorCode.RESERVATION_TIME_NOT_FOUND);

        verify(reservationRepository, never()).update(any(Reservation.class));
    }

    @Test
    void 이미_예약된_날짜와_시간으로_예약을_변경하면_예외가_발생한다() {
        ReservationTime newTime = new ReservationTime(2L, LocalTime.of(12, 0));
        Reservation reservation = new Reservation(
                7L,
                "브라운",
                LocalDate.of(2026, 5, 10),
                new ReservationTime(1L, LocalTime.of(10, 0)),
                new Theme(1L, "공포방", "무서운방입니다.", "image-url")
        );
        when(reservationRepository.findById(7L)).thenReturn(Optional.of(reservation));
        when(reservationTimeRepository.findById(2L)).thenReturn(Optional.of(newTime));
        when(reservationRepository.findBySchedule(any(LocalDate.class), anyLong(), anyLong()))
                .thenReturn(Optional.of(new Reservation(
                        8L,
                        "어셔",
                        LocalDate.of(2026, 5, 11),
                        newTime,
                        reservation.getTheme()
                )));

        assertThatThrownBy(() -> reservationService.updateReservation(7L, "브라운", LocalDate.of(2026, 5, 11), 2L))
                .isInstanceOf(BusinessConflictException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(ErrorCode.DUPLICATE_RESERVATION);

        verify(reservationRepository, never()).update(any(Reservation.class));
    }

    @Test
    void 존재하지_않는_예약을_삭제하면_예외가_발생한다() {
        when(reservationRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> reservationService.deleteUserReservation(999L, "레서"))
                .isInstanceOf(ResourceNotFoundException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(ErrorCode.RESERVATION_NOT_FOUND);

        verify(reservationRepository, never()).delete(any(Reservation.class));
    }

    @Test
    void 본인의_예약이_아닌_예약을_삭제하면_도메인_충돌_예외가_발생한다() {
        Reservation reservation = new Reservation(
                7L,
                "브라운",
                LocalDate.of(2026, 5, 10),
                new ReservationTime(1L, LocalTime.of(10, 0)),
                new Theme(1L, "공포방", "무서운방입니다.", "image-url")
        );
        when(reservationRepository.findById(7L)).thenReturn(Optional.of(reservation));

        assertThatThrownBy(() -> reservationService.deleteUserReservation(7L, "레서"))
                .isInstanceOf(DomainConflictException.class);

        verify(reservationRepository, never()).delete(any(Reservation.class));
    }

    @Test
    void 예약과_예약_대기_목록을_함께_조회한다() {
        //given
        ReservationTime time = new ReservationTime(1L, LocalTime.of(18, 0));
        Theme theme = new Theme(1L, "공포방", "무서운방입니다.", "image-url");

        List<UserReservation> userReservations = List.of(
                UserReservation.reserved(new Reservation(1L, "브라운", LocalDate.of(2026, 5, 11), time, theme)),
                UserReservation.waiting(new Waiting(2L, "브라운", LocalDate.of(2026, 5, 11), time, theme), 2L)
        );

        when(userReservationRepository.findByName("브라운", 0, 10)).thenReturn(userReservations);

        //when
        List<UserReservation> result = reservationService.findUserReservations("브라운", 0, 10);

        //then
        assertThat(result).hasSize(2);
        assertThat(result)
                .extracting(UserReservation::status)
                .containsExactlyInAnyOrder(ReservationStatus.RESERVED, ReservationStatus.WAITING);
        UserReservation waitingResult = result.stream()
                .filter(it -> it.status() == ReservationStatus.WAITING)
                .findFirst()
                .orElseThrow();
        assertThat(waitingResult.name()).isEqualTo("브라운");
        assertThat(waitingResult.rank()).isEqualTo(2L);
    }

    @Test
    void 예약_취소_시_대기_1번이_자동으로_예약으로_전환된다() {
        //given
        Reservation reservation = new Reservation(
                1L,
                "브라운",
                LocalDate.of(2026, 5, 10),
                new ReservationTime(1L, LocalTime.of(10, 0)),
                new Theme(1L, "공포방", "무서운방입니다.", "image-url")
        );

        Waiting waiting = new Waiting(
                1L,
                "어셔",
                LocalDate.of(2026, 5, 10),
                new ReservationTime(1L, LocalTime.of(10, 0)),
                new Theme(1L, "공포방", "무서운방입니다.", "image-url")
        );

        when(reservationRepository.findById(1L)).thenReturn(Optional.of(reservation));
        when(waitingService.findFirstWaiting(reservation.getDate(), reservation.getTime().getId(),
                reservation.getTheme().getId()))
                .thenReturn(Optional.of(waiting));

        //when
        reservationService.deleteUserReservation(1L, "브라운");

        //then
        verify(reservationRepository).delete(reservation);
        verify(waitingService).promoteWaiting(waiting);

        ArgumentCaptor<Reservation> captor = ArgumentCaptor.forClass(Reservation.class);
        verify(reservationRepository).save(captor.capture());
        Reservation saved = captor.getValue();

        assertThat(saved.getName()).isEqualTo(waiting.getName());
        assertThat(saved.getDate()).isEqualTo(waiting.getDate());
        assertThat(saved.getTime()).isEqualTo(waiting.getTime());
        assertThat(saved.getTheme()).isEqualTo(waiting.getTheme());
    }

    @Test
    void 대기가_없는_예약을_취소하면_예약만_삭제된다() {
        //given
        Reservation reservation = new Reservation(
                1L,
                "브라운",
                LocalDate.of(2026, 5, 10),
                new ReservationTime(1L, LocalTime.of(10, 0)),
                new Theme(1L, "공포방", "무서운방입니다.", "image-url")
        );

        when(reservationRepository.findById(1L)).thenReturn(Optional.of(reservation));
        when(waitingService.findFirstWaiting(any(), any(), any())).thenReturn(Optional.empty());

        //when
        reservationService.deleteUserReservation(1L, "브라운");

        //then
        verify(reservationRepository).delete(reservation);
        verify(waitingService, never()).promoteWaiting(any(Waiting.class));
        verify(reservationRepository, never()).save(any(Reservation.class));
    }

    @Test
    void 대기가_여러_개일_때_대기_1번만_예약으로_전환되고_나머지는_그대로다() {
        //given
        Reservation reservation = new Reservation(
                1L,
                "브라운",
                LocalDate.of(2026, 5, 10),
                new ReservationTime(1L, LocalTime.of(10, 0)),
                new Theme(1L, "공포방", "무서운방입니다.", "image-url")
        );

        Waiting waiting1 = new Waiting(
                1L,
                "레서",
                LocalDate.of(2026, 5, 10),
                new ReservationTime(1L, LocalTime.of(10, 0)),
                new Theme(1L, "공포방", "무서운방입니다.", "image-url")
        );

        Waiting waiting2 = new Waiting(
                2L,
                "밍구",
                LocalDate.of(2026, 5, 10),
                new ReservationTime(1L, LocalTime.of(10, 0)),
                new Theme(1L, "공포방", "무서운방입니다.", "image-url")
        );

        when(reservationRepository.findById(1L)).thenReturn(Optional.of(reservation));
        when(waitingService.findFirstWaiting(reservation.getDate(), reservation.getTime().getId(),
                reservation.getTheme().getId()))
                .thenReturn(Optional.of(waiting1));

        //when
        reservationService.deleteUserReservation(1L, "브라운");

        //then
        verify(reservationRepository).delete(reservation);
        verify(waitingService).promoteWaiting(waiting1);
        verify(waitingService, never()).promoteWaiting(waiting2);

        ArgumentCaptor<Reservation> captor = ArgumentCaptor.forClass(Reservation.class);
        verify(reservationRepository).save(captor.capture());
        Reservation saved = captor.getValue();

        assertThat(saved.getName()).isEqualTo(waiting1.getName());
        assertThat(saved.getDate()).isEqualTo(waiting1.getDate());
        assertThat(saved.getTime()).isEqualTo(waiting1.getTime());
        assertThat(saved.getTheme()).isEqualTo(waiting1.getTheme());
    }
}
