package roomescape.service;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static roomescape.config.FixedClockConfig.FUTURE_DATE;
import static roomescape.config.FixedClockConfig.TODAY;

import java.time.LocalDate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import roomescape.common.exception.ConflictException;
import roomescape.common.exception.ForbiddenException;
import roomescape.common.exception.NotFoundException;
import roomescape.common.exception.UnprocessableEntityException;
import roomescape.service.dto.command.ReservationCommand;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
public class ReservationServiceTest {
    private final String name = "브라운";
    private final LocalDate futureDate = LocalDate.parse(FUTURE_DATE);
    private final Long timeId = 1L;
    private final Long themeId = 1L;

    @Autowired
    private ReservationService reservationService;

    @Test
    @DisplayName("정상 예약을 생성하면 통과한다.")
    void 정상_예약_생성_테스트() {
        ReservationCommand command = new ReservationCommand(
                name,
                futureDate,
                timeId,
                themeId
        );

        assertDoesNotThrow(() -> reservationService.registerReservation(command));
    }

    @Test
    @DisplayName("존재하지 않는 시간 식별자로 예약하면 예외가 발생한다")
    void 없는_시간_식별자_예외_테스트() {
        Long invalidTimeId = 999L;
        ReservationCommand command = new ReservationCommand(
                name,
                futureDate,
                invalidTimeId,
                themeId
        );

        assertThatThrownBy(() -> reservationService.registerReservation(command))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("존재하지 않는 시간입니다.");
    }

    @Test
    @DisplayName("존재하지 않는 테마 식별자로 예약하면 예외가 발생한다")
    void 없는_테마_식별자_예외_테스트() {
        Long invalidThemeId = 999L;
        ReservationCommand command = new ReservationCommand(
                name,
                futureDate,
                timeId,
                invalidThemeId
        );

        assertThatThrownBy(() -> reservationService.registerReservation(command))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("존재하지 않는 테마입니다.");
    }

    @Test
    @DisplayName("과거의 시점으로 예약을 하면 예외가 발생한다.")
    void 과거_예약_생성_예외_테스트() {
        LocalDate date = LocalDate.parse(TODAY);
        Long timeId = 1L;
        ReservationCommand command = new ReservationCommand(
                name,
                date,
                timeId,
                themeId
        );

        assertThatThrownBy(() -> reservationService.registerReservation(command))
                .isInstanceOf(UnprocessableEntityException.class)
                .hasMessageContaining("이미 지난 시간입니다.");
    }

    @Test
    @DisplayName("이미 존재하는 예약 건과 중복으로 예약하면 예외가 발생한다.")
    void 중복_예약_예외_테스트() {
        Long timeId = 5L;
        ReservationCommand command = new ReservationCommand(
                name,
                futureDate,
                timeId,
                themeId
        );

        assertThatThrownBy(() -> reservationService.registerReservation(command))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("이미 존재하는 예약 건입니다.");
    }

    @Test
    @DisplayName("다른 사람의 예약을 변경하려하면 예외가 발생한다.")
    void 타인_예약_변경_예외_테스트() {
        Long id = 24L;
        String otherName = "브리";
        ReservationCommand command = new ReservationCommand(
                otherName,
                futureDate,
                timeId,
                themeId
        );

        assertThatThrownBy(() -> reservationService.changeDateTime(id, command))
                .isInstanceOf(ForbiddenException.class)
                .hasMessageContaining("다른 사람의 예약은 변경할 수 없습니다.");
    }
}
