package roomescape.fixture;

import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
public class DateFixture {

    public static LocalDate FROM_DATE = LocalDate.of(2024, 5, 24);

    public static LocalDate TO_DATE = LocalDate.of(2024, 5, 26);
}
