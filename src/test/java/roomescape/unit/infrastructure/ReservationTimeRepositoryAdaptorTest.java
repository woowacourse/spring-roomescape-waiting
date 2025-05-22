package roomescape.unit.infrastructure;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.jdbc.Sql;
import roomescape.domain.ReservationTime;
import roomescape.infrastructure.JpaReservationTimeRepository;
import roomescape.infrastructure.ReservationTimeRepositoryAdaptor;

@DataJpaTest
@Sql(value = "/sql/testReservationTime.sql")
class ReservationTimeRepositoryAdaptorTest {

    @Autowired
    private JpaReservationTimeRepository jpaReservationTimeRepository;

    private ReservationTimeRepositoryAdaptor reservationTimeRepositoryAdaptor;

    private List<ReservationTime> reservationTimes;

    @BeforeEach
    void setUp() {
        reservationTimeRepositoryAdaptor = new ReservationTimeRepositoryAdaptor(jpaReservationTimeRepository);
        reservationTimes = reservationTimeRepositoryAdaptor.findAll();
    }

    @Test
    void 모든_예약시간_조회_테스트() {
        assertThat(reservationTimes.size()).isEqualTo(3);
    }

    @Test
    void id로_삭제_테스트() {
        //given
        Long id = reservationTimes.getFirst().getId();
        reservationTimeRepositoryAdaptor.deleteById(id);

        //when & then
        List<ReservationTime> reservationTimes = reservationTimeRepositoryAdaptor.findAll();

        assertThat(reservationTimes.size()).isEqualTo(2);
    }

    @Test
    void id로_찾기_테스트() {
        //given
        Long id = reservationTimes.getFirst().getId();

        //when & then
        Optional<ReservationTime> reservationTime = reservationTimeRepositoryAdaptor.findById(id);

        assertThat(reservationTime).isPresent();
    }

    @Test
    void 저장_테스트() {
        //given
        ReservationTime reservationTime = ReservationTime.createWithoutId(LocalTime.of(18, 0));

        //when & then
        reservationTimeRepositoryAdaptor.save(reservationTime);
        List<ReservationTime> reservationTimes = reservationTimeRepositoryAdaptor.findAll();
        
        assertThat(reservationTimes.size()).isEqualTo(4);
    }
}
