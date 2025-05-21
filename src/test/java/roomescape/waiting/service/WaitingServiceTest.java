package roomescape.waiting.service;

import java.time.LocalDate;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import roomescape.reservationtime.ReservationTimeTestDataConfig;
import roomescape.reservationtime.domain.ReservationTime;
import roomescape.theme.ThemeTestDataConfig;
import roomescape.theme.domain.Theme;
import roomescape.user.MemberTestDataConfig;
import roomescape.user.domain.User;
import roomescape.waiting.domain.dto.WaitingRequestDto;
import roomescape.waiting.domain.dto.WaitingResponseDto;
import roomescape.waiting.fixture.WaitingFixture;

@DataJpaTest
@Import({
        WaitingService.class,
        MemberTestDataConfig.class,
        ReservationTimeTestDataConfig.class,
        ThemeTestDataConfig.class
})
class WaitingServiceTest {

    @Autowired
    private WaitingService waitingService;

    private static User savedMember;
    private static ReservationTime savedTime;
    private static Theme savedTheme;

    @BeforeAll
    public static void setUp(
                             @Autowired MemberTestDataConfig memberTestDataConfig,
                             @Autowired ReservationTimeTestDataConfig reservationTimeTestDataConfig,
                             @Autowired ThemeTestDataConfig themeTestDataConfig
    ) {
        savedMember = memberTestDataConfig.getSavedUser();
        savedTime = reservationTimeTestDataConfig.getSavedReservationTime();
        savedTheme = themeTestDataConfig.getSavedTheme();
    }

    @Nested
    @DisplayName("예약 대기 추가 기능")
    class create {

        @DisplayName("예약 대기 추가 기능이 잘 작동하는 지")
        @Test
        void create_success() {
            // given
            WaitingRequestDto requestDto = WaitingFixture.createReqDto(
                    LocalDate.now().plusDays(2),
                    savedTime.getId(),
                    savedTheme.getId());

            // when
            WaitingResponseDto responseDto = waitingService.create(requestDto, savedMember);

            // then
            SoftAssertions.assertSoftly(softly -> {
                softly.assertThat(responseDto.date()).isEqualTo(requestDto.date());
                softly.assertThat(responseDto.timeDto().id()).isEqualTo(requestDto.timeId());
                softly.assertThat(responseDto.themeDto().id()).isEqualTo(requestDto.themeId());
            });
        }
    }
}
