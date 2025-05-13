package roomescape.reservation.service;

import java.time.LocalTime;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.core.JdbcTemplate;
import roomescape.common.exception.DataExistException;
import roomescape.common.exception.DataNotFoundException;
import roomescape.reservation.domain.ReservationTime;
import roomescape.reservation.repository.JdbcReservationRepository;
import roomescape.reservation.repository.JdbcReservationTimeRepository;
import roomescape.reservation.repository.ReservationRepository;
import roomescape.reservation.repository.ReservationTimeRepository;

@JdbcTest
class ReservationTimeServiceTest {

    @Autowired
    private ReservationTimeService reservationTimeService;

    @Autowired
    private ReservationTimeRepository reservationTimeRepository;

    @ParameterizedTest
    @CsvSource(value = {
            "20:00", "22:00"
    })
    void 예약시간을_추가한다(final LocalTime startAt) {
        // when & then
        Assertions.assertThatCode(() -> reservationTimeService.save(startAt))
                .doesNotThrowAnyException();
    }

    @Test
    void 예약시간을_삭제한다() {
        // given
        final LocalTime startAt = LocalTime.of(20, 28);
        final Long id = reservationTimeRepository.save(new ReservationTime(startAt));

        // when & then
        Assertions.assertThatCode(() -> reservationTimeService.deleteById(id))
                .doesNotThrowAnyException();
    }

    @Test
    void 예약시간을_id로_조회한다() {
        // given
        final LocalTime startAt = LocalTime.of(20, 28);
        final ReservationTime reservationTime = new ReservationTime(startAt);
        final Long id = reservationTimeRepository.save(reservationTime);

        // when
        final ReservationTime found = reservationTimeService.getById(id);

        // then
        Assertions.assertThat(found.getStartAt()).isEqualTo(reservationTime.getStartAt());
    }

    @Test
    void 이미_존재하는_예약시간을_추가하면_예외가_발생한다() {
        // given
        final LocalTime startAt = LocalTime.of(19, 55);
        reservationTimeRepository.save(new ReservationTime(startAt));

        // when & then
        Assertions.assertThatThrownBy(() -> reservationTimeService.save(startAt))
                .isInstanceOf(DataExistException.class);
    }

    @Test
    void 삭제할_예약시간이_없으면_예외가_발생한다() {
        // given
        final Long id = Long.MAX_VALUE;

        // when & then
        Assertions.assertThatThrownBy(() -> reservationTimeService.deleteById(id))
                .isInstanceOf(DataNotFoundException.class);
    }

    @TestConfiguration
    static class TestConfig {

        @Bean
        public ReservationTimeRepository reservationTimeRepository(
                final JdbcTemplate jdbcTemplate
        ) {
            return new JdbcReservationTimeRepository(jdbcTemplate);
        }

        @Bean
        public ReservationRepository reservationRepository(
                final JdbcTemplate jdbcTemplate
        ) {
            return new JdbcReservationRepository(jdbcTemplate);
        }

        @Bean
        public ReservationTimeService reservationTimeService(
                final ReservationTimeRepository reservationTimeRepository
        ) {
            return new ReservationTimeService(reservationTimeRepository);
        }
    }
}
