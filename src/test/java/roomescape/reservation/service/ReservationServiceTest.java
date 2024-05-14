package roomescape.reservation.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import roomescape.member.domain.Member;
import roomescape.name.domain.Name;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.dto.ReservationRequest;
import roomescape.reservation.dto.ReservationResponse;
import roomescape.reservation.repository.ReservationRepository;
import roomescape.theme.domain.Theme;
import roomescape.time.domain.Time;

@ExtendWith(MockitoExtension.class)
class ReservationServiceTest {

    private final Reservation reservation = Reservation.reservationOf(1L, LocalDate.now().plusDays(1),
            new Time(1L, LocalTime.now()), Theme.themeOf(1L, "pollaBang", "폴라 방탈출", "thumbnail"),
            Member.memberOf(1L, "polla", "kyunellroll@gmail.com", "polla99", "admin"));

    @InjectMocks
    private ReservationService reservationService;
    @Mock
    private ReservationRepository reservationRepository;

    @Mock
    private Name name;

    @Test
    @DisplayName("예약을 추가한다.")
    void addReservation() {
        when(reservationRepository.save(any()))
                .thenReturn(reservation);

        ReservationRequest reservationRequest = new ReservationRequest(reservation.getDate(),
                reservation.getReservationTime().getId(), reservation.getTheme().getId());

        ReservationResponse reservationResponse = reservationService.addReservation(reservationRequest,
                reservation.getMember().getId());

        Assertions.assertThat(reservationResponse.id()).isEqualTo(1);
    }

    @Test
    @DisplayName("예약을 찾는다.")
    void findReservations() {
        when(reservationRepository.findAllReservationOrderByDateAndTimeStartAt())
                .thenReturn(List.of(reservation));

        List<ReservationResponse> reservationResponses = reservationService.findReservations();

        Assertions.assertThat(reservationResponses).hasSize(1);
    }

    @Test
    @DisplayName("예약을 지운다.")
    void removeReservations() {
        doNothing()
                .when(reservationRepository)
                .deleteById(reservation.getId());

        assertDoesNotThrow(() -> reservationService.removeReservations(reservation.getId()));
    }
}
