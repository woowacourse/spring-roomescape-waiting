package roomescape.reservation.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;
import static roomescape.reservation.exception.ReservationErrorInformation.RESERVATION_ALREADY_BOOKED;
import static roomescape.reservation.exception.ReservationErrorInformation.RESERVATION_ALREADY_CANCELED;
import static roomescape.reservation.exception.ReservationErrorInformation.RESERVATION_ALREADY_PAST;
import static roomescape.reservation.exception.ReservationErrorInformation.RESERVATION_ALREADY_WAITING;
import static roomescape.reservation.exception.ReservationErrorInformation.RESERVATION_NEW_SCHEDULE_PAST_NOT_ALLOWED;
import static roomescape.reservation.exception.ReservationErrorInformation.RESERVATION_NOT_OWNER;
import static roomescape.reservation.fixture.ReservationFixture.reservation;
import static roomescape.reservation.fixture.ReservationFixture.toCommand;
import static roomescape.reservation.fixture.ReservationFixture.waitReservation;
import static roomescape.theme.exception.ThemeErrorInformation.THEME_NOT_FOUND;
import static roomescape.time.exception.ReservationTimeErrorInformation.TIME_NOT_FOUND;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.util.ReflectionTestUtils;
import roomescape.date.domain.ReservationDate;
import roomescape.date.fixture.ReservationDateFixture;
import roomescape.date.repository.ReservationDateRepository;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationStatus;
import roomescape.reservation.exception.ReservationException;
import roomescape.reservation.fixture.ReservationFixture;
import roomescape.reservation.repository.ReservationRepository;
import roomescape.reservation.repository.ReservationSlotRepository;
import roomescape.reservation.service.dto.ReservationChangeCommand;
import roomescape.reservation.service.dto.ReservationSaveCommand;
import roomescape.theme.domain.Theme;
import roomescape.theme.exception.ThemeException;
import roomescape.theme.fixture.ThemeFixture;
import roomescape.theme.repository.ThemeRepository;
import roomescape.time.domain.ReservationTime;
import roomescape.time.exception.ReservationTimeException;
import roomescape.time.fixture.ReservationTimeFixture;
import roomescape.time.repository.ReservationTimeRepository;

@DataJpaTest(showSql = false)
class ReservationServiceTest {

    private final String name = "한다";
    private ReservationTime reservationTime1;
    private ReservationTime reservationTime2;
    private ReservationDate reservationDate1;
    private ReservationDate reservationDate2;
    private Theme theme1;
    private Theme theme2;

    @Autowired
    private ReservationSlotRepository reservationSlotRepository;
    @Autowired
    private ReservationRepository reservationRepository;
    @Autowired
    private ReservationTimeRepository reservationTimeRepository;
    @Autowired
    private ReservationDateRepository reservationDateRepository;
    @Autowired
    private ThemeRepository themeRepository;

    private ReservationService reservationService;

    @BeforeEach
    void setup() {
        this.reservationService = new ReservationService(reservationSlotRepository,
            reservationRepository, reservationTimeRepository, reservationDateRepository,
            themeRepository);

        reservationTime1 = reservationTimeRepository.save(ReservationTimeFixture.time15());
        reservationTime2 = reservationTimeRepository.save(ReservationTimeFixture.time16());

        reservationDate1 = reservationDateRepository.save(ReservationDateFixture.oneWeekLater());
        reservationDate2 = reservationDateRepository.save(ReservationDateFixture.twoWeeksLater());

        theme1 = themeRepository.save(ThemeFixture.theme("테마1"));
        theme2 = themeRepository.save(ThemeFixture.theme("테마2"));
    }

    private Reservation save(Reservation reservation) {
        return reservationRepository.save(reservation);
    }

    private ReservationDate savePastDate() {
        ReservationDate pastDate = reservationDateRepository.saveAndFlush(
            ReservationDate.create(LocalDate.now().plusYears(100)));
        ReflectionTestUtils.setField(pastDate, "date", LocalDate.now().minusDays(1));
        return reservationDateRepository.saveAndFlush(pastDate);
    }

    private Reservation savePastReservation() {
        ReservationDate pastDate = savePastDate();
        Reservation reservation = save(reservation(name, reservationDate1, reservationTime1, theme1));
        ReflectionTestUtils.setField(reservation, "date", pastDate);
        return reservationRepository.saveAndFlush(reservation);
    }

