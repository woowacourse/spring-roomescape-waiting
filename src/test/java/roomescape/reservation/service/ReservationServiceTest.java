package roomescape.reservation.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import roomescape.exception.ConflictException;
import roomescape.member.domain.Member;
import roomescape.member.dto.MemberProfileInfo;
import roomescape.member.repository.MemberRepository;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationDetail;
import roomescape.reservation.dto.ReservationRequest;
import roomescape.reservation.dto.ReservationResponse;
import roomescape.reservation.repository.ReservationDetailRepository;
import roomescape.reservation.repository.ReservationRepository;
import roomescape.theme.domain.Theme;
import roomescape.time.domain.Time;

@ExtendWith(MockitoExtension.class)
class ReservationServiceTest {
    private final Time time = new Time(1L, LocalTime.of(12, 0));
    private final Theme theme = new Theme(1L, "그켬미", "켬미 방탈출", "thumbnail");
    private final Member member = new Member(1L, "켬미", "kyummi@email.com", "pass");
    private final ReservationDetail detail = new ReservationDetail(1L, theme, time, LocalDate.MAX);
    private final Reservation reservation = new Reservation(1L, member, detail);

    private final MemberProfileInfo memberProfileInfo = new MemberProfileInfo(1L, "Dobby", "kimdobby@wotaeco.com");
    @InjectMocks
    private ReservationService reservationService;
    @Mock
    private ReservationRepository reservationRepository;
    @Mock
    private ReservationDetailRepository detailRepository;
    @Mock
    private MemberRepository memberRepository;

    @Test
    @DisplayName("예약을 추가한다.")
    void addReservation() {
        Mockito.when(reservationRepository.save(any()))
                .thenReturn(reservation);
        Mockito.when(memberRepository.findById(any(Long.class)))
                .thenReturn(Optional.ofNullable(reservation.getMember()));
        Mockito.when(detailRepository.findById(any(Long.class)))
                .thenReturn(Optional.ofNullable(reservation.getDetail()));
        Mockito.when(reservationRepository.findByDetail_Id(any(Long.class)))
                .thenReturn(Optional.empty());

        ReservationRequest reservationRequest = new ReservationRequest(
                reservation.getMemberId(),
                reservation.getDetailId());
        ReservationResponse reservationResponse = reservationService.addReservation(reservationRequest);

        assertThat(reservationResponse.id()).isEqualTo(1);
    }

    @Test
    @DisplayName("예약을 찾는다.")
    void findReservations() {
        Mockito.when(reservationRepository.findAllByOrderByDetailDateAsc())
                .thenReturn(List.of(reservation));

        List<ReservationResponse> reservationResponses = reservationService.findReservations();

        assertThat(reservationResponses).hasSize(1);
    }

    @Test
    @DisplayName("예약을 지운다.")
    void removeReservations() {
        Mockito.doNothing()
                .when(reservationRepository)
                .deleteById(reservation.getId());

        assertThatCode(() -> reservationService.removeReservations(reservation.getId()))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("특정 테마의 예약이 존재하는 시간에 예약을 요청할 때 예외를 던진다.")
    void addReservation_ShouldThrowException_WhenDuplicatedReservationRequestOccurs() {
        Mockito.when(detailRepository.findById(any(Long.class)))
                .thenReturn(Optional.ofNullable(reservation.getDetail()));
        Mockito.when(memberRepository.findById(any(Long.class)))
                .thenReturn(Optional.ofNullable(reservation.getMember()));
        Mockito.when(reservationRepository.findByDetail_Id(any(Long.class)))
                .thenReturn(Optional.of(reservation));

        ReservationRequest reservationRequest = new ReservationRequest(
                reservation.getMemberId(),
                reservation.getDetailId());

        assertThatThrownBy(() -> reservationService.addReservation(reservationRequest))
                .isInstanceOf(ConflictException.class);
    }
}
