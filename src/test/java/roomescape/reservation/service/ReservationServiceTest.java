package roomescape.reservation.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
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
import roomescape.exception.RoomEscapeException;
import roomescape.exception.model.ReservationExceptionCode;
import roomescape.registration.domain.reservation.service.ReservationService;
import roomescape.member.domain.Member;
import roomescape.member.repository.MemberRepository;
import roomescape.vo.Name;
import roomescape.member.domain.MemberRole;
import roomescape.registration.domain.reservation.domain.Reservation;
import roomescape.registration.domain.reservation.dto.ReservationRequest;
import roomescape.registration.domain.reservation.dto.ReservationResponse;
import roomescape.registration.domain.reservation.repository.ReservationRepository;
import roomescape.theme.domain.Theme;
import roomescape.theme.repository.ThemeRepository;
import roomescape.reservationtime.domain.ReservationTime;
import roomescape.reservationtime.repository.ReservationTimeRepository;

@ExtendWith(MockitoExtension.class)
class ReservationServiceTest {

    private static final LocalDate BEFORE = LocalDate.now().minusDays(1);
    private static final LocalTime TIME = LocalTime.of(9, 0);

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
    private ReservationTimeRepository reservationTimeRepository;

    @Mock
    private ThemeRepository themeRepository;

    @Mock
    private MemberRepository memberRepository;

    @Test
    @DisplayName("예약을 추가한다.")
    void addReservation() {
        when(reservationRepository.save(any()))
                .thenReturn(reservation);

        when(reservationTimeRepository.findById(1L))
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
        when(reservationRepository.findAllByOrderByDateAscReservationTimeAsc())
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

        assertDoesNotThrow(() -> reservationService.removeReservation(reservation.getId()));
    }

    @Test
    @DisplayName("과거의 날짜를 예약하려고 시도하는 경우 에러를 발생한다.")
    void validation_ShouldThrowException_WhenReservationDateIsPast() {
        when(themeRepository.findById(1L))
                .thenReturn(Optional.of(new Theme(1L, new Name("레모네 테마"), "무서워용", "공포.jpg")));
        when(reservationTimeRepository.findById(1L))
                .thenReturn(Optional.of(new ReservationTime(1L, TIME)));
        when(memberRepository.findMemberById(1L))
                .thenReturn(Optional.of(new Member(1L, new Name("폴라"), "polla@naver.com", "polla12", MemberRole.MEMBER)));

        ReservationRequest reservationRequest = new ReservationRequest(BEFORE, 1L, 1L);

        Throwable pastDateReservation = assertThrows(RoomEscapeException.class,
                () -> reservationService.addReservation(reservationRequest, 1L));

        assertEquals(ReservationExceptionCode.RESERVATION_DATE_IS_PAST_EXCEPTION.getMessage(),
                pastDateReservation.getMessage());
    }
}
