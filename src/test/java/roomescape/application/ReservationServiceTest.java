package roomescape.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;
import static roomescape.DateUtils.afterNDay;
import static roomescape.DateUtils.today;
import static roomescape.DateUtils.tomorrow;
import static roomescape.DateUtils.yesterday;
import static roomescape.DomainFixtures.JUNK_THEME;
import static roomescape.DomainFixtures.JUNK_TIME_SLOT;
import static roomescape.DomainFixtures.JUNK_USER;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import roomescape.domain.Reservation;
import roomescape.domain.repository.ReservationSearchFilter;
import roomescape.infrastructure.fake.ReservationFakeRepository;
import roomescape.infrastructure.fake.ThemeFakeRepository;
import roomescape.infrastructure.fake.TimeSlotFakeRepository;

class ReservationServiceTest {

    private ReservationService service;

    @BeforeEach
    void setUp() {
        var reservationRepository = new ReservationFakeRepository();
        var timeSlotRepository = new TimeSlotFakeRepository();
        var themeRepository = new ThemeFakeRepository();
        timeSlotRepository.save(JUNK_TIME_SLOT);
        themeRepository.save(JUNK_THEME);

        service = new ReservationService(reservationRepository, timeSlotRepository, themeRepository);
    }

    @Test
    @DisplayName("예약을 추가할 수 있다.")
    void reserve() {
        // given
        var user = JUNK_USER;
        var date = tomorrow();
        var timeSlotId = JUNK_TIME_SLOT.id();
        var themeId = JUNK_THEME.id();

        // when
        Reservation reserved = service.reserve(user, date, timeSlotId, themeId);

        // then
        var reservations = service.findAllReservations();
        assertThat(reservations).contains(reserved);
    }

    @Test
    @DisplayName("예약을 삭제할 수 있다.")
    void deleteReservation() {
        // given
        var user = JUNK_USER;
        var date = tomorrow();
        var timeSlotId = JUNK_TIME_SLOT.id();
        var themeId = JUNK_THEME.id();
        var reserved = service.reserve(user, date, timeSlotId, themeId);

        // when
        boolean isRemoved = service.removeById(reserved.id());

        // then
        var reservations = service.findAllReservations();
        assertAll(
            () -> assertThat(isRemoved).isTrue(),
            () -> assertThat(reservations).doesNotContain(reserved)
        );
    }

    @Test
    @DisplayName("검색 필터로 예약을 조회할 수 있다.")
    void findAllReservationsWithFilter() {
        // given
        var afterOneDay = service.reserve(JUNK_USER, tomorrow(), JUNK_TIME_SLOT.id(), JUNK_THEME.id());
        var afterTwoDay = service.reserve(JUNK_USER, afterNDay(2), JUNK_TIME_SLOT.id(), JUNK_THEME.id());
        var afterThreeDay = service.reserve(JUNK_USER, afterNDay(3), JUNK_TIME_SLOT.id(), JUNK_THEME.id());

        // when
        var fromYesterday_toToday = new ReservationSearchFilter(JUNK_THEME.id(), JUNK_USER.id(), yesterday(), today());
        var fromToday_toTomorrow = new ReservationSearchFilter(JUNK_THEME.id(), JUNK_USER.id(), today(), tomorrow());
        var fromTomorrow_toThreeDays = new ReservationSearchFilter(JUNK_THEME.id(), JUNK_USER.id(), tomorrow(), afterThreeDay.date());

        assertAll(
            () -> assertThat(service.findAllReservations(fromYesterday_toToday)).isEmpty(),
            () -> assertThat(service.findAllReservations(fromToday_toTomorrow)).containsOnly(afterOneDay),
            () -> assertThat(service.findAllReservations(fromTomorrow_toThreeDays)).containsExactly(afterOneDay, afterTwoDay, afterThreeDay)
        );
    }

    @Test
    @DisplayName("지나간 날짜와 시간에 대한 예약 생성은 불가능하다.")
    void cannotReservePastDateTime() {
        // given
        var user = JUNK_USER;
        var date = yesterday();
        var timeSlotId = JUNK_TIME_SLOT.id();
        var themeId = JUNK_THEME.id();

        // when & then
        assertThatThrownBy(() -> service.reserve(user, date, timeSlotId, themeId))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("지나가지 않은 날짜와 시간에 대한 예약 생성은 가능하다.")
    void canReserveFutureDateTime() {
        // given
        var user = JUNK_USER;
        var date = tomorrow();
        var timeSlotId = JUNK_TIME_SLOT.id();
        var themeId = JUNK_THEME.id();

        // when & then
        assertThatCode(
            () -> service.reserve(user, date, timeSlotId, themeId)
        ).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("이미 예약된 날짜와 시간에 대한 예약 생성은 불가능하다.")
    void cannotReserveIdenticalDateTimeMultipleTimes() {
        // given
        var user = JUNK_USER;
        var date = tomorrow();
        var timeSlotId = JUNK_TIME_SLOT.id();
        var themeId = JUNK_THEME.id();

        // when
        service.reserve(user, date, timeSlotId, themeId);

        // then
        assertThatThrownBy(
            () -> service.reserve(user, date, timeSlotId, themeId)
        ).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("이미 해당 날짜, 시간, 테마에 대한 예약이 존재하는 경우 중복된 예약은 불가능하다.")
    void cannotReserveDuplicate() {
        // given
        var user = JUNK_USER;
        var date = tomorrow();
        var timeSlotId = JUNK_TIME_SLOT.id();
        var themeId = JUNK_THEME.id();

        service.reserve(user, date, timeSlotId, themeId);

        // when & then
        assertThatThrownBy(
            () -> service.reserve(user, date, timeSlotId, themeId)
        ).isInstanceOf(IllegalArgumentException.class);
    }
}
