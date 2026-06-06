package roomescape.time.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import roomescape.support.ConcurrentExecutor;
import roomescape.support.ConcurrentResult;
import roomescape.support.ServiceIntegrationTest;
import roomescape.time.exception.DuplicateTimeException;
import roomescape.time.exception.TimeNotFoundException;
import roomescape.time.service.dto.ReservationTimeCommand;

public class ReservationTimeServiceIntegrationTest extends ServiceIntegrationTest {

    @Autowired
    ReservationTimeService reservationTimeService;

    @DisplayName("동일한 예약 시간을 동시에 생성하면 하나만 성공하고 나머지는 중복 예외가 발생한다")
    @Test
    void registerReservationTimeTest_duplicate() throws InterruptedException {
        //when
        List<ConcurrentResult> results = ConcurrentExecutor.executeConcurrently(100, () -> {
            try {
                reservationTimeService.registerReservationTime(
                        new ReservationTimeCommand(LocalTime.of(10, 0))
                );

                return ConcurrentResult.withSuccess();
            } catch (Throwable e) {
                return ConcurrentResult.withFail(e);
            }
        });

        //then
        assertThat(results).filteredOn(ConcurrentResult::success).hasSize(1);

        assertThat(results).filteredOn(result -> !result.success()).hasSize(99);
        assertThat(results)
                .filteredOn(result -> !result.success())
                .extracting(ConcurrentResult::exception)
                .allMatch(DuplicateTimeException.class::isInstance);
    }

    @DisplayName("예약 시간 삭제 요청이 동시에 들어오면 하나만 성공하고 나머지는 예외가 발생한다")
    @Test
    void removeReservationTimeByIdTest_duplicate() throws InterruptedException {
        //given
        reservationTimeService.registerReservationTime(
                new ReservationTimeCommand(LocalTime.of(10, 0))
        );

        //when
        List<ConcurrentResult> results = ConcurrentExecutor.executeConcurrently(100, () -> {
            try {
                reservationTimeService.removeReservationTimeById(1L);

                return ConcurrentResult.withSuccess();
            } catch (Throwable e) {
                return ConcurrentResult.withFail(e);
            }
        });

        //then
        assertThat(results).filteredOn(ConcurrentResult::success).hasSize(1);

        assertThat(results).filteredOn(result -> !result.success()).hasSize(99);
        assertThat(results)
                .filteredOn(result -> !result.success())
                .extracting(ConcurrentResult::exception)
                .allMatch(TimeNotFoundException.class::isInstance);
    }
}
