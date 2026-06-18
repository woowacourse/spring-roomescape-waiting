package roomescape.reservationwaiting.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import roomescape.exception.ConflictException;
import roomescape.reservationtime.ReservationTime;
import roomescape.reservationwaiting.ReservationWaiting;
import roomescape.reservationwaiting.repository.JpaReservationWaitingRepository;
import roomescape.theme.Theme;

@ExtendWith(MockitoExtension.class)
public class ReservationWaitingServiceTest {

    @Mock
    JpaReservationWaitingRepository reservationWaitingRepository;

    @InjectMocks
    ReservationWaitingService reservationWaitingService;

    LocalDate TOMORROW = LocalDate.now().plusDays(1);

    @Test
    void 중복으로_대기할_수_없다() {
        // given
        Theme theme = Theme.of(1L, "테마이름", "테마설명", "썸네일URL");
        ReservationTime time = ReservationTime.of(1L, LocalTime.of(10, 0));

        when(reservationWaitingRepository.existsByDateAndThemeIdAndTimeIdAndName(TOMORROW, 1L, 1L, "도우너"))
                .thenReturn(true);

        // when & then
        assertThrows(ConflictException.class, () -> reservationWaitingService.save("도우너", TOMORROW, theme, time));
    }

    @Test
    void 예약_대기를_저장한다() {
        // given
        Theme theme = Theme.of(1L, "테마이름", "테마설명", "썸네일URL");
        ReservationTime time = ReservationTime.of(1L, LocalTime.of(10, 0));
        ReservationWaiting savedWaiting = ReservationWaiting.createNew(
                TOMORROW,
                theme,
                time,
                "도우너",
                LocalDateTime.parse("2026-06-18T12:00:00")
        ).withId(1L);

        when(reservationWaitingRepository.existsByDateAndThemeIdAndTimeIdAndName(TOMORROW, 1L, 1L, "도우너"))
                .thenReturn(false);
        when(reservationWaitingRepository.save(any(ReservationWaiting.class))).thenReturn(savedWaiting);

        // when
        ReservationWaiting result = reservationWaitingService.save("도우너", TOMORROW, theme, time);

        // then
        assertThat(result).isEqualTo(savedWaiting);
        verify(reservationWaitingRepository).save(any(ReservationWaiting.class));
    }

    @Test
    void 존재하는_예약_대기는_삭제한다() {
        // given
        when(reservationWaitingRepository.existsById(1L)).thenReturn(true);

        // when
        reservationWaitingService.deleteById(1L);

        // then
        verify(reservationWaitingRepository).deleteById(1L);
    }

    @Test
    void 이름이_일치하는_예약_대기는_삭제한다() {
        // given
        when(reservationWaitingRepository.existsByIdAndName(1L, "도우너")).thenReturn(true);

        // when
        reservationWaitingService.deleteByIdAndName(1L, "도우너");

        // then
        verify(reservationWaitingRepository).deleteByIdAndName(1L, "도우너");
    }
}
