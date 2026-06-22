package roomescape.jpa;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.persistence.EntityManager;
import java.time.LocalDate;
import java.time.LocalTime;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationTime;
import roomescape.domain.Theme;
import roomescape.repository.jpa.JpaReservationRepository;
import roomescape.repository.jpa.JpaReservationTimeRepository;
import roomescape.repository.jpa.JpaThemeRepository;

@SpringBootTest
@Transactional
class PersistenceContextTest {

    @Autowired
    private JpaReservationRepository reservationRepository;

    @Autowired
    private JpaReservationTimeRepository reservationTimeRepository;

    @Autowired
    private JpaThemeRepository themeRepository;

    @Autowired
    private EntityManager entityManager;

    @Test
    void 같은_트랜잭션에서_두_번_조회한다() {
        ReservationTime time = reservationTimeRepository.save(
                new ReservationTime(null, LocalTime.of(10, 0))
        );

        Theme theme = themeRepository.save(
                new Theme(null, "테마", "설명", "thumbnail")
        );

        Reservation saved = reservationRepository.save(
                new Reservation(
                        null,
                        "브라운",
                        LocalDate.now().plusDays(1),
                        time,
                        theme
                )
        );

        entityManager.flush();
        entityManager.clear();

        Reservation first = reservationRepository.findById(saved.getId())
                .orElseThrow();

        Reservation second = reservationRepository.findById(saved.getId())
                .orElseThrow();

        assertThat(first).isSameAs(second);
    }
}
