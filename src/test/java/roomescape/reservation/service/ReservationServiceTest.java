package roomescape.reservation.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import roomescape.member.domain.Member;
import roomescape.member.repository.MemberRepository;
import roomescape.vo.Name;
import roomescape.member.role.MemberRole;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.dto.ReservationRequest;
import roomescape.reservation.dto.ReservationResponse;
import roomescape.reservation.repository.ReservationRepository;
import roomescape.theme.domain.Theme;
import roomescape.theme.repository.ThemeRepository;
import roomescape.reservationtime.domain.ReservationTime;
import roomescape.reservationtime.repository.TimeRepository;

@ExtendWith(MockitoExtension.class)
class ReservationServiceTest {

    private final Reservation reservation = new Reservation(
            1L,
            LocalDate.now().plusDays(1),
            new ReservationTime(1L, LocalTime.now()),
            new Theme(1L, new Name("pollaBang"), "폴라 방탈출", "thumbnail"),
            new Member(1L, new Name("polla"), "kyunellroll@gmail.com", "polla99", MemberRole.MEMBER)
    );

    @InjectMocks
    private ReservationService reservationService;
    @Mock
    private ReservationRepository reservationRepository;

    @Mock
    private TimeRepository timeRepository;

    @Mock
    private ThemeRepository themeRepository;

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private Name name;

    @Test
    @DisplayName("예약을 추가한다.")
    void addReservation() {
        when(reservationRepository.save(any()))
                .thenReturn(reservation);

        when(timeRepository.findById(1L))
                .thenReturn(Optional.of(new ReservationTime()));

        when(themeRepository.findById(1L))
                .thenReturn(Optional.of(new Theme()));

        when(memberRepository.findMemberById(1L))
                .thenReturn(Optional.of(new Member()));

        ReservationRequest reservationRequest = new ReservationRequest(reservation.getDate(),
                reservation.getReservationTime().getId(), reservation.getTheme().getId());

        ReservationResponse reservationResponse = reservationService.addReservation(reservationRequest,
                reservation.getMember().getId());

        Assertions.assertThat(reservationResponse.id()).isEqualTo(1);
    }

    @Test
    @DisplayName("예약을 찾는다.")
    void findReservations() {
        when(reservationRepository.findAllByOrderByDateAscTimeAsc())
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
