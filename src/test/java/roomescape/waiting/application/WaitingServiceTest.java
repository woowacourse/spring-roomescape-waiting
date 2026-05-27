package roomescape.waiting.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import java.time.LocalTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import roomescape.global.exception.customException.BusinessException;
import roomescape.reservationTime.domain.ReservationTime;
import roomescape.reservationTime.domain.ReservationTimeRepository;
import roomescape.reservationTime.fake.FakeReservationTimeRepository;
import roomescape.theme.domain.Theme;
import roomescape.theme.domain.ThemeRepository;
import roomescape.theme.fake.FakeThemeRepository;
import roomescape.waiting.application.dto.WaitingCreateCommand;
import roomescape.waiting.domain.Waiting;
import roomescape.waiting.fake.FakeWaitingRepository;

class WaitingServiceTest {

    private FakeWaitingRepository waitingRepository;
    private ReservationTimeRepository reservationTimeRepository;
    private ThemeRepository themeRepository;
    private WaitingService waitingService;

    @BeforeEach
    void setUp() {
        waitingRepository = new FakeWaitingRepository();
        reservationTimeRepository = new FakeReservationTimeRepository();
        themeRepository = new FakeThemeRepository();
        waitingService = new WaitingService(waitingRepository, reservationTimeRepository, themeRepository);
    }

    @Test
    @DisplayName("예약 대기를 저장한다")
    void save_success() {
        // given
        ReservationTime savedTime = reservationTimeRepository.save(ReservationTime.create(LocalTime.now().plusHours(1)));
        Theme savedTheme = themeRepository.save(Theme.create("공포", "무서운 테마", "https://good.com/thumb-nail/1"));
        WaitingCreateCommand command = new WaitingCreateCommand(
                "브라운",
                LocalDate.now().plusDays(1),
                savedTime.getId(),
                savedTheme.getId()
        );

        // when
        Waiting waiting = waitingService.save(command);

        // then
        assertThat(waiting.getId()).isNotNull();
        assertThat(waiting.getName()).isEqualTo(command.name());
        assertThat(waiting.getDate()).isEqualTo(command.date());
        assertThat(waiting.getTime()).isEqualTo(savedTime);
        assertThat(waiting.getTheme()).isEqualTo(savedTheme);
        assertThat(waitingRepository.findById(waiting.getId())).contains(waiting);
    }

    @Test
    @DisplayName("존재하지 않는 예약 시간으로 대기를 저장하면 예외가 발생한다")
    void save_fail_with_not_found_time() {
        // given
        Theme savedTheme = themeRepository.save(Theme.create("공포", "무서운 테마", "https://good.com/thumb-nail/1"));
        WaitingCreateCommand command = new WaitingCreateCommand(
                "브라운",
                LocalDate.now().plusDays(1),
                999L,
                savedTheme.getId()
        );

        // when & then
        assertThatThrownBy(() -> waitingService.save(command))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("존재하지 않는 예약 시간입니다.");
        assertThat(waitingRepository.isEmpty()).isTrue();
    }

    @Test
    @DisplayName("존재하지 않는 테마로 대기를 저장하면 예외가 발생한다")
    void save_fail_with_not_found_theme() {
        // given
        ReservationTime savedTime = reservationTimeRepository.save(ReservationTime.create(LocalTime.now().plusHours(1)));
        WaitingCreateCommand command = new WaitingCreateCommand(
                "브라운",
                LocalDate.now().plusDays(1),
                savedTime.getId(),
                999L
        );

        // when & then
        assertThatThrownBy(() -> waitingService.save(command))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("존재하지 않는 예약 테마입니다.");
        assertThat(waitingRepository.isEmpty()).isTrue();
    }
}
