package roomescape.time.dao;

import java.time.LocalTime;
import java.util.List;
import javax.sql.DataSource;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.dao.DataAccessException;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.Sql.ExecutionPhase;
import roomescape.time.domain.Time;

@JdbcTest
@Sql(scripts = "/data-test.sql", executionPhase = ExecutionPhase.BEFORE_TEST_METHOD)
class TimeJdbcDaoTest {

    private final TimeJdbcDao timeJdbcDao;

    @Autowired
    public TimeJdbcDaoTest(DataSource dataSource) {
        this.timeJdbcDao = new TimeJdbcDao(dataSource);
    }

    @Test
    @DisplayName("시간 데이터들이 잘 저장되는지 확인.")
    void saveTime() {
        Time time = new Time(LocalTime.of(12,0));
        timeJdbcDao.save(time);

        Assertions.assertThat(time.getId())
                .isNotEqualTo(0);
        timeJdbcDao.deleteById(time.getId());
    }

    @Test
    @DisplayName("시간 데이터들을 잘 가져오는지 확인.")
    void getTimes() {
        List<Time> times = timeJdbcDao.findAllReservationTimesInOrder();

        Assertions.assertThat(times.size())
                .isEqualTo(3);
    }

    @Test
    @DisplayName("시간 데이터들의 연관관계가 없다면 잘 지우는지 확인")
    void deleteTime() {
        timeJdbcDao.deleteById(3L);

        Assertions.assertThat(timeJdbcDao.findAllReservationTimesInOrder()
                                      .size())
                .isEqualTo(2);
    }

    @Test
    @DisplayName("시간 데이터들의 연관관계가 있다면 에러가 나는지 확인")
    void canNotDeleteTime() {
        Assertions.assertThatThrownBy(() -> timeJdbcDao.deleteById(1L))
                .isInstanceOf(DataAccessException.class);
    }

}
