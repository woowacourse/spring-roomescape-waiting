package roomescape.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.jdbc.Sql;
import roomescape.domain.reservationtime.ReservationTime;
import roomescape.domain.reservationtime.ReservationTimeRepository;
import roomescape.dto.request.ReservationTimeRequest;
import roomescape.dto.response.AvailableReservationTimeResponse;
import roomescape.dto.response.ReservationTimeResponse;

class ReservationTimeServiceTest extends BaseServiceTest {

    @Autowired
    private ReservationTimeService reservationTimeService;

    @Autowired
    private ReservationTimeRepository reservationTimeRepository;

    @Test
    @DisplayName("모든 예약 시간들을 조회한다.")
    void getAllReservationTimes() {
        ReservationTime reservationTime = new ReservationTime(LocalTime.of(10, 30));
        reservationTimeRepository.save(reservationTime);

        List<ReservationTimeResponse> responses = reservationTimeService.getAllReservationTimes();

        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(responses).hasSize(1);
            softly.assertThat(responses.get(0).startAt()).isEqualTo("10:30");
        });
    }

    @Test
    @DisplayName("예약 시간을 추가한다.")
    void addReservationTime() {
        ReservationTimeRequest request = new ReservationTimeRequest(LocalTime.of(10, 30));
        ReservationTimeResponse response = reservationTimeService.addReservationTime(request);

        assertThat(response.startAt()).isEqualTo("10:30");
    }

    @Test
    @DisplayName("id로 예약 시간을 삭제한다.")
    void deleteReservationTimeById() {
        ReservationTime reservationTime = new ReservationTime(LocalTime.of(10, 30));
        ReservationTime savedReservationTime = reservationTimeRepository.save(reservationTime);

        reservationTimeService.deleteReservationTimeById(savedReservationTime.getId());

        assertThat(reservationTimeRepository.findById(savedReservationTime.getId())).isEmpty();
    }

    @Test
    @DisplayName("날짜와 테마 id로 예약 가능한 시간들을 조회한다.")
    @Sql("/available-reservation-times.sql")
    void getAvailableReservationTimes() {
        LocalDate date = LocalDate.of(2024, 4, 9);
        Long themeId = 1L;

        List<AvailableReservationTimeResponse> response = reservationTimeService
                .getAvailableReservationTimes(date, themeId);

        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(response).hasSize(4);

            softly.assertThat(response.get(0).timeId()).isEqualTo(1L);
            softly.assertThat(response.get(0).startAt()).isEqualTo("09:00");
            softly.assertThat(response.get(0).alreadyBooked()).isFalse();

            softly.assertThat(response.get(1).timeId()).isEqualTo(2L);
            softly.assertThat(response.get(1).startAt()).isEqualTo("12:00");
            softly.assertThat(response.get(1).alreadyBooked()).isTrue();

            softly.assertThat(response.get(2).timeId()).isEqualTo(3L);
            softly.assertThat(response.get(2).startAt()).isEqualTo("17:00");
            softly.assertThat(response.get(2).alreadyBooked()).isFalse();

            softly.assertThat(response.get(3).timeId()).isEqualTo(4L);
            softly.assertThat(response.get(3).startAt()).isEqualTo("21:00");
            softly.assertThat(response.get(3).alreadyBooked()).isTrue();
        });
    }
}
