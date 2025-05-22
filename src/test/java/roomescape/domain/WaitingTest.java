package roomescape.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static roomescape.test.fixture.DateFixture.NEXT_DAY;
import static roomescape.test.fixture.DateFixture.TODAY;

import java.time.LocalDate;
import java.time.LocalTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class WaitingTest {

    @Nested
    @DisplayName("대기 데이터를 생성할 때 검증을 수행한다.")
    public class validate {

        @DisplayName("비어있는 날짜로 대기 데이터를 생성할 수 없다.")
        @Test
        void cannotCreateBecauseNullDate() {
            // given
            LocalDate nullDate = null;
            Theme theme = new Theme(1L, "테마", "설명", "섬네일");
            ReservationTime time = new ReservationTime(1L, LocalTime.of(10, 0));
            Member member = new Member(1L, Role.GENERAL, "회웜", "test@email.com", "qwer1234!");

            // when & then
            assertThatThrownBy(() -> Waiting.createWithoutId(nullDate, theme, time, member))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("비어있는 날짜로 대기를 생성할 수 없습니다.");
        }

        @DisplayName("비어있는 테마로 대기 데이터를 생성할 수 없다.")
        @Test
        void cannotCreateBecauseNullTheme() {
            // given
            LocalDate date = NEXT_DAY;
            Theme nullTheme = null;
            ReservationTime time = new ReservationTime(1L, LocalTime.of(10, 0));
            Member member = new Member(1L, Role.GENERAL, "회웜", "test@email.com", "qwer1234!");

            // when & then
            assertThatThrownBy(() -> Waiting.createWithoutId(date, nullTheme, time, member))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("비어있는 테마로 대기를 생성할 수 없습니다.");
        }

        @DisplayName("비어있는 예약시간으로 대기 데이터를 생성할 수 없다.")
        @Test
        void cannotCreateBecauseNullReservationTime() {
            // given
            LocalDate date = NEXT_DAY;
            Theme theme = new Theme(1L, "테마", "설명", "섬네일");
            ReservationTime nullTime = null;
            Member member = new Member(1L, Role.GENERAL, "회웜", "test@email.com", "qwer1234!");

            // when & then
            assertThatThrownBy(() -> Waiting.createWithoutId(date, theme, nullTime, member))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("비어있는 시간으로 대기를 생성할 수 없습니다.");
        }

        @DisplayName("비어있는 회원으로 대기 데이터를 생성할 수 없다.")
        @Test
        void cannotCreateBecauseNullMember() {
            // given
            LocalDate date = NEXT_DAY;
            Theme theme = new Theme(1L, "테마", "설명", "섬네일");
            ReservationTime time = new ReservationTime(1L, LocalTime.of(10, 0));
            Member member = null;

            // when & then
            assertThatThrownBy(() -> Waiting.createWithoutId(date, theme, time, member))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("비어있는 회원으로 대기를 생성할 수 없습니다.");
        }
    }

    @Nested()
    @DisplayName("과거 대기 데이터인지 확인할 수 있다.")
    public class isPastWaiting {

        @DisplayName("과거 대기 데이터인 경우 false 리턴")
        @Test
        void checkPastWaiting() {
            // given
            Theme theme = new Theme(1L, "테마", "설명", "섬네일");
            ReservationTime time = new ReservationTime(1L, LocalTime.now().minusSeconds(1));
            Member member = new Member(1L, Role.GENERAL, "회원", "test@email.com", "qwer1234!");
            Waiting pastWaiting = Waiting.createWithoutId(TODAY, theme, time, member);

            // when
            boolean isPast = pastWaiting.isPastWaiting();

            // then
            assertThat(isPast).isTrue();
        }

        @DisplayName("과거 대기 데이터가 아닌 경우 true 리턴")
        @Test
        void checkNotPastWaiting() {
            // given
            Theme theme = new Theme(1L, "테마", "설명", "섬네일");
            ReservationTime time = new ReservationTime(1L, LocalTime.now().plusSeconds(1));
            Member member = new Member(1L, Role.GENERAL, "회원", "test@email.com", "qwer1234!");
            Waiting pastWaiting = Waiting.createWithoutId(TODAY, theme, time, member);

            // when
            boolean isPast = pastWaiting.isPastWaiting();

            // then
            assertThat(isPast).isFalse();
        }
    }
}
