package roomescape.reservation.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static roomescape.reservation.domain.ReservationStatus.CANCELED;
import static roomescape.reservation.domain.ReservationStatus.RESERVED;
import static roomescape.reservation.exception.ReservationErrorInformation.RESERVATION_ALREADY_CANCELED;
import static roomescape.reservation.exception.ReservationErrorInformation.RESERVATION_ALREADY_PAST;
import static roomescape.reservation.exception.ReservationErrorInformation.RESERVATION_ALREADY_WAITING;
import static roomescape.reservation.exception.ReservationErrorInformation.RESERVATION_DATE_IS_NULL;
import static roomescape.reservation.exception.ReservationErrorInformation.RESERVATION_ID_IS_NULL;
import static roomescape.reservation.exception.ReservationErrorInformation.RESERVATION_MEMBER_IS_NULL;
import static roomescape.reservation.exception.ReservationErrorInformation.RESERVATION_NEW_SCHEDULE_PAST_NOT_ALLOWED;
import static roomescape.reservation.exception.ReservationErrorInformation.RESERVATION_NOT_OWNER;
import static roomescape.reservation.exception.ReservationErrorInformation.RESERVATION_TIME_IS_NULL;

import java.time.LocalDate;
import java.time.LocalTime;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import roomescape.date.domain.ReservationDate;
import roomescape.date.fixture.ReservationDateFixture;
import roomescape.member.domain.Member;
import roomescape.member.domain.Role;
import roomescape.reservation.exception.ReservationException;
import roomescape.reservation.fixture.ReservationFixture;
import roomescape.theme.domain.Theme;
import roomescape.time.domain.ReservationTime;
import roomescape.time.fixture.ReservationTimeFixture;

class ReservationTest {

    private final String name = "한다";
    private final Member member = Member.load(1L, name, "password", Role.MEMBER);
    private final Member anotherMember = Member.load(2L, "다른사람", "password", Role.MEMBER);
    private final LocalDate date = LocalDate.now().plusMonths(1);
    private final ReservationDate reservationDate = ReservationDate.create(date);
    private final ReservationDate pastDate = ReservationDate.load(2L, LocalDate.now().minusDays(1),
        true);
    private final LocalTime startAt = LocalTime.of(15, 40);
    private final ReservationTime reservationTime = ReservationTime.create(startAt);
    private final Theme theme = Theme.load(1L, "테마", "설명", "썸네일", true);


    @Nested
    @DisplayName("validateMember 메서드는")
    class ValidateMemberTest {


        @Test
        @DisplayName("member가 null이면 예외가 발생한다")
        void 실패() {
            // given
            Member nullMember = null;

            // when & then
            assertThatThrownBy(
                () -> Reservation.reserved(nullMember, reservationDate, reservationTime, theme))
                .isInstanceOf(ReservationException.class)
                .hasMessage(RESERVATION_MEMBER_IS_NULL.getMessage());
        }
    }

    @Nested
    @DisplayName("validateTime 메서드는")
    class ValidateTimeTest {


        @Test
        @DisplayName("time이 null이면 예외가 발생한다")
        void 실패() {
            // given
            ReservationTime nullTime = null;

            // when & then
            assertThatThrownBy(
                () -> Reservation.reserved(member, reservationDate, nullTime, theme))
                .isInstanceOf(ReservationException.class)
                .hasMessage(RESERVATION_TIME_IS_NULL.getMessage());
        }
    }

    @Nested
    @DisplayName("validateDate 메서드는")
    class ValidateDateTest {


        @Test
        @DisplayName("date가 null이면 예외가 발생한다")
        void 실패() {
            // given
            ReservationDate nullDate = null;

            // when & then
            assertThatThrownBy(
                () -> Reservation.reserved(member, nullDate, reservationTime, theme))
                .isInstanceOf(ReservationException.class)
                .hasMessage(RESERVATION_DATE_IS_NULL.getMessage());
        }
    }

    @Nested
    @DisplayName("validateId 메서드는")
    class ValidateIdTest {


        @Test
        @DisplayName("id가 null이면 예외가 발생한다")
        void 실패() {
            // given
            Long nullId = null;

            // when & then
            assertThatThrownBy(
                () -> Reservation.load(nullId, member, reservationDate, reservationTime, theme,
                    RESERVED,
                    0L))
                .isInstanceOf(ReservationException.class)
                .hasMessage(RESERVATION_ID_IS_NULL.getMessage());
        }
    }

