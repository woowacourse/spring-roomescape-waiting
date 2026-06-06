package roomescape.reservation.service;

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
import roomescape.reservation.repository.dto.ReservationWithSlotInformation;
import roomescape.slot.domain.ReservationSlot;
import roomescape.slot.exception.ReservationSlotException;
import roomescape.support.ServiceSupport;
import roomescape.theme.domain.Theme;
import roomescape.time.domain.ReservationTime;
import roomescape.time.fixture.ReservationTimeFixture;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static roomescape.reservation.domain.ReservationStatus.CANCELED;
import static roomescape.reservation.domain.ReservationStatus.RESERVED;
import static roomescape.reservation.exception.ReservationErrorInformation.*;
import static roomescape.slot.exception.ReservationSlotErrorInformation.SLOT_NOT_FOUND;

@Import({ReservationService.class})
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
        List<ReservationWithSlotInformation> actual = reservationService.readAllByName(name);

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
        List<ReservationWithSlotInformation> actual = reservationService.readAllByName(name);

        // then
        Assertions.assertThat(actual.getFirst().waitingTurn())
                .isNull();
    }

    @Nested
    @DisplayName("예약 비즈니스 로직 통합 테스트")
    class reserved_test {

        @Test
        @DisplayName("존재하지 않는 슬롯에 예약시, 예외가 발생한다.")
        void reserve_does_not_exist_reservation_time() {
            assertThatThrownBy(() -> reservationService.reserve(name, Long.MIN_VALUE))
                    .isInstanceOf(ReservationSlotException.class)
                    .hasMessage(SLOT_NOT_FOUND.getMessage());
        }

        @Test
        @DisplayName("예약된 날짜/시간/테마를 중복 예약하면 대기 예약으로 들어간다.")
        void reserved_duplicated() {
            // given
            String anotherName = "다른사람";
            saveReservation(name, slot1);

            // when
            Reservation actual = reservationService.reserve(anotherName, slot1.getId());

            // then
            assertThat(actual.getStatus())
                    .isEqualTo(ReservationStatus.WAITING);
        }

        @Test
        @DisplayName("취소된 예약을 동일한 사람이 새롭게 예약할 수 있다.")
        void reserved_when_cancel_same_name() {
            // given
            Reservation reservation = saveReservation(name, slot1);
            reservationService.cancelByManager(slot1.getId(), reservation.getId());

            // when
            Reservation actual = reservationService.reserve(name, slot1.getId());

            // then
            Assertions.assertThat(actual.getStatus())
                    .isEqualTo(RESERVED);
        }

        @Test
        @DisplayName("취소된 예약을 다른 사람이 새롭게 예약할 수 있다.")
        void reserved_when_cancel_another_name() {
            // given
            String anotherName = "다른사람";
            Reservation saved = saveReservation(name, slot1);
            reservationService.cancelByManager(slot1.getId(), saved.getId());

            // when
            Reservation actual = reservationService.reserve(anotherName, slot1.getId());

            // then
            Assertions.assertThat(actual.getStatus())
                    .isEqualTo(RESERVED);
        }

        @Test
        @DisplayName("이미 슬롯에 내 예약이 있으면 예약할 수 없다.")
        void reserve_duplicated() {
            // given
            saveReservation(name, slot1);

            // when & then
            assertThatThrownBy(() -> reservationService.reserve(name, slot1.getId()))
                    .isInstanceOf(ReservationException.class)
                    .hasMessage(ReservationErrorInformation.RESERVATION_ALREADY_BOOKED.getMessage());
        }

        @Test
        @DisplayName("slotId를 기반으로 예약을 생성할 수 있다.")
        void reserve_with_slotId() {
            // given & when
            Reservation actual = reservationService.reserve(name, slot1.getId());

            // then
            Assertions.assertThat(actual.getSlotId())
                    .isEqualTo(slot1.getId());

            Assertions.assertThat(actual.getStatus())
                    .isEqualTo(RESERVED);
        }

    }

    @Nested
    @DisplayName("예약 취소 비즈니스 로직 통합 테스트")
    class cancle_test {

        @Test
        @DisplayName("관리자 전용으로 예약을 취소하면 CANCELED 상태가 된다.")
        void cancelByManager() {
            // given
            Reservation saved = saveReservation(name, slot1);

            // when
            Reservation actual = reservationService.cancelByManager(slot1.getId(), saved.getId());

            // then
            Assertions.assertThat(actual.getStatus())
                    .isEqualTo(ReservationStatus.CANCELED);
        }

        @Test
        @DisplayName("아직 지나지 않은 본인의 예약은 취소할 수 있다.")
        void cancel() {
            // given
            saveReservation(name, slot1);

            // when
            Reservation actual = reservationService.cancel(slot1.getId(), name);

            // then
            Assertions.assertThat(actual.getStatus())
                    .isEqualTo(ReservationStatus.CANCELED);
        }

        @Test
        @DisplayName("본인의 예약이 아닌데 취소를하면 예외가 발생한다.")
        void cancel_not_owner() {
            // given
            saveReservation(name, slot1);
            String anotherName = "다른사람";

            // when & then
            assertThatThrownBy(() -> reservationService.cancel(slot1.getId(), anotherName))
                    .isInstanceOf(ReservationException.class)
                    .hasMessage(RESERVATION_NOT_FOUND.getMessage());
        }

        @Test
        @DisplayName("이미 지난 예약을 취소하면 예외가 발생한다.")
        @Sql(scripts = {"classpath:past-reservation.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
        void cancel_not_past() {
            assertThatThrownBy(() -> reservationService.cancel(1L, "member"))
                    .isInstanceOf(ReservationException.class)
                    .hasMessage(RESERVATION_ALREADY_PAST.getMessage());
        }

        @Test
        @DisplayName("예약취소 시, 타겟은 CANCELED 대기 1순위가 RESERVED가 된다.")
        void cancel_with_promote() {
            // given
            String name1 = "송송";
            String name2 = "피온";
            Reservation reservation1 = saveReservation(name1, slot1);
            Reservation reservation2 = saveWaitReservation(name2, slot1);

            // when
            reservationService.cancel(slot1.getId(), name1);
            Reservation canceled = reservationRepository.findById(reservation1.getId()).get();
            Reservation promoted = reservationRepository.findById(reservation2.getId()).get();

            // then
            Assertions.assertThat(canceled.getStatus())
                    .isEqualTo(CANCELED);

            Assertions.assertThat(promoted.getStatus())
                    .isEqualTo(RESERVED);
        }

        @Test
        @DisplayName("관리자가 예약취소 시, 타겟은 CANCELED 대기 1순위가 RESERVED가 된다.")
        void cancelByManager_with_promote() {
            // given
            String name1 = "송송";
            String name2 = "피온";
            Reservation reservation1 = saveReservation(name1, slot1);
            Reservation reservation2 = saveWaitReservation(name2, slot1);

            // when
            reservationService.cancelByManager(slot1.getId(), reservation1.getId());
            Reservation canceled = reservationRepository.findById(reservation1.getId()).get();
            Reservation promoted = reservationRepository.findById(reservation2.getId()).get();

            // then
            Assertions.assertThat(canceled.getStatus())
                    .isEqualTo(CANCELED);

            Assertions.assertThat(promoted.getStatus())
                    .isEqualTo(RESERVED);
        }

    }

}
