package roomescape.date.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static roomescape.date.exception.ReservationDateErrorInformation.DATE_IS_NULL;
import static roomescape.date.exception.ReservationDateErrorInformation.ID_IS_NULL;
import static roomescape.date.exception.ReservationDateErrorInformation.PAST_DATE_NOT_ALLOWED;

import java.time.LocalDate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import roomescape.date.exception.ReservationDateException;

class ReservationDateTest {


    @Nested
    @DisplayName("create 메서드는")
    class CreateTest {


        @Test
        @DisplayName("객체를 생성한다")
        void 성공1() {
            // given
            LocalDate validDate = LocalDate.of(2099, 1, 1);

            // when & then
            assertThatCode(() -> ReservationDate.create(validDate))
                .doesNotThrowAnyException();
        }


        @Test
        @DisplayName("필드를 검증한다")
        void 성공3() {
            // given
            LocalDate validDate = LocalDate.of(2099, 1, 1);

            // when
            ReservationDate reservationDate = ReservationDate.create(validDate);

            // then
            assertThat(reservationDate.getDate())
                .isEqualTo(validDate);
        }


        @Test
        @DisplayName("date가 Null이면 예외가 발생한다")
        void 실패1() {
            // given
            LocalDate nullDate = null;

            // when & then
            assertThatThrownBy(() -> ReservationDate.create(nullDate))
                .isInstanceOf(ReservationDateException.class)
                .hasMessage(DATE_IS_NULL.getMessage());
        }


        @Test
        @DisplayName("과거 날짜면 예외가 발생한다")
        void 실패2() {
            // given
            LocalDate pastDate = LocalDate.of(2000, 1, 1);

            // when & then
            assertThatThrownBy(() -> ReservationDate.create(pastDate))
                .isInstanceOf(ReservationDateException.class)
                .hasMessage(PAST_DATE_NOT_ALLOWED.getMessage());
        }
    }

    @Nested
    @DisplayName("load 메서드는")
    class LoadTest {


        @Test
        @DisplayName("객체를 생성한다")
        void 성공() {
            // given
            Long loadValidId = 1L;
            LocalDate loadValidDate = LocalDate.of(2099, 1, 1);

            // when & then
            assertThatCode(() -> {
                ReservationDate.load(loadValidId, loadValidDate, false);
            }).doesNotThrowAnyException();
        }


        @Test
        @DisplayName("id가 null이면 예외가 발생한다")
        void 실패() {
            // given
            Long nullId = null;
            LocalDate validDate = LocalDate.of(2099, 1, 1);

            // when & then
            assertThatThrownBy(() -> ReservationDate.load(nullId, validDate, false))
                .isInstanceOf(ReservationDateException.class)
                .hasMessage(ID_IS_NULL.getMessage());
        }
    }

    @Nested
    @DisplayName("create 메서드는")
    class CreatTest {


        @Test
        @DisplayName("객체를 생성한다")
        void 성공() {
            // given
            Long loadValidId = 1L;
            LocalDate loadValidDate = LocalDate.of(2099, 1, 1);
            ReservationDate createdDate = ReservationDate.create(loadValidDate);
            ReservationDate loadedDate = ReservationDate.load(loadValidId, loadValidDate, true);

            // when & then
            assertThat(createdDate)
                .usingRecursiveComparison()
                .ignoringFields("id")
                .isEqualTo(loadedDate);
        }
    }
}
