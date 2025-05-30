package roomescape.integration.reservation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.jdbc.Sql;
import roomescape.common.exception.RoomescapeException;
import roomescape.member.domain.MemberRepository;
import roomescape.support.util.TestCurrentDateTime;
import roomescape.theme.domain.ThemeRepository;
import roomescape.timeslot.domain.TimeSlotRepository;
import roomescape.waiting.application.dto.WaitingCreateCommand;
import roomescape.waiting.application.dto.WaitingInfo;
import roomescape.waiting.application.service.WaitingService;
import roomescape.waiting.domain.Waiting;
import roomescape.waiting.domain.WaitingRepository;

@SpringBootTest
@DirtiesContext(classMode = ClassMode.BEFORE_EACH_TEST_METHOD)
@Sql(scripts = {"/schema.sql", "/test-data.sql"})
public class WaitingServiceIntegrationTest {

    @Autowired
    private WaitingRepository waitingRepository;

    @Autowired
    private TimeSlotRepository timeSlotRepository;

    @Autowired
    private ThemeRepository themeRepository;

    @Autowired
    private MemberRepository memberRepository;

    private WaitingService waitingService;
    private TestCurrentDateTime currentDateTime;

    @BeforeEach
    void init() {
        final LocalDateTime now = LocalDateTime.of(2025, 5, 1, 10, 0);
        currentDateTime = new TestCurrentDateTime(now);
        waitingService = new WaitingService(waitingRepository, timeSlotRepository, themeRepository, memberRepository, currentDateTime);
    }

    @DisplayName("새로운 예약 대기를 추가할 수 있다")
    @Test
    void createWaiting() {
        // given
        final LocalDate date = currentDateTime.getDate().plusDays(1);
        final WaitingCreateCommand request = new WaitingCreateCommand(date, 1L, 1L, 1L);
        // when
        final WaitingInfo result = waitingService.createWaiting(request);
        // then
        assertAll(
                () -> assertThat(result.id()).isNotNull(),
                () -> assertThat(result.member().name()).isEqualTo("리버"),
                () -> assertThat(result.date()).isEqualTo(date),
                () -> assertThat(result.time().id()).isNotNull(),
                () -> assertThat(result.time().startAt()).isEqualTo(LocalTime.of(10, 0))
        );
    }

    @DisplayName("날짜와 시간과 테마가 같은 예약 대기가 이미 존재하면 예외가 발생한다")
    @Test
    void should_ThrowException_WhenDuplicateWaiting() {
        // given
        final LocalDate date = currentDateTime.getDate().plusDays(1);
        final WaitingCreateCommand request = new WaitingCreateCommand(date, 1L, 1L, 1L);
        // when
        waitingService.createWaiting(request);
        // when & then
        assertThatThrownBy(() -> waitingService.createWaiting(request))
                .isInstanceOf(RoomescapeException.class)
                .hasMessageContaining("해당 시간에 이미 예약 대기가 존재합니다.");
    }

    @DisplayName("날짜와 시간이 같아도 테마가 다르면 중복 예외가 발생하지 않는다")
    @Test
    void shouldNot_ThrowException_WhenThemeIsDifferent() {
        // given
        final LocalDate date = LocalDate.of(2025, 5, 5);
        final WaitingCreateCommand request1 = new WaitingCreateCommand(date, 1L, 1L, 11L);
        waitingService.createWaiting(request1);
        final WaitingCreateCommand request2 = new WaitingCreateCommand(date, 1L, 1L, 10L);
        // when & then
        assertThatCode(() -> waitingService.createWaiting(request2))
                .doesNotThrowAnyException();
    }

    @DisplayName("현재 혹은 과거 시간에 새로운 예약 대기를 추가할 경우 예외가 발생한다")
    @Test
    void should_ThrowException_WhenNotFuture() {
        // given
        final LocalDate date = currentDateTime.getDate().minusDays(1);
        final WaitingCreateCommand request = new WaitingCreateCommand(date, 1L, 1L, 3L);
        // when & then
        assertThatThrownBy(() -> waitingService.createWaiting(request))
                .isInstanceOf(RoomescapeException.class)
                .hasMessageContaining("지나간 날짜와 시간은 예약 대기할 수 없습니다.");
    }

    @DisplayName("id를 기반으로 예약 대기를 취소할 수 있다")
    @Test
    void cancelWaitingById() {
        // when
        final List<Waiting> cancelBeforeWaitings = waitingRepository.findAll();
        waitingService.cancelWaitingById(1L);
        // then
        final List<Waiting> cancelAfterWaitings = waitingRepository.findAll();
        assertThat(cancelAfterWaitings).hasSize(cancelBeforeWaitings.size() - 1);
    }
}
