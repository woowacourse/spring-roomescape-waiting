package roomescape.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import roomescape.domain.user.User;
import roomescape.domain.user.UserRepository;
import roomescape.domain.user.UserRole;
import roomescape.domain.waiting.Waiting;
import roomescape.domain.waiting.WaitingRepository;
import roomescape.exception.AlreadyExistedException;
import roomescape.exception.BusinessRuleViolationException;
import roomescape.exception.NotFoundException;

@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class WaitingServiceTest {

    @Autowired
    private WaitingService service;

    @Autowired
    private WaitingRepository waitingRepository;

    @Autowired
    private UserRepository userRepository;

    @Test
    @DisplayName("대기를 등록할 수 있다.")
    void saveWaiting() {
        // given
        var user = userRepository.findById(2L).orElseThrow();
        var date = LocalDate.of(3000, 5, 8);
        var timeSlotId = 1L;
        var themeId = 1L;

        // when
        Waiting created = service.saveWaiting(user, date, timeSlotId, themeId);

        // then
        var waitings = waitingRepository.findAll();
        assertThat(waitings).contains(created);
    }

    @Test
    @DisplayName("이미 대기한 내역이 있는 경우 예외가 발생한다.")
    void saveWaiting_WhenAlreadyExists() {
        // given
        var user = User.ofExisting(3L, "사용자3", UserRole.USER, "user3@email.com", "password3");
        var date = LocalDate.of(2025, 5, 5);
        var timeSlotId = 1L;
        var themeId = 1L;

        // when & then
        assertThatThrownBy(() -> service.saveWaiting(user, date, timeSlotId, themeId))
                .isInstanceOf(AlreadyExistedException.class)
                .hasMessage("이미 예약 대기한 내역이 있습니다.");
    }

    @Test
    @DisplayName("이미 예약한 내역이 있는 경우 예외가 발생한다.")
    void saveWaiting_WhenAlreadyReserved() {
        // given
        var user = userRepository.findById(2L).orElseThrow();
        var date = LocalDate.of(2025, 5, 5);
        var timeSlotId = 1L;
        var themeId = 1L;

        // when & then
        assertThatThrownBy(() -> service.saveWaiting(user, date, timeSlotId, themeId))
                .isInstanceOf(BusinessRuleViolationException.class)
                .hasMessage("해당 테마의 시간대에 이미 예약되어 있습니다.");
    }

    @Test
    @DisplayName("존재하지 않는 시간대로 대기 등록 시 예외가 발생한다.")
    void saveWaiting_WhenTimeSlotNotFound() {
        // given
        var user = userRepository.findById(2L).orElseThrow();
        var date = LocalDate.of(2025, 5, 8);
        var invalidTimeSlotId = 999L;
        var themeId = 1L;

        // when & then
        assertThatThrownBy(() -> service.saveWaiting(user, date, invalidTimeSlotId, themeId))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("존재하지 않는 타임 슬롯입니다.");
    }

    @Test
    @DisplayName("존재하지 않는 테마로 대기 등록 시 예외가 발생한다.")
    void saveWaiting_WhenThemeNotFound() {
        // given
        var user = userRepository.findById(2L).orElseThrow();
        var date = LocalDate.of(2025, 5, 8);
        var timeSlotId = 1L;
        var invalidThemeId = 999L;

        // when & then
        assertThatThrownBy(() -> service.saveWaiting(user, date, timeSlotId, invalidThemeId))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("존재하지 않는 테마입니다.");
    }

    @Test
    @DisplayName("모든 대기를 조회할 수 있다.")
    void findAllWaitings() {
        // when
        var waitings = service.findAllWaitings();

        // then
        assertThat(waitings).hasSize(2);
    }

    @Test
    @DisplayName("대기를 삭제할 수 있다.")
    void removeById() {
        // given
        var waitingId = 1L;

        // when
        service.removeById(waitingId);

        // then
        var waitings = service.findAllWaitings();
        assertThat(waitings).hasSize(1);
    }

    @Test
    @DisplayName("존재하지 않는 대기 삭제 시 예외가 발생한다.")
    void removeById_WhenWaitingNotFound() {
        // given
        var invalidWaitingId = 999L;

        // when & then
        assertThatThrownBy(() -> service.removeById(invalidWaitingId))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("존재하지 않는 예약 대기입니다.");
    }
}
