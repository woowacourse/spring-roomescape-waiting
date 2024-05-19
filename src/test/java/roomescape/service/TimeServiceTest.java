package roomescape.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import roomescape.controller.time.dto.AvailabilityTimeResponse;
import roomescape.controller.time.dto.CreateTimeRequest;
import roomescape.controller.time.dto.ReadTimeResponse;
import roomescape.service.exception.TimeNotFoundException;
import roomescape.service.exception.TimeUsedException;

import java.time.LocalTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Transactional
class TimeServiceTest {

    @Autowired
    private TimeService timeService;

    @Test
    @DisplayName("예약 시간 목록을 조회한다.")
    void getTimes() {
        // given
        List<ReadTimeResponse> expected = List.of(
                new ReadTimeResponse(1L, "15:00"),
                new ReadTimeResponse(2L, "16:00"),
                new ReadTimeResponse(3L, "17:00"),
                new ReadTimeResponse(4L, "18:00"),
                new ReadTimeResponse(5L, "19:00")
        );

        // when
        List<ReadTimeResponse> actual = timeService.getTimes();

        // then
        assertThat(actual).containsExactlyElementsOf(expected);
    }

    @Test
    @DisplayName("예약 시간을 추가한다.")
    void addTIme() {
        // given
        CreateTimeRequest request = new CreateTimeRequest(LocalTime.parse("13:30"));
        AvailabilityTimeResponse expected = new AvailabilityTimeResponse(6L, "13:30", false);

        // when
        AvailabilityTimeResponse actual = timeService.addTime(request);

        // then
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    @DisplayName("존재하는 예약 시간을 삭제한다.")
    void deleteTimePresent() {
        // given
        long id = 5L;

        // when & then
        assertThatCode(() -> timeService.deleteTime(id))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("존재하지 않는 예약 시간을 삭제할 경우 예외가 발생한다.")
    void deleteTImeNotPresent() {
        // given
        long id = 100L;

        // when & then
        assertThatThrownBy(() -> timeService.deleteTime(id))
                .isInstanceOf(TimeNotFoundException.class);
    }

    @Test
    @DisplayName("예약이 있는 시간을 삭제할 경우 예외가 발생한다.")
    void invalidDelete() {
        assertThatThrownBy(() -> timeService.deleteTime(2L))
                .isInstanceOf(TimeUsedException.class);
    }
}
