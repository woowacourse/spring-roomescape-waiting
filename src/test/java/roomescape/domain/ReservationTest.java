package roomescape.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import java.time.LocalTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import roomescape.exception.BusinessException;
import roomescape.exception.ErrorCode;

class ReservationTest {

    private ReservationTime futureTime;
    private Theme theme;

    @BeforeEach
    void setUp() {
        futureTime = ReservationTime.createWithId(1L, LocalTime.now().plusHours(1), LocalTime.now().plusHours(2));
        theme = Theme.createWithId(1L, "테마", "테스트용 테마 설명입니다.", "https://img.com");
    }

    @Nested
    class 생성 {

        @Test
        void 성공() {
            // given
            String name = "검프";
            LocalDate date = LocalDate.now().plusDays(1);

            // when
            Reservation reservation = Reservation.create(name, date, futureTime, theme);

            // then
            assertThat(reservation.getId()).isNull();
            assertThat(reservation.getName()).isEqualTo(name);
            assertThat(reservation.getReservationDate().getDate()).isEqualTo(date);
            assertThat(reservation.getTime()).isEqualTo(futureTime);
            assertThat(reservation.getTheme()).isEqualTo(theme);
        }

        @Test
        void withId로_id_부여() {
            // given
            String name = "검프";
            Reservation reservation = Reservation.create(name, LocalDate.now().plusDays(1), futureTime, theme);

            // when
            Reservation withId = reservation.withId(10L);

            // then
            assertThat(withId.getId()).isEqualTo(10L);
            assertThat(withId.getName()).isEqualTo(name);
        }

        @Test
        void withId에_null_id_전달시_예외발생() {
            // given
            Reservation reservation = Reservation.create("검프", LocalDate.now().plusDays(1), futureTime, theme);

            // when & then
            assertThatThrownBy(() -> reservation.withId(null))
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.RESERVATION_ID_NULL);
        }
    }

    @Nested
    class modify {

        @Test
        void 날짜_시간_테마_변경() {
            // given
            String name = "검프";
            Reservation reservation = Reservation.createWithId(1L, name, LocalDate.now().plusDays(1), futureTime, theme);
            LocalDate newDate = LocalDate.now().plusDays(3);
            ReservationTime newTime = ReservationTime.createWithId(2L, LocalTime.of(14, 0), LocalTime.of(15, 0));
            Theme newTheme = Theme.createWithId(2L, "새테마", "새로운 테마 설명입니다.", "https://new.com");

            // when
            Reservation modified = reservation.modify(newDate, newTime, newTheme);

            // then
            assertThat(modified.getId()).isEqualTo(1L);
            assertThat(modified.getName()).isEqualTo(name);
            assertThat(modified.getReservationDate().getDate()).isEqualTo(newDate);
            assertThat(modified.getTime()).isEqualTo(newTime);
            assertThat(modified.getTheme()).isEqualTo(newTheme);
        }
    }

    @Nested
    class validateNotPast {

        @Test
        void 미래_날짜면_통과() {
            String name = "검프";
            Reservation reservation = Reservation.create(name, LocalDate.now().plusDays(1), futureTime, theme);
            reservation.validateNotPast();
        }

        @Test
        void 오늘_미래_시간이면_통과() {
            String name = "검프";
            Reservation reservation = Reservation.create(name, LocalDate.now(), futureTime, theme);
            reservation.validateNotPast();
        }

        @Test
        void 과거_날짜면_예외발생() {
            String name = "검프";
            Reservation reservation = Reservation.create(name, LocalDate.now().minusDays(1), futureTime, theme);

            assertThatThrownBy(reservation::validateNotPast)
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.DATE_ALREADY_PASSED);
        }

        @Test
        void 오늘_과거_시간이면_예외발생() {
            String name = "검프";
            ReservationTime pastTime = ReservationTime.createWithId(2L, LocalTime.now().minusHours(1), LocalTime.now());
            Reservation reservation = Reservation.create(name, LocalDate.now(), pastTime, theme);

            assertThatThrownBy(reservation::validateNotPast)
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.TIME_ALREADY_PASSED);
        }
    }

    @Nested
    class isSameSlot {

        @Test
        void 날짜_시간_테마_모두_같으면_true() {
            // given
            LocalDate date = LocalDate.now().plusDays(1);
            Reservation a = Reservation.createWithId(1L, "검프", date, futureTime, theme);
            Reservation b = Reservation.createWithId(2L, "리오", date, futureTime, theme);

            // when & then
            assertThat(a.isSameSlot(b)).isTrue();
        }

        @Test
        void 날짜가_다르면_false() {
            // given
            Reservation a = Reservation.createWithId(1L, "검프", LocalDate.now().plusDays(1), futureTime, theme);
            Reservation b = Reservation.createWithId(2L, "검프", LocalDate.now().plusDays(2), futureTime, theme);

            // when & then
            assertThat(a.isSameSlot(b)).isFalse();
        }

        @Test
        void 시간이_다르면_false() {
            // given
            LocalDate date = LocalDate.now().plusDays(1);
            ReservationTime otherTime = ReservationTime.createWithId(2L, LocalTime.of(14, 0), LocalTime.of(15, 0));
            Reservation a = Reservation.createWithId(1L, "검프", date, futureTime, theme);
            Reservation b = Reservation.createWithId(2L, "검프", date, otherTime, theme);

            // when & then
            assertThat(a.isSameSlot(b)).isFalse();
        }

        @Test
        void 테마가_다르면_false() {
            // given
            LocalDate date = LocalDate.now().plusDays(1);
            Theme otherTheme = Theme.createWithId(2L, "다른테마", "다른 테마 설명입니다.", "https://other.com");
            Reservation a = Reservation.createWithId(1L, "검프", date, futureTime, theme);
            Reservation b = Reservation.createWithId(2L, "검프", date, futureTime, otherTheme);

            // when & then
            assertThat(a.isSameSlot(b)).isFalse();
        }
    }

    @Nested
    class validateOwner {

        @Test
        void 이름_일치시_통과() {
            String name = "검프";
            Reservation reservation = Reservation.create(name, LocalDate.now().plusDays(1), futureTime, theme);
            reservation.validateOwner(name);
        }

        @Test
        void 이름_불일치시_예외발생() {
            String ownerName = "검프";
            String otherName = "거위";
            Reservation reservation = Reservation.create(ownerName, LocalDate.now().plusDays(1), futureTime, theme);

            assertThatThrownBy(() -> reservation.validateOwner(otherName))
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.USER_NAME_NOT_MATCHED);
        }
    }
}
