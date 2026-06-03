package roomescape.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import roomescape.exception.BusinessException;
import roomescape.exception.ErrorCode;

class WaitingListTest {

    private Theme theme;
    private ReservationTime futureTime;

    @BeforeEach
    void setUp() {
        theme = Theme.createWithId(1L, "테마", "테스트용 테마 설명입니다.", "https://img.com");
        futureTime = ReservationTime.createWithId(1L, LocalTime.now().plusHours(1), LocalTime.now().plusHours(2));
    }

    @Nested
    class 생성 {

        @Test
        void 성공() {
            // given
            String name = "검프";
            LocalDate date = LocalDate.now().plusDays(1);

            // when
            WaitingList waitingList = WaitingList.create(name, date, theme, futureTime);

            // then
            assertThat(waitingList.getId()).isNull();
            assertThat(waitingList.getName()).isEqualTo(name);
            assertThat(waitingList.getReservationDate().getDate()).isEqualTo(date);
            assertThat(waitingList.getTheme()).isEqualTo(theme);
            assertThat(waitingList.getReservationTime()).isEqualTo(futureTime);
            assertThat(waitingList.getCreatedAt()).isNotNull();
        }

        @Test
        void withId로_id_부여() {
            // given
            String name = "검프";
            WaitingList waitingList = WaitingList.create(name, LocalDate.now().plusDays(1), theme, futureTime);

            // when
            WaitingList withId = waitingList.withId(10L);

            // then
            assertThat(withId.getId()).isEqualTo(10L);
            assertThat(withId.getName()).isEqualTo(name);
        }
    }

    @Nested
    class validateNotPast {

        @Test
        void 미래_날짜면_통과() {
            String name = "검프";
            WaitingList waitingList = WaitingList.create(name, LocalDate.now().plusDays(1), theme, futureTime);
            waitingList.validateNotPast();
        }

        @Test
        void 오늘_미래_시간이면_통과() {
            String name = "검프";
            WaitingList waitingList = WaitingList.create(name, LocalDate.now(), theme, futureTime);
            waitingList.validateNotPast();
        }

        @Test
        void 과거_날짜면_예외발생() {
            String name = "검프";
            WaitingList waitingList = WaitingList.createWithId(
                    1L, name, LocalDate.now().minusDays(1), theme, futureTime, LocalDateTime.now()
            );

            assertThatThrownBy(waitingList::validateNotPast)
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.DATE_ALREADY_PASSED);
        }

        @Test
        void 오늘_과거_시간이면_예외발생() {
            String name = "검프";
            ReservationTime pastTime = ReservationTime.createWithId(2L, LocalTime.now().minusHours(1), LocalTime.now());
            WaitingList waitingList = WaitingList.create(name, LocalDate.now(), theme, pastTime);

            assertThatThrownBy(waitingList::validateNotPast)
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.TIME_ALREADY_PASSED);
        }
    }

    @Nested
    class validateOwner {

        @Test
        void 이름_일치시_통과() {
            String name = "검프";
            WaitingList waitingList = WaitingList.create(name, LocalDate.now().plusDays(1), theme, futureTime);
            waitingList.validateOwner(name);
        }

        @Test
        void 이름_불일치시_예외발생() {
            String ownerName = "검프";
            String otherName = "거위";
            WaitingList waitingList = WaitingList.create(ownerName, LocalDate.now().plusDays(1), theme, futureTime);

            assertThatThrownBy(() -> waitingList.validateOwner(otherName))
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.USER_NAME_NOT_MATCHED);
        }
    }
}
