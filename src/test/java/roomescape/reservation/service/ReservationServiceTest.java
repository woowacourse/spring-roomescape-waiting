package roomescape.reservation.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static roomescape.reservation.exception.ReservaitonErrorInformation.*;
import static roomescape.reservation.fixture.ReservationFixture.reservation;
import static roomescape.reservation.fixture.ReservationFixture.toCommand;
import static roomescape.theme.exception.ThemeErrorInformation.THEME_NOT_FOUND;
import static roomescape.time.exception.ReservationTimeErrorInformation.TIME_NOT_FOUND;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import roomescape.date.domain.ReservationDate;
import roomescape.date.fixture.FakeReservationDateRepository;
import roomescape.date.fixture.ReservationDateFixture;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationStatus;
import roomescape.reservation.exception.ReservationException;
import roomescape.reservation.fixture.FakeReservationRepository;
import roomescape.reservation.fixture.ReservationFixture;
import roomescape.reservation.service.dto.ReservationChangeCommand;
import roomescape.reservation.service.dto.ReservationSaveCommand;
import roomescape.theme.domain.Theme;
import roomescape.theme.exception.ThemeException;
import roomescape.theme.fixture.FakeThemeRepository;
import roomescape.theme.fixture.ThemeFixture;
import roomescape.time.domain.ReservationTime;
import roomescape.time.exception.ReservationTimeException;
import roomescape.time.fixture.FakeReservationTimeRepository;
import roomescape.time.fixture.ReservationTimeFixture;

class ReservationServiceTest {

    private final String name = "한다";
    private ReservationTime reservationTime1;
    private ReservationTime reservationTime2;
    private ReservationDate reservationDate1;
    private ReservationDate reservationDate2;
    private Theme theme1;
    private Theme theme2;

    private FakeReservationRepository reservationRepository;
    private FakeReservationTimeRepository reservationTimeRepository;
    private FakeReservationDateRepository reservationDateRepository;
    private FakeThemeRepository themeRepository;

    private ReservationService reservationService;

    @BeforeEach
    void setup() {
        reservationRepository = new FakeReservationRepository();
        reservationTimeRepository = new FakeReservationTimeRepository();
        reservationDateRepository = new FakeReservationDateRepository();
        themeRepository = new FakeThemeRepository();

        this.reservationService = new ReservationService(reservationRepository, reservationTimeRepository,
                reservationDateRepository, themeRepository);

        reservationTime1 = reservationTimeRepository.save(ReservationTimeFixture.time15());
        reservationTime2 = reservationTimeRepository.save(ReservationTimeFixture.time16());

        reservationDate1 = reservationDateRepository.save(ReservationDateFixture.oneWeekLater());
        reservationDate2 = reservationDateRepository.save(ReservationDateFixture.twoWeeksLater());

        theme1 = themeRepository.save(ThemeFixture.theme("테마1"));
        theme2 = themeRepository.save(ThemeFixture.theme("테마2"));
    }

