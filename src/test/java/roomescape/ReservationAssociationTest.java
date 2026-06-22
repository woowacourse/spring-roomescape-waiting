package roomescape;

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
class ReservationAssociationTest {

    @Autowired
    private JpaReservationRepository reservationRepository;

    @Autowired
    private JpaReservationTimeRepository reservationTimeRepository;

    @Autowired
    private JpaThemeRepository themeRepository;

    @Autowired
    private EntityManager entityManager;

    @Test
    void 예약에서_예약시간을_지연_로딩한다() {
        ReservationTime time = reservationTimeRepository.save(
                new ReservationTime(null, LocalTime.of(10, 0))
        );

        Theme theme = themeRepository.save(
                new Theme(null, "테마", "설명", "thumbnail")
        );

        Reservation reservation = reservationRepository.save(
                new Reservation(
                        null,
                        "브라운",
                        LocalDate.now().plusDays(1),
                        time,
                        theme
                )
        );

        // INSERT를 DB에 반영하고 영속성 컨텍스트를 비운다.
        entityManager.flush();
        entityManager.clear();

        Reservation found = reservationRepository
                .findById(reservation.getId())
                .orElseThrow();

        LocalTime startAt = found.getTime().getStartAt();

        assertThat(startAt).isEqualTo(LocalTime.of(10, 0));
    }
}
