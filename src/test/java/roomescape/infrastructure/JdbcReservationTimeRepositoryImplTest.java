package roomescape.infrastructure;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.jdbc.core.JdbcTemplate;
import roomescape.domain.ReservationTime;
import roomescape.domain.ReservationTimeRepository;

@JdbcTest
class JdbcReservationTimeRepositoryImplTest {

    private ReservationTimeRepository reservationTimeRepository;

    @Autowired
    public JdbcReservationTimeRepositoryImplTest(JdbcTemplate jdbcTemplate) {
        this.reservationTimeRepository = new JdbcReservationTimeRepositoryImpl(jdbcTemplate);
    }

    @DisplayName("id값이 db에 존재하지 않으면 optional.empty를 반환한다.")
    @Test
    void findById_NoSuchId() {
        Optional<ReservationTime> actual = reservationTimeRepository.findById(1L);

        assertThat(actual).isEqualTo(Optional.empty());
    }
}
