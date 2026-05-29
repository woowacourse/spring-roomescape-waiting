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
import roomescape.config.TestTimeConfig;
import roomescape.reservation.application.dto.ReservationChangeCommand;
import roomescape.reservation.application.dto.ReservationCreateCommand;
import roomescape.reservation.application.dto.ReservationInfo;
import roomescape.reservation.domain.Status;
import roomescape.reservation.domain.exception.DuplicatedReservationException;
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
                ReservationTime.builder()
                        .startAt(LocalTime.of(14, 0))
                        .build()
        );

        savedTheme = themeRepository.save(
                Theme.builder()
                        .name("판타지 테마")
                        .description("진짜 재밌는 방탈출")
                        .durationTime(LocalTime.of(1, 0))
                        .thumbnailImageUrl("https://example.com/image.png")
                        .build()
        );
        targetDate = LocalDate.now(clock).plusDays(3);
    }

    @Test
    @DisplayName("해당 타겟 시간에 예약이 전혀 없으면, ACTIVE(확정) 상태로 DB에 정상 저장된다.")
    void addReservation_success_active() {
        ReservationCreateCommand command = new ReservationCreateCommand(
                "포비", targetDate, savedTime.getId(), savedTheme.getId()
        );

        ReservationInfo result = reservationService.addReservation(command);

        assertThat(result.id()).isNotNull();
        assertThat(result.status()).isEqualTo(Status.ACTIVE);
        assertThat(result.name()).isEqualTo("포비");
    }

    @Test
    @DisplayName("해당 타겟 시간에 이미 예약이 존재하면, 동일 인물이 아닐 때 PENDING(대기) 상태로 DB에 정상 저장된다.")
    void addReservation_success_pending() {
        ReservationCreateCommand firstCommand = new ReservationCreateCommand(
                "포비", targetDate, savedTime.getId(), savedTheme.getId()
        );
        reservationService.addReservation(firstCommand);

        ReservationCreateCommand secondCommand = new ReservationCreateCommand(
                "리사", targetDate, savedTime.getId(), savedTheme.getId()
        );

        ReservationInfo result = reservationService.addReservation(secondCommand);

        assertThat(result.status()).isEqualTo(Status.PENDING);
        assertThat(result.name()).isEqualTo("리사");
    }

    @Test
    @DisplayName("동일한 사람이 이미 대기를 걸어둔 상태에서 또 예약을 시도하면 중복 대기 예외가 발생한다.")
    void addReservation_fail_duplicated_pending() {
        ReservationCreateCommand activeCommand = new ReservationCreateCommand(
                "포비", targetDate, savedTime.getId(), savedTheme.getId()
        );
        reservationService.addReservation(activeCommand);

        ReservationCreateCommand pendingCommand = new ReservationCreateCommand(
                "리사", targetDate, savedTime.getId(), savedTheme.getId()
        );
        reservationService.addReservation(pendingCommand);

        assertThatThrownBy(() -> reservationService.addReservation(pendingCommand))
                .isInstanceOf(DuplicatedReservationException.class)
                .hasMessageContaining("이미 예약 대기 중입니다.");
    }

    @Test
    @DisplayName("Active인 예약을 취소하면 Pending 상태인 예약중 첫번째 예약이 Active로 바뀐다.")
    void updatePendingReservationToActive() {
        ReservationCreateCommand activeCommand = new ReservationCreateCommand(
                "포비", targetDate, savedTime.getId(), savedTheme.getId()
        );
        ReservationInfo reservationInfo = reservationService.addReservation(activeCommand);
        ReservationCreateCommand pendingFirstCommand = new ReservationCreateCommand(
                "리사", targetDate, savedTime.getId(), savedTheme.getId()
        );
        reservationService.addReservation(pendingFirstCommand);
        ReservationCreateCommand pendingSecondCommand = new ReservationCreateCommand(
                "브리", targetDate, savedTime.getId(), savedTheme.getId()
        );
        reservationService.addReservation(pendingSecondCommand);
        reservationService.cancelReservation(reservationInfo.id(), reservationInfo.name());

        Assertions.assertThat(reservationService.getReservationsByName(pendingFirstCommand.name()).getFirst().status())
                .isEqualTo(Status.ACTIVE);
    }

    @Test
    @DisplayName("대기 예약을 다른 시간으로 확정 예약 변경할 수 있다.")
    void changePendingReservationToActive() {
        reservationService.addReservation(new ReservationCreateCommand(
                "포비", targetDate, savedTime.getId(), savedTheme.getId()
        ));
        ReservationInfo pendingReservation = reservationService.addReservation(new ReservationCreateCommand(
                "리사", targetDate, savedTime.getId(), savedTheme.getId()
        ));
        ReservationTime anotherTime = timeRepository.save(
                ReservationTime.builder()
                        .startAt(LocalTime.of(15, 0))
                        .build()
        );

        ReservationInfo changedReservation = reservationService.changeReservation(
                pendingReservation.id(),
                new ReservationChangeCommand("리사", anotherTime.getId(), savedTheme.getId(), targetDate)
        );

        assertThat(changedReservation.status()).isEqualTo(Status.ACTIVE);
        assertThat(changedReservation.time().id()).isEqualTo(anotherTime.getId());
    }

    @Test
    @DisplayName("대기 예약도 예약자 본인이 취소할 수 있다.")
    void cancelPendingReservation() {
        reservationService.addReservation(new ReservationCreateCommand(
                "포비", targetDate, savedTime.getId(), savedTheme.getId()
        ));
        ReservationInfo pendingReservation = reservationService.addReservation(new ReservationCreateCommand(
                "리사", targetDate, savedTime.getId(), savedTheme.getId()
        ));

        reservationService.cancelReservation(pendingReservation.id(), "리사");

        Assertions.assertThat(reservationService.getReservationsByName("리사").size())
                .isZero();
    }
}
