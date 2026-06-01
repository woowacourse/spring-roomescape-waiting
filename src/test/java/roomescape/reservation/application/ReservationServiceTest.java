package roomescape.reservation.application;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalTime;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.annotation.Transactional;
import roomescape.common.exception.ConflictException;
import roomescape.config.TestTimeConfig;
import roomescape.reservation.application.dto.ReservationChangeCommand;
import roomescape.reservation.application.dto.ReservationCreateCommand;
import roomescape.reservation.application.dto.ReservationInfo;
import roomescape.reservation.domain.Status;
import roomescape.theme.domain.Theme;
import roomescape.theme.domain.ThemeRepository;
import roomescape.time.domain.ReservationTime;
import roomescape.time.domain.ReservationTimeRepository;

@SpringBootTest
@Transactional
@Import(TestTimeConfig.class)
class ReservationServiceTest {

    @Autowired
    private ReservationService reservationService;

    @Autowired
    private ReservationTimeRepository timeRepository;

    @Autowired
    private ThemeRepository themeRepository;

    @Autowired
    private Clock clock;

    private ReservationTime savedTime;
    private Theme savedTheme;
    private LocalDate targetDate;

    @BeforeEach
    void setUp() {
        savedTime = timeRepository.save(
                ReservationTime.create(LocalTime.of(14, 0))
        );

        savedTheme = themeRepository.save(
                Theme.create("판타지 테마", "https://example.com/image.png", "진짜 재밌는 방탈출")
        );
        targetDate = LocalDate.now(clock).plusDays(3);
    }

    @Test
    @DisplayName("해당 타겟 시간에 예약이 전혀 없으면, RESERVED(확정) 상태로 DB에 정상 저장된다.")
    void create_success_reserved() {
        ReservationCreateCommand command = new ReservationCreateCommand(
                "포비", targetDate, savedTime.getId(), savedTheme.getId()
        );

        ReservationInfo result = reservationService.create(command);

        assertThat(result.id()).isNotNull();
        assertThat(result.status()).isEqualTo(Status.RESERVED);
        assertThat(result.name()).isEqualTo("포비");
    }

    @Test
    @DisplayName("해당 타겟 시간에 이미 예약이 존재하면, 동일 인물이 아닐 때 WAITING(대기) 상태로 DB에 정상 저장된다.")
    void create_success_pending() {
        ReservationCreateCommand firstCommand = new ReservationCreateCommand(
                "포비", targetDate, savedTime.getId(), savedTheme.getId()
        );
        reservationService.create(firstCommand);

        ReservationCreateCommand secondCommand = new ReservationCreateCommand(
                "리사", targetDate, savedTime.getId(), savedTheme.getId()
        );

        ReservationInfo result = reservationService.create(secondCommand);

        assertThat(result.status()).isEqualTo(Status.WAITING);
        assertThat(result.name()).isEqualTo("리사");
    }

    @Test
    @DisplayName("동일한 사람이 이미 대기를 걸어둔 상태에서 또 예약을 시도하면 중복 대기 예외가 발생한다.")
    void create_fail_duplicated_pending() {
        ReservationCreateCommand activeCommand = new ReservationCreateCommand(
                "포비", targetDate, savedTime.getId(), savedTheme.getId()
        );
        reservationService.create(activeCommand);

        ReservationCreateCommand pendingCommand = new ReservationCreateCommand(
                "리사", targetDate, savedTime.getId(), savedTheme.getId()
        );
        reservationService.create(pendingCommand);

        assertThatThrownBy(() -> reservationService.create(pendingCommand))
                .isInstanceOf(ConflictException.class);
    }

    @Test
    @DisplayName("Active인 예약을 취소하면 Pending 상태인 예약중 첫번째 예약이 Active로 바뀐다.")
    void modifyPendingReservationToReserved() {
        ReservationCreateCommand activeCommand = new ReservationCreateCommand(
                "포비", targetDate, savedTime.getId(), savedTheme.getId()
        );
        ReservationInfo reservationInfo = reservationService.create(activeCommand);
        ReservationCreateCommand pendingFirstCommand = new ReservationCreateCommand(
                "리사", targetDate, savedTime.getId(), savedTheme.getId()
        );
        reservationService.create(pendingFirstCommand);
        ReservationCreateCommand pendingSecondCommand = new ReservationCreateCommand(
                "브리", targetDate, savedTime.getId(), savedTheme.getId()
        );
        reservationService.create(pendingSecondCommand);
        reservationService.cancel(reservationInfo.id(), reservationInfo.name());

        Assertions.assertThat(reservationService.getReservationsByName(pendingFirstCommand.name()).getFirst().status())
                .isEqualTo(Status.RESERVED);
    }

    @Test
    @DisplayName("대기 예약을 다른 시간으로 확정 예약 변경할 수 있다.")
    void changePendingReservationToReserved() {
        reservationService.create(new ReservationCreateCommand(
                "포비", targetDate, savedTime.getId(), savedTheme.getId()
        ));
        ReservationInfo pendingReservation = reservationService.create(new ReservationCreateCommand(
                "리사", targetDate, savedTime.getId(), savedTheme.getId()
        ));
        ReservationTime anotherTime = timeRepository.save(
                ReservationTime.create(LocalTime.of(15, 0))
        );

        ReservationInfo changedReservation = reservationService.modify(
                pendingReservation.id(),
                new ReservationChangeCommand("리사", anotherTime.getId(), savedTheme.getId(), targetDate)
        );

        assertThat(changedReservation.status()).isEqualTo(Status.RESERVED);
        assertThat(changedReservation.time().id()).isEqualTo(anotherTime.getId());
    }

    @Test
    @DisplayName("대기 예약도 예약자 본인이 취소할 수 있다.")
    void cancelPendingReservation() {
        reservationService.create(new ReservationCreateCommand(
                "포비", targetDate, savedTime.getId(), savedTheme.getId()
        ));
        ReservationInfo pendingReservation = reservationService.create(new ReservationCreateCommand(
                "리사", targetDate, savedTime.getId(), savedTheme.getId()
        ));

        reservationService.cancel(pendingReservation.id(), "리사");

        Assertions.assertThat(reservationService.getReservationsByName("리사").size())
                .isZero();
    }
}
