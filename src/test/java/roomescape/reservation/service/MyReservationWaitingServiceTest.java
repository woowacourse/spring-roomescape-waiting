package roomescape.reservation.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import roomescape.member.domain.Member;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.dto.MyReservationWaitingResponse;
import roomescape.theme.domain.Theme;
import roomescape.time.domain.ReservationTime;
import roomescape.waiting.domain.Waiting;
import roomescape.waiting.domain.WaitingWithOrder;
import roomescape.waiting.service.WaitingService;

@ExtendWith(MockitoExtension.class)
class MyReservationWaitingServiceTest {
    @Mock
    private ReservationService reservationService;
    @Mock
    private WaitingService waitingService;
    @InjectMocks
    private MyReservationWaitingService myReservationWaitingService;

    @DisplayName("나의 예약 및 대기 목록을 조회할 수 있다.")
    @Test
    void findMyReservationsWaitingsTest() {
        Member member = new Member(1L, "브라운", "brown@abc.com");
        Reservation reservation1 = new Reservation(
                1L,
                new Member(1L, "브라운", "brown@abc.com"),
                LocalDate.of(2100, 1, 1),
                new ReservationTime(1L, LocalTime.of(19, 0)),
                new Theme(1L, "레벨2 탈출", "레벨2 탈출하기", "https://img.jpg"));
        Reservation reservation2 = new Reservation(
                2L, new Member(1L, "브라운", "bri@abc.com"),
                LocalDate.of(2100, 3, 1),
                new ReservationTime(1L, LocalTime.of(19, 0)),
                new Theme(1L, "레벨1 탈출", "레벨1 탈출하기", "https://img.jpg"));
        WaitingWithOrder waitingWithOrder1 = new WaitingWithOrder(new Waiting(new Reservation(
                2L, new Member(2L, "낙낙", "bri@abc.com"),
                LocalDate.of(2100, 2, 1),
                new ReservationTime(1L, LocalTime.of(19, 0)),
                new Theme(1L, "레벨3 탈출", "레벨3 탈출하기", "https://img.jpg")),
                member),
                3L);
        WaitingWithOrder waitingWithOrder2 = new WaitingWithOrder(new Waiting(new Reservation(
                2L, new Member(2L, "낙낙", "bri@abc.com"),
                LocalDate.of(2100, 4, 1),
                new ReservationTime(1L, LocalTime.of(19, 0)),
                new Theme(1L, "레벨4 탈출", "레벨4 탈출하기", "https://img.jpg")),
                member),
                5L);

        given(reservationService.findMyReservations(member.getId())).willReturn(List.of(
                MyReservationWaitingResponse.from(reservation1),
                MyReservationWaitingResponse.from(reservation2)
        ));

        given(waitingService.findMyWaitings(member.getId())).willReturn(List.of(
                MyReservationWaitingResponse.from(waitingWithOrder1),
                MyReservationWaitingResponse.from(waitingWithOrder2)
        ));

        List<MyReservationWaitingResponse> actual =
                myReservationWaitingService.findMyReservationsWaitings(member.getId());

        List<MyReservationWaitingResponse> expected = List.of(
                MyReservationWaitingResponse.from(reservation1),
                MyReservationWaitingResponse.from(waitingWithOrder1),
                MyReservationWaitingResponse.from(reservation2),
                MyReservationWaitingResponse.from(waitingWithOrder2)
        );

        assertThat(actual).isEqualTo(expected);
    }
}
