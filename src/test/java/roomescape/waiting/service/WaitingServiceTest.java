package roomescape.waiting.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

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
import roomescape.member.domain.Member;
import roomescape.member.dto.MemberResponse;
import roomescape.member.repository.MemberRepository;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.dto.MyReservationWaitingResponse;
import roomescape.reservation.dto.ReservationResponse;
import roomescape.reservation.repository.ReservationRepository;
import roomescape.theme.domain.Theme;
import roomescape.time.domain.ReservationTime;
import roomescape.waiting.domain.Waiting;
import roomescape.waiting.domain.WaitingWithOrder;
import roomescape.waiting.dto.WaitingCreateRequest;
import roomescape.waiting.dto.WaitingResponse;
import roomescape.waiting.repository.WaitingRepository;

@ExtendWith(MockitoExtension.class)
class WaitingServiceTest {
    @Mock
    private WaitingRepository waitingRepository;
    @Mock
    private ReservationRepository reservationRepository;
    @Mock
    private MemberRepository memberRepository;
    @InjectMocks
    private WaitingService waitingService;

    @DisplayName("나의 예약 대기 목록을 조회할 수 있다.")
    @Test
    void findMyWaitingTest() {
        LocalDate date = LocalDate.now().plusDays(7);
        WaitingCreateRequest request = new WaitingCreateRequest(date, 1L, 1L);
        Reservation reservation = new Reservation(
                1L,
                new Member(1L, "브라운", "brown@abc.com"),
                LocalDate.of(2024, 8, 15),
                new ReservationTime(1L, LocalTime.of(19, 0)),
                new Theme(1L, "레벨2 탈출", "레벨2 탈출하기", "https://img.jpg"));
        Member waitingMember = new Member(2L, "낙낙", "naknak@abc.com");
        WaitingWithOrder waitingWithOrder = new WaitingWithOrder(new Waiting(reservation, waitingMember), 1L);

        given(waitingRepository.findByMember_idWithRank(2L))
                .willReturn(List.of(waitingWithOrder));

        List<MyReservationWaitingResponse> expected = List.of(MyReservationWaitingResponse.from(waitingWithOrder));

        List<MyReservationWaitingResponse> actual = waitingService.findMyWaitings(2L);

        assertThat(actual).isEqualTo(expected);
    }

    @DisplayName("예약 대기를 생성할 수 있다.")
    @Test
    void createWaitingTest() {
        LocalDate date = LocalDate.now().plusDays(7);
        WaitingCreateRequest request = new WaitingCreateRequest(date, 1L, 1L);
        Reservation reservation = new Reservation(
                1L,
                new Member(1L, "브라운", "brown@abc.com"),
                LocalDate.of(2024, 8, 15),
                new ReservationTime(1L, LocalTime.of(19, 0)),
                new Theme(1L, "레벨2 탈출", "레벨2 탈출하기", "https://img.jpg"));
        Member waitingMember = new Member(2L, "낙낙", "naknak@abc.com");

        given(reservationRepository.findByDateAndTime_idAndTheme_id(date, 1L, 1L))
                .willReturn(Optional.of(reservation));
        given(memberRepository.findById(2L))
                .willReturn(Optional.of(waitingMember));
        given(waitingRepository.save(any()))
                .willReturn(new Waiting(1L, reservation, waitingMember));

        WaitingResponse expected = new WaitingResponse(
                1L,
                ReservationResponse.from(reservation),
                MemberResponse.from(waitingMember));

        WaitingResponse actual = waitingService.createWaiting(request, waitingMember.getId());

        assertThat(actual)
                .usingRecursiveComparison()
                .isEqualTo(expected);
    }

    @DisplayName("예약 대기시, 예약이 존재하지 않는다면 예외를 던진다.")
    @Test
    void createWaitingTest_whenReservationNotExist() {
        LocalDate date = LocalDate.now().plusDays(7);
        WaitingCreateRequest request = new WaitingCreateRequest(date, 1L, 1L);
        Reservation reservation = new Reservation(
                1L,
                new Member(1L, "브라운", "brown@abc.com"),
                LocalDate.of(2024, 8, 15),
                new ReservationTime(1L, LocalTime.of(19, 0)),
                new Theme(1L, "레벨2 탈출", "레벨2 탈출하기", "https://img.jpg"));
        Member waitingMember = new Member(2L, "낙낙", "naknak@abc.com");

        given(reservationRepository.findByDateAndTime_idAndTheme_id(date, 1L, 1L))
                .willReturn(Optional.empty());

        assertThatThrownBy(() -> waitingService.createWaiting(request, waitingMember.getId()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("존재하지 않는 예약에 대해 대기할 수 없습니다.");
    }

    @DisplayName("예약 대기시, 이미 예약 대기를 한적이 있다면 예외를 발생시킨다.")
    @Test
    void createWaitingTest_whenWaitingDuplicate() {
        LocalDate date = LocalDate.now().plusDays(7);
        WaitingCreateRequest request = new WaitingCreateRequest(date, 1L, 1L);
        Reservation reservation = new Reservation(
                1L,
                new Member(1L, "브라운", "brown@abc.com"),
                LocalDate.of(2024, 8, 15),
                new ReservationTime(1L, LocalTime.of(19, 0)),
                new Theme(1L, "레벨2 탈출", "레벨2 탈출하기", "https://img.jpg"));
        Member waitingMember = new Member(2L, "낙낙", "naknak@abc.com");

        given(reservationRepository.findByDateAndTime_idAndTheme_id(date, 1L, 1L))
                .willReturn(Optional.of(reservation));
        given(memberRepository.findById(2L))
                .willReturn(Optional.of(waitingMember));
        given(waitingRepository.existsByReservation_idAndMember_id(1L, 2L))
                .willReturn(true);

        assertThatThrownBy(() -> waitingService.createWaiting(request, waitingMember.getId()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("중복으로 예약 대기를 할 수 없습니다.");
    }
}