    @Nested
    @DisplayName("cancel 메서드는")
    class CancelTest {


        @Test
        @DisplayName("예약 상태를 취소로 변경한다")
        void 성공() {
            // given
            Reservation reserved = ReservationFixture.reservation(name, reservationDate,
                reservationTime, theme);

            // when
            reserved.cancel(reserved.getMember());

            // then
            assertThat(reserved.getStatus())
                .isEqualTo(CANCELED);
        }


        @Test
        @DisplayName("예약자가 아니면 예외가 발생한다")
        void 실패1() {
            // given
            Reservation reserved = ReservationFixture.reservation(name, reservationDate,
                reservationTime, theme);

            // when & then
            assertThatThrownBy(() -> reserved.cancel(anotherMember))
                .isInstanceOf(ReservationException.class)
                .hasMessage(RESERVATION_NOT_OWNER.getMessage());
        }


        @Test
        @DisplayName("이미 취소된 경우 예외가 발생한다")
        void 실패2() {
            // given
            Reservation reserved = ReservationFixture.reservation(name, reservationDate,
                reservationTime, theme);
            reserved.updateStatus(CANCELED);

            // when & then
            assertThatThrownBy(() -> reserved.cancel(reserved.getMember()))
                .isInstanceOf(ReservationException.class)
                .hasMessage(RESERVATION_ALREADY_CANCELED.getMessage());
        }


        @Test
        @DisplayName("과거 예약을 취소하려고 하면 예외가 발생한다")
        void 실패3() {
            // given
            Reservation reserved = Reservation.load(2L, member, pastDate, reservationTime, theme,
                RESERVED, 0L);

            // when & then
            assertThatThrownBy(() -> reserved.cancel(member))
                .isInstanceOf(ReservationException.class)
                .hasMessage(RESERVATION_ALREADY_PAST.getMessage());
        }
    }

    @Nested
    @DisplayName("changeSchedule 메서드는")
    class ChangeScheduleTest {


        @Test
        @DisplayName("예약 정보를 변경한다")
        void 성공() {
            // given
            Reservation reserved = ReservationFixture.reservation(name, reservationDate,
                reservationTime, theme);
            ReservationDate changedDate = ReservationDateFixture.activeOneWeekLater();
            ReservationTime changedTime = ReservationTimeFixture.activeTime15();

            // when
            reserved.changeSchedule(reserved.getMember(), changedDate, changedTime);

            // then
            assertThat(reserved.getDate())
                .usingRecursiveComparison()
                .isEqualTo(changedDate);
            assertThat(reserved.getTime())
                .usingRecursiveComparison()
                .isEqualTo(changedTime);
        }


        @Test
        @DisplayName("예약자가 아니면 예외가 발생한다")
        void 실패1() {
            // given
            Reservation reserved = ReservationFixture.reservation(name, reservationDate,
                reservationTime, theme);
            ReservationDate changedDate = ReservationDateFixture.activeOneWeekLater();

            // when && then
            Assertions.assertThatThrownBy(
                    () -> reserved.changeSchedule(anotherMember, changedDate, reservationTime))
                .isInstanceOf(ReservationException.class)
                .hasMessage(RESERVATION_NOT_OWNER.getMessage());
        }


        @Test
        @DisplayName("이미 취소된 예약이면 예외가 발생한다")
        void 실패2() {
            // given
            Reservation reserved = ReservationFixture.reservation(name, reservationDate,
                reservationTime, theme);
            reserved.updateStatus(CANCELED);
            ReservationDate changedDate = ReservationDateFixture.activeOneWeekLater();

            // when && then
            Assertions.assertThatThrownBy(
                    () -> reserved.changeSchedule(reserved.getMember(), changedDate, reservationTime))
                .isInstanceOf(ReservationException.class)
                .hasMessage(RESERVATION_ALREADY_CANCELED.getMessage());
        }


