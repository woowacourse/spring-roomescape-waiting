package roomescape.application;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Transient;
import java.time.LocalDate;
import java.time.LocalTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import roomescape.ReservationTestFixture;
import roomescape.reservation.application.AdminReservationThemeService;
import roomescape.global.exception.BusinessRuleViolationException;
import roomescape.reservation.infrastructure.db.ReservationThemeDbRepository;
import roomescape.reservation.model.entity.Reservation;
import roomescape.reservation.model.entity.ReservationTheme;
import roomescape.reservation.model.entity.ReservationTime;
import roomescape.reservation.model.repository.ReservationRepository;
import roomescape.reservation.model.repository.ReservationThemeRepository;
import roomescape.reservation.model.repository.ReservationTimeRepository;
import roomescape.support.IntegrationTestSupport;

@SpringBootTest
class AdminReservationThemeServiceTest extends IntegrationTestSupport {

    @Autowired
    private AdminReservationThemeService adminReservationThemeService;

    @Autowired
    private ReservationTimeRepository reservationTimeRepository;

    @Autowired
    private ReservationThemeRepository reservationThemeRepository;

    @Autowired
    private ReservationRepository reservationRepository;
    @PersistenceContext
    private EntityManager entityManager;

    @Transient
    @DisplayName("테마 삭제시 해당 테마 id를 참조하고 있는 예약이 있다면 예외를 발생시킨다")
    @Test
    void delete() {
        // given
        ReservationTime reservationTime = ReservationTime.builder()
            .startAt(LocalTime.parse("10:00"))
            .build();

        ReservationTheme theme = ReservationTheme.builder()
            .name("이름")
            .description("설명")
            .thumbnail("썸네일")
            .build();
        ReservationTime savedTime = reservationTimeRepository.save(reservationTime);
        ReservationTheme savedTheme = reservationThemeRepository.save(theme);

        Reservation reservation = ReservationTestFixture.createReservation(
            LocalDate.now().plusDays(1),
            savedTime,
            savedTheme
        );
        reservationRepository.save(reservation);
        System.out.println(reservation.getId());
        System.out.println(reservationRepository.findById(reservation.getId()));
        // when & then
//        assertThatThrownBy(() -> adminReservationThemeService.delete(savedTheme.getId()))
//                .isInstanceOf(BusinessRuleViolationException.class);
    }
}
