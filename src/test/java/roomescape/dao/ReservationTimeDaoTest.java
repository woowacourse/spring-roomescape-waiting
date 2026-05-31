package roomescape.dao;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;

@JdbcTest
@Import(ReservationTimeDao.class)
class ReservationTimeDaoTest {

    @Autowired
    private ReservationTimeDao reservationTimeDao;

    @Test
    @DisplayName("이미 존재하는 시작 시간이면 true를 반환한다.")
    void 이미_존재하는_시작_시간_확인_테스트() {
        boolean result = reservationTimeDao.existsByStartAt(LocalTime.of(10, 0));

        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("존재하지 않는 시작 시간이면 false를 반환한다.")
    void 존재하지_않는_시작_시간_확인_테스트() {
        boolean result = reservationTimeDao.existsByStartAt(LocalTime.of(19, 0));

        assertThat(result).isFalse();
    }
}