    private void cancelByManager(Reservation reservation) {
        reservationService.cancelByManager(reservation.getId());
    }


    @Nested
    @DisplayName("readAll 메서드는")
    class ReadAllTest {


        @Test
        @DisplayName("모든 예약을 조회한다")
        void 성공() {
            //given & when
            List<Reservation> reservations = List.of(
                reservation(name, reservationDate1, reservationTime1, theme1),
                reservation(name, reservationDate2, reservationTime1, theme2)
            );
            reservationRepository.saveAll(reservations);
            List<Reservation> actual = reservationService.readAll();

            //then
            assertThat(actual)
                .hasSize(reservations.size());
        }
    }

    @Nested
    @DisplayName("reserve 메서드는")
    class ReserveTest {


        @Test
        @DisplayName("예약을 생성한다")
        void 성공1() {
            //given & when
            List<Reservation> reservations = List.of();
            reservationService.reserve(name,
                ReservationFixture.toCommand(reservationDate1, reservationTime1, theme1));

            //then
            assertThat(reservationService.readAll())
                .hasSize(reservations.size() + 1);
        }


        @Test
        @DisplayName("같은 슬롯에 예약이 있으면 대기 상태로 예약된다")
        void 성공2() {
            // given
            String otherName = "다른 이용자";
            Reservation reservation = reservation(otherName, reservationDate1, reservationTime1,
                theme1);
            ReservationSaveCommand duplicated = ReservationFixture.toCommand(reservationDate1,
                reservationTime1, theme1);
            save(reservation);

            // when
            Reservation actual = reservationService.reserve(name, duplicated);

            // then
            assertThat(actual.getStatus())
                .isEqualTo(ReservationStatus.WAITING);
        }


        @Test
        @DisplayName("기존 예약이 취소된 슬롯에 예약을 하면 reserved로 처리된다")
        void 성공3() {
            // given
            Reservation reservation = save(
                reservation(name, reservationDate1, reservationTime1, theme1));
            ReservationSaveCommand duplicated = ReservationFixture.toCommand(reservationDate1,
                reservationTime1, theme1);
            cancelByManager(reservation);

            // when
            Reservation actual = reservationService.reserve(name, duplicated);

            // then
            assertThat(actual.getStatus())
                .isEqualTo(ReservationStatus.RESERVED);
        }


        @Test
        @DisplayName("다른 사람의 예약이 취소된 슬롯에 예약을 하면 reserved로 처리된다")
        void 성공4() {
            // given
            String anotherName = "다른사람";
            Reservation reservation = save(
                reservation(name, reservationDate1, reservationTime1, theme1));
            ReservationSaveCommand duplicated = ReservationFixture.toCommand(reservationDate1,
                reservationTime1, theme1);
            cancelByManager(reservation);

            // when
            Reservation actual = reservationService.reserve(anotherName, duplicated);

            // then
            Assertions.assertThat(actual.getStatus())
                .isEqualTo(ReservationStatus.RESERVED);
        }


        @Test
        @DisplayName("시간이 존재하지 않으면 예외가 발생한다")
        void 실패1() {
            // given
            Long wrongTimeId = Long.MIN_VALUE;
            ReservationSaveCommand command = ReservationFixture.toCommand(reservationDate1,
                wrongTimeId, theme1);

            // when & then
            assertThatThrownBy(() -> reservationService.reserve(name, command))
                .isInstanceOf(ReservationTimeException.class)
                .hasMessage(TIME_NOT_FOUND.getMessage());
        }


        @Test
        @DisplayName("테마가 존재하지 않으면 예외가 발생한다")
        void 실패2() {
            // given
            Long wrongThemeId = Long.MIN_VALUE;
            ReservationSaveCommand command = toCommand(reservationDate1, reservationTime1,
                wrongThemeId);

            // when & then
            assertThatThrownBy(() -> reservationService.reserve(name, command))
                .isInstanceOf(ThemeException.class)
                .hasMessage(THEME_NOT_FOUND.getMessage());
        }
    }

    @Nested
    @DisplayName("cancel 메서드는")
    class CancelTest {


        @Test
        @DisplayName("예약을 취소한다")
        void 성공() {
            // given
            Reservation savedReservation = save(
                reservation(name, reservationDate1, reservationTime1, theme1));

            // when
            Reservation actual = reservationService.cancel(savedReservation.getId(), name);

            // then
            Assertions.assertThat(actual.getStatus())
                .isEqualTo(ReservationStatus.CANCELED);
        }

