package roomescape.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import roomescape.IntegrationTestSupport;
import roomescape.controller.time.dto.AvailabilityTimeRequest;
import roomescape.controller.time.dto.AvailabilityTimeResponse;
import roomescape.controller.time.dto.CreateTimeRequest;
import roomescape.controller.time.dto.ReadTimeResponse;
import roomescape.service.exception.TimeNotFoundException;
import roomescape.service.exception.TimeUsedException;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Transactional
class TimeServiceTest extends IntegrationTestSupport {

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

        // when
        AvailabilityTimeResponse actual = timeService.addTime(request);
        AvailabilityTimeResponse expected = new AvailabilityTimeResponse(actual.id(), "13:30", false);

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

    @ParameterizedTest
    @MethodSource("getAvailableTimesParameterProvider")
    @DisplayName("가능 시간 조회")
    void getAvailableTimes(final AvailabilityTimeRequest request, final int expected) {
        assertThat(timeService.getAvailableTimes(request)).hasSize(expected);
    }

    static Stream<Arguments> getAvailableTimesParameterProvider() {
        final LocalDate now = LocalDate.now();
        return Stream.of(
                Arguments.of(new AvailabilityTimeRequest(now.minusDays(3), 1L), 0),
                Arguments.of(new AvailabilityTimeRequest(now.plusDays(1), 1L), 5)
        );
    }
}
