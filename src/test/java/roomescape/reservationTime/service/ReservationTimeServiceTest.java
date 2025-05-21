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
import roomescape.common.exception.BusinessException;
import roomescape.member.domain.Email;
import roomescape.member.domain.Member;
import roomescape.member.domain.Name;
import roomescape.member.domain.Password;
import roomescape.member.infrastructure.JpaMemberRepository;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationRepository;
import roomescape.reservation.domain.Status;
import roomescape.reservation.infrastructure.JpaReservationRepository;
import roomescape.reservation.infrastructure.JpaReservationRepositoryAdapter;
import roomescape.reservationTime.domain.ReservationTime;
import roomescape.reservationTime.domain.ReservationTimeRepository;
import roomescape.reservationTime.infrastructure.JpaReservationTimeRepository;
import roomescape.reservationTime.infrastructure.JpaReservationTimeRepositoryAdaptor;
import roomescape.reservationTime.presentation.dto.TimeConditionRequest;
import roomescape.reservationTime.presentation.dto.TimeConditionResponse;
import roomescape.reservationTime.service.ReservationTimeServiceTest.ReservationTimeConfig;
import roomescape.theme.domain.Theme;
import roomescape.theme.infrastructure.JpaThemeRepository;

@DataJpaTest
@Import(ReservationTimeConfig.class)
class ReservationTimeServiceTest {

    @Autowired
    private ReservationTimeService reservationTimeService;

    @Autowired
    private JpaThemeRepository jpaThemeRepository;

    @Autowired
    private JpaReservationTimeRepository jpaReservationTimeRepository;

    @Autowired
    private JpaReservationRepository jpaReservationRepository;

    @Autowired
    private JpaMemberRepository jpaMemberRepository;


    @DisplayName("이미 존재하는 예약이 있는 경우 예약 시간을 삭제할 수 없다.")
    @Test
    void can_not_delete_when_reservation_exists() {
        Assertions.assertThatThrownBy(() -> reservationTimeService.deleteReservationTimeById(1L))
            .isInstanceOf(BusinessException.class);
    }

    @DisplayName("예약 가능 및 불가능 시간을 조회할 수 있다.")
    @Test
    void time_condition_test() {
        // given
        Theme theme = jpaThemeRepository.save(new Theme("test", "test", "test"));
        ReservationTime time1 = jpaReservationTimeRepository.save(new ReservationTime(LocalTime.of(9, 0)));
        ReservationTime time2 = jpaReservationTimeRepository.save(new ReservationTime(LocalTime.of(10, 0)));
        ReservationTime time3 = jpaReservationTimeRepository.save(new ReservationTime(LocalTime.of(11, 0)));

        LocalDate now = LocalDate.now();

        Member member = jpaMemberRepository.save(
            new Member(new Name("율무"), new Email("test@email.com"), new Password("password"))
        );

        jpaReservationRepository.save(new Reservation(now, time1, theme, member, Status.RESERVED));
        jpaReservationRepository.save(new Reservation(now, time2, theme, member, Status.RESERVED));

        // when
        List<TimeConditionResponse> responses = reservationTimeService.getTimesWithCondition(
            new TimeConditionRequest(now, theme.getId()));

        // then
        Assertions.assertThat(responses).containsExactlyInAnyOrder(
            new TimeConditionResponse(time1.getId(), time1.getStartAt(), true),
            new TimeConditionResponse(time2.getId(), time2.getStartAt(), true),
            new TimeConditionResponse(time3.getId(), time3.getStartAt(), false)
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
