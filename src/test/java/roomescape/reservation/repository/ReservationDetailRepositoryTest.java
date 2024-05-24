package roomescape.reservation.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.time.LocalTime;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;

import roomescape.reservation.domain.ReservationDetail;
import roomescape.theme.domain.Theme;
import roomescape.time.domain.Time;

@DataJpaTest
@DirtiesContext(classMode = ClassMode.BEFORE_EACH_TEST_METHOD)
class ReservationDetailRepositoryTest {
    private static final Theme THEME = new Theme(1L, "Harry Potter", "해리포터와 도비", "thumbnail.jpg");
    private static final Time TIME = new Time(1L, LocalTime.of(12, 0));
    public static final ReservationDetail RESERVATION_DETAIL = new ReservationDetail(1L, THEME, TIME, LocalDate.MAX);
    @PersistenceContext
    EntityManager entityManager;

    @Autowired
    private ReservationDetailRepository detailRepository;

    @BeforeEach
    void setUp() {
        entityManager.merge(TIME);
        entityManager.merge(THEME);
        entityManager.merge(RESERVATION_DETAIL);
    }

    @Test
    @DisplayName("존재하는 예약시간인지 확인한다.")
    void countReservationTime() {
        assertThat(detailRepository.countReservationsByTime_Id(RESERVATION_DETAIL.getTimeId()))
                .isEqualTo(1);
    }
}
