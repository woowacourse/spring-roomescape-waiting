package roomescape.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.jdbc.Sql;
import roomescape.domain.time.ReservationTime;

@Sql("/repository-test-data.sql")
@DataJpaTest
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class ReservationTimeRepositoryTest {

    @Autowired
    ReservationTimeRepository reservationTimeRepository;

    @Test
    void 주어진_id에_해당하지_않는_예약시간을_조회() {
        //given
        ReservationTime savedTime1 = reservationTimeRepository.save(new ReservationTime(LocalTime.parse("01:00")));
        ReservationTime savedTime2 = reservationTimeRepository.save(new ReservationTime(LocalTime.parse("02:00")));
        ReservationTime savedTime3 = reservationTimeRepository.save(new ReservationTime(LocalTime.parse("03:00")));

        //when
        List<ReservationTime> timeByIdNotIn = reservationTimeRepository.findByIdNotIn(
                List.of(savedTime1.getId(), savedTime2.getId()));

        //then
        assertThat(timeByIdNotIn).extracting(ReservationTime::getId).containsExactly(savedTime3.getId());
    }

    @Test
    void 주어진_시간으로_등록된_예약_시간이_있는지_확인() {
        //given
        ReservationTime savedTime = reservationTimeRepository.save(new ReservationTime(LocalTime.parse("01:00")));

        //when
        boolean result = reservationTimeRepository.existsByStartAt(savedTime.getStartAt());

        //then
        assertThat(result).isTrue();
    }
}
