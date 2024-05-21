package roomescape.time.repository;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.jdbc.Sql;
import roomescape.time.domain.ReservationTime;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
@Sql(value = {"/recreate_table.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
public class ReservationTimeRepositoryTest {

    @Autowired
    ReservationTimeRepository reservationTimeRepository;

    @DisplayName("startAt nullable false 테스트")
    @Test
    void startAtNullableFalseTest() {
        ReservationTime reservationTime = new ReservationTime(
                null,
                null
        );

        assertThatThrownBy(() -> reservationTimeRepository.save(reservationTime))
                .isInstanceOf(DataIntegrityViolationException.class);
    }
}