        @Test
        @DisplayName("대기 상태인 예약도 취소한다")
        void 성공2() {
            // given
            String nameInWaiting = "대기중인 사용자";
            save(reservation(name, reservationDate1, reservationTime1, theme1));
            Reservation reservationInWaiting =
                save(waitReservation(nameInWaiting, reservationDate1, reservationTime1, theme1));

            // when
            Reservation actual = reservationService.cancel(reservationInWaiting.getId(),
                nameInWaiting);

            // then
            Assertions.assertThat(actual.getStatus())
                .isEqualTo(ReservationStatus.CANCELED);
        }


        @Test
        @DisplayName("WAITING 예약을 취소하면 승격이 일어나지 않는다")
        void 성공3() {
            // given
            String name2 = "사용자2";
            String name3 = "사용자3";
            save(reservation(name, reservationDate1, reservationTime1, theme1));
            Reservation reservationInWaiting = save(
                waitReservation(name2, reservationDate1, reservationTime1, theme1));
            save(waitReservation(name3, reservationDate1, reservationTime1, theme1));

            // when
            reservationService.cancel(reservationInWaiting.getId(), name2);

            // then
            assertThat(reservationRepository.findAll())
                .filteredOn(reservation -> reservation.getStatus() == ReservationStatus.RESERVED)
                .hasSize(1);
        }


        @Test
        @DisplayName("RESERVED 예약을 취소하면 첫 번째 WAITING 예약만 RESERVED로 승격된다")
        void 성공4() {
            // given
            String name2 = "사용자2";
            Reservation reservationToCancel = save(
                reservation(name, reservationDate1, reservationTime1, theme1));
            Reservation reservationInWaiting = save(
                waitReservation(name2, reservationDate1, reservationTime1, theme1));

            // when
            reservationService.cancel(reservationToCancel.getId(), name);

            // then
            assertThat(reservationInWaiting)
                .extracting(Reservation::getStatus)
                .isEqualTo(ReservationStatus.RESERVED);
        }

        @Test
        @DisplayName("예약자가 아니면 예외가 발생한다")
        void 실패1() {
            // given
            Reservation saved = save(reservation(name, reservationDate1, reservationTime1, theme1));
            String anotherName = "다른사람";
            Long savedId = saved.getId();

            // when & then
            assertThatThrownBy(() -> reservationService.cancel(savedId, anotherName))
                .isInstanceOf(ReservationException.class)
                .hasMessage(RESERVATION_NOT_OWNER.getMessage());
        }


        @Test
        @DisplayName("이미 취소된 예약이면 예외가 발생한다")
        void 실패2() {
            // given
            Reservation saved = save(reservation(name, reservationDate1, reservationTime1, theme1));
            saved.updateStatus(ReservationStatus.CANCELED);
            reservationRepository.saveAndFlush(saved);
            Long savedId = saved.getId();

            // when & then
            assertThatThrownBy(() -> reservationService.cancel(savedId, name))
                .isInstanceOf(ReservationException.class)
                .hasMessage(RESERVATION_ALREADY_CANCELED.getMessage());
        }


        @Test
        @DisplayName("과거 예약은 취소할 수 없다")
        void 실패3() {
            // given
            Reservation saved = savePastReservation();
            Long savedId = saved.getId();

            // when & then
            Assertions.assertThatThrownBy(() -> reservationService.cancel(savedId, name))
                .isInstanceOf(ReservationException.class)
                .hasMessage(RESERVATION_ALREADY_PAST.getMessage());
        }
    }

    @Nested
    @DisplayName("cancelByManager 메서드는")
    class CancelByManagerTest {


        @Test
        @DisplayName("예약을 취소한다")
        void 성공1() {
            // given
            Reservation savedReservation = save(
                reservation(name, reservationDate1, reservationTime1, theme1));

            // when
            Reservation actual = reservationService.cancelByManager(savedReservation.getId());

            // then
            Assertions.assertThat(actual.getStatus())
                .isEqualTo(ReservationStatus.CANCELED);
        }

