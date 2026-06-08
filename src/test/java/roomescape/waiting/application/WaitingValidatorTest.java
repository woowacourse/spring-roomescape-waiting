package roomescape.waiting.application;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import java.time.LocalTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import roomescape.global.exception.customException.BusinessException;
import roomescape.reservationTime.domain.ReservationTime;
import roomescape.theme.domain.Theme;
import roomescape.waiting.application.dto.WaitingCreateCommand;
import roomescape.waiting.domain.Waiting;
import roomescape.waiting.fake.FakeWaitingRepository;

class WaitingValidatorTest {

    private FakeWaitingRepository waitingRepository;
    private WaitingValidator waitingValidator;

    @BeforeEach
    void setUp() {
        waitingRepository = new FakeWaitingRepository();
        waitingValidator = new WaitingValidator(waitingRepository);
    }

    @Test
    @DisplayName("같은 슬롯에 다른 사용자의 대기가 있으면 예외가 발생하지 않는다")
    void validateAlreadyMyWaiting_success_when_other_user_waiting_exists() {
        // given
        ReservationTime time = ReservationTime.createRow(1L, LocalTime.of(10, 0));
        Theme theme = Theme.createRow(1L, "공포", "설명", "https://good.com");
        LocalDate date = LocalDate.now().plusDays(1);
        waitingRepository.save(Waiting.create("리오", date, time, theme));
        WaitingCreateCommand command = new WaitingCreateCommand(
                "브라운",
                date,
                time.getId(),
                theme.getId()
        );

        // when & then
        assertThatCode(() -> waitingValidator.validateAlreadyMyWaiting(command))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("같은 슬롯에 같은 사용자의 대기가 있으면 예외가 발생한다")
    void validateAlreadyMyWaiting_fail_with_same_user_waiting() {
        // given
        ReservationTime time = ReservationTime.createRow(1L, LocalTime.of(10, 0));
        Theme theme = Theme.createRow(1L, "공포", "설명", "https://good.com");
        LocalDate date = LocalDate.now().plusDays(1);
        waitingRepository.save(Waiting.create("브라운", date, time, theme));
        WaitingCreateCommand command = new WaitingCreateCommand(
                "브라운",
                date,
                time.getId(),
                theme.getId()
        );

        // when & then
        assertThatThrownBy(() -> waitingValidator.validateAlreadyMyWaiting(command))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("이미 예약된 시간입니다. 다른 시간을 선택해 주세요.");
    }
}
