package roomescape.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import roomescape.domain.Reservation;
import roomescape.domain.Theme;
import roomescape.domain.TimeSlot;
import roomescape.repository.ReservationRepository;
import roomescape.repository.ThemeRepository;
import roomescape.repository.TimeSlotRepository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
class ReservationServiceExperiment {

    @Mock
    private ReservationRepository reservationRepository;

    @Mock
    private TimeSlotRepository timeSlotRepository;

    @Mock
    private ThemeRepository themeRepository;

    @InjectMocks
    private ReservationService reservationService;

    @Test
    void 예약_저장_결과_검증() {
        LocalDate date = LocalDate.now().plusDays(1);
        TimeSlot timeSlot = mock(TimeSlot.class);
        Theme theme = mock(Theme.class);
        Reservation expected = mock(Reservation.class);
        stubDomainBehaviors(timeSlot, theme);
        stubRepositoryBehaviors(date, timeSlot, theme, expected);
        Reservation actual = reservationService.saveReservation("브라운", date, 1L, 1L);
        assertThat(actual).isEqualTo(expected);
    }

    private void stubDomainBehaviors(TimeSlot timeSlot, Theme theme) {
        given(timeSlot.getId()).willReturn(1L);
        given(timeSlot.getStartAt()).willReturn(LocalTime.of(10, 0));
        given(theme.getId()).willReturn(1L);
    }

    private void stubRepositoryBehaviors(LocalDate date, TimeSlot time, Theme theme, Reservation expected) {
        given(timeSlotRepository.findById(1L)).willReturn(Optional.of(time));
        given(themeRepository.findById(1L)).willReturn(Optional.of(theme));
        given(reservationRepository.findByDateAndTimeIdAndThemeId(date, 1L, 1L)).willReturn(Optional.empty());
        given(reservationRepository.save(any(Reservation.class))).willReturn(expected);
    }
}
