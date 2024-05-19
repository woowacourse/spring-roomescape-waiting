package roomescape.exception;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import jakarta.validation.ConstraintValidatorContext;
import java.time.LocalDate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import roomescape.reservation.dto.ReservationSearchRequest;

class DateValidatorTest {

    @ParameterizedTest
    @CsvSource({"'2024-03-01', '2024-03-02', true", "'2024-03-01', '2024-04-01', false"})
    @DisplayName("기간이 30일을 넘지 않으면 true, 넘으면 false를 반환한다.")
    void isValid(LocalDate dateFrom, LocalDate dateTo, boolean expected) {
        ReservationSearchRequest reservationSearchRequest = new ReservationSearchRequest(
                1L,
                1L,
                dateFrom,
                dateTo
        );

        DateValidator dateValidator = new DateValidator();
        boolean valid = dateValidator.isValid(reservationSearchRequest, mock(ConstraintValidatorContext.class));

        assertThat(valid).isEqualTo(expected);
    }
}
