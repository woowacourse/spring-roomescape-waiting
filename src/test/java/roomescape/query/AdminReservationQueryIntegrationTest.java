package roomescape.query;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import roomescape.service.result.ReservationResult;
import roomescape.support.BaseIntegrationTest;
import roomescape.support.DatabaseCleaner;
import roomescape.support.ReservationDataSource;
import roomescape.support.TestDateTimes;

class AdminReservationQueryIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private ReservationQueryRepository reservationQueryRepository;

    @Autowired
    private ReservationDataSource dataSource;

    @Autowired
    private DatabaseCleaner databaseCleaner;

    @BeforeEach
    void setUp() {
        databaseCleaner.clear();
        dataSource.insertTheme("공포", "어마무시한 공포 테마", "https://theme.com/image.png");
        dataSource.insertReservationTime(TestDateTimes.defaultTime());
    }

    @Test
    void 관리자_예약_목록을_예약_엔트리_정보와_함께_조회한다() {
        // given
        LocalDate date = TestDateTimes.tomorrow();
        dataSource.insertReservation("이프", date, 1L, 1L);

        // when
        List<ReservationResult> result = reservationQueryRepository.getAllReservations();

        // then
        assertThat(result).hasSize(1);
        ReservationResult reservation = result.getFirst();
        assertThat(reservation.reservationId()).isEqualTo(1L);
        assertThat(reservation.date()).isEqualTo(date);
        assertThat(reservation.theme().name()).isEqualTo("공포");
        assertThat(reservation.time().startAt()).isEqualTo(TestDateTimes.defaultTime());
        assertThat(reservation.entry().name()).isEqualTo("이프");
        assertThat(reservation.entry().status()).isEqualTo("RESERVED");
    }
}
