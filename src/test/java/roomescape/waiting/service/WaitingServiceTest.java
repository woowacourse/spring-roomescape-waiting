package roomescape.waiting.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

import java.time.LocalDate;
import java.time.LocalTime;
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
import roomescape.reservation.dto.ReservationResponse;
import roomescape.reservation.repository.ReservationRepository;
import roomescape.theme.domain.Theme;
import roomescape.time.domain.ReservationTime;
import roomescape.waiting.domain.Waiting;
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
}