        @Test
        @DisplayName("과거 예약이면 예외가 발생한다")
        void 실패3() {
            // given
            Reservation reserved = Reservation.load(2L, member, pastDate, reservationTime, theme,
                RESERVED, 0L);
            ReservationDate changedDate = ReservationDateFixture.activeOneWeekLater();

            // when & then
            assertThatThrownBy(() -> reserved.changeSchedule(member, changedDate, reservationTime))
                .isInstanceOf(ReservationException.class)
                .hasMessage(RESERVATION_ALREADY_PAST.getMessage());
        }


        @Test
        @DisplayName("변경하려는 날짜가 과거이면 예외가 발생한다")
        void 실패4() {
            // given
            Reservation reserved = Reservation.load(2L, member, reservationDate, reservationTime,
                theme,
                RESERVED, 0L);

            // when & then
            assertThatThrownBy(() -> reserved.changeSchedule(member, pastDate, reservationTime))
                .isInstanceOf(ReservationException.class)
                .hasMessage(RESERVATION_NEW_SCHEDULE_PAST_NOT_ALLOWED.getMessage());
        }


        @Test
        @DisplayName("대기 상태이면 예외가 발생한다")
        void 실패5() {
            // given
            Reservation waiting = ReservationFixture.waitReservation(name, reservationDate,
                reservationTime, theme);
            ReservationDate changedDate = ReservationDateFixture.activeOneWeekLater();
            ReservationTime changedTime = ReservationTimeFixture.activeTime15();

            // when & then
            assertThatThrownBy(() -> waiting.changeSchedule(waiting.getMember(), changedDate, changedTime))
                .isInstanceOf(ReservationException.class)
                .hasMessage(RESERVATION_ALREADY_WAITING.getMessage());
        }
    }

    @Nested
    @DisplayName("changeScheduleByManager 메서드는")
    class ChangeScheduleByManagerTest {


        @Test
        @DisplayName("예약 정보를 변경한다")
        void 성공() {
            // given
            Reservation reserved = ReservationFixture.reservation(name, reservationDate,
                reservationTime, theme);
            ReservationDate changedDate = ReservationDateFixture.activeOneWeekLater();
            ReservationTime changedTime = ReservationTimeFixture.activeTime15();

            // when
            reserved.changeScheduleByManager(changedDate, changedTime);

            // then
            assertThat(reserved.getDate())
                .usingRecursiveComparison()
                .isEqualTo(changedDate);
            assertThat(reserved.getTime())
                .usingRecursiveComparison()
                .isEqualTo(changedTime);
        }


        @Test
        @DisplayName("과거 날짜로 변경하려고 하면 예외가 발생한다")
        void 실패1() {
            // given
            Reservation reserved = ReservationFixture.reservation(name, reservationDate,
                reservationTime, theme);
            ReservationDate yesterday = ReservationDate.load(2L, LocalDate.now().minusDays(1),
                true);
            ReservationTime time = ReservationTimeFixture.activeTime15();

            // when & then
            assertThatThrownBy(() -> reserved.changeScheduleByManager(yesterday, time))
                .isInstanceOf(ReservationException.class)
                .hasMessage(RESERVATION_NEW_SCHEDULE_PAST_NOT_ALLOWED.getMessage());
        }


        @Test
        @DisplayName("이미 취소된 예약이면 예외가 발생한다")
        void 실패2() {
            // given
            Reservation reserved = ReservationFixture.reservation(name, reservationDate,
                reservationTime, theme);
            reserved.updateStatus(CANCELED);
            ReservationDate changedDate = ReservationDateFixture.activeOneWeekLater();

            // when && then
            Assertions.assertThatThrownBy(
                    () -> reserved.changeScheduleByManager(changedDate, reservationTime))
                .isInstanceOf(ReservationException.class)
                .hasMessage(RESERVATION_ALREADY_CANCELED.getMessage());
        }


        @Test
        @DisplayName("대기 상태이면 예외가 발생한다")
        void 실패3() {
            // given
            Reservation waiting = ReservationFixture.waitReservation(name, reservationDate,
                reservationTime, theme);
            ReservationDate changedDate = ReservationDateFixture.activeOneWeekLater();
            ReservationTime changedTime = ReservationTimeFixture.activeTime15();

            // when & then
            assertThatThrownBy(() -> waiting.changeScheduleByManager(changedDate, changedTime))
                .isInstanceOf(ReservationException.class)
                .hasMessage(RESERVATION_ALREADY_WAITING.getMessage());
        }
    }
}
