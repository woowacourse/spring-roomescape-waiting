package roomescape.date.domain;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import roomescape.date.exception.ReservationDateException;

import java.time.LocalDate;

import static roomescape.date.exception.ReservationDateErrorInformation.*;

class ReservationDateTest {

    @Test
    @DisplayName("유효한 날짜로 ReservationDate를 등록할 수 있다.")
    void create_with_valid_field() {
        // given
        LocalDate validDate = LocalDate.of(2099, 1, 1);

        // when & then
        Assertions.assertThatCode(() -> ReservationDate.create(validDate))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("오늘 날짜로 ReservationDate를 등록할 수 있다.")
    void create_with_today() {
        // given
        LocalDate today = LocalDate.now();

        // when & then
        Assertions.assertThatCode(() -> ReservationDate.create(today))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("유효한 날짜로 ReservationDate를 생성하면 입력한 날짜를 그대로 유지한다.")
    void create_compare_date_field() {
        // given
        LocalDate validDate = LocalDate.of(2099, 1, 1);

        // when
        ReservationDate reservationDate = ReservationDate.create(validDate);

        // then
        Assertions.assertThat(reservationDate.getDate())
                .isEqualTo(validDate);
    }

    @Test
    @DisplayName("DB로부터 유효한 값을 가져오면 ReservationDate를 생성할 수 있다.")
    void load() {
        // given
        Long loadValidId = 1L;
        LocalDate loadValidDate = LocalDate.of(2099, 1, 1);

        // when & then
        Assertions.assertThatCode(() -> {
            ReservationDate.load(loadValidId, loadValidDate, false);
        }).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("등록, 로드한 ReservationDate는 id를 제외한 모든 필드가 일치한다.")
    void creat_compare_load() {
        // given
        Long loadValidId = 1L;
        LocalDate loadValidDate = LocalDate.of(2099, 1, 1);
        ReservationDate createdDate = ReservationDate.create(loadValidDate);
        ReservationDate loadedDate = ReservationDate.load(loadValidId, loadValidDate, true);

        // when & then
        Assertions.assertThat(createdDate)
                .usingRecursiveComparison()
                .ignoringFields("id")
                .isEqualTo(loadedDate);
    }

    @Test
    @DisplayName("DB에서 로드할 때 ID가 null이면 예외가 발생한다.")
    void load_null_id() {
        // given
        Long nullId = null;
        LocalDate validDate = LocalDate.of(2099, 1, 1);

        // when & then
        Assertions.assertThatThrownBy(() -> ReservationDate.load(nullId, validDate, false))
                .isInstanceOf(ReservationDateException.class)
                .hasMessage(ID_IS_NULL.getMessage());
    }

    @Test
    @DisplayName("등록할 날짜가 null이면 예외가 발생한다.")
    void create_null_date() {
        // given
        LocalDate nullDate = null;

        // when & then
        Assertions.assertThatThrownBy(() -> ReservationDate.create(nullDate))
                .isInstanceOf(ReservationDateException.class)
                .hasMessage(DATE_IS_NULL.getMessage());
    }

    @Test
    @DisplayName("등록할 날짜가 과거이면 예외가 발생한다.")
    void create_before_date() {
        // given
        LocalDate pastDate = LocalDate.of(2000, 1, 1);

        // when & then
        Assertions.assertThatThrownBy(() -> ReservationDate.create(pastDate))
                .isInstanceOf(ReservationDateException.class)
                .hasMessage(PAST_DATE_NOT_ALLOWED.getMessage());
    }

}
