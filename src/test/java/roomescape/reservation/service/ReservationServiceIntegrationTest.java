package roomescape.reservation.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static roomescape.reservation.exception.ReservationErrorInformation.*;
import static roomescape.reservation.fixture.ReservationFixture.toCommand;
import static roomescape.slot.exception.ReservationSlotErrorInformation.SLOT_NOT_FOUND;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.jdbc.Sql;
import roomescape.date.domain.ReservationDate;
import roomescape.date.fixture.ReservationDateFixture;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationStatus;
import roomescape.reservation.exception.ReservationErrorInformation;
import roomescape.reservation.exception.ReservationException;
import roomescape.reservation.fixture.ReservationFixture;
import roomescape.reservation.repository.dto.ReservationWithWaitingTurn;
import roomescape.reservation.service.dto.ReservationChangeCommand;
import roomescape.reservation.service.dto.ReservationSaveCommand;
import roomescape.slot.domain.ReservationSlot;
import roomescape.slot.exception.ReservationSlotException;
import roomescape.support.ServiceSupport;
import roomescape.theme.domain.Theme;
import roomescape.time.domain.ReservationTime;
import roomescape.time.fixture.ReservationTimeFixture;

import java.util.List;

@Import(ReservationService.class)
class ReservationServiceIntegrationTest extends ServiceSupport {

    private final String name = "송송";
    private final String themeName = "테마1";

    private ReservationDate date1;
    private ReservationTime time1;
    private ReservationDate date2;
    private ReservationTime time2;
    private ReservationSlot slot1;
    private ReservationSlot slot2;
    private Theme theme;

    @BeforeEach
    void setUp() {
        date1 = saveDate(ReservationDateFixture.oneWeekLater());
        time1 = saveTime(ReservationTimeFixture.activeTime15());
        date2 = saveDate(ReservationDateFixture.twoWeeksLater());
        time2 = saveTime(ReservationTimeFixture.activeTime16());
        theme = saveTheme(themeName);
        slot1 = saveSlot(ReservationSlot.of(date1, time1, theme));
        slot2 = saveSlot(ReservationSlot.of(date2, time2, theme));
    }

    @Autowired
    private ReservationService reservationService;

    @Test
    @DisplayName("나의 예약 목록을 조회하면, 대기 순번을 조회한다.")
    void getMyReservations() {
        // given
        String name1 = "사람1";
        String name2 = "사람2";

        saveReservation(name1, slot1);
        saveWaitReservation(name2, slot1);
        saveWaitReservation(name, slot1);

        // when
        List<ReservationWithWaitingTurn> actual = reservationService.readAllByName(name);

        // then
        Assertions.assertThat(actual.getFirst().waitingTurn())
                .isEqualTo(2);
    }

    @Test
    @DisplayName("나의 예약 목록을 조회할때, 예약상태면 대기 순번이 없다.")
    void getMyReservations_no_waiting_turn() {
        // given
        saveReservation(name, slot1);

        // when
        List<ReservationWithWaitingTurn> actual = reservationService.readAllByName(name);

        // then
        Assertions.assertThat(actual.getFirst().waitingTurn())
                .isNull();
    }

    @Nested
    @DisplayName("예약 비즈니스 로직 통합 테스트")
    class reserved_test {

        @Test
        @DisplayName("예약시, 슬롯에 등록되지 않은 예약 시간이면 예외를 발생한다.")
        void reserve_does_not_exist_reservation_time() {
            // given
            Long wrongTimeId = Long.MIN_VALUE;
            ReservationSaveCommand command = ReservationFixture.toCommand(date1, wrongTimeId, theme);

            // when & then
            assertThatThrownBy(() -> reservationService.reserve(name, command))
                    .isInstanceOf(ReservationSlotException.class)
                    .hasMessage(SLOT_NOT_FOUND.getMessage());
        }

        @Test
        @DisplayName("예약시, 슬롯에 등록되지 않은 테마이면 예외를 발생한다.")
        void reserve_does_not_exist_theme() {
            // given
            Long wrongThemeId = Long.MIN_VALUE;
            ReservationSaveCommand command = toCommand(date1, time1, wrongThemeId);

            // when & then
            assertThatThrownBy(() -> reservationService.reserve(name, command))
                    .isInstanceOf(ReservationSlotException.class)
                    .hasMessage(SLOT_NOT_FOUND.getMessage());
        }

