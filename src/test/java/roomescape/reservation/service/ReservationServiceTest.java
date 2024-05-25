package roomescape.reservation.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
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

import roomescape.exception.ConflictException;
import roomescape.member.domain.Member;
import roomescape.member.repository.MemberRepository;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationDetail;
import roomescape.reservation.dto.MyReservationResponse;
import roomescape.reservation.dto.ReservationConditionSearchRequest;
import roomescape.reservation.dto.ReservationRequest;
import roomescape.reservation.dto.ReservationResponse;
import roomescape.reservation.dto.ReservationTimeAvailabilityResponse;
import roomescape.reservation.repository.ReservationDetailRepository;
import roomescape.reservation.repository.ReservationRepository;
import roomescape.theme.domain.Theme;
import roomescape.time.domain.Time;
import roomescape.time.repository.TimeRepository;

@ExtendWith(MockitoExtension.class)
class ReservationServiceTest {
    private final Time time = new Time(1L, LocalTime.of(12, 0));
    private final Theme theme = new Theme(1L, "그켬미", "켬미 방탈출", "thumbnail");
    private final Member member = new Member(1L, "켬미", "kyummi@email.com", "pass");
    private final ReservationDetail detail = new ReservationDetail(1L, theme, time, LocalDate.MAX.minusDays(1));
    private final Reservation reservation = new Reservation(1L, member, detail);

    @InjectMocks
    private ReservationService reservationService;
    @Mock
    private ReservationRepository reservationRepository;
    @Mock
    private ReservationDetailRepository detailRepository;
    @Mock
    private MemberRepository memberRepository;
    @Mock
    private TimeRepository timeRepository;

    @Test
    @DisplayName("성공 : 예약 정보를 얻을 수 있다.")
    void findReservations() {
        // Given
        ReservationResponse expected = ReservationResponse.from(reservation);
        when(reservationRepository.findAllByOrderByDetailDateAsc())
                .thenReturn(List.of(reservation));

        // When
        List<ReservationResponse> reservationResponses = reservationService.findReservations();

        // Then
        assertThat(reservationResponses).containsExactly(expected);
    }

    @Test
    @DisplayName("성공 : 멤버, 테마, 정해진 기간이 주어졌을 때 해당되는 예약 정보를 얻을 수 있다.")
    void findReservationsByConditions() {
        // Given
        ReservationConditionSearchRequest request = new ReservationConditionSearchRequest(
                member.getId(), theme.getId(), LocalDate.MIN, LocalDate.MAX);
        ReservationResponse expected = ReservationResponse.from(reservation);
        when(reservationRepository.findAllByMember_Id(any(Long.class)))
                .thenReturn(List.of(reservation));

        // When
        List<ReservationResponse> reservationResponses = reservationService.findReservationsByConditions(request);

        // Then
        assertThat(reservationResponses).containsExactly(expected);
    }

    @Test
    @DisplayName("성공 : 예약 정보를 얻을 수 있다.")
    void findReservationByMemberId() {
        // Given
        MyReservationResponse expected = MyReservationResponse.from(reservation);
        when(reservationRepository.findAllByMember_IdOrderByDetailDateAsc(any(Long.class)))
                .thenReturn(List.of(reservation));

        // When
        List<MyReservationResponse> reservationResponses
                = reservationService.findReservationByMemberId(reservation.getId());

        // Then
        assertThat(reservationResponses).containsExactly(expected);
    }

    @Test
    @DisplayName("성공 : 사용자, 예약 정보인 예약이 없다.")
    void findReservationByDetailId() {
        // Given
        ReservationRequest request = new ReservationRequest(member.getId(), detail.getId());
        when(reservationRepository.findByDetail_IdAndMember_Id(any(Long.class), any(Long.class)))
                .thenReturn(Optional.empty());

        // Then
        assertThatCode(() -> reservationService.findReservationByDetailId(request))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("실패 : 해당 사용자는 예약되어있으므로 예외가 발생한다.")
    void findReservationByDetailId_Exception() {
        // Given
        ReservationRequest request = new ReservationRequest(member.getId(), detail.getId());
        when(reservationRepository.findByDetail_IdAndMember_Id(any(Long.class), any(Long.class)))
                .thenReturn(Optional.of(reservation));

        // Then
        assertThatThrownBy(() -> reservationService.findReservationByDetailId(request))
                .isInstanceOf(ConflictException.class)
                .hasMessage("해당 테마(%s)의 해당 시간(%s)에 이미 예약 되어있습니다.".formatted(theme.getName(), time.getStartAt()));
    }

    @Test
    @DisplayName("성공 : 사용가능한 시간을 알 수 있다.")
    void findTimeAvailability() {
        // Given
        ReservationTimeAvailabilityResponse expected = ReservationTimeAvailabilityResponse.from(time, true);
        when(timeRepository.findAllByOrderByStartAtAsc())
                .thenReturn(List.of(time));
        when(reservationRepository.findAllByDetailTheme_IdAndDetailDate(any(Long.class), any(LocalDate.class)))
                .thenReturn(List.of(reservation));

        // Then
        assertThat(reservationService.findTimeAvailability(theme.getId(), detail.getDate()))
                .containsExactly(expected);
    }

    @Test
    @DisplayName("예약을 추가한다.")
    void addReservation() {
        // given
        when(memberRepository.findById(any(Long.class)))
                .thenReturn(Optional.ofNullable(reservation.getMember()));
        when(detailRepository.findById(any(Long.class)))
                .thenReturn(Optional.ofNullable(reservation.getDetail()));
        when(reservationRepository.findByDetail_Id(any(Long.class)))
                .thenReturn(Optional.empty());
        when(reservationRepository.save(any(Reservation.class)))
                .thenReturn(reservation);

        // when
        ReservationRequest reservationRequest = new ReservationRequest(
                reservation.getMemberId(),
                reservation.getDetailId());
        ReservationResponse reservationResponse = reservationService.addReservation(reservationRequest);

        // then
        assertThat(reservationResponse.id()).isEqualTo(reservation.getId());
    }
}
