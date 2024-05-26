package roomescape.reservation.domain.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static roomescape.fixture.ReservationTimeFixture.getNoon;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import roomescape.reservation.domain.ReservationTime;
import roomescape.util.RepositoryTest;

@DisplayName("예약 시간 레포지토리 테스트")
class ReservationTimeRepositoryTest extends RepositoryTest {

    @Autowired
    private ReservationTimeRepository timeRepository;

    @DisplayName("예약 시간을 저장한다.")
    @Test
    void save() {
        //given & when
        ReservationTime time = timeRepository.save(getNoon());

        //then
        assertAll(() -> assertThat(time.getId()).isNotNull(),
                () -> assertThat(time.getStartAt()).isEqualTo(getNoon().getStartAt())
        );
    }
}
