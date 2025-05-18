package roomescape.reservationTime.service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import roomescape.reservation.domain.ReservationRepository;
import roomescape.reservation.infrastructure.JpaReservationRepository;
import roomescape.reservation.infrastructure.JpaReservationRepositoryAdapter;
import roomescape.reservationTime.domain.ReservationTimeRepository;
import roomescape.reservationTime.exception.ReservationTimeException;
import roomescape.reservationTime.infrastructure.JpaReservationTimeRepository;
import roomescape.reservationTime.infrastructure.JpaReservationTimeRepositoryAdaptor;
import roomescape.reservationTime.presentation.dto.TimeConditionRequest;
import roomescape.reservationTime.presentation.dto.TimeConditionResponse;
import roomescape.reservationTime.service.ReservationTimeServiceTest.ReservationTimeConfig;

@DataJpaTest
@Import(ReservationTimeConfig.class)
class ReservationTimeServiceTest {

    @Autowired
    private ReservationTimeService reservationTimeService;

    @DisplayName("이미 존재하는 예약이 있는 경우 예약 시간을 삭제할 수 없다.")
    @Test
    void can_not_delete_when_reservation_exists() {
        Assertions.assertThatThrownBy(() -> reservationTimeService.deleteReservationTimeById(1L))
            .isInstanceOf(ReservationTimeException.class);
    }

    @DisplayName("예약 가능 시간 조회 테스트")
    @Test
    void time_condition_test() {
        LocalDate localDate = LocalDate.now().minusDays(1);
        Long themeId = 1L;

        List<TimeConditionResponse> responses = reservationTimeService.getTimesWithCondition(
            new TimeConditionRequest(localDate, themeId));

        Assertions.assertThat(responses).containsExactlyInAnyOrder(
            new TimeConditionResponse(1L, LocalTime.of(10, 0), true),
            new TimeConditionResponse(2L, LocalTime.of(11, 0), true),
            new TimeConditionResponse(3L, LocalTime.of(12, 0), false)
        );
    }

    static class ReservationTimeConfig {

        @Bean
        public ReservationRepository reservationRepository(JpaReservationRepository jpaReservationRepository) {
            return new JpaReservationRepositoryAdapter(jpaReservationRepository);
        }

        @Bean
        public ReservationTimeRepository reservationTimeRepository(
            JpaReservationTimeRepository jpaReservationTimeRepository) {
            return new JpaReservationTimeRepositoryAdaptor(jpaReservationTimeRepository);
        }

        @Bean
        public ReservationTimeService reservationTimeService(ReservationRepository reservationRepository,
                                                             ReservationTimeRepository reservationTimeRepository) {
            return new ReservationTimeService(reservationRepository, reservationTimeRepository);
        }
    }
}
