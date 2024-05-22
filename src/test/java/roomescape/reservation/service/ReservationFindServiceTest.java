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
import roomescape.member.dto.MemberResponse;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.dto.ReservationResponse;
import roomescape.reservation.repository.ReservationRepository;
import roomescape.theme.domain.Theme;
import roomescape.theme.dto.ThemeResponse;
import roomescape.time.domain.ReservationTime;
import roomescape.time.dto.TimeResponse;

@ExtendWith(MockitoExtension.class)
class ReservationFindServiceTest {
    @Mock
    private ReservationRepository reservationRepository;
    @InjectMocks
    private ReservationFindService reservationFindService;

    @DisplayName("모든 예약을 조회할 수 있다.")
    @Test
    void findReservationsTest() {
        given(reservationRepository.findAll()).willReturn(List.of(
                new Reservation(
                        1L, new Member(1L, "브라운", "brown@abc.com"),
                        LocalDate.of(2024, 8, 15),
                        new ReservationTime(1L, LocalTime.of(19, 0)),
                        new Theme(1L, "레벨2 탈출", "레벨2 탈출하기", "https://img.jpg")),
                new Reservation(
                        2L, new Member(2L, "브리", "bri@abc.com"),
                        LocalDate.of(2024, 8, 20),
                        new ReservationTime(1L, LocalTime.of(19, 0)),
                        new Theme(1L, "레벨2 탈출", "레벨2 탈출하기", "https://img.jpg"))));
        List<ReservationResponse> expected = List.of(
                new ReservationResponse(
                        1L, new MemberResponse(1L, "브라운"),
                        LocalDate.of(2024, 8, 15),
                        new TimeResponse(1L, LocalTime.of(19, 0)),
                        new ThemeResponse(1L, "레벨2 탈출", "레벨2 탈출하기", "https://img.jpg")),
                new ReservationResponse(
                        2L, new MemberResponse(2L, "브리"),
                        LocalDate.of(2024, 8, 20),
                        new TimeResponse(1L, LocalTime.of(19, 0)),
                        new ThemeResponse(1L, "레벨2 탈출", "레벨2 탈출하기", "https://img.jpg")));

        List<ReservationResponse> actual = reservationFindService.findReservations();

        assertThat(actual).isEqualTo(expected);
    }
}
