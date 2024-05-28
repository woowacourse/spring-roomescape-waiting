package roomescape.service.booking.time;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

import java.time.LocalTime;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.test.context.jdbc.Sql;
import roomescape.domain.time.ReservationTime;
import roomescape.dto.reservationtime.ReservationTimeRequest;
import roomescape.repository.ReservationTimeRepository;
import roomescape.service.booking.time.module.TimeRegisterService;

@Sql("/all-test-data.sql")
@SpringBootTest(webEnvironment = WebEnvironment.NONE)
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class TimeRegisterServiceTest {

    @Autowired
    TimeRegisterService timeRegisterService;

    @Autowired
    ReservationTimeRepository timeRepository;

    @Test
    void 시간_등록() {
        //given
        ReservationTimeRequest reservationTimeRequest = new ReservationTimeRequest(LocalTime.parse("00:00"));

        //when
        Long timeId = timeRegisterService.registerTime(reservationTimeRequest);

        //then
        ReservationTime time = timeRepository.findById(timeId).orElseThrow();
        assertAll(
                () -> assertThat(time.getId()).isEqualTo(timeId),
                () -> assertThat(time.getStartAt()).isEqualTo(reservationTimeRequest.startAt())
        );
    }

    @Test
    void 동일한_시간을_추가할_경우_예외_발생() {
        //given
        ReservationTimeRequest reservationTimeRequest = new ReservationTimeRequest(LocalTime.parse("10:00"));

        //when, then
        assertThatThrownBy(() -> timeRegisterService.registerTime(reservationTimeRequest))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
