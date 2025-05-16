package roomescape.domain;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import roomescape.exception.local.InvalidReservationTimeException;
import roomescape.test.fixture.ReservationFixture;
import roomescape.test.fixture.ReservationTimeFixture;
import roomescape.test.fixture.ThemeFixture;
import roomescape.test.fixture.UserFixture;

@DataJpaTest
class ReservationTest {

    @Autowired
    private TestEntityManager entityManager;

    private User savedMember;
    private Theme savedTheme;

    @BeforeEach
    void setUp() {
        // Create and persist test data
        User member = UserFixture.create(Role.ROLE_MEMBER, "member_dummyName", "member_dummyEmail",
                "member_dummyPassword");
        Theme theme = ThemeFixture.create("dummyName", "dummyDescription", "dummyThumbnail");

        savedMember = entityManager.persist(member);
        savedTheme = entityManager.persist(theme);

        entityManager.flush();
    }

    private Reservation createReservation(LocalDate date, ReservationTime time) {
        return ReservationFixture.createByBookedStatus(date, time, savedTheme, savedMember);
    }

    @Nested
    @DisplayName("예약 시점이 현재보다 과거이면 예외가 발생해야 한다.")
    class isPastTense {

        @DisplayName("예약 시점이 과거이면 예외가 발생한다.")
        @Test
        void isPastTense_throwsExceptionByPastTime() {
            // given
            LocalDate dummyPastDate = LocalDate.of(2024, 4, 25);
            LocalTime dummyTime = LocalTime.of(11, 13);
            ReservationTime reservationTime = ReservationTimeFixture.create(dummyTime);

            // when & then
            Assertions.assertThatThrownBy(
                    () -> createReservation(dummyPastDate, reservationTime)
            ).isInstanceOf(InvalidReservationTimeException.class);
        }

        @DisplayName("예약 시점이 미래이면 예외를 발생하지 않는다.")
        @Test
        void isPastTense_doesNotThrowExceptionByFutureTime() {
            // given
            LocalDateTime dummyFuture = LocalDateTime.now().plusDays(1);
            LocalDate dummyPastDate = dummyFuture.toLocalDate();
            LocalTime dummyTime = dummyFuture.toLocalTime();
            ReservationTime reservationTime = ReservationTimeFixture.create(dummyTime);

            // when & then
            Assertions.assertThatCode(
                    () -> createReservation(dummyPastDate, reservationTime)
            ).doesNotThrowAnyException();
        }

        @DisplayName("localDate만 다르고 localTime이 같으면 isSameDateTime에서 false이다.")
        @Test
        void isSameDateTime_false_bySameDateDifferenceTime() {
            // given
            LocalDateTime dummyDateTime1 = LocalDateTime.now().plusDays(1);
            ReservationTime duplicateReservationTime = ReservationTimeFixture.create(dummyDateTime1.toLocalTime());

            Reservation reservation1 = createReservation(dummyDateTime1.toLocalDate(),
                    duplicateReservationTime);

            LocalDateTime dummyDateTime2 = LocalDateTime.now().plusDays(2);
            Reservation reservation2 = createReservation(dummyDateTime2.toLocalDate(),
                    duplicateReservationTime);

            // when
            Assertions.assertThat(reservation1.isSameDateTime(reservation2)).isFalse();
        }

        @DisplayName("date와 reservationTime의 startAt 필드를 합쳐서 LocalDateTime 형식으로 반환할 수 있다")
        @Test
        void getDateTime() {
            // given
            LocalDate localDate = LocalDate.of(2023, 11, 2);
            LocalTime localTime = LocalTime.of(22, 33);

            LocalDateTime expected = LocalDateTime.of(localDate, localTime);

            ReservationTime reservationTime = ReservationTimeFixture.create(localTime);

            // when
            LocalDateTime actual = LocalDateTime.of(localDate, reservationTime.getStartAt());

            // then
            Assertions.assertThat(actual).isEqualTo(expected);
        }
    }
}
