package roomescape.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static roomescape.DateUtils.tomorrow;
import static roomescape.DateUtils.yesterday;
import static roomescape.DomainFixtures.JUNK_THEME;
import static roomescape.DomainFixtures.JUNK_TIME_SLOT;
import static roomescape.DomainFixtures.JUNK_USER;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import roomescape.domain.reservation.Reservation;
import roomescape.domain.reservation.ReservationRepository;
import roomescape.domain.theme.ThemeRepository;
import roomescape.domain.timeslot.TimeSlotRepository;
import roomescape.domain.user.UserRepository;
import roomescape.domain.waiting.Waiting;
import roomescape.domain.waiting.WaitingRepository;
import roomescape.exception.AlreadyExistedException;
import roomescape.exception.BusinessRuleViolationException;

@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class WaitingServiceTest {

    private WaitingService service;

    @Autowired
    private ReservationRepository reservationRepository;
    @Autowired
    private WaitingRepository waitingRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ThemeRepository themeRepository;
    @Autowired
    private TimeSlotRepository timeSlotRepository;


    @BeforeEach
    void setUp() {
        service =
                new WaitingService(reservationRepository, waitingRepository, timeSlotRepository, themeRepository,
                        userRepository);
        userRepository.save(JUNK_USER);
        themeRepository.save(JUNK_THEME);
        timeSlotRepository.save(JUNK_TIME_SLOT);
    }


    @Test
    @DisplayName("예약 대기를 추가할 수 있다.")
    void saveWaiting() {
        // given
        var date = tomorrow();
        var timeSlotId = JUNK_TIME_SLOT.id();
        var themeId = JUNK_THEME.id();

        // when
        Waiting reserved = service.saveWaiting(JUNK_USER, date, timeSlotId, themeId);

        // then
        var waitings = waitingRepository.findAll();
        assertThat(waitings).contains(reserved);
    }

    @Test
    @DisplayName("예약 대기를 삭제할 수 있다.")
    void removeById() {
        // given
        var date = tomorrow();
        var timeSlotId = JUNK_TIME_SLOT.id();
        var themeId = JUNK_THEME.id();
        Waiting reserved = service.saveWaiting(JUNK_USER, date, timeSlotId, themeId);

        // when
        service.removeById(reserved.id());

        // then
        var waitings = waitingRepository.findAll();
        assertThat(waitings).doesNotContain(reserved);
    }

    @Test
    @DisplayName("지나간 날짜와 시간에 대한 예약 대기 생성은 불가능하다.")
    void cannotSaveWaitingPastDateTime() {
        // given
        var date = yesterday();
        var timeSlotId = JUNK_TIME_SLOT.id();
        var themeId = JUNK_THEME.id();

        // when & then
        assertThatThrownBy(() -> service.saveWaiting(JUNK_USER, date, timeSlotId, themeId))
                .isInstanceOf(BusinessRuleViolationException.class);
    }

    @Test
    @DisplayName("지나가지 않은 날짜와 시간에 대한 예약 대기 생성은 가능하다.")
    void canSaveWaitingFutureDateTime() {
        // given
        var date = tomorrow();
        var timeSlotId = JUNK_TIME_SLOT.id();
        var themeId = JUNK_THEME.id();

        // when & then
        assertThatCode(() -> service.saveWaiting(JUNK_USER, date, timeSlotId, themeId))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("이미 해당 날짜, 시간, 테마에 대한 예약이 존재하는 경우 예약 대기는 불가능하다.")
    void cannotSaveWaitingWhenReservedAlreadyIdenticalDateTimeMultipleTimes() {
        // given
        var date = tomorrow();
        var timeSlotId = JUNK_TIME_SLOT.id();
        var themeId = JUNK_THEME.id();
        reservationRepository.save(Reservation.register(JUNK_USER, date, JUNK_TIME_SLOT, JUNK_THEME));

        // when & then
        assertThatThrownBy(() -> service.saveWaiting(JUNK_USER, date, timeSlotId, themeId))
                .isInstanceOf(BusinessRuleViolationException.class);
    }

    @Test
    @DisplayName("동일한 날짜, 시간, 테마에 대하여 중복 예약 대기 신청은 불가능하다.")
    void cannotSaveWaitingIdenticalDateTimeMultipleTimes() {
        // given
        var user = JUNK_USER;
        var date = tomorrow();
        var timeSlotId = JUNK_TIME_SLOT.id();
        var themeId = JUNK_THEME.id();

        // when
        service.saveWaiting(user, date, timeSlotId, themeId);

        // then
        assertThatThrownBy(() -> service.saveWaiting(user, date, timeSlotId, themeId))
                .isInstanceOf(AlreadyExistedException.class);
    }


    @Test
    @DisplayName("사용자의 예약 대기를 조회할 수 있다.")
    void findReservationsByUserId() {
        // given
        var date = tomorrow();
        var timeSlotId = JUNK_TIME_SLOT.id();
        var themeId = JUNK_THEME.id();
        Waiting savedWaiting = service.saveWaiting(JUNK_USER, date, timeSlotId, themeId);

        // when
        var waitings = service.findAllWaitings();

        // then
        assertThat(waitings).contains(savedWaiting);
    }
}
