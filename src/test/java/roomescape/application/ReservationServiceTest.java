package roomescape.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;
import static roomescape.DateUtils.afterNDay;
import static roomescape.DateUtils.today;
import static roomescape.DateUtils.tomorrow;
import static roomescape.DateUtils.yesterday;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import roomescape.TestRepositoryHelper;
import roomescape.domain.reservation.ReservationSearchFilter;
import roomescape.domain.theme.Theme;
import roomescape.domain.timeslot.TimeSlot;
import roomescape.domain.user.User;
import roomescape.exception.AlreadyExistedException;
import roomescape.exception.BusinessRuleViolationException;

@DataJpaTest
@ActiveProfiles("test")
@Import({ReservationService.class, TestRepositoryHelper.class})
class ReservationServiceTest {

    private static final ReservationSearchFilter NONE_FILTERING = new ReservationSearchFilter(null, null, null, null);

    @Autowired
    private ReservationService service;
    @Autowired
    private TestRepositoryHelper repositoryHelper;

    private User user;
    private TimeSlot timeSlot;
    private Theme theme;

    @BeforeEach
    void setUp() {
        user = repositoryHelper.saveAnyUser();
        timeSlot = repositoryHelper.saveAnyTimeSlot();
        theme = repositoryHelper.saveAnyTheme();
    }

    @Test
    @DisplayName("예약을 추가할 수 있다.")
    void reserve() {
        var reserved = service.reserve(user, tomorrow(), timeSlot.id(), theme.id());

        var reservations = service.findAllReservations(NONE_FILTERING);
        assertThat(reservations).contains(reserved);
    }

    @Test
    @DisplayName("예약을 삭제할 수 있다.")
    void deleteReservation() {
        // given
        var reserved = service.reserve(user, tomorrow(), timeSlot.id(), theme.id());

        // when
        service.removeById(reserved.id());

        // then
        var reservations = service.findAllReservations(NONE_FILTERING);
        assertThat(reservations).doesNotContain(reserved);
    }

    @Test
    @DisplayName("검색 필터로 예약을 조회할 수 있다.")
    void findAllReservationsWithFilter() {
        // given
        var afterOneDay = service.reserve(user, tomorrow(), timeSlot.id(), theme.id());
        var afterTwoDay = service.reserve(user, afterNDay(2), timeSlot.id(), theme.id());
        var afterThreeDay = service.reserve(user, afterNDay(3), timeSlot.id(), theme.id());

        // when
        var fromYesterday_toToday = new ReservationSearchFilter(theme.id(), user.id(), yesterday(), today());
        var fromToday_toTomorrow = new ReservationSearchFilter(theme.id(), user.id(), today(), tomorrow());
        var fromTomorrow_toThreeDays = new ReservationSearchFilter(theme.id(), user.id(), tomorrow(), afterThreeDay.dateTime().date());

        assertAll(
                () -> assertThat(service.findAllReservations(fromYesterday_toToday)).isEmpty(),
                () -> assertThat(service.findAllReservations(fromToday_toTomorrow)).containsOnly(afterOneDay),
                () -> assertThat(service.findAllReservations(fromTomorrow_toThreeDays)).containsExactly(afterOneDay, afterTwoDay, afterThreeDay)
        );
    }

    @Test
    @DisplayName("지나간 날짜와 시간에 대한 예약 생성은 불가능하다.")
    void cannotReservePastDateTime() {
        assertThatThrownBy(() -> service.reserve(user, yesterday(), timeSlot.id(), theme.id()))
                .isInstanceOf(BusinessRuleViolationException.class);
    }

    @Test
    @DisplayName("이미 해당 날짜, 시간, 테마에 대한 예약이 존재하는 경우 중복된 예약은 불가능하다.")
    void cannotReserveDuplicate() {
        // given
        service.reserve(user, tomorrow(), timeSlot.id(), theme.id());

        // when & then
        assertThatThrownBy(
                () -> service.reserve(user, tomorrow(), timeSlot.id(), theme.id())
        ).isInstanceOf(AlreadyExistedException.class);
    }
}
