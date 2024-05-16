package roomescape.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.anyLong;
import static org.mockito.Mockito.when;

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
import roomescape.domain.Member;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationTime;
import roomescape.domain.Role;
import roomescape.domain.Theme;
import roomescape.repository.ReservationRepository;
import roomescape.repository.ReservationTimeRepository;
import roomescape.repository.ThemeRepository;
import roomescape.service.dto.request.AvailableTimeRequest;
import roomescape.service.dto.response.AvailableTimeResponse;

@ExtendWith(MockitoExtension.class)
class ReservationTimeServiceMockTest {

    @InjectMocks
    private ReservationTimeService reservationTimeService;
    @Mock
    private ReservationTimeRepository reservationTimeRepository;
    @Mock
    private ThemeRepository themeRepository;
    @Mock
    private ReservationRepository reservationRepository;

    @DisplayName("예약 가능한 시간 조회")
    @Test
    void findAvailableTimes() {
        //given
        LocalDate searchDate = LocalDate.of(2999, 12, 12);
        ReservationTime reservedTime = new ReservationTime(1L, LocalTime.of(10, 0));
        ReservationTime nonReservedTime = new ReservationTime(2L, LocalTime.of(12, 0));

        Theme savedTheme = new Theme(1L, "happy", "hi", "abcd.html");
        when(themeRepository.findById(anyLong())).thenReturn(Optional.of(savedTheme));

        List<Reservation> reservations = List.of(
                new Reservation(1L,
                        new Member(1L, "asd", "asd@email.com", "password", Role.USER),
                        searchDate,
                        reservedTime,
                        savedTheme
                )
        );
        when(reservationRepository.findAllByDateAndTheme(searchDate, savedTheme)).thenReturn(reservations);

        List<ReservationTime> reservationTimes = List.of(
                reservedTime,
                nonReservedTime
        );
        when(reservationTimeRepository.findAll()).thenReturn(reservationTimes);

        AvailableTimeRequest request = new AvailableTimeRequest(savedTheme.getId(), searchDate);

        // when && then
        assertThat(reservationTimeService.findAvailableTimes(request))
                .isEqualTo(List.of(
                        AvailableTimeResponse.of(reservedTime, false),
                        AvailableTimeResponse.of(nonReservedTime, true)
                ));
    }

}
