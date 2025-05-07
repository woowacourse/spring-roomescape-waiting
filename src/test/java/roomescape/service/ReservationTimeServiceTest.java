package roomescape.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationTime;
import roomescape.domain.Theme;
import roomescape.dto.request.ReservationTimeCreateRequest;
import roomescape.dto.response.ReservationTimeWithIsBookedGetResponse;
import roomescape.exception.DuplicateTimeException;
import roomescape.exception.InvalidInputException;
import roomescape.fake.FakeReservationDao;
import roomescape.fake.FakeReservationTimeDao;

public class ReservationTimeServiceTest {

    private ReservationTimeService reservationTimeService;

    @BeforeEach
    void setUp() {
        reservationTimeService = new ReservationTimeService(new FakeReservationTimeDao(), new FakeReservationDao());
    }

    @Test
    @DisplayName("예약시간을 추가를 할 수 있다.")
    void createReservationTime() {
        ReservationTimeCreateRequest request = new ReservationTimeCreateRequest(LocalTime.of(10, 0));

        ReservationTime actual = reservationTimeService.createReservationTime(request);

        assertAll(() -> {
            assertThat(actual.getId()).isEqualTo(1L);
            assertThat(actual.getStartAt()).isEqualTo(LocalTime.of(10, 0));
        });
    }

    @Test
    @DisplayName("이미 존재하는 시간에는 예약시간을 추가를 할 수 없다.")
    void cannotCreateReservationTime() {
        // Given
        ReservationTimeCreateRequest request = new ReservationTimeCreateRequest(LocalTime.of(10, 0));
        ReservationTimeCreateRequest request2 = new ReservationTimeCreateRequest(LocalTime.of(10, 0));
        reservationTimeService.createReservationTime(request);

        // When & Then
        assertThatThrownBy(() -> reservationTimeService.createReservationTime(request2))
                .isInstanceOf(DuplicateTimeException.class);
    }

    @Test
    @DisplayName("모든 예약시간 정보를 가져올 수 있다.")
    void findAllReservationTimes() {
        ReservationTimeCreateRequest request1 = new ReservationTimeCreateRequest(LocalTime.of(10, 0));
        ReservationTimeCreateRequest request2 = new ReservationTimeCreateRequest(LocalTime.of(12, 0));

        reservationTimeService.createReservationTime(request1);
        reservationTimeService.createReservationTime(request2);

        assertThat(reservationTimeService.findAllReservationTimes()).hasSize(2);
    }

    @Test
    @DisplayName("주어진 날짜와 테마id에 해당하는 예약시간 및 예약여부를 조회한다")
    void findReservationTimeByDateAndThemeIdWithIsBooked() {
        // Given
        FakeReservationDao reservationDao = new FakeReservationDao();
        ReservationTime time = new ReservationTime(1L, LocalTime.of(10, 0));
        Theme theme = new Theme(1L, "테마", "테마설명", "테마썸네일");
        reservationDao.add(new Reservation(null, "프리", LocalDate.of(2025, 4, 28), time, theme));
        reservationTimeService = new ReservationTimeService(new FakeReservationTimeDao(), reservationDao);
        reservationTimeService.createReservationTime(new ReservationTimeCreateRequest(LocalTime.of(10, 0)));
        reservationTimeService.createReservationTime(new ReservationTimeCreateRequest(LocalTime.of(12, 0)));
        reservationTimeService.createReservationTime(new ReservationTimeCreateRequest(LocalTime.of(14, 0)));

        // When
        List<ReservationTimeWithIsBookedGetResponse> responses = reservationTimeService.findReservationTimeByDateAndThemeIdWithIsBooked(LocalDate.of(2025, 4, 28), 1L);

        // Then
        assertAll(() -> {
            assertThat(responses.size()).isEqualTo(3);
            assertThat(responses.get(0).isBooked()).isTrue();
            assertThat(responses.get(1).isBooked()).isFalse();
            assertThat(responses.get(2).isBooked()).isFalse();
        });
    }

    @Test
    @DisplayName("예약시간을 id를 통해 제거할 수 있다.")
    void deleteReservationTimeById() {
        ReservationTimeCreateRequest request = new ReservationTimeCreateRequest(LocalTime.of(10, 0));
        reservationTimeService.createReservationTime(request);

        reservationTimeService.deleteReservationTimeById(1L);

        assertThat(reservationTimeService.findAllReservationTimes()).hasSize(0);
    }

    @Test
    @DisplayName("존재하지 않는 예약시간 id로는 제거할 수 없다.")
    void cannotDeleteReservationTimeById() {
        // Given
        // When
        // Then
        assertThatThrownBy(() -> reservationTimeService.deleteReservationTimeById(500L))
                .isInstanceOf(InvalidInputException.class)
                .hasMessage("존재하지 않는 예약시간 id이다.");
    }
}