    @Test
    @DisplayName("전체 예약 정보를 가져온다.")
    void readAll() {
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

    @Test
    @DisplayName("나의 예약들을 조회하면 날짜/시간 오름차순으로 정렬해 모두 조회한다.")
    void readAllByName() {
        // given
        List<Reservation> reservations = reservationRepository.saveAll(
                List.of(reservation(name, reservationDate1, reservationTime1, theme1),
                        reservation(name, reservationDate1, reservationTime2, theme1),
                        reservation(name, reservationDate2, reservationTime1, theme1),
                        reservation(name, reservationDate2, reservationTime2, theme1))
        );

        // when
        List<Reservation> actual = reservationService.readAllByName(name);

        // then
        Assertions.assertThat(actual)
                .usingRecursiveComparison()
                .isEqualTo(reservations);
    }

    @Test
    @DisplayName("예약을 추가한다.")
    void reserve() {
        //given & when
        List<Reservation> reservations = List.of();
        reservationService.reserve(name, ReservationFixture.toCommand(reservationDate1, reservationTime1, theme1));

        //then
        assertThat(reservationService.readAll())
                .hasSize(reservations.size() + 1);
    }

    @Test
    @DisplayName("예약시, 등록되지 않은 예약 시간이면 예외를 발생한다.")
    void reserve_does_not_exist_reservation_time() {
        // given
        Long wrongTimeId = Long.MIN_VALUE;
        ReservationSaveCommand command = ReservationFixture.toCommand(reservationDate1, wrongTimeId, theme1);

        // when & then
        assertThatThrownBy(() -> reservationService.reserve(name, command))
                .isInstanceOf(ReservationTimeException.class)
                .hasMessage(TIME_NOT_FOUND.getMessage());
    }


    @Test
    @DisplayName("예약시, 등록되지 않은 테마이면 예외를 발생한다.")
    void reserve_does_not_exist_theme() {
        // given
        Long wrongThemeId = Long.MIN_VALUE;
        ReservationSaveCommand command = toCommand(reservationDate1, reservationTime1, wrongThemeId);

        // when & then
        assertThatThrownBy(() -> reservationService.reserve(name, command))
                .isInstanceOf(ThemeException.class)
                .hasMessage(THEME_NOT_FOUND.getMessage());
    }

    @Test
    @DisplayName("예약된 날짜/시간/테마를 중복 예약하면 대기 예약으로 들어간다.")
    void reserved_duplicated() {
        // given
        Reservation reservation = reservation(name, reservationDate1, reservationTime1, theme1);
        ReservationSaveCommand duplicated = ReservationFixture.toCommand(reservationDate1, reservationTime1, theme1);
        save(reservation);

        // when
        Reservation actual = reservationService.reserve(name, duplicated);

        // then
        assertThat(actual.getStatus())
            .isEqualTo(ReservationStatus.WAITING);
    }

    @Test
    @DisplayName("취소된 예약을 동일한 사람이 새롭게 예약할 수 있다.")
    void reserved_when_cancel_same_name() {
        // given
        Reservation reservation = save(reservation(name, reservationDate1, reservationTime1, theme1));
        ReservationSaveCommand duplicated = ReservationFixture.toCommand(reservationDate1, reservationTime1, theme1);
        cancelByManager(reservation);

        // when
        Reservation actual = reservationService.reserve(name, duplicated);

        // then
        Assertions.assertThat(actual.getStatus())
                .isEqualTo(ReservationStatus.RESERVED);
    }

    @Test
    @DisplayName("취소된 예약을 다른 사람이 새롭게 예약할 수 있다.")
    void reserved_when_cancel_another_name() {
        // given
        String anotherName = "다른사람";
        Reservation reservation = save(reservation(name, reservationDate1, reservationTime1, theme1));
        ReservationSaveCommand duplicated = ReservationFixture.toCommand(reservationDate1, reservationTime1, theme1);
        cancelByManager(reservation);

        // when
        Reservation actual = reservationService.reserve(anotherName, duplicated);

        // then
        Assertions.assertThat(actual.getStatus())
                .isEqualTo(ReservationStatus.RESERVED);
    }

    @Test
    @DisplayName("관리자 전용으로 예약을 취소하면 CANCELED 상태가 된다.")
    void cancelByManager() {
        // given
        Reservation savedReservation = save(reservation(name, reservationDate1, reservationTime1, theme1));

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
        Reservation savedReservation = save(reservation(name, reservationDate1, reservationTime1, theme1));

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
        Reservation saved = save(reservation(name, reservationDate1, reservationTime1, theme1));
        String anotherName = "다른사람";
        Long savedId = saved.getId();

        // when & then
        assertThatThrownBy(() -> reservationService.cancel(savedId, anotherName))
                .isInstanceOf(ReservationException.class)
                .hasMessage(RESERVATION_NOT_OWNER.getMessage());
    }

    @Test
    @DisplayName("이미 취소된 예약을 취소하면 예외가 발생한다.")
    void cancel_already_canceled() {
        // given
        Reservation saved = save(reservation(name, reservationDate1, reservationTime1, theme1));
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
    void cancel_not_past() {
        // given
        ReservationDate pastDate = ReservationDate.load(1L, LocalDate.now().minusDays(1), true);
        Reservation saved =
                save(Reservation.load(1L, name, pastDate, reservationTime1, theme1, ReservationStatus.RESERVED, LocalDateTime.now()));
        Long savedId = saved.getId();

        // when & then
        Assertions.assertThatThrownBy(() -> reservationService.cancel(savedId, name))
                .isInstanceOf(ReservationException.class)
                .hasMessage(RESERVATION_ALREADY_PAST.getMessage());
    }

    @Test
    @DisplayName("예약 가능한 날짜로 변경할 수 있다.")
    void changeSchedule() {
        // given
        Reservation saved = save(reservation(name, reservationDate1, reservationTime1, theme1));
        ReservationChangeCommand changeCommand = new ReservationChangeCommand(saved.getId(), name, reservationDate2.getId(), reservationTime2.getId());

        // when
        reservationService.changeSchedule(changeCommand);

        // then
        Assertions.assertThat(reservationRepository.findById(saved.getId()))
                .contains(saved);
    }

    @Test
    @DisplayName("본인의 예약이 아닌데 변경을 시도하면 예외가 발생한다.")
    void changeSchedule_not_owner() {
        // given
        Reservation saved = save(reservation(name, reservationDate1, reservationTime1, theme1));
        String notOwerName = "다른사람";
        ReservationChangeCommand changeCommand = new ReservationChangeCommand(saved.getId(), notOwerName, reservationDate2.getId(), reservationTime2.getId());

        // when & then
        assertThatThrownBy(() -> {
                    reservationService.changeSchedule(changeCommand);
                }).isInstanceOf(ReservationException.class)
                .hasMessage(RESERVATION_NOT_OWNER.getMessage());
    }

    @Test
    @DisplayName("이미 취소된 예약을 변경하면 예외가 발생한다.")
    void changeSchedule_already_canceled() {
        // given
        Reservation saved = save(reservation(name, reservationDate1, reservationTime1, theme1));
        saved.updateStatus(ReservationStatus.CANCELED);
        reservationRepository.updateStatus(saved);
        ReservationChangeCommand changeCommand = new ReservationChangeCommand(saved.getId(), name, reservationDate2.getId(), reservationTime2.getId());

        // when
        assertThatThrownBy(() -> reservationService.changeSchedule(changeCommand))
                .isInstanceOf(ReservationException.class)
                .hasMessage(RESERVATION_ALREADY_CANCELED.getMessage());
    }

    @Test
    @DisplayName("이미 지난 예약을 변경하면 예외가 발생한다.")
    void changeSchedule_past() {
        // given
        ReservationDate pastDate = ReservationDate.load(1L, LocalDate.now().minusDays(1), true);
        Reservation saved =
                save(Reservation.load(1L, name, pastDate, reservationTime1, theme1, ReservationStatus.RESERVED, LocalDateTime.now()));
        ReservationChangeCommand changeCommand = new ReservationChangeCommand(saved.getId(), name, reservationDate2.getId(), reservationTime2.getId());

        // when
        assertThatThrownBy(() -> {
                    reservationService.changeSchedule(changeCommand);
                }).isInstanceOf(ReservationException.class)
                .hasMessage(RESERVATION_ALREADY_PAST.getMessage());
    }

    @Test
    @DisplayName("지난 날짜/시간으로 예약을 변경하면 예외가 발생한다.")
    void changeSchedule_new_datetime_is_past() {
        // given
        ReservationDate pastDate = reservationDateRepository.save(ReservationDate.load(20L, LocalDate.now().minusDays(1), true));
        Reservation saved = save(reservation(name, reservationDate1, reservationTime1, theme1));
        ReservationChangeCommand changeCommand = new ReservationChangeCommand(saved.getId(), name, pastDate.getId(), reservationTime2.getId());

        // when
        assertThatThrownBy(() -> {
                    reservationService.changeSchedule(changeCommand);
                }).isInstanceOf(ReservationException.class)
                .hasMessage(RESERVATION_NEW_SCHEDULE_PAST_NOT_ALLOWED.getMessage());
    }

    @Test
    @DisplayName("일반유저가 이미 존재하는 날짜/시간으로 예약을 변경하면 예외가 발생한다.")
    @Disabled
    void changeSchedule_duplicated() {
        // given
        Reservation saved = save(reservation(name, reservationDate1, reservationTime1, theme1));
        save(reservation(name, reservationDate2, reservationTime2, theme1));
        ReservationChangeCommand changeCommand = new ReservationChangeCommand(saved.getId(), name, reservationDate2.getId(), reservationTime2.getId());

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
        Reservation saved = save(reservation(name, reservationDate1, reservationTime1, theme1));
        ReservationChangeCommand changeCommand = new ReservationChangeCommand(saved.getId(), null, reservationDate2.getId(), reservationTime2.getId());

        // when
        reservationService.changeScheduleByManager(changeCommand);

        // then
        Assertions.assertThat(reservationRepository.findById(saved.getId()))
                .contains(saved);
    }

    @Test
    @DisplayName("이미 취소된 예약을 변경하면 예외가 발생한다.")
    void changeScheduleByManager_already_canceled() {
        // given
        Reservation saved = save(reservation(name, reservationDate1, reservationTime1, theme1));
        saved.updateStatus(ReservationStatus.CANCELED);
        reservationRepository.updateStatus(saved);
        ReservationChangeCommand changeCommand = new ReservationChangeCommand(saved.getId(), null, reservationDate2.getId(), reservationTime2.getId());

        // when
        assertThatThrownBy(() -> reservationService.changeScheduleByManager(changeCommand))
                .isInstanceOf(ReservationException.class)
                .hasMessage(RESERVATION_ALREADY_CANCELED.getMessage());
    }

    @Test
    @DisplayName("관리자가 예약을 과거의 날짜/시간으로 변경하면 예외가 발생한다.")
    void changeScheduleByManager_pastDateTime() {
        // given
        Reservation saved = save(reservation(name, reservationDate1, reservationTime1, theme1));
        ReservationDate pastDate = reservationDateRepository.save(ReservationDate.load(1L, LocalDate.now().minusDays(1), true));
        ReservationTime pastTime = reservationTimeRepository.save(ReservationTimeFixture.time16());
        ReservationChangeCommand changeCommand = new ReservationChangeCommand(saved.getId(), null, pastDate.getId(), pastTime.getId());

        // when & then
        assertThatThrownBy(() ->
                        reservationService.changeScheduleByManager(changeCommand))
                .isInstanceOf(ReservationException.class)
                .hasMessage(RESERVATION_NEW_SCHEDULE_PAST_NOT_ALLOWED.getMessage());
    }

    @Test
    @DisplayName("관리자가 이미 존재하는 날짜/시간으로 예약을 변경하면 예외가 발생한다.")
    @Disabled
    void changeScheduleByManager_duplicated() {
        // given
        Reservation saved = save(reservation(name, reservationDate1, reservationTime1, theme1));
        save(reservation(name, reservationDate2, reservationTime2, theme1));
        ReservationChangeCommand changeCommand = new ReservationChangeCommand(saved.getId(), null, reservationDate2.getId(), reservationTime2.getId());

        // when & then
        assertThatThrownBy(() ->
                reservationService.changeScheduleByManager(changeCommand))
                .isInstanceOf(ReservationException.class)
                .hasMessage(RESERVATION_ALREADY_BOOKED.getMessage());
    }

    private Reservation save(Reservation reservation) {
        return reservationRepository.save(reservation);
    }

    private void cancelByManager(Reservation reservation) {
        reservationService.cancelByManager(reservation.getId());
    }

}
