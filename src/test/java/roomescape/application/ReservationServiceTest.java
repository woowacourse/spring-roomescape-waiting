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
import roomescape.TestRepositoryHelper;
import roomescape.domain.reservation.ReservationSearchFilter;
import roomescape.domain.reservation.ReservationStatus;
import roomescape.domain.reservation.ReservationWithOrder;
import roomescape.domain.theme.Theme;
import roomescape.domain.timeslot.TimeSlot;
import roomescape.domain.user.Email;
import roomescape.domain.user.Password;
import roomescape.domain.user.User;
import roomescape.domain.user.UserName;
import roomescape.exception.AlreadyExistedException;
import roomescape.exception.BusinessRuleViolationException;
import roomescape.exception.NotFoundException;

@DataJpaTest
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
        repositoryHelper.flushAndClear();
    }

    @Test
    @DisplayName("예약을 추가한다.")
    void reserve() {
        var reserved = service.reserve(user.id(), tomorrow(), timeSlot.id(), theme.id());

        var reservations = service.findAllReservations(NONE_FILTERING);
        assertThat(reservations).contains(reserved);
    }

    @Test
    @DisplayName("예약 대기를 건다.")
    void waitFor() {
        var waited = service.waitFor(user.id(), tomorrow(), timeSlot.id(), theme.id());

        var reservations = service.findAllReservations(NONE_FILTERING);
        assertAll(
            () -> assertThat(waited.status()).isEqualTo(ReservationStatus.WAITING),
            () -> assertThat(reservations).contains(waited)
        );
    }

    @Test
    @DisplayName("한 유저가 중복으로 예약 대기를 하려고 하면 예외가 발생한다.")
    void waitForDuplicates() {
        service.waitFor(user.id(), tomorrow(), timeSlot.id(), theme.id());

        assertThatThrownBy(() -> service.waitFor(user.id(), tomorrow(), timeSlot.id(), theme.id()))
            .isInstanceOf(AlreadyExistedException.class);
    }

    @Test
    @DisplayName("예약을 삭제한다.")
    void deleteReservation() {
        // given
        var reserved = service.reserve(user.id(), tomorrow(), timeSlot.id(), theme.id());

        // when
        service.removeByIdForce(reserved.id());

        // then
        var reservations = service.findAllReservations(NONE_FILTERING);
        assertThat(reservations).doesNotContain(reserved);
    }

    @Test
    @DisplayName("예약 대기를 취소한다.")
    void cancelWaiting() {
        // given
        User user2 = new User(new UserName("user2"), new Email("user2@email.com"), new Password("pw"));
        repositoryHelper.saveUser(user2);

        service.reserve(user.id(), tomorrow(), timeSlot.id(), theme.id());
        var waited = service.waitFor(user2.id(), tomorrow(), timeSlot.id(), theme.id());

        // when
        service.cancelWaiting(user2.id(), waited.id());

        // then
        var reservations = user.reservations();
        assertThat(reservations).doesNotContain(waited);
    }

    @Test
    @DisplayName("대기 상태가 아닌 예약은 취소할 수 없다.")
    void cancelWaitingThatIsNotWaiting() {
        var reserved = service.reserve(user.id(), tomorrow(), timeSlot.id(), theme.id());

        assertThatThrownBy(() -> service.cancelWaiting(user.id(), reserved.id()))
            .isInstanceOf(BusinessRuleViolationException.class);
    }

    @Test
    @DisplayName("다른 사용자의 예약 대기를 취소할 수 없다.")
    void cancelWaitingCanOnlyMine() {
        // given
        User anotherUser = new User(new UserName("user2"), new Email("user2@email.com"), new Password("pw"));
        repositoryHelper.saveUser(anotherUser);

        service.reserve(anotherUser.id(), tomorrow(), timeSlot.id(), theme.id());
        var waited = service.waitFor(user.id(), tomorrow(), timeSlot.id(), theme.id());

        // when & then
        assertThatThrownBy(() -> service.cancelWaiting(anotherUser.id(), waited.id()))
            .isInstanceOf(NotFoundException.class);
    }

    @Test
    @DisplayName("검색 필터로 예약을 조회한다.")
    void findAllReservationsWithFilter() {
        // given
        var afterOneDay = service.reserve(user.id(), tomorrow(), timeSlot.id(), theme.id());
        var afterTwoDay = service.reserve(user.id(), afterNDay(2), timeSlot.id(), theme.id());
        var afterThreeDay = service.reserve(user.id(), afterNDay(3), timeSlot.id(), theme.id());

        // when
        var fromYesterday_toToday = new ReservationSearchFilter(theme.id(), user.id(), yesterday(), today());
        var fromToday_toTomorrow = new ReservationSearchFilter(theme.id(), user.id(), today(), tomorrow());
        var fromTomorrow_toThreeDays = new ReservationSearchFilter(theme.id(), user.id(), tomorrow(), afterThreeDay.slot().date());

        // then
        assertAll(
                () -> assertThat(service.findAllReservations(fromYesterday_toToday)).isEmpty(),
                () -> assertThat(service.findAllReservations(fromToday_toTomorrow)).containsOnly(afterOneDay),
                () -> assertThat(service.findAllReservations(fromTomorrow_toThreeDays)).containsExactly(afterOneDay, afterTwoDay, afterThreeDay)
        );
    }

    @Test
    @DisplayName("모든 대기 예약을 조회한다.")
    void findAllWaitings() {
        // given
        var user1 = repositoryHelper.saveAnyUser();
        var user2 = repositoryHelper.saveAnyUser();
        var user3 = repositoryHelper.saveAnyUser();
        repositoryHelper.flushAndClear();

        var reserved = service.reserve(user1.id(), tomorrow(), timeSlot.id(), theme.id());
        var waited1 = service.waitFor(user2.id(), tomorrow(), timeSlot.id(), theme.id());
        var waited2 = service.waitFor(user3.id(), tomorrow(), timeSlot.id(), theme.id());

        // when
        var waitings = service.findAllWaitings();

        // then
        assertThat(waitings).containsOnly(
            new ReservationWithOrder(waited1, 1),
            new ReservationWithOrder(waited2, 2)
        );
    }

    @Test
    @DisplayName("지나간 날짜와 시간에 대한 예약 생성은 불가능하다.")
    void cannotReservePastDateTime() {
        assertThatThrownBy(() -> service.reserve(user.id(), yesterday(), timeSlot.id(), theme.id()))
                .isInstanceOf(BusinessRuleViolationException.class);
    }

    @Test
    @DisplayName("이미 해당 날짜, 시간, 테마에 대한 예약이 존재하는 경우 중복된 예약은 불가능하다.")
    void cannotReserveDuplicate() {
        // given
        service.reserve(user.id(), tomorrow(), timeSlot.id(), theme.id());

        // when & then
        assertThatThrownBy(
                () -> service.reserve(user.id(), tomorrow(), timeSlot.id(), theme.id())
        ).isInstanceOf(AlreadyExistedException.class);
    }
}
