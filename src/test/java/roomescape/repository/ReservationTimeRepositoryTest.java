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

@Sql("/all-test-data.sql")
@DataJpaTest
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class ReservationTimeRepositoryTest {

    @Autowired
    ReservationTimeRepository reservationTimeRepository;

    @Test
    void 주어진_id에_해당하지_않는_예약시간을_조회() {
        //when
        List<ReservationTime> timeByIdNotIn = reservationTimeRepository.findByIdNotIn(List.of(1L, 2L));

        //then
        assertThat(timeByIdNotIn).extracting(ReservationTime::getId).containsOnly(3L);
    }

    @Test
    void 주어진_시간으로_등록된_예약_시간이_있는지_확인() {
        //when
        boolean result = reservationTimeRepository.existsByStartAt(LocalTime.parse("10:00"));

        //then
        assertThat(result).isTrue();
    }
}
