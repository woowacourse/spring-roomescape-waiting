package roomescape.reservation.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import roomescape.global.exception.ReservationErrorCode;
import roomescape.global.exception.customException.BusinessException;
import roomescape.global.exception.customException.EntityNotFoundException;
import roomescape.reservation.application.dto.ReservationCreateCommand;
import roomescape.reservation.application.dto.ReservationUpdateCommand;
import roomescape.reservation.application.dto.UserReservationResult;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationRepository;
import roomescape.reservation.fake.FakeReservationRepository;
import roomescape.reservationTime.domain.ReservationTime;
import roomescape.reservationTime.domain.ReservationTimeRepository;
import roomescape.reservationTime.fake.FakeReservationTimeRepository;
import roomescape.theme.domain.Theme;
import roomescape.theme.domain.ThemeRepository;
import roomescape.theme.fake.FakeThemeRepository;
import roomescape.waiting.application.WaitingReference;
import roomescape.waiting.application.WaitingService;
import roomescape.waiting.application.WaitingValidator;
import roomescape.waiting.application.dto.WaitingCreateCommand;
import roomescape.waiting.domain.Waiting;
import roomescape.waiting.domain.WaitingRepository;
import roomescape.waiting.fake.FakeWaitingRepository;

class ReservationServiceTest {

    private ReservationRepository reservationRepository;
    private ReservationTimeRepository reservationTimeRepository;
    private ThemeRepository themeRepository;
    private WaitingRepository waitingRepository;
    private ReservationService reservationService;

    @BeforeEach
    void setUp() {
        reservationRepository = new FakeReservationRepository();
        reservationTimeRepository = new FakeReservationTimeRepository();
        themeRepository = new FakeThemeRepository();
        waitingRepository = new FakeWaitingRepository();
        WaitingService waitingService = new WaitingService(
                waitingRepository,
                reservationTimeRepository,
                themeRepository,
                new WaitingReference() {
                    @Override
                    public void validateExistReservation(WaitingCreateCommand waitingCreateCommand) {
                    }

                    @Override
                    public void promoteToReservation(Waiting waiting) {
                        reservationRepository.save(Reservation.create(
                                waiting.getName(),
                                waiting.getDate(),
                                waiting.getTime(),
                                waiting.getTheme()
                        ));
                    }
                },
                new WaitingValidator(waitingRepository)
        );
        reservationService = new ReservationService(
                reservationRepository,
                reservationTimeRepository,
                themeRepository,
                waitingRepository,
                new ReservationValidator(reservationRepository),
                waitingService
        );
    }

    @Test
    @DisplayName("예약을 저장한다")
    void saveReservation_success() {
        // given
        ReservationTime savedTime = reservationTimeRepository.save(ReservationTime.create(LocalTime.now().plusHours(1)));
        Theme savedTheme = themeRepository.save(Theme.create("공포", "아니", "https://good.com/thumb-nail/1"));
        ReservationCreateCommand command = new ReservationCreateCommand(
                "흑곰",
                LocalDate.now(),
                savedTime.getId(),
                savedTheme.getId()
        );

        // when
        Reservation reservation = reservationService.saveReservation(command);

        // then
        assertThat(reservation.getId()).isNotNull();
        assertThat(reservation.getName()).isEqualTo(command.name());
        assertThat(reservation.getDate()).isEqualTo(command.date());
        assertThat(reservation.getTime()).isEqualTo(savedTime);
        assertThat(reservation.getTheme()).isEqualTo(savedTheme);
        assertThat(reservationRepository.findById(reservation.getId())).contains(reservation);
    }

    @Test
    @DisplayName("존재하지 않는 예약 시간으로 예약하면 잘못된 요청 예외가 전파된다")
    void saveReservation_fail_with_not_found_time() {
        // given
        Long notExistTimeId = 999L;
        Theme savedTheme = themeRepository.save(Theme.create("공포", "아니", "https://good.com/thumb-nail/1"));

        ReservationCreateCommand command = new ReservationCreateCommand(
                "흑곰",
                LocalDate.now(),
                notExistTimeId,
                savedTheme.getId()
        );

        // when & then
        assertThatThrownBy(
                () -> reservationService.saveReservation(command)
        )
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("존재하지 않는 예약 시간입니다.");
        assertThat(reservationRepository.findAll()).isEmpty();
    }

