package roomescape.reservation.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
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
import roomescape.exception.BadArgumentRequestException;
import roomescape.member.domain.Member;
import roomescape.member.dto.MemberResponse;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.dto.MyReservationResponse;
import roomescape.reservation.dto.ReservationResponse;
import roomescape.reservation.repository.ReservationRepository;
import roomescape.theme.domain.Theme;
import roomescape.theme.dto.ThemeResponse;
import roomescape.time.domain.ReservationTime;
import roomescape.time.dto.TimeResponse;

@ExtendWith(MockitoExtension.class)
class ReservationFindServiceTest {
    private static final Reservation RESERVATION1 = new Reservation(
            1L, new Member(1L, "브라운", "brown@abc.com"),
            LocalDate.of(2024, 8, 15),
            new ReservationTime(1L, LocalTime.of(19, 0)),
            new Theme(1L, "레벨2 탈출", "레벨2 탈출하기", "https://img.jpg"));
    private static final Reservation RESERVATION2 = new Reservation(
            2L, new Member(2L, "브리", "bri@abc.com"),
            LocalDate.of(2024, 8, 20),
            new ReservationTime(1L, LocalTime.of(19, 0)),
            new Theme(1L, "레벨2 탈출", "레벨2 탈출하기", "https://img.jpg"));
    private static final ReservationResponse RESPONSE1 = new ReservationResponse(
            1L, new MemberResponse(1L, "브라운"),
            LocalDate.of(2024, 8, 15),
            new TimeResponse(1L, LocalTime.of(19, 0)),
            new ThemeResponse(1L, "레벨2 탈출", "레벨2 탈출하기", "https://img.jpg"));
    private static final ReservationResponse RESPONSE2 = new ReservationResponse(
            2L, new MemberResponse(2L, "브리"),
            LocalDate.of(2024, 8, 20),
            new TimeResponse(1L, LocalTime.of(19, 0)),
            new ThemeResponse(1L, "레벨2 탈출", "레벨2 탈출하기", "https://img.jpg"));
    private static final MyReservationResponse MY_RESPONSE1 = new MyReservationResponse(
            1L, "레벨2 탈출",
            LocalDate.of(2024, 8, 15), LocalTime.of(19, 0),
            "예약", null
    );

    @Mock
    private ReservationRepository reservationRepository;
    @InjectMocks
    private ReservationFindService reservationFindService;

    @DisplayName("id를 통해 예약을 조회할 수 있다.")
    @Test
    void findReservationTest() {
        given(reservationRepository.findById(1L)).willReturn(Optional.of(RESERVATION1));
        ReservationResponse expected = RESPONSE1;

        assertThat(reservationFindService.findReservation(1L)).isIn(RESPONSE1);
    }

    @DisplayName("id에 해당하는 예약이 없을 때, 예외를 던진다.")
    @Test
    void findReservationTest_whenReservationNotExist() {
        given(reservationRepository.findById(1L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> reservationFindService.findReservation(1L))
                .isInstanceOf(BadArgumentRequestException.class)
                .hasMessage("해당 예약을 찾을 수 없습니다.");
    }

    @DisplayName("모든 예약을 조회할 수 있다.")
    @Test
    void findReservationsTest() {
        given(reservationRepository.findAll()).willReturn(List.of(RESERVATION1, RESERVATION2));
        List<ReservationResponse> expected = List.of(RESPONSE1, RESPONSE2);

        List<ReservationResponse> actual = reservationFindService.findReservations();

        assertThat(actual).isEqualTo(expected);
    }

    @DisplayName("멤버 id를 통해 해당하는 모든 예약을 조회할 수 있다.")
    @Test
    void findReservationsByMemberIdTest() {
        given(reservationRepository.findByMemberId(1L)).willReturn(List.of(RESERVATION1));
        List<MyReservationResponse> expected = List.of(MY_RESPONSE1);

        List<MyReservationResponse> actual = reservationFindService.findReservationsByMemberId(1L);

        assertThat(actual).isEqualTo(expected);
    }
}
