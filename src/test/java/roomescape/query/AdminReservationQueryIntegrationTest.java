package roomescape.query;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.jdbc.Sql;
import roomescape.domain.ReservationTime;
import roomescape.domain.Theme;
import roomescape.domain.TimeStatus;
import roomescape.domain.fixture.ReservationFixture;
import roomescape.repository.ReservationRepository;
import roomescape.service.result.ReservationResult;
import roomescape.support.IntegrationTest;
import roomescape.support.TestDateTimes;

@IntegrationTest
@Sql("/integration-fixture.sql")
class AdminReservationQueryIntegrationTest {

    private final Theme theme = Theme.restore(1L, "공포", "어마무시한 공포 테마", "https://theme.com/image.png", 30000L, true);
    private final ReservationTime time = ReservationTime.restore(1L, TestDateTimes.defaultTime(), TimeStatus.ACTIVE);
    @Autowired
    private ReservationQueryRepository reservationQueryRepository;
    @Autowired
    private ReservationRepository reservationRepository;

    @Test
    void 관리자_예약_목록을_예약_엔트리_정보와_함께_조회한다() {
        // given
        LocalDate date = TestDateTimes.tomorrow();
        reservationRepository.save(ReservationFixture.createWithAll("이프", date, theme, time));

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
