package roomescape.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

import java.time.LocalDate;
import java.time.LocalTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import roomescape.dao.ReservationDao;
import roomescape.dao.ReservationTimeDao;
import roomescape.dao.ThemeDao;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationTime;
import roomescape.domain.Theme;
import roomescape.dto.request.ReservationRequest;
import roomescape.exception.DuplicateReservationException;
import roomescape.exception.NotCorrectDateTimeException;
import roomescape.fake.FakeReservationDao;
import roomescape.fake.FakeReservationTimeDao;
import roomescape.fake.FakeThemeDao;

public class ReservationServiceTest {

    private ReservationService reservationService;

    @BeforeEach
    void setUp() {
        ReservationDao reservationDao = new FakeReservationDao();
        ReservationTimeDao reservationTimeDao = new FakeReservationTimeDao();
        ThemeDao themeDao = new FakeThemeDao();
        reservationService = new ReservationService(reservationDao, reservationTimeDao, themeDao);

        reservationTimeDao.add(new ReservationTime(1L, LocalTime.of(10, 0)));
        themeDao.add(new Theme(1L, "레벨1", "탈출하기", "http://~"));
    }

    @Test
    @DisplayName("예약 추가를 할 수 있다.")
    void createReservation() {
        ReservationRequest request = new ReservationRequest(LocalDate.of(2024, 4, 26), "사나", 1L, 1L);
        Reservation actual = reservationService.createReservation(request);

        assertAll(() -> {
            assertThat(actual.getId()).isEqualTo(1L);
            assertThat(actual.getName()).isEqualTo("사나");
            assertThat(actual.getDate()).isEqualTo(LocalDate.of(2024, 4, 26));
            assertThat(actual.getTime().getId()).isEqualTo(1L);
            assertThat(actual.getTheme().getId()).isEqualTo(1L);
        });
    }

    @Test
    @DisplayName("createReservationAfterNow 메서드를 사용하면 이전 시간으로의 예약은 추가할 수 없다.")
    void createReservationAfterNow() {
        // Given
        ReservationRequest request = new ReservationRequest(LocalDate.of(2024, 4, 26), "프리", 1L, 1L);

        // When & Then
        assertThatThrownBy(() -> reservationService.createReservationAfterNow(request))
                .isInstanceOf(NotCorrectDateTimeException.class)
                .hasMessage("지나간 날짜와 시간에 대한 예약 생성은 불가능하다.");
    }

    @Test
    @DisplayName("날짜와 시간, 테마가 같은 경우에는 예약 추가를 할 수 없다.")
    void cannotCreateReservation() {
        // Given
        ReservationRequest request = new ReservationRequest(LocalDate.of(2024, 4, 26), "사나", 1L, 1L);
        ReservationRequest request2 = new ReservationRequest(LocalDate.of(2024, 4, 26), "프리", 1L, 1L);
        reservationService.createReservation(request);

        // When & Then
        assertThatThrownBy(() -> reservationService.createReservation(request2))
                .isInstanceOf(DuplicateReservationException.class);
    }

    @Test
    @DisplayName("모든 예약 정보를 가져올 수 있다.")
    void findAllReservations() {
        ReservationRequest request1 = new ReservationRequest(LocalDate.of(2024, 4, 26), "사나", 1L, 1L);
        ReservationRequest request2 = new ReservationRequest(LocalDate.of(2024, 4, 28), "프리", 1L, 1L);

        reservationService.createReservation(request1);
        reservationService.createReservation(request2);

        assertThat(reservationService.findAllReservations()).hasSize(2);
    }

    @Test
    @DisplayName("예약을 id를 통해 제거할 수 있다.")
    void deleteReservationById() {
        ReservationRequest request = new ReservationRequest(LocalDate.of(2024, 4, 26), "사나", 1L, 1L);
        reservationService.createReservation(request);

        reservationService.deleteReservationById(1L);

        assertThat(reservationService.findAllReservations()).hasSize(0);
    }
}
