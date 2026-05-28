package roomescape.waiting.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import java.time.LocalTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import roomescape.global.exception.WaitingErrorCode;
import roomescape.global.exception.customException.BusinessException;
import roomescape.global.exception.customException.EntityNotFoundException;
import roomescape.reservationTime.domain.ReservationTime;
import roomescape.reservationTime.domain.ReservationTimeRepository;
import roomescape.reservationTime.fake.FakeReservationTimeRepository;
import roomescape.theme.domain.Theme;
import roomescape.theme.domain.ThemeRepository;
import roomescape.theme.fake.FakeThemeRepository;
import roomescape.waiting.application.dto.WaitingCreateCommand;
import roomescape.waiting.application.WaitingReference;
import roomescape.waiting.domain.Waiting;
import roomescape.waiting.domain.WaitingValidator;
import roomescape.waiting.fake.FakeWaitingRepository;

class WaitingServiceTest {

    private FakeWaitingRepository waitingRepository;
    private ReservationTimeRepository reservationTimeRepository;
    private ThemeRepository themeRepository;
    private WaitingReference waitingReference;
    private WaitingService waitingService;

    @BeforeEach
    void setUp() {
        waitingRepository = new FakeWaitingRepository();
        reservationTimeRepository = new FakeReservationTimeRepository();
        themeRepository = new FakeThemeRepository();
        waitingReference = command -> {
        };
        waitingService = new WaitingService(
                waitingRepository,
                reservationTimeRepository,
                themeRepository,
                waitingReference,
                new WaitingValidator(waitingRepository)
        );
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

    @Test
    @DisplayName("같은 슬롯에 이미 대기가 있으면 예약 대기 저장 시 예외가 발생한다")
    void 같은_슬롯에_이미_대기가_있으면_예약_대기_저장_시_예외가_발생한다() {
        // given
        ReservationTime savedTime = reservationTimeRepository.save(ReservationTime.create(LocalTime.now().plusHours(1)));
        Theme savedTheme = themeRepository.save(Theme.create("공포", "무서운 테마", "https://good.com/thumb-nail/1"));
        LocalDate date = LocalDate.now().plusDays(1);
        waitingRepository.save(Waiting.create("리오", date, savedTime, savedTheme));
        WaitingCreateCommand command = new WaitingCreateCommand(
                "브라운",
                date,
                savedTime.getId(),
                savedTheme.getId()
        );

        // when & then
        assertThatThrownBy(() -> waitingService.save(command))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("이미 예약된 시간입니다. 다른 시간을 선택해 주세요.");
    }

    @Test
    @DisplayName("예약이 존재하지 않는 슬롯에 대기를 저장하면 예외가 발생한다")
    void 예약이_존재하지_않는_슬롯에_대기를_저장하면_예외가_발생한다() {
        // given
        ReservationTime savedTime = reservationTimeRepository.save(ReservationTime.create(LocalTime.now().plusHours(1)));
        Theme savedTheme = themeRepository.save(Theme.create("공포", "무서운 테마", "https://good.com/thumb-nail/1"));
        WaitingService waitingService = new WaitingService(
                waitingRepository,
                reservationTimeRepository,
                themeRepository,
                command -> {
                    throw new BusinessException(WaitingErrorCode.WAITING_NOT_EXIST_RESERVATION);
                },
                new WaitingValidator(waitingRepository)
        );
        WaitingCreateCommand command = new WaitingCreateCommand(
                "브라운",
                LocalDate.now().plusDays(1),
                savedTime.getId(),
                savedTheme.getId()
        );

        // when & then
        assertThatThrownBy(() -> waitingService.save(command))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("예약이 존재하지 않으면, 대기요청을 할 수 없습니다.");
        assertThat(waitingRepository.isEmpty()).isTrue();
    }

    @Test
    @DisplayName("본인의 예약 대기를 취소한다")
    void cancelWaiting_success() {
        // given
        ReservationTime savedTime = reservationTimeRepository.save(ReservationTime.create(LocalTime.now().plusHours(1)));
        Theme savedTheme = themeRepository.save(Theme.create("공포", "무서운 테마", "https://good.com/thumb-nail/1"));
        Waiting savedWaiting = waitingRepository.save(
                Waiting.create("브라운", LocalDate.now().plusDays(1), savedTime, savedTheme)
        );

        // when
        waitingService.cancelWaiting(savedWaiting.getId(), "브라운");

        // then
        assertThat(waitingRepository.findById(savedWaiting.getId())).isEmpty();
    }

    @Test
    @DisplayName("존재하지 않는 예약 대기를 취소하면 예외가 발생한다")
    void cancelWaiting_fail_with_not_found_waiting() {
        // when & then
        assertThatThrownBy(() -> waitingService.cancelWaiting(999L, "브라운"))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("예약을 찾을 수 없습니다. id: 999");
    }

    @Test
    @DisplayName("다른 사용자의 예약 대기를 취소하면 예외가 발생하고 삭제되지 않는다")
    void cancelWaiting_fail_with_owner_mismatch() {
        // given
        ReservationTime savedTime = reservationTimeRepository.save(ReservationTime.create(LocalTime.now().plusHours(1)));
        Theme savedTheme = themeRepository.save(Theme.create("공포", "무서운 테마", "https://good.com/thumb-nail/1"));
        Waiting savedWaiting = waitingRepository.save(
                Waiting.create("브라운", LocalDate.now().plusDays(1), savedTime, savedTheme)
        );

        // when & then
        assertThatThrownBy(() -> waitingService.cancelWaiting(savedWaiting.getId(), "다른사람"))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("수정할 수 있는 권한이 없습니다.");
        assertThat(waitingRepository.findById(savedWaiting.getId())).contains(savedWaiting);
    }
}
