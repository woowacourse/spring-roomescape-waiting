package roomescape.waiting.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;

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
import roomescape.exception.BadArgumentRequestException;
import roomescape.member.domain.Member;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.dto.MyReservationResponse;
import roomescape.theme.domain.Theme;
import roomescape.time.domain.ReservationTime;
import roomescape.waiting.domain.Waiting;
import roomescape.waiting.dto.WaitingResponse;
import roomescape.waiting.repository.WaitingRepository;

@ExtendWith(MockitoExtension.class)
class WaitingServiceTest {
    private static final Member RESERVATION_OWNER = new Member(1L, "브라운", "brown@abc.com");
    private static final Member WAITING_OWNER = new Member(2L, "브리", "bri@abc.com");
    private static final Member NOT_WAITING_OWNER = new Member(3L, "썬", "sun@abc.com");
    private static final Reservation RESERVATION = new Reservation(1L, RESERVATION_OWNER, LocalDate.now().plusDays(7),
            new ReservationTime(1L, LocalTime.of(19, 0)), new Theme(1L, "레벨2 탈출", "레벨2 탈출하기", "https://img.jpg"));
    private static final Reservation BEFORE_RESERVATION = new Reservation(1L, RESERVATION_OWNER,
            LocalDate.now().minusDays(7),
            new ReservationTime(1L, LocalTime.of(19, 0)), new Theme(1L, "레벨2 탈출", "레벨2 탈출하기", "https://img.jpg"));
    private static final Waiting WAITING = new Waiting(3L, RESERVATION, WAITING_OWNER, LocalDateTime.now());
    private static final Waiting BEFORE_WAITING = new Waiting(3L, BEFORE_RESERVATION, WAITING_OWNER,
            LocalDateTime.now());
    private static final WaitingResponse RESPONSE1 = new WaitingResponse(3L, "브리", "레벨2 탈출",
            LocalDate.now().plusDays(7), LocalTime.of(19, 0));
    private static final MyReservationResponse MY_RESPONSE1 = new MyReservationResponse(1L, "레벨2 탈출",
            LocalDate.now().plusDays(7), LocalTime.of(19, 0), "1번째 예약 대기", 3L);

    @Mock
    private WaitingRepository waitingRepository;
    @InjectMocks
    private WaitingService waitingService;

    @DisplayName("모든 예약 대기를 불러올 수 있다.")
    @Test
    void findWaitingsTest() {
        given(waitingRepository.findAll()).willReturn(List.of(WAITING));
        List<WaitingResponse> expected = List.of(RESPONSE1);

        List<WaitingResponse> actual = waitingService.findWaitings();

        assertThat(actual).isEqualTo(expected);

    }

    @DisplayName("특정 회원의 모든 예약 대기를 불러올 수 있다.")
    @Test
    void findWaitingsByMemberIdTest() {
        given(waitingRepository.findByMemberId(2L)).willReturn(List.of(WAITING));
        given(waitingRepository.countByReservationAndCreatedAtLessThanEqual(eq(RESERVATION), any(LocalDateTime.class)))
                .willReturn(1L);
        List<MyReservationResponse> expected = List.of(MY_RESPONSE1);

        List<MyReservationResponse> actual = waitingService.findWaitingsByMemberId(2L);

        assertThat(actual).isEqualTo(expected);
    }

    @DisplayName("가장 최우선 예약 대기를 조회할 수 있다.")
    @Test
    void findHighPriorityWaitingTest() {
        given(waitingRepository.findTopByReservationIdOrderByCreatedAtAsc(1L)).willReturn(Optional.of(WAITING));
        Optional<WaitingResponse> expected = Optional.of(RESPONSE1);

        Optional<WaitingResponse> actual = waitingService.findHighPriorityWaiting(1L);

        assertThat(actual).isEqualTo(expected);
    }

    @DisplayName("예약 대기에서 예약을 확정할 수 있다.")
    @Test
    void confirmReservationTest() {
        given(waitingRepository.findById(3L)).willReturn(Optional.of(WAITING));

        waitingService.confirmReservation(3L);

        Member currentReservationOwner = RESERVATION.getMember();
        assertThat(currentReservationOwner).isEqualTo(WAITING_OWNER);
    }

    @DisplayName("예약 대기에서 예약을 확정 시, 예약이 없다면 예외를 던진다.")
    @Test
    void confirmReservationTest_whenWaitingNotExist() {
        given(waitingRepository.findById(3L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> waitingService.confirmReservation(3L))
                .isInstanceOf(BadArgumentRequestException.class)
                .hasMessage("해당 예약 대기가 존재하지 않습니다.");
    }

    @DisplayName("예약 대기를 삭제할 수 있다.")
    @Test
    void deleteWaitingTest() {
        given(waitingRepository.findById(3L)).willReturn(Optional.of(WAITING));

        assertThatCode(() -> waitingService.deleteWaiting(3L, WAITING_OWNER.getId()))
                .doesNotThrowAnyException();
    }

    @DisplayName("현재보다 이전 예약 대기를 삭제할 수 없다.")
    @Test
    void deleteWaitingTest_whenRequesterIsNotOwner() {
        given(waitingRepository.findById(3L)).willReturn(Optional.of(BEFORE_WAITING));

        assertThatThrownBy(() -> waitingService.deleteWaiting(3L, NOT_WAITING_OWNER.getId()))
                .isInstanceOf(BadArgumentRequestException.class)
                .hasMessage("해당 예약을 취소할 수 없습니다.");
    }
}