    @Test
    @DisplayName("존재하지 않는 테마 아이디로 예약하면 잘못된 요청 예외가 전파된다")
    void saveReservation_fail_with_not_found_theme() {
        // given
        ReservationTime savedTime = reservationTimeRepository.save(ReservationTime.create(LocalTime.now().plusHours(1)));
        Long notExistThemeId = 999L;

        ReservationCreateCommand command = new ReservationCreateCommand(
                "흑곰",
                LocalDate.now(),
                savedTime.getId(),
                notExistThemeId
        );

        // when & then
        assertThatThrownBy(
                () -> reservationService.saveReservation(command)
        )
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("존재하지 않는 예약 테마입니다.");
        assertThat(reservationRepository.findAll()).isEmpty();
    }

    @Test
    @DisplayName("예약 목록을 조회한다")
    void getReservations_success() {
        // given
        ReservationTime savedTime = reservationTimeRepository.save(ReservationTime.create(LocalTime.now().plusHours(1)));
        Theme savedTheme = themeRepository.save(Theme.create("공포", "아니", "https://good.com/thumb-nail/1"));
        Reservation savedReservation = reservationRepository.save(Reservation.create(
                "인직",
                LocalDate.now(),
                savedTime,
                savedTheme
        ));

        // when
        List<Reservation> reservations = reservationService.getReservations();

        // then
        assertThat(reservations).containsExactly(savedReservation);
    }

    @Test
    @DisplayName("날짜와 테마를 기반으로 예약을 조회한다")
    void getReservations_success_when_date_and_theme_exist() {
        // given
        LocalDate date = LocalDate.now();
        ReservationTime savedTime = reservationTimeRepository.save(ReservationTime.create(LocalTime.now().plusHours(1)));
        Theme savedTheme = themeRepository.save(Theme.create("공포", "아니", "https://good.com/thumb-nail/1"));
        Reservation savedReservation = reservationRepository.save(Reservation.create(
                "인직",
                date,
                savedTime,
                savedTheme
        ));

        // when
        List<Reservation> reservations = reservationService.getReservationsByDateAndTheme(date, savedTheme.getId());

        // then
        assertThat(reservations).containsExactly(savedReservation);
    }

    @Test
    @DisplayName("예약을 삭제한다")
    void deleteReservation_success() {
        // given
        ReservationTime savedTime = reservationTimeRepository.save(ReservationTime.create(LocalTime.now().plusHours(1)));
        Theme savedTheme = themeRepository.save(Theme.create("공포", "아니", "https://good.com/thumb-nail/1"));
        Reservation savedReservation = reservationRepository.save(Reservation.create(
                "인직",
                LocalDate.now(),
                savedTime,
                savedTheme
        ));

        // when
        reservationService.deleteReservation(savedReservation.getId());

        // then
        assertThat(reservationRepository.findById(savedReservation.getId())).isEmpty();
    }

    @Test
    @DisplayName("관리자 예약 삭제 시 같은 슬롯의 1순위 대기를 예약으로 전환한다")
    void deleteReservation_success_when_first_waiting_exists() {
        // given
        ReservationTime savedTime = reservationTimeRepository.save(ReservationTime.create(LocalTime.now().plusHours(1)));
        Theme savedTheme = themeRepository.save(Theme.create("공포", "아니", "https://good.com/thumb-nail/1"));
        LocalDate date = LocalDate.now().plusDays(1);
        Reservation savedReservation = reservationRepository.save(Reservation.create(
                "인직",
                date,
                savedTime,
                savedTheme
        ));
        Waiting savedWaiting = waitingRepository.save(Waiting.create(
                "브라운",
                date,
                savedTime,
                savedTheme
        ));

        // when
        reservationService.deleteReservation(savedReservation.getId());

        // then
        Reservation promotedReservation = reservationRepository.findByDateAndTimeIdAndThemeId(
                date,
                savedTime.getId(),
                savedTheme.getId()
        ).orElseThrow();
        assertThat(promotedReservation.getName()).isEqualTo("브라운");
        assertThat(waitingRepository.findById(savedWaiting.getId())).isEmpty();
    }

    @Test
    @DisplayName("존재하지 않는 예약 아이디로 삭제하면 예외가 발생한다")
    void deleteReservation_fail_with_not_found_reservation() {
        // when & then
        assertThatThrownBy(() -> reservationService.deleteReservation(999L))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("예약을 찾을 수 없습니다.");
    }

