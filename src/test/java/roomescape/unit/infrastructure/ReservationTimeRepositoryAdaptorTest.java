package roomescape.unit.infrastructure;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import java.time.LocalTime;
import java.util.List;
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

    @BeforeEach
    void setUp() {
        reservationTimeRepositoryAdaptor = new ReservationTimeRepositoryAdaptor(jpaReservationTimeRepository);
    }

    @Test
    void 모든_예약시간_조회_테스트() {
        //given
        List<ReservationTime> reservationTimes = reservationTimeRepositoryAdaptor.findAll();

        //when & then
        assertThat(reservationTimes.size()).isEqualTo(3);
    }

    @Test
    void deleteById() {
        //given
        Long id = 1L;
        reservationTimeRepositoryAdaptor.deleteById(id);
        List<ReservationTime> reservationTimes = reservationTimeRepositoryAdaptor.findAll();

        //when & then
        assertThat(reservationTimes.size()).isEqualTo(2);
    }

    @Test
    void findById() {
        //given
        Long id = 1L;
        ReservationTime reservationTime = reservationTimeRepositoryAdaptor.findById(id).get();

        //when & then
        assertThat(reservationTime.getId()).isEqualTo(id);
    }

    @Test
    void save() {
        //given
        ReservationTime reservationTime = ReservationTime.createWithoutId(LocalTime.of(18, 0));
        reservationTimeRepositoryAdaptor.save(reservationTime);
        List<ReservationTime> reservationTimes = reservationTimeRepositoryAdaptor.findAll();

        //when & then
        assertThat(reservationTimes.size()).isEqualTo(4);
    }
}