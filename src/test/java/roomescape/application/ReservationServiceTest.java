package roomescape.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;
import static roomescape.DomainFixtures.JUNK_THEME;
import static roomescape.DomainFixtures.JUNK_TIME_SLOT;
import static roomescape.DomainFixtures.JUNK_USER;

import java.time.LocalDate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import roomescape.domain.reservation.Reservation;
import roomescape.domain.reservation.ReservationRepository;
import roomescape.domain.reservation.ReservationSearchFilter;
import roomescape.domain.theme.ThemeRepository;
import roomescape.domain.timeslot.TimeSlotRepository;
import roomescape.domain.user.UserRepository;
import roomescape.exception.AlreadyExistedException;
import roomescape.exception.BusinessRuleViolationException;

@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class ReservationServiceTest {

    @Autowired
    private ReservationService service;

    @Autowired
    private ReservationRepository reservationRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ThemeRepository themeRepository;
    @Autowired
    private TimeSlotRepository timeSlotRepository;

    @BeforeEach
    void setUp() {
        userRepository.save(JUNK_USER);
        themeRepository.save(JUNK_THEME);
        timeSlotRepository.save(JUNK_TIME_SLOT);
    }

    @Test
    @DisplayName("예약을 추가할 수 있다.")
    void saveReservation() {
        // given
        var tomorrow = LocalDate.now().plusDays(1);
        var timeSlotId = 1L;
        var themeId = 1L;
        var userId = 1L;

        // when
        Reservation reserved = service.saveReservation(userId, tomorrow, timeSlotId, themeId);

        // then
        var reservations = reservationRepository.findAll();
        assertThat(reservations).contains(reserved);
    }

    @Test
    @DisplayName("예약을 삭제할 수 있다.")
    void deleteReservation() {
        // given
        var tomorrow = LocalDate.now().plusDays(1);
        var timeSlotId = JUNK_TIME_SLOT.id();
        var themeId = JUNK_THEME.id();
        var reserved = service.saveReservation(JUNK_USER.id(), tomorrow, timeSlotId, themeId);

        // when
        service.removeById(reserved.id());

        // then
        var reservations = reservationRepository.findAll();
        assertThat(reservations).doesNotContain(reserved);
    }

    @Test
    @DisplayName("검색 필터로 예약을 조회할 수 있다.")
    void findReservationsByFilterWithFilter() {
        // given
        var tomorrow = LocalDate.now().plusDays(1);
        var afterOneDay = service.saveReservation(JUNK_USER.id(), tomorrow, JUNK_TIME_SLOT.id(), JUNK_THEME.id());
        var afterTwoDay = service.saveReservation(JUNK_USER.id(), LocalDate.now().plusDays(2), JUNK_TIME_SLOT.id(),
                JUNK_THEME.id());
        var afterThreeDay = service.saveReservation(JUNK_USER.id(), LocalDate.now().plusDays(3), JUNK_TIME_SLOT.id(),
                JUNK_THEME.id());

        // when
        var fromYesterday_toToday = new ReservationSearchFilter(JUNK_THEME.id(), JUNK_USER.id(),
                LocalDate.now().minusDays(1),
                LocalDate.now());
        var fromToday_toTomorrow = new ReservationSearchFilter(JUNK_THEME.id(), JUNK_USER.id(), LocalDate.now(),
                tomorrow);
        var fromTomorrow_toThreeDays = new ReservationSearchFilter(JUNK_THEME.id(), JUNK_USER.id(), tomorrow,
                afterThreeDay.date());

        assertAll(
                () -> assertThat(service.findReservationsByFilter(fromYesterday_toToday)).isEmpty(),
                () -> assertThat(service.findReservationsByFilter(fromToday_toTomorrow)).containsOnly(afterOneDay),
                () -> assertThat(service.findReservationsByFilter(fromTomorrow_toThreeDays)).containsExactly(
                        afterOneDay,
                        afterTwoDay, afterThreeDay)
        );
    }

    @Test
    @DisplayName("지나간 날짜와 시간에 대한 예약 생성은 불가능하다.")
    void cannotSaveReservationPastDateTime() {
        // given
        var yesterday = LocalDate.now().minusDays(1);
        var timeSlotId = JUNK_TIME_SLOT.id();
        var themeId = JUNK_THEME.id();

        // when & then
        assertThatThrownBy(() -> service.saveReservation(JUNK_USER.id(), yesterday, timeSlotId, themeId))
                .isInstanceOf(BusinessRuleViolationException.class);
    }

    @Test
    @DisplayName("지나가지 않은 날짜와 시간에 대한 예약 생성은 가능하다.")
    void canSaveReservationFutureDateTime() {
        // given
        var tomorrow = LocalDate.now().plusDays(1);
        var timeSlotId = JUNK_TIME_SLOT.id();
        var themeId = JUNK_THEME.id();

        // when & then
        assertThatCode(() -> service.saveReservation(JUNK_USER.id(), tomorrow, timeSlotId, themeId))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("이미 예약된 날짜와 시간에 대한 예약 생성은 불가능하다.")
    void cannotSaveReservationIdenticalDateTimeMultipleTimes() {
        // given
        var user = JUNK_USER;
        var tomorrow = LocalDate.now().plusDays(1);
        var timeSlotId = JUNK_TIME_SLOT.id();
        var themeId = JUNK_THEME.id();

        // when
        service.saveReservation(user.id(), tomorrow, timeSlotId, themeId);

        // then
        assertThatThrownBy(() -> service.saveReservation(user.id(), tomorrow, timeSlotId, themeId))
                .isInstanceOf(AlreadyExistedException.class);
    }

    @Test
    @DisplayName("이미 해당 날짜, 시간, 테마에 대한 예약이 존재하는 경우 중복된 예약은 불가능하다.")
    void cannotSaveReservationDuplicate() {
        // given
        var user = JUNK_USER;
        var tomorrow = LocalDate.now().plusDays(1);
        var timeSlotId = JUNK_TIME_SLOT.id();
        var themeId = JUNK_THEME.id();

        service.saveReservation(user.id(), tomorrow, timeSlotId, themeId);

        // when & then
        assertThatThrownBy(() -> service.saveReservation(user.id(), tomorrow, timeSlotId, themeId))
                .isInstanceOf(AlreadyExistedException.class);
    }

    @Test
    @DisplayName("사용자의 예약을 조회할 수 있다.")
    void findReservationsByUserId() {
        // given
        var savedTimeSlot = timeSlotRepository.save(JUNK_TIME_SLOT);
        var savedTheme = themeRepository.save(JUNK_THEME);
        var createdUser = userRepository.save(JUNK_USER);
        var tomorrow = LocalDate.now().plusDays(1);
        var savedReservation = reservationRepository.save(
                Reservation.register(createdUser, tomorrow, savedTimeSlot, savedTheme));

        // when
        var reservations = service.findReservationsByUserId(createdUser.id());

        // then
        assertThat(reservations).contains(savedReservation);
    }
}
