package roomescape.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import roomescape.domain.theme.Theme;
import roomescape.domain.timeslot.TimeSlot;
import roomescape.domain.user.User;
import roomescape.exception.NotFoundException;

@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class EntityLookupServiceTest {

    @Autowired
    private EntityLookupService entityLookupService;

    @Test
    @DisplayName("존재하는 사용자 ID로 사용자를 조회할 수 있다")
    void getUserById_WhenExists() {
        // given
        long existingUserId = 1L;

        // when
        User user = entityLookupService.getUserById(existingUserId);

        // then
        assertThat(user.id()).isEqualTo(existingUserId);
    }

    @Test
    @DisplayName("존재하지 않는 사용자 ID로 조회시 예외가 발생한다")
    void getUserById_WhenNotExists() {
        // given
        long nonExistentUserId = 999L;

        // when & then
        assertThatThrownBy(() -> entityLookupService.getUserById(nonExistentUserId))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("존재하지 않는 사용자입니다.");
    }

    @Test
    @DisplayName("존재하는 타임슬롯 ID로 타임슬롯을 조회할 수 있다")
    void getTimeSlotById_WhenExists() {
        // given
        long existingTimeSlotId = 1L;

        // when
        TimeSlot timeSlot = entityLookupService.getTimeSlotById(existingTimeSlotId);

        // then
        assertThat(timeSlot.id()).isEqualTo(existingTimeSlotId);
    }

    @Test
    @DisplayName("존재하지 않는 타임슬롯 ID로 조회시 예외가 발생한다")
    void getTimeSlotById_WhenNotExists() {
        // given
        long nonExistentTimeSlotId = 999L;

        // when & then
        assertThatThrownBy(() -> entityLookupService.getTimeSlotById(nonExistentTimeSlotId))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("존재하지 않는 타임 슬롯입니다.");
    }

    @Test
    @DisplayName("존재하는 테마 ID로 테마를 조회할 수 있다")
    void getThemeById_WhenExists() {
        // given
        long existingThemeId = 1L;

        // when
        Theme theme = entityLookupService.getThemeById(existingThemeId);

        // then
        assertThat(theme.id()).isEqualTo(existingThemeId);
    }

    @Test
    @DisplayName("존재하지 않는 테마 ID로 조회시 예외가 발생한다")
    void getThemeById_WhenNotExists() {
        // given
        long nonExistentThemeId = 999L;

        // when & then
        assertThatThrownBy(() -> entityLookupService.getThemeById(nonExistentThemeId))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("존재하지 않는 테마입니다.");
    }
}
