package roomescape.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import roomescape.dto.request.ReservationTimeRegisterDto;
import roomescape.dto.response.ReservationTimeResponseDto;

@SpringBootTest
@DirtiesContext(classMode = ClassMode.BEFORE_EACH_TEST_METHOD)
class ReservationTimeServiceTest {

    @Autowired
    ReservationTimeService reservationTimeService;

    @Test
    void 시간을_저장한다() {
        // given
        ReservationTimeRegisterDto request = new ReservationTimeRegisterDto(LocalTime.of(15, 0).toString());

        // when
        ReservationTimeResponseDto resaponse = reservationTimeService.saveTime(request);

        // then
        assertThat(resaponse.id()).isNotNull();
        assertThat(resaponse.startAt()).isEqualTo(LocalTime.of(15, 0));
    }

    @Test
    void 모든_시간을_조회한다() {
        // when
        List<ReservationTimeResponseDto> times = reservationTimeService.getAllTimes();

        // then
        assertThat(times).hasSize(3);
        assertThat(times).extracting("startAt")
                .containsExactlyInAnyOrder(
                        LocalTime.of(10, 0),
                        LocalTime.of(14, 0),
                        LocalTime.of(18, 0));
    }

    @Test
    void 시간을_삭제한다() {
        // given
        ReservationTimeResponseDto saved = reservationTimeService.saveTime(
                new ReservationTimeRegisterDto(LocalTime.of(15, 0).toString())
        );

        // when
        reservationTimeService.deleteTime(saved.id());

        // then
        List<LocalTime> times = reservationTimeService.getAllTimes().stream()
                .map(ReservationTimeResponseDto::startAt)
                .toList();

        assertThat(times).doesNotContain(LocalTime.of(15, 0));
    }

}