        @Test
        @DisplayName("대기 상태인 예약도 취소한다")
        void 성공2() {
            // given
            String nameInWaiting = "대기중인 사용자";
            save(reservation(name, reservationDate1, reservationTime1, theme1));
            Reservation reservationInWaiting =
                save(waitReservation(nameInWaiting, reservationDate1, reservationTime1, theme1));

            // when
            Reservation actual = reservationService.cancelByManager(reservationInWaiting.getId());

            // then
            Assertions.assertThat(actual.getStatus())
                .isEqualTo(ReservationStatus.CANCELED);
        }


        @Test
        @DisplayName("WAITING 상태인 예약이 취소되어도 같은 슬롯에서 승격이 이루어지지 않는다")
        void 성공3() {
            // given
            String name2 = "사용자2";
            String name3 = "사용자3";
            save(reservation(name, reservationDate1, reservationTime1, theme1));
            Reservation reservationInWaiting = save(
                waitReservation(name2, reservationDate1, reservationTime1, theme1));
            save(waitReservation(name3, reservationDate1, reservationTime1, theme1));

            // when
            reservationService.cancelByManager(reservationInWaiting.getId());

            // then
            assertThat(reservationRepository.findAll())
                .filteredOn(reservation -> reservation.getStatus() == ReservationStatus.RESERVED)
                .hasSize(1);
        }


        @Test
        @DisplayName("RESERVED 예약을 취소하면 첫 번째 WAITING 예약만 RESERVED로 승격된다")
        void 성공4() {
            // given
            String name2 = "사용자2";
            Reservation reservationToCancel = save(
                reservation(name, reservationDate1, reservationTime1, theme1));
            Reservation reservationInWaiting = save(
                waitReservation(name2, reservationDate1, reservationTime1, theme1));

            // when
            reservationService.cancelByManager(reservationToCancel.getId());

            // then
            assertThat(reservationInWaiting)
                .extracting(Reservation::getStatus)
                .isEqualTo(ReservationStatus.RESERVED);
        }

        @Test
        @DisplayName("이미 취소된 예약이면 예외가 발생한다")
        void 실패1() {
            // given
            Reservation saved = save(reservation(name, reservationDate1, reservationTime1, theme1));
            saved.updateStatus(ReservationStatus.CANCELED);
            reservationRepository.saveAndFlush(saved);
            Long savedId = saved.getId();

            // when & then
            assertThatThrownBy(() -> reservationService.cancelByManager(savedId))
                .isInstanceOf(ReservationException.class)
                .hasMessage(RESERVATION_ALREADY_CANCELED.getMessage());
        }


        @Test
        @DisplayName("과거 예약은 취소할 수 없다")
        void 실패2() {
            // given
            Reservation saved = savePastReservation();
            Long savedId = saved.getId();

            // when & then
            Assertions.assertThatThrownBy(() -> reservationService.cancelByManager(savedId))
                .isInstanceOf(ReservationException.class)
                .hasMessage(RESERVATION_ALREADY_PAST.getMessage());
        }
    }

    @Nested
    @DisplayName("changeSchedule 메서드는")
    class ChangeScheduleTest {


        @Test
        @DisplayName("날짜 및 시간을 변경한다")
        void 성공() {
            // given
            Reservation saved = save(reservation(name, reservationDate1, reservationTime1, theme1));
            ReservationChangeCommand changeCommand = new ReservationChangeCommand(saved.getId(),
                name, reservationDate2.getId(), reservationTime2.getId());

            // when
            reservationService.changeSchedule(changeCommand);

            // then
            Reservation actual = reservationRepository.findById(saved.getId()).get();
            assertThat(actual.getDate()).isEqualTo(reservationDate2);
            assertThat(actual.getTime()).isEqualTo(reservationTime2);
            assertThat(actual.getStatus()).isEqualTo(ReservationStatus.RESERVED);
        }


        @Test
        @DisplayName("변경하려는 날짜 및 시간에 다른 예약이 있으면 대기로 변경한다")
        void 성공2() {
            // given
            String otherName = "다른 이용자";
            Reservation saved = save(reservation(name, reservationDate1, reservationTime1, theme1));
            save(reservation(otherName, reservationDate2, reservationTime2, theme1));
            ReservationChangeCommand changeCommand = new ReservationChangeCommand(saved.getId(),
                name, reservationDate2.getId(), reservationTime2.getId());

            // when
            reservationService.changeSchedule(changeCommand);

            // then
            Reservation actual = reservationRepository.findById(saved.getId()).get();
            assertThat(actual.getDate()).isEqualTo(reservationDate2);
            assertThat(actual.getTime()).isEqualTo(reservationTime2);
            assertThat(actual.getStatus()).isEqualTo(ReservationStatus.WAITING);
        }