        @Test
        @DisplayName("예약된 날짜/시간/테마를 중복 예약하면 대기 예약으로 들어간다.")
        void reserved_duplicated() {
            // given
            String anotherName = "다른사람";
            ReservationSaveCommand duplicated = ReservationFixture.toCommand(date1, time1, theme);
            saveReservation(name, slot1);

            // when
            Reservation actual = reservationService.reserve(anotherName, duplicated);

            // then
            assertThat(actual.getStatus())
                    .isEqualTo(ReservationStatus.WAITING);
        }

        @Test
        @DisplayName("취소된 예약을 동일한 사람이 새롭게 예약할 수 있다.")
        void reserved_when_cancel_same_name() {
            // given
            Reservation reservation = saveReservation(name, slot1);
            ReservationSaveCommand saveCommand = ReservationFixture.toCommand(date1, time1, theme);
            reservationService.cancelByManager(reservation.getId());

            // when
            Reservation actual = reservationService.reserve(name, saveCommand);

            // then
            Assertions.assertThat(actual.getStatus())
                    .isEqualTo(ReservationStatus.RESERVED);
        }

        @Test
        @DisplayName("취소된 예약을 다른 사람이 새롭게 예약할 수 있다.")
        void reserved_when_cancel_another_name() {
            // given
            String anotherName = "다른사람";

            Reservation savedReservation = saveReservation(name, slot1);
            ReservationSaveCommand saveCommand = ReservationFixture.toCommand(date1, time1, theme);
            reservationService.cancelByManager(savedReservation.getId());

            // when
            Reservation actual = reservationService.reserve(anotherName, saveCommand);

            // then
            Assertions.assertThat(actual.getStatus())
                    .isEqualTo(ReservationStatus.RESERVED);
        }

        @Test
        @DisplayName("이미 슬롯에 내 예약이 있으면 예약할 수 없다.")
        void reserve_duplicated() {
            // given
            saveReservation(name, slot1);
            ReservationSaveCommand command = new ReservationSaveCommand(date1.getId(), time1.getId(), theme.getId());

            // when & then
            assertThatThrownBy(() -> reservationService.reserve(name, command))
                    .isInstanceOf(ReservationException.class)
                    .hasMessage(ReservationErrorInformation.RESERVATION_ALREADY_BOOKED.getMessage());
        }

    }

    @Nested
    @DisplayName("예약 취소 비즈니스 로직 통합 테스트")
    class cancle_test {

        @Test
        @DisplayName("관리자 전용으로 예약을 취소하면 CANCELED 상태가 된다.")
        void cancelByManager() {
            // given
            Reservation savedReservation = saveReservation(name, slot1);

            // when
            Reservation actual = reservationService.cancelByManager(savedReservation.getId());

            // then
            Assertions.assertThat(actual.getStatus())
                    .isEqualTo(ReservationStatus.CANCELED);
        }

        @Test
        @DisplayName("아직 지나지 않은 본인의 예약은 취소할 수 있다.")
        void cancel() {
            // given
            Reservation savedReservation = saveReservation(name, slot1);

            // when
            Reservation actual = reservationService.cancel(savedReservation.getId(), name);

            // then
            Assertions.assertThat(actual.getStatus())
                    .isEqualTo(ReservationStatus.CANCELED);
        }

        @Test
        @DisplayName("본인의 예약이 아닌데 취소를하면 예외가 발생한다.")
        void cancel_not_owner() {
            // given
            Reservation savedReservation = saveReservation(name, slot1);

            String anotherName = "다른사람";
            Long savedId = savedReservation.getId();

            // when & then
            assertThatThrownBy(() -> reservationService.cancel(savedId, anotherName))
                    .isInstanceOf(ReservationException.class)
                    .hasMessage(RESERVATION_NOT_OWNER.getMessage());
        }

        @Test
        @DisplayName("이미 취소된 예약을 취소하면 예외가 발생한다.")
        void cancel_already_canceled() {
            // given
            Reservation saved = saveReservation(name, slot1);
            saved.updateStatus(ReservationStatus.CANCELED);
            reservationRepository.updateStatus(saved);
            Long savedId = saved.getId();

            // when & then
            assertThatThrownBy(() -> reservationService.cancel(savedId, name))
                    .isInstanceOf(ReservationException.class)
                    .hasMessage(RESERVATION_ALREADY_CANCELED.getMessage());
        }

