package roomescape.reservationtime.service;

import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import java.time.LocalTime;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import roomescape.global.exception.ConflictException;
import roomescape.global.exception.NotFoundException;
import roomescape.global.exception.RoomEscapeException;
import roomescape.reservationtime.application.dto.ReservationTimeCreateCommand;
import roomescape.reservationtime.application.dto.ReservationTimeResult;
import roomescape.reservationtime.application.service.ReservationTimeCommandService;
import roomescape.support.ServiceTest;
import roomescape.support.TestDataHelper;

@ServiceTest
@Transactional
public class ReservationTimeCommandServiceTest {

    @Autowired
    private ReservationTimeCommandService timeCommandService;

    @Autowired
    private TestDataHelper testHelper;

    @DisplayName("예약 시간의 정상 추가를 테스트합니다.")
    @Test
    void save_time_successfully() {
        ReservationTimeResult result = timeCommandService.save(
                new ReservationTimeCreateCommand(LocalTime.of(9, 0))
        );

        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(result.id()).isPositive();
            softly.assertThat(result.startAt()).isEqualTo(LocalTime.of(9, 0));
        });
    }

    @DisplayName("중복된 예약 시간 추가 시 예외 발생을 테스트합니다.")
    @Test
    void save_duplicated_time_exception() {
        testHelper.insertReservationTime(LocalTime.of(9, 0));

        assertThatThrownBy(() -> timeCommandService.save(new ReservationTimeCreateCommand(LocalTime.of(9, 0))))
                .isInstanceOf(ConflictException.class)
                .hasMessage("시간 09:00이(가) 이미 존재합니다.");
    }

    @DisplayName("예약 시간의 삭제를 테스트합니다.")
    @Test
    void delete_time() {
        Long resultId = testHelper.insertReservationTime(LocalTime.of(9, 0));

        assertThatNoException().isThrownBy(() -> timeCommandService.delete(resultId));
    }

    @DisplayName("삭제할 예약 시간이 없을 시 예외 발생을 테스트합니다.")
    @Test
    void delete_not_found_time_exception() {
        assertThatThrownBy(() -> timeCommandService.delete(1L))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("존재하지 않는 시간입니다.");
    }

    @DisplayName("예약 시간 삭제 시 해당 시간을 사용한 예약이 존재하면 예외 발생을 테스트합니다.")
    @Test
    void delete_time_with_existing_reservation_exception() {
        Long themeId = testHelper.insertTheme("테마1", "설명1", "img1.jpg");
        Long timeId = testHelper.insertReservationTime(LocalTime.of(10, 0));
        LocalDate date = LocalDate.of(2026, 5, 10);
        testHelper.insertReservation(
                "스타크",
                date,
                themeId,
                timeId
        );

        assertThatThrownBy(() -> timeCommandService.delete(timeId))
                .isInstanceOf(RoomEscapeException.class)
                .hasMessage("해당 시간에 예약이 존재하여 삭제할 수 없습니다.");
    }
}
