package roomescape.reservation.repository;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.jdbc.Sql;
import roomescape.Fixtures;
import roomescape.reservation.domain.Reservation;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
@Sql(value = {"/recreate_table.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
public class ReservationRepositoryTest {

    @Autowired
    ReservationRepository reservationRepository;

    @DisplayName("date nullable false 테스트")
    @Test
    void dateNullableFalseTest() {
        Reservation reservation = new Reservation(
                null,
                Fixtures.memberFixture,
                null,
                Fixtures.reservationTimeFixture,
                Fixtures.themeFixture
        );

        assertThatThrownBy(() -> reservationRepository.save(reservation))
                .isInstanceOf(DataIntegrityViolationException.class);
    }
}


