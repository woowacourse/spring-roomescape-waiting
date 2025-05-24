package roomescape.reservation.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;
import roomescape.member.model.Member;
import roomescape.member.model.Role;
import roomescape.reservation.application.dto.response.MyBookingServiceResponse;
import roomescape.reservation.model.entity.Reservation;
import roomescape.reservation.model.entity.ReservationTheme;
import roomescape.reservation.model.entity.ReservationTime;
import roomescape.reservation.model.repository.ReservationRepository;
import roomescape.reservation.model.repository.WaitingRepository;

@SpringBootTest
class MyBookingServiceMockTest {

    @Mock
    ReservationRepository reservationRepository;

    @Mock
    WaitingRepository waitingRepository;

    MyBookingService myBookingService;

    private ReservationTime time10Am;
    private ReservationTime time2Pm;
    private ReservationTheme theme1;
    private ReservationTheme theme2;
    private LocalDate today = LocalDate.now();

    @BeforeEach
    void setUp() {
        myBookingService = new MyBookingService(reservationRepository, waitingRepository);
        time10Am = ReservationTime.builder()
            .id(1L)
            .startAt(LocalTime.of(10, 0))
            .build();
        time2Pm = ReservationTime
            .builder()
            .id(2L)
            .startAt(LocalTime.of(14, 0))
            .build();
        theme1 = ReservationTheme
            .builder()
            .id(1L)
            .name("공포")
            .description("무서운 테마")
            .thumbnail("abc.jpg")
            .build();
        theme2 = ReservationTheme
            .builder()
            .id(2L)
            .name("모험")
            .description("신나는 테마")
            .thumbnail("abc.jpg")
            .build();
    }

    @Test
    @DisplayName("memberId로 올바른 응답을 반환한다")
    void getAllByMemberId() {
        Member member1 = new Member("1234", Role.USER, "a@naver.com", "유저2", 1L);

        Reservation beforeTodayReservation = Reservation.builder()
            .id(1L)
            .theme(theme1)
            .time(time2Pm)
            .date(today.minusDays(2))
            .member(member1)
            .build();
        Reservation afterTodayReservation = Reservation.builder()
            .id(1L)
            .theme(theme1)
            .time(time2Pm)
            .date(today.plusDays(2))
            .member(member1)
            .build();
        List<Reservation> reservations = List.of(beforeTodayReservation, afterTodayReservation);
        given(reservationRepository.findAllByMemberId(1L)).willReturn(reservations);

        List<MyBookingServiceResponse> expected = List.of(
            MyBookingServiceResponse.from(beforeTodayReservation),
            MyBookingServiceResponse.from(afterTodayReservation)
        );
        assertThat(myBookingService.getAllByMemberId(1L)).isEqualTo(expected);
    }
}