        @Test
        @DisplayName("예약자가 아니면 예외가 발생한다")
        void 실패1() {
            // given
            Reservation saved = save(reservation(name, reservationDate1, reservationTime1, theme1));
            String notOwerName = "다른사람";
            ReservationChangeCommand changeCommand = new ReservationChangeCommand(saved.getId(),
                notOwerName, reservationDate2.getId(), reservationTime2.getId());

            // when & then
            assertThatThrownBy(() -> {
                reservationService.changeSchedule(changeCommand);
            }).isInstanceOf(ReservationException.class)
                .hasMessage(RESERVATION_NOT_OWNER.getMessage());
        }


        @Test
        @DisplayName("이미 취소된 예약이면 예외가 발생한다")
        void 실패2() {
            // given
            Reservation saved = save(reservation(name, reservationDate1, reservationTime1, theme1));
            saved.updateStatus(ReservationStatus.CANCELED);
            reservationRepository.saveAndFlush(saved);
            ReservationChangeCommand changeCommand = new ReservationChangeCommand(saved.getId(),
                name, reservationDate2.getId(), reservationTime2.getId());

            // when
            assertThatThrownBy(() -> reservationService.changeSchedule(changeCommand))
                .isInstanceOf(ReservationException.class)
                .hasMessage(RESERVATION_ALREADY_CANCELED.getMessage());
        }


        @Test
        @DisplayName("과거 예약이면 예외가 발생한다")
        void 실패3() {
            // given
            Reservation saved = savePastReservation();
            ReservationChangeCommand changeCommand = new ReservationChangeCommand(saved.getId(),
                name, reservationDate2.getId(), reservationTime2.getId());

            // when
            assertThatThrownBy(() -> {
                reservationService.changeSchedule(changeCommand);
            }).isInstanceOf(ReservationException.class)
                .hasMessage(RESERVATION_ALREADY_PAST.getMessage());
        }


        @Test
        @DisplayName("변경하려는 날짜가 과거이면 예외가 발생한다")
        void 실패4() {
            // given
            ReservationDate pastDate = savePastDate();
            Reservation saved = save(reservation(name, reservationDate1, reservationTime1, theme1));
            ReservationChangeCommand changeCommand = new ReservationChangeCommand(saved.getId(),
                name, pastDate.getId(), reservationTime2.getId());

            // when
            assertThatThrownBy(() -> {
                reservationService.changeSchedule(changeCommand);
            }).isInstanceOf(ReservationException.class)
                .hasMessage(RESERVATION_NEW_SCHEDULE_PAST_NOT_ALLOWED.getMessage());
        }


        @Test
        @DisplayName("변경하려는 날짜에 예약이 존재하면 예외가 발생한다")
        void 실패5() {
            // given
            Reservation saved = save(reservation(name, reservationDate1, reservationTime1, theme1));
            save(reservation(name, reservationDate2, reservationTime2, theme1));
            ReservationChangeCommand changeCommand = new ReservationChangeCommand(saved.getId(),
                name, reservationDate2.getId(), reservationTime2.getId());

            // when & then
            assertThatThrownBy(() ->
                reservationService.changeSchedule(changeCommand))
                .isInstanceOf(ReservationException.class)
                .hasMessage(RESERVATION_ALREADY_BOOKED.getMessage());
        }


        @Test
        @DisplayName("대기 상태인 예약이면 예외가 발생한다")
        void 실패6() {
            // given
            Reservation saved = save(
                waitReservation(name, reservationDate1, reservationTime1, theme1));
            ReservationChangeCommand changeCommand = new ReservationChangeCommand(saved.getId(),
                name, reservationDate2.getId(), reservationTime2.getId());

            // when & then
            assertThatThrownBy(() -> reservationService.changeSchedule(changeCommand))
                .isInstanceOf(ReservationException.class)
                .hasMessage(RESERVATION_ALREADY_WAITING.getMessage());
        }
    }

    @Nested
    @DisplayName("changeScheduleByManager 메서드는")
    class ChangeScheduleByManagerTest {


