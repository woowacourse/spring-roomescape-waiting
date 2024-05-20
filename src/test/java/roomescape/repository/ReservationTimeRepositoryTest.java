package roomescape.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.jdbc.Sql;
import roomescape.domain.ReservationTime;
import roomescape.domain.repository.ReservationTimeRepository;

@DataJpaTest
class ReservationTimeRepositoryTest {

    @Autowired
    private ReservationTimeRepository reservationTimeRepository;

    @DisplayName("특정 날짜와 테마에 대해 예약된 시간을 모두 조회한다")
    @Sql(value = {"/test-data/times.sql", "/test-data/themes.sql", "/test-data/members.sql",
            "/test-data/reservations-details.sql", "/test-data/reservations.sql"})
    @Test
    void when_findAllReservedTimeByDateAndThemeId_then_returnReservedTimes() {
        // given
        LocalDate date = LocalDate.of(2099, 7, 1);
        Long themeId = 1L;

        // when
        List<ReservationTime> reservedTimes = reservationTimeRepository.findAllReservedTimeByDateAndThemeId(
                date, themeId);

        // then
        assertThat(reservedTimes.size())
                .isEqualTo(8);
    }

}