    @Test
    @DisplayName("관리자 예약 삭제 시 대기 승격 실패는 예약 삭제를 막지 않는다")
    void deleteReservation_success_when_waiting_promotion_fails() {
        // given
        ReservationTime savedTime = reservationTimeRepository.save(ReservationTime.create(LocalTime.now().plusHours(1)));
        Theme savedTheme = themeRepository.save(Theme.create("공포", "아니", "https://good.com/thumb-nail/1"));
        LocalDate date = LocalDate.now().plusDays(1);
        Reservation savedReservation = reservationRepository.save(Reservation.create(
                "인직",
                date,
                savedTime,
                savedTheme
        ));
        waitingRepository.save(Waiting.create(
                "브라운",
                date,
                savedTime,
                savedTheme
        ));
        WaitingService failingWaitingService = new WaitingService(
                waitingRepository,
                reservationTimeRepository,
                themeRepository,
                new WaitingReference() {
                    @Override
                    public void validateExistReservation(WaitingCreateCommand waitingCreateCommand) {
                    }

                    @Override
                    public void promoteToReservation(Waiting waiting) {
                        throw new BusinessException(ReservationErrorCode.RESERVATION_CREATE_IN_PAST);
                    }
                },
                new WaitingValidator(waitingRepository)
        );
        ReservationService reservationService = new ReservationService(
                reservationRepository,
                reservationTimeRepository,
                themeRepository,
                waitingRepository,
                new ReservationValidator(reservationRepository),
                failingWaitingService
        );

        // when
        reservationService.deleteReservation(savedReservation.getId());

        // then
        assertThat(reservationRepository.findById(savedReservation.getId())).isEmpty();
    }

    @Test
    @DisplayName("이름을 기반으로 자신의 예약 목록을 조회한다")
    void getReservationsByName_success() {
        // given
        ReservationTime savedTime = reservationTimeRepository.save(ReservationTime.create(LocalTime.now().plusHours(1)));
        Theme savedTheme = themeRepository.save(Theme.create("공포", "아니", "https://good.com/thumb-nail/1"));
        Reservation savedReservation = reservationRepository.save(Reservation.create(
                "인직",
                LocalDate.now(),
                savedTime,
                savedTheme
        ));
        Waiting savedWaiting = waitingRepository.save(Waiting.create(
                "인직",
                LocalDate.now().plusDays(1),
                savedTime,
                savedTheme
        ));

        // when
        UserReservationResult result = reservationService.getReservationsByName("인직");

        // then
        assertThat(result.reservations()).containsExactly(savedReservation);
        assertThat(result.waitings()).containsExactly(savedWaiting);
    }

    @Test
    @DisplayName("자신의 예약 날짜와 시간을 수정한다")
    void updateReservationSchedule_success() {
        // given
        ReservationTime savedTime1 = reservationTimeRepository.save(
                ReservationTime.create(LocalTime.now().plusHours(1))
        );

        ReservationTime savedTime2 = reservationTimeRepository.save(
                ReservationTime.create(LocalTime.now().plusHours(2))
        );

        Theme savedTheme = themeRepository.save(
                Theme.create("공포", "아니", "https://good.com/thumb-nail/1")
        );

        Reservation savedReservation = reservationRepository.save(
                Reservation.create(
                        "인직",
                        LocalDate.now().plusDays(1),
                        savedTime1,
                        savedTheme
                )
        );

        LocalDate changedDate = LocalDate.now().plusDays(2);

        // when
        reservationService.updateReservationSchedule(new ReservationUpdateCommand(
                savedReservation.getId(),
                changedDate,
                savedTime2.getId(),
                "인직"
        ));

        // then
        Reservation updatedReservation = reservationRepository.findById(savedReservation.getId())
                .orElseThrow();

        assertThat(updatedReservation.getDate()).isEqualTo(changedDate);
        assertThat(updatedReservation.getTime().getId()).isEqualTo(savedTime2.getId());
    }

    @Test
    @DisplayName("예약 변경 시 기존 예약 시간의 1순위 대기를 예약으로 전환한다")
    void updateReservationSchedule_success_when_first_waiting_exists() {
        // given
        ReservationTime savedTime1 = reservationTimeRepository.save(
                ReservationTime.create(LocalTime.now().plusHours(1))
        );
        ReservationTime savedTime2 = reservationTimeRepository.save(
                ReservationTime.create(LocalTime.now().plusHours(2))
        );
        Theme savedTheme = themeRepository.save(
                Theme.create("공포", "아니", "https://good.com/thumb-nail/1")
        );
        LocalDate date = LocalDate.now().plusDays(1);
        Reservation savedReservation = reservationRepository.save(
                Reservation.create(
                        "인직",
                        date,
                        savedTime1,
                        savedTheme
                )
        );
        Waiting savedWaiting = waitingRepository.save(
                Waiting.create("브라운", date, savedTime1, savedTheme)
        );

        // when
        reservationService.updateReservationSchedule(new ReservationUpdateCommand(
                savedReservation.getId(),
                date,
                savedTime2.getId(),
                "인직"
        ));

        // then
        Reservation promotedReservation = reservationRepository.findByDateAndTimeIdAndThemeId(
                date,
                savedTime1.getId(),
                savedTheme.getId()
        ).orElseThrow();
        assertThat(promotedReservation.getName()).isEqualTo("브라운");
        assertThat(waitingRepository.findById(savedWaiting.getId())).isEmpty();
    }