        @Test
        @DisplayName("이미 지난 예약을 취소하면 예외가 발생한다.")
        @Sql(
                scripts = {"classpath:past-reservation.sql"},
                executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD
        )
        void cancel_not_past() {
            // given
            String sqlName = "member";
            Long savedId = 1L;

            // when & then
            Assertions.assertThatThrownBy(() -> reservationService.cancel(savedId, sqlName))
                    .isInstanceOf(ReservationException.class)
                    .hasMessage(RESERVATION_ALREADY_PAST.getMessage());
        }

    }

    @Nested
    @DisplayName("예약 변경 비즈니스 로직 통합 테스트")
    class change_test {

        @Test
        @DisplayName("예약 가능한 날짜로 변경할 수 있다.")
        void changeSchedule() {
            // given
            Reservation saved = saveReservation(name, slot1);
            ReservationChangeCommand changeCommand = new ReservationChangeCommand(saved.getId(), name, date2.getId(), time2.getId());

            // when
            reservationService.changeSchedule(changeCommand);

            // then
            Reservation actual = reservationRepository.findById(saved.getId()).get();

            Assertions.assertThat(actual.getDate().getId()).isEqualTo(date2.getId());
            Assertions.assertThat(actual.getDate().getDate()).isEqualTo(date2.getDate());
            Assertions.assertThat(actual.getTime().getId()).isEqualTo(time2.getId());
            Assertions.assertThat(actual.getTime().getStartAt()).isEqualTo(time2.getStartAt());
        }

        @Test
        @DisplayName("본인의 예약이 아닌데 변경을 시도하면 예외가 발생한다.")
        void changeSchedule_not_owner() {
            // given
            Reservation saved = saveReservation(name, slot1);
            String notOwerName = "다른사람";
            ReservationChangeCommand changeCommand = new ReservationChangeCommand(saved.getId(), notOwerName, date2.getId(), time2.getId());

            // when & then
            assertThatThrownBy(() -> reservationService.changeSchedule(changeCommand))
                    .isInstanceOf(ReservationException.class)
                    .hasMessage(RESERVATION_NOT_OWNER.getMessage());
        }

        @Test
        @DisplayName("이미 취소된 예약을 변경하면 예외가 발생한다.")
        void changeSchedule_already_canceled() {
            // given
            Reservation saved = saveReservation(name, slot1);
            saved.updateStatus(ReservationStatus.CANCELED);
            reservationRepository.updateStatus(saved);
            ReservationChangeCommand changeCommand = new ReservationChangeCommand(saved.getId(), name, date2.getId(), time2.getId());

            // when
            assertThatThrownBy(() -> reservationService.changeSchedule(changeCommand))
                    .isInstanceOf(ReservationException.class)
                    .hasMessage(RESERVATION_ALREADY_CANCELED.getMessage());
        }

        @Test
        @DisplayName("이미 지난 예약을 변경하면 예외가 발생한다.")
        void changeSchedule_past() {
            // given
            ReservationDate pastDate = saveDate(ReservationDateFixture.pastDate());
            ReservationTime pastTime = saveTime(ReservationTimeFixture.activeTime17());
            ReservationSlot slot = saveSlot(ReservationSlot.of(pastDate, pastTime, theme));
            Reservation pastReservation = savePastReservation("과거 예약", slot);

            ReservationChangeCommand changeCommand = new ReservationChangeCommand(pastReservation.getId() , "과거 예약", date1.getId(), time1.getId());

            // when
            assertThatThrownBy(() -> reservationService.changeSchedule(changeCommand))
                    .isInstanceOf(ReservationException.class)
                    .hasMessage(RESERVATION_ALREADY_PAST.getMessage());
        }

        @Test
        @DisplayName("지난 날짜로 예약을 변경하면 예외가 발생한다.")
        void changeSchedule_new_datetime_is_past() {
            // given
            ReservationDate pastDate = saveDate(ReservationDateFixture.pastDate());
            ReservationTime pastTime = saveTime(ReservationTimeFixture.activeTime17());
            savePastSlot(pastDate.getId(), pastTime.getId(), theme);

            Reservation saved = saveReservation(name, slot1);
            ReservationChangeCommand changeCommand = new ReservationChangeCommand(saved.getId(), name, pastDate.getId(), pastTime.getId());

            // when
            assertThatThrownBy(() -> reservationService.changeSchedule(changeCommand))
                    .isInstanceOf(ReservationException.class)
                    .hasMessage(RESERVATION_ALREADY_PAST.getMessage());
        }

