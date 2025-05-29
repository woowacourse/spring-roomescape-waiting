package roomescape.reservationtime.repository;

import static roomescape.constant.TestData.RESERVATION_TIME_COUNT;

import java.time.LocalDate;
import java.util.List;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.jdbc.Sql;
import roomescape.reservationtime.dto.AvailableReservationTimeResponse;

@DataJpaTest
@Sql("/data.sql")
class ReservationTimeRepositoryTest {

    @Autowired
    private ReservationTimeRepository repository;

    @Test
    void 예약가능한_모든_시간을_조회한다() {
        // given - data.sql
        // when
        List<AvailableReservationTimeResponse> allAvailable = repository.findAllAvailable(
                LocalDate.of(2999, 5, 1), 1L);

        // then
        SoftAssertions.assertSoftly(soft -> {
                    soft.assertThat(allAvailable).hasSize(RESERVATION_TIME_COUNT);
                    soft.assertThat(allAvailable).extracting(AvailableReservationTimeResponse::alreadyBooked)
                            .containsExactly(true, false);
                }
        );
    }
}