    @Test
    @DisplayName("예약 변경 시 대기 승격 실패는 예약 변경을 막지 않는다")
    void updateReservationSchedule_success_when_waiting_promotion_fails() {
        // given
        ReservationTime savedTime1 = reservationTimeRepository.save(
                ReservationTime.create(LocalTime.now().plusHours(1))
        );
        ReservationTime savedTime2 = reservationTimeRepository.save(
                ReservationTime.create(LocalTime.now().plusHours(2))
        );
        Theme savedTheme = themeRepository.save(
                Theme.create("공포", "아니", "https://good.com/thumb-nail/1")
        );
        LocalDate date = LocalDate.now().plusDays(1);
        Reservation savedReservation = reservationRepository.save(
                Reservation.create(
                        "인직",
                        date,
                        savedTime1,
                        savedTheme
                )
        );
        waitingRepository.save(Waiting.create(
                "브라운",
                date,
                savedTime1,
                savedTheme
        ));
        WaitingService failingWaitingService = new WaitingService(
                waitingRepository,
                reservationTimeRepository,
                themeRepository,
                new WaitingReference() {
                    @Override
                    public void validateExistReservation(WaitingCreateCommand waitingCreateCommand) {
                    }

                    @Override
                    public void promoteToReservation(Waiting waiting) {
                        throw new BusinessException(ReservationErrorCode.RESERVATION_CREATE_IN_PAST);
                    }
                },
                new WaitingValidator(waitingRepository)
        );
        ReservationService reservationService = new ReservationService(
                reservationRepository,
                reservationTimeRepository,
                themeRepository,
                waitingRepository,
                new ReservationValidator(reservationRepository),
                failingWaitingService
        );

        // when
        reservationService.updateReservationSchedule(new ReservationUpdateCommand(
                savedReservation.getId(),
                date,
                savedTime2.getId(),
                "인직"
        ));

        // then
        Reservation updatedReservation = reservationRepository.findById(savedReservation.getId())
                .orElseThrow();
        assertThat(updatedReservation.getTime().getId()).isEqualTo(savedTime2.getId());
    }

    @Test
    @DisplayName("예약 시간이 변경되지 않으면 대기를 승격하지 않는다")
    void updateReservationSchedule_success_when_schedule_not_changed() {
        // given
        ReservationTime savedTime = reservationTimeRepository.save(
                ReservationTime.create(LocalTime.now().plusHours(1))
        );
        Theme savedTheme = themeRepository.save(
                Theme.create("공포", "아니", "https://good.com/thumb-nail/1")
        );
        LocalDate date = LocalDate.now().plusDays(1);
        Reservation savedReservation = reservationRepository.save(
                Reservation.create(
                        "인직",
                        date,
                        savedTime,
                        savedTheme
                )
        );
        Waiting savedWaiting = waitingRepository.save(
                Waiting.create("브라운", date, savedTime, savedTheme)
        );

        // when
        reservationService.updateReservationSchedule(new ReservationUpdateCommand(
                savedReservation.getId(),
                date,
                savedTime.getId(),
                "인직"
        ));

        // then
        assertThat(reservationRepository.findAll()).containsExactly(savedReservation);
        assertThat(waitingRepository.findById(savedWaiting.getId())).contains(savedWaiting);
    }

    @Test
    @DisplayName("존재하지 않는 예약 아이디로 수정하면 예외가 발생한다")
    void updateReservationSchedule_fail_with_not_found_reservation() {
        // given
        ReservationTime savedTime = reservationTimeRepository.save(
                ReservationTime.create(LocalTime.now().plusHours(1))
        );

        // when & then
        assertThatThrownBy(() -> reservationService.updateReservationSchedule(new ReservationUpdateCommand(
                999L,
                LocalDate.now().plusDays(1),
                savedTime.getId(),
                "인직"
        )))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("예약을 찾을 수 없습니다.");
    }