        @Test
        @DisplayName("일반유저가 이미 존재하는 날짜/시간으로 예약을 변경하면 예외가 발생한다.")
        void changeSchedule_duplicated() {
            // given
            Reservation saved = saveReservation(name, slot1);
            saveReservation(name, slot2);
            ReservationChangeCommand changeCommand = new ReservationChangeCommand(saved.getId(), name, date2.getId(), time2.getId());

            // when & then
            assertThatThrownBy(() ->
                    reservationService.changeSchedule(changeCommand))
                    .isInstanceOf(ReservationException.class)
                    .hasMessage(RESERVATION_ALREADY_BOOKED.getMessage());
        }

        @Test
        @DisplayName("관리자는 예약자 확인 없이, 예약 날짜/시간을 변경할 수 있다.")
        void changeScheduleByManager() {
            // given
            Reservation saved = saveReservation(name, slot1);
            ReservationChangeCommand changeCommand = new ReservationChangeCommand(saved.getId(), null, date2.getId(), time2.getId());

            // when
            reservationService.changeScheduleByManager(changeCommand);

            // then
            Reservation actual = reservationRepository.findById(saved.getId()).get();

            Assertions.assertThat(actual.getDate().getId()).isEqualTo(date2.getId());
            Assertions.assertThat(actual.getDate().getDate()).isEqualTo(date2.getDate());
            Assertions.assertThat(actual.getTime().getId()).isEqualTo(time2.getId());
            Assertions.assertThat(actual.getTime().getStartAt()).isEqualTo(time2.getStartAt());
        }

        @Test
        @DisplayName("이미 취소된 예약을 변경하면 예외가 발생한다.")
        void changeScheduleByManager_already_canceled() {
            // given
            Reservation saved = saveReservation(name, slot1);
            saved.updateStatus(ReservationStatus.CANCELED);
            reservationRepository.updateStatus(saved);
            ReservationChangeCommand changeCommand = new ReservationChangeCommand(saved.getId(), null, date2.getId(), time2.getId());

            // when
            assertThatThrownBy(() -> reservationService.changeScheduleByManager(changeCommand))
                    .isInstanceOf(ReservationException.class)
                    .hasMessage(RESERVATION_ALREADY_CANCELED.getMessage());
        }

        @Test
        @DisplayName("관리자가 예약을 과거의 날짜/시간으로 변경하면 예외가 발생한다.")
        void changeScheduleByManager_pastDateTime() {
            // given
            ReservationDate pastDate = saveDate(ReservationDateFixture.pastDate());
            ReservationTime pastTime = saveTime(ReservationTimeFixture.activeTime17());
            saveSlot(ReservationSlot.of(pastDate, pastTime, theme));
            Reservation saved = saveReservation(name, slot1);

            ReservationChangeCommand changeCommand = new ReservationChangeCommand(saved.getId(), null, pastDate.getId(), pastTime.getId());

            // when & then
            assertThatThrownBy(() -> reservationService.changeScheduleByManager(changeCommand))
                    .isInstanceOf(ReservationException.class)
                    .hasMessage(RESERVATION_ALREADY_PAST.getMessage());
        }

        @Test
        @DisplayName("관리자가 이미 존재하는 날짜/시간으로 예약을 변경하면 예외가 발생한다.")
        void changeScheduleByManager_duplicated() {
            // given
            Reservation saved = saveReservation(name, slot1);
            saveReservation(name, slot2);
            ReservationChangeCommand changeCommand = new ReservationChangeCommand(saved.getId(), null, date2.getId(), time2.getId());

            // when & then
            assertThatThrownBy(() ->
                    reservationService.changeScheduleByManager(changeCommand))
                    .isInstanceOf(ReservationException.class)
                    .hasMessage(RESERVATION_ALREADY_BOOKED.getMessage());
        }

    }

    @Test
    @DisplayName("대기 상태인 예약은 변경할 수 없다.")
    void waiting_reserve_not_changeable() {
        // given
        Reservation saved = saveWaitReservation(name, slot1);
        ReservationChangeCommand command = new ReservationChangeCommand(
                saved.getId(), saved.getName(), saved.getDate().getId(), saved.getTime().getId()
        );

        // when & then
        assertThatThrownBy(() -> reservationService.changeSchedule(command))
                .isInstanceOf(ReservationException.class)
                .hasMessage(ReservationErrorInformation.RESERVATION_ALREADY_WAITING.getMessage());
    }

    private void savePastSlot(Long pastDateId, Long pastTimeId, Theme theme) {
        ReservationTime pastTime = reservationTimeRepository.findById(pastDateId).get();
        ReservationDate pastDate = reservationDateRepository.findById(pastTimeId).get();
        saveSlot(ReservationSlot.of(pastDate, pastTime, theme));
    }

}
