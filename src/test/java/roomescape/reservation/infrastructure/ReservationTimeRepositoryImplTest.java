package roomescape.reservation.infrastructure;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;
import static roomescape.fixture.domain.ReservationTimeFixture.NOT_SAVED_RESERVATION_TIME_1;

import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator.ReplaceUnderscores;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import roomescape.exception.resource.ResourceNotFoundException;
import roomescape.fixture.config.TestConfig;
import roomescape.reservation.domain.ReservationTime;
import roomescape.reservation.domain.ReservationTimeCommandRepository;

@DataJpaTest
@Import(TestConfig.class)
@DisplayNameGeneration(ReplaceUnderscores.class)
class ReservationTimeRepositoryImplTest {

    @Autowired
    ReservationTimeRepositoryImpl reservationTimeRepository;

    @Autowired
    ReservationTimeCommandRepository reservationTimeCommandRepository;

    @Test
    void ID_값에_해당하는_예약_시간을_반환한다() {
        // given
        final ReservationTime saved = reservationTimeCommandRepository.save(NOT_SAVED_RESERVATION_TIME_1());

        // when
        final ReservationTime found = reservationTimeRepository.getByIdOrThrow(saved.getId());

        // then
        assertAll(
                () -> assertThat(found.getId()).isEqualTo(saved.getId()),
                () -> assertThat(found.getStartAt()).isEqualTo(saved.getStartAt())
        );
    }

    @Test
    void ID_값에_해당하는_예약_시간이_없으면_예외가_발생한다() {
        // given
        final Long id = 99L;

        // when & then
        assertThatThrownBy(() -> reservationTimeRepository.getByIdOrThrow(id))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("해당 예약 시간이 존재하지 않습니다.");
    }
}
