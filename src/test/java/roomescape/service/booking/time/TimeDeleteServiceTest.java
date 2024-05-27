package roomescape.service.booking.time;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.test.context.jdbc.Sql;
import roomescape.domain.time.ReservationTime;
import roomescape.repository.ReservationTimeRepository;
import roomescape.service.booking.time.module.TimeDeleteService;

@Sql("/all-test-data.sql")
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class TimeDeleteServiceTest {

    @Autowired
    TimeDeleteService timeDeleteService;

    @Autowired
    ReservationTimeRepository timeRepository;

    @Test
    void 테마_삭제() {
        //when
        timeDeleteService.deleteTime(3L);

        //when
        List<ReservationTime> allTimes = timeRepository.findAll();
        assertThat(allTimes).extracting(ReservationTime::getId)
                .isNotEmpty()
                .doesNotContain(3L);
    }

    @Test
    void 예약이_존재하는_시간대를_삭제할_경우_예외_발생() {
        //then
        assertThatThrownBy(() -> timeDeleteService.deleteTime(1L))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