    @Test
    @DisplayName("본인의 예약이 아니면 수정 시 예외가 발생한다")
    void updateReservationSchedule_fail_with_invalid_owner() {
        // given
        ReservationTime savedTime = reservationTimeRepository.save(
                ReservationTime.create(LocalTime.now().plusHours(1))
        );

        Theme savedTheme = themeRepository.save(
                Theme.create("공포", "설명", "https://good.com")
        );

        Reservation savedReservation = reservationRepository.save(
                Reservation.create(
                        "인직",
                        LocalDate.now().plusDays(1),
                        savedTime,
                        savedTheme
                )
        );

        // when & then
        assertThatThrownBy(() -> reservationService.updateReservationSchedule(new ReservationUpdateCommand(
                savedReservation.getId(),
                LocalDate.now().plusDays(2),
                savedTime.getId(),
                "포비"
        )))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("수정할 수 있는 권한이 없습니다.");
    }

    @Test
    @DisplayName("이름을 기반으로 자신의 예약을 취소한다")
    void cancelReservation_success() {
        // given
        ReservationTime savedTime = reservationTimeRepository.save(ReservationTime.create(LocalTime.now().plusHours(1)));
        Theme savedTheme = themeRepository.save(Theme.create("공포", "아니", "https://good.com/thumb-nail/1"));
        Reservation savedReservation = reservationRepository.save(Reservation.create(
                "인직",
                LocalDate.now(),
                savedTime,
                savedTheme
        ));

        // when
        reservationService.cancelReservation(savedReservation.getId(), "인직");

        // then
        assertThat(reservationRepository.findById(savedReservation.getId())).isEmpty();
    }

    @Test
    @DisplayName("예약 취소 시 같은 슬롯의 1순위 대기를 예약으로 전환한다")
    void cancelReservation_success_when_first_waiting_exists() {
        // given
        ReservationTime savedTime = reservationTimeRepository.save(ReservationTime.create(LocalTime.now().plusHours(1)));
        Theme savedTheme = themeRepository.save(Theme.create("공포", "아니", "https://good.com/thumb-nail/1"));
        LocalDate date = LocalDate.now().plusDays(1);
        Reservation savedReservation = reservationRepository.save(Reservation.create(
                "인직",
                date,
                savedTime,
                savedTheme
        ));
        Waiting savedWaiting = waitingRepository.save(Waiting.create(
                "브라운",
                date,
                savedTime,
                savedTheme
        ));

        // when
        reservationService.cancelReservation(savedReservation.getId(), "인직");

        // then
        Reservation promotedReservation = reservationRepository.findByDateAndTimeIdAndThemeId(
                date,
                savedTime.getId(),
                savedTheme.getId()
        ).orElseThrow();
        assertThat(promotedReservation.getName()).isEqualTo("브라운");
        assertThat(waitingRepository.findById(savedWaiting.getId())).isEmpty();
    }

    @Test
    @DisplayName("대기 승격 실패는 예약 취소를 막지 않는다")
    void cancelReservation_success_when_waiting_promotion_fails() {
        // given
        ReservationTime savedTime = reservationTimeRepository.save(ReservationTime.create(LocalTime.now().plusHours(1)));
        Theme savedTheme = themeRepository.save(Theme.create("공포", "아니", "https://good.com/thumb-nail/1"));
        LocalDate date = LocalDate.now().plusDays(1);
        Reservation savedReservation = reservationRepository.save(Reservation.create(
                "인직",
                date,
                savedTime,
                savedTheme
        ));
        waitingRepository.save(Waiting.create(
                "브라운",
                date,
                savedTime,
                savedTheme
        ));
        WaitingService failingWaitingService = new WaitingService(
                waitingRepository,
                reservationTimeRepository,
                themeRepository,
                new WaitingReference() {
                    @Override
                    public void validateExistReservation(WaitingCreateCommand waitingCreateCommand) {
                    }

                    @Override
                    public void promoteToReservation(Waiting waiting) {
                        throw new BusinessException(ReservationErrorCode.RESERVATION_CREATE_IN_PAST);
                    }
                },
                new WaitingValidator(waitingRepository)
        );
        ReservationService reservationService = new ReservationService(
                reservationRepository,
                reservationTimeRepository,
                themeRepository,
                waitingRepository,
                new ReservationValidator(reservationRepository),
                failingWaitingService
        );

        // when
        reservationService.cancelReservation(savedReservation.getId(), "인직");

        // then
        assertThat(reservationRepository.findById(savedReservation.getId())).isEmpty();
    }

}
