package roomescape.application.mapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import roomescape.application.dto.TimeCreateDto;
import roomescape.application.dto.TimeDto;
import roomescape.domain.ReservationTime;

class ReservationTimeMapperTest {

    @DisplayName("request를 ReservationTime으로 변경한다.")
    @Test
    void request_toReservation() {
        // given
        LocalTime time = LocalTime.of(10, 0);
        TimeCreateDto request = new TimeCreateDto(time);

        // when
        ReservationTime reservationTime = ReservationTime.withoutId(request.startAt());

        // then
        assertAll(
                () -> assertThat(reservationTime.getId()).isNull(),
                () -> assertThat(reservationTime.getStartAt()).isEqualTo(time)
        );
    }

    @DisplayName("ReservationTime을 Response로 변환한다.")
    @Test
    void reservationTime_toResponse() {
        // given
        ReservationTime reservationTime = ReservationTime.of(1L, LocalTime.of(10, 0));

        // when
        TimeDto dto = TimeDto.from(reservationTime);

        // then
        assertAll(
                () -> assertThat(dto.id()).isEqualTo(1L),
                () -> assertThat(dto.startAt()).isEqualTo("10:00")
        );
    }

    @DisplayName("여러 개의 ReservationTimes를 한꺼번에 Response리스트로 변환한다.")
    @Test
    void multiple_reservationTimes_toResponses() {
        // given
        LocalTime time1 = LocalTime.of(10, 0);
        LocalTime time2 = LocalTime.of(11, 0);
        LocalTime time3 = LocalTime.of(12, 0);

        ReservationTime reservationTime1 = ReservationTime.of(1L, time1);
        ReservationTime reservationTime2 = ReservationTime.of(2L, time2);
        ReservationTime reservationTime3 = ReservationTime.of(3L, time3);

        List<ReservationTime> reservationTimes = List.of(reservationTime1, reservationTime2, reservationTime3);

        // when
        List<TimeDto> dtos = TimeDto.from(reservationTimes);

        // then
        assertAll(
                () -> assertThat(dtos).hasSize(3),
                () -> assertThat(dtos)
                        .extracting(TimeDto::startAt)
                        .containsExactly(time1, time2, time3)
        );
    }

}
