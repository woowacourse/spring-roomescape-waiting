package roomescape.reservation.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.LocalDateTime;
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
import roomescape.reservation.domain.ReservationDetail;
import roomescape.reservation.domain.ReservationWaiting;
import roomescape.reservation.dto.MyReservationResponse;
import roomescape.reservation.dto.ReservationRequest;
import roomescape.reservation.dto.ReservationResponse;
import roomescape.reservation.repository.ReservationDetailRepository;
import roomescape.reservation.repository.ReservationWaitingRepository;
import roomescape.theme.domain.Theme;
import roomescape.time.domain.Time;

@ExtendWith(MockitoExtension.class)
class ReservationWaitingServiceTest {
    private final Time time = new Time(1L, LocalTime.of(12, 0));
    private final Theme theme = new Theme(1L, "그켬미", "켬미 방탈출", "thumbnail");
    private final Member member = new Member(1L, "켬미", "kyummi@email.com", "pass");
    private final ReservationDetail detail = new ReservationDetail(1L, theme, time, LocalDate.MAX.minusDays(1));
    private final ReservationWaiting reservationWaiting = new ReservationWaiting(1L, member, detail);

    @InjectMocks
    private ReservationWaitingService waitingService;
    @Mock
    private ReservationWaitingRepository waitingRepository;
    @Mock
    private ReservationDetailRepository detailRepository;
    @Mock
    private MemberRepository memberRepository;

    @Test
    @DisplayName("성공 : 예약 대기 정보를 얻을 수 있다.")
    void findReservations() {
        // Given
        ReservationResponse expected = ReservationResponse.from(reservationWaiting);
        when(waitingRepository.findAllByOrderById())
                .thenReturn(List.of(reservationWaiting));

        // When
        List<ReservationResponse> reservationResponses = waitingService.findReservationWaitings();

        // Then
        assertThat(reservationResponses).containsExactly(expected);
    }

    @Test
    @DisplayName("성공 : 아이디로 예약 대기 정보를 얻을 수 있다.")
    void findReservationWaitingByMemberId() {
        // Given
        MyReservationResponse expected = MyReservationResponse.from(reservationWaiting, 1);
        when(waitingRepository.findAllByMember_IdOrderByDetailDateAsc(any(Long.class)))
                .thenReturn(List.of(reservationWaiting));
        when(waitingRepository.countByCreateAtBeforeAndAndDetail_id(any(LocalDateTime.class), any(Long.class)))
                .thenReturn(0);

        // When
        List<MyReservationResponse> reservationResponses
                = waitingService.findReservationWaitingByMemberId(reservationWaiting.getId());

        // Then
        assertThat(reservationResponses).containsExactly(expected);
    }

    @Test
    @DisplayName("성공 : 사용자, 예약 정보인 예약이 없다.")
    void findReservationWaitingByDetailId() {
        // Given
        ReservationRequest request = new ReservationRequest(member.getId(), detail.getId());
        when(waitingRepository.findByMember_IdAndDetail_Id(any(Long.class), any(Long.class)))
                .thenReturn(Optional.empty());

        // Then
        assertThatCode(() -> waitingService.checkExistsReservationWaiting(request))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("실패 : 해당 사용자는 예약되어있으므로 예외가 발생한다.")
    void findReservationWaitingByDetailId_Exception() {
        // Given
        ReservationRequest request = new ReservationRequest(member.getId(), detail.getId());
        when(waitingRepository.findByMember_IdAndDetail_Id(any(Long.class), any(Long.class)))
                .thenReturn(Optional.of(reservationWaiting));

        // Then
        assertThatThrownBy(() -> waitingService.checkExistsReservationWaiting(request))
                .isInstanceOf(ConflictException.class)
                .hasMessage("해당 테마(%s)의 해당 시간(%s)에 이미 예약 대기가 존재합니다."
                        .formatted(theme.getName(), time.getStartAt()));
    }


    @Test
    @DisplayName("성공 : 예약 대기를 추가한다.")
    void addReservationWaiting() {
        // given
        when(memberRepository.findById(any(Long.class)))
                .thenReturn(Optional.ofNullable(reservationWaiting.getMember()));
        when(detailRepository.findById(any(Long.class)))
                .thenReturn(Optional.ofNullable(reservationWaiting.getDetail()));
        when(waitingRepository.save(any(ReservationWaiting.class)))
                .thenReturn(reservationWaiting);

        // when
        ReservationRequest reservationRequest = new ReservationRequest(
                reservationWaiting.getMemberId(),
                reservationWaiting.getDetailId());
        ReservationResponse reservationResponse = waitingService.addReservationWaiting(reservationRequest);

        // then
        assertThat(reservationResponse.id()).isEqualTo(reservationWaiting.getId());
    }
}