        @Test
        @DisplayName("날짜 및 시간을 변경한다")
        void 성공1() {
            // given
            Reservation saved = save(reservation(name, reservationDate1, reservationTime1, theme1));
            ReservationChangeCommand changeCommand = new ReservationChangeCommand(saved.getId(),
                null, reservationDate2.getId(), reservationTime2.getId());

            // when
            reservationService.changeScheduleByManager(changeCommand);

            // then
            Reservation actual = reservationRepository.findById(saved.getId()).get();
            assertThat(actual.getDate()).isEqualTo(reservationDate2);
            assertThat(actual.getTime()).isEqualTo(reservationTime2);
            assertThat(actual.getStatus()).isEqualTo(ReservationStatus.RESERVED);
        }

        @Test
        @DisplayName("변경하려는 날짜 및 시간에 다른 예약이 있으면 대기로 변경한다")
        void 성공2() {
            // given
            String otherName = "다른 이용자";
            Reservation saved = save(reservation(name, reservationDate1, reservationTime1, theme1));
            save(reservation(otherName, reservationDate2, reservationTime2, theme1));
            ReservationChangeCommand changeCommand = new ReservationChangeCommand(saved.getId(),
                null, reservationDate2.getId(), reservationTime2.getId());

            // when
            reservationService.changeScheduleByManager(changeCommand);
            Optional<Reservation> actual = reservationRepository.findById(saved.getId());

            // then
            assertThat(actual).isPresent()
                .get()
                .extracting("status")
                .isEqualTo(ReservationStatus.WAITING);
        }

        @Test
        @DisplayName("이미 취소된 예약이면 예외가 발생한다")
        void 실패1() {
            // given
            Reservation saved = save(reservation(name, reservationDate1, reservationTime1, theme1));
            saved.updateStatus(ReservationStatus.CANCELED);
            reservationRepository.saveAndFlush(saved);
            ReservationChangeCommand changeCommand = new ReservationChangeCommand(saved.getId(),
                null, reservationDate2.getId(), reservationTime2.getId());

            // when
            assertThatThrownBy(() -> reservationService.changeScheduleByManager(changeCommand))
                .isInstanceOf(ReservationException.class)
                .hasMessage(RESERVATION_ALREADY_CANCELED.getMessage());
        }


        @Test
        @DisplayName("과거로 날짜 및 시간을 변경하려 하면 예외가 발생한다")
        void 실패2() {
            // given
            Reservation saved = save(reservation(name, reservationDate1, reservationTime1, theme1));
            ReservationDate pastDate = savePastDate();
            ReservationTime pastTime = reservationTimeRepository.save(
                ReservationTimeFixture.time16());
            ReservationChangeCommand changeCommand = new ReservationChangeCommand(saved.getId(),
                null, pastDate.getId(), pastTime.getId());

            // when & then
            assertThatThrownBy(() ->
                reservationService.changeScheduleByManager(changeCommand))
                .isInstanceOf(ReservationException.class)
                .hasMessage(RESERVATION_NEW_SCHEDULE_PAST_NOT_ALLOWED.getMessage());
        }

        @Test
        @DisplayName("변경하려는 날짜 및 시간에 변경을 요청한 사용자의 예약이 있으면 예외가 발생한다")
        void 실패3() {
            // given
            Reservation saved = save(reservation(name, reservationDate1, reservationTime1, theme1));
            save(reservation(name, reservationDate2, reservationTime2, theme1));
            ReservationChangeCommand changeCommand = new ReservationChangeCommand(saved.getId(),
                null, reservationDate2.getId(), reservationTime2.getId());

            // when & then
            assertThatThrownBy(() ->
                reservationService.changeScheduleByManager(changeCommand))
                .isInstanceOf(ReservationException.class)
                .hasMessage(RESERVATION_ALREADY_BOOKED.getMessage());
        }


        @Test
        @DisplayName("대기 상태인 예약이면 예외가 발생한다")
        void 실패4() {
            // given
            Reservation saved = save(
                waitReservation(name, reservationDate1, reservationTime1, theme1));
            ReservationChangeCommand changeCommand = new ReservationChangeCommand(saved.getId(),
                name, reservationDate2.getId(), reservationTime2.getId());

            // when & then
            assertThatThrownBy(() -> reservationService.changeScheduleByManager(changeCommand))
                .isInstanceOf(ReservationException.class)
                .hasMessage(RESERVATION_ALREADY_WAITING.getMessage());
        }
    }
}
