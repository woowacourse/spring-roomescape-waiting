package roomescape.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import roomescape.domain.ReservationTime;
import roomescape.domain.Slot;
import roomescape.domain.Theme;
import roomescape.domain.Wait;

@DataJpaTest
class WaitRepositoryTest {

    @Autowired
    private WaitRepository waitRepository;

    @Autowired
    private ReservationTimeRepository reservationTimeRepository;

    @Autowired
    private ThemeRepository themeRepository;

    private ReservationTime reservationTime;
    private Theme theme;

    @BeforeEach
    void beforeEach() {
        reservationTime = reservationTimeRepository.save(new ReservationTime(LocalTime.of(10, 0)));
        theme = themeRepository.save(new Theme("방탈출1", "방탈출1 설명", "url.jpg"));
    }

    @Test
    void saveTest() {
        Wait waitWithoutId = new Wait(LocalDateTime.of(2026, 5, 21, 10, 0), "luke",
                new Slot(LocalDate.of(2026, 5, 27), reservationTime, theme));

        Wait wait = waitRepository.save(waitWithoutId);

        assertThat(wait.getId()).isNotNull();
        assertThat(wait.getName()).isEqualTo("luke");
    }

    @Test
    void findByIdTest() {
        Wait wait1 = waitRepository.save(new Wait(LocalDateTime.of(2026, 5, 21, 10, 0), "luke",
                new Slot(LocalDate.of(2026, 5, 27), reservationTime, theme)));
        Wait wait2 = waitRepository.save(new Wait(LocalDateTime.of(2026, 5, 21, 10, 1), "fizz",
                new Slot(LocalDate.of(2026, 5, 27), reservationTime, theme)));
        Wait wait3 = waitRepository.save(new Wait(LocalDateTime.of(2026, 5, 21, 10, 2), "neo",
                new Slot(LocalDate.of(2026, 5, 27), reservationTime, theme)));

        Optional<Wait> waitLuke = waitRepository.findById(wait1.getId());
        Optional<Wait> waitFizz = waitRepository.findById(wait2.getId());
        Optional<Wait> waitNeo = waitRepository.findById(wait3.getId());

        assertThat(waitLuke).isNotEmpty();
        assertThat(waitFizz).isNotEmpty();
        assertThat(waitNeo).isNotEmpty();
    }

    @Test
    void findBySlotTest() {
        waitRepository.save(new Wait(LocalDateTime.of(2026, 5, 21, 10, 0), "luke",
                new Slot(LocalDate.of(2026, 5, 27), reservationTime, theme)));
        waitRepository.save(new Wait(LocalDateTime.of(2026, 5, 21, 10, 1), "fizz",
                new Slot(LocalDate.of(2026, 5, 27), reservationTime, theme)));
        waitRepository.save(new Wait(LocalDateTime.of(2026, 5, 21, 10, 2), "neo",
                new Slot(LocalDate.of(2026, 5, 27), reservationTime, theme)));

        List<Wait> waits = waitRepository.findBySlot(LocalDate.of(2026, 5, 27), reservationTime.getId(),
                theme.getId());

        assertThat(waits.size()).isEqualTo(3);
    }

    @Test
    void findByNameTest() {
        waitRepository.save(new Wait(LocalDateTime.of(2026, 5, 21, 10, 0), "luke",
                new Slot(LocalDate.of(2026, 5, 27), reservationTime, theme)));
        waitRepository.save(new Wait(LocalDateTime.of(2026, 5, 21, 10, 0), "fizz",
                new Slot(LocalDate.of(2026, 5, 28), reservationTime, theme)));
        waitRepository.save(new Wait(LocalDateTime.of(2026, 5, 21, 10, 1), "luke",
                new Slot(LocalDate.of(2026, 5, 28), reservationTime, theme)));
        waitRepository.save(new Wait(LocalDateTime.of(2026, 5, 21, 10, 0), "fizz",
                new Slot(LocalDate.of(2026, 5, 29), reservationTime, theme)));
        waitRepository.save(new Wait(LocalDateTime.of(2026, 5, 21, 10, 1), "neo",
                new Slot(LocalDate.of(2026, 5, 29), reservationTime, theme)));
        waitRepository.save(new Wait(LocalDateTime.of(2026, 5, 21, 10, 2), "luke",
                new Slot(LocalDate.of(2026, 5, 29), reservationTime, theme)));

        List<Wait> waits = waitRepository.findByName("luke");

        assertThat(waits.size()).isEqualTo(3);
        assertThat(waits.get(0).getName()).isEqualTo("luke");
        assertThat(waits.get(1).getName()).isEqualTo("luke");
        assertThat(waits.get(2).getName()).isEqualTo("luke");
    }

    @Test
    void findAllTest() {
        waitRepository.save(new Wait(LocalDateTime.of(2026, 5, 21, 10, 0), "luke",
                new Slot(LocalDate.of(2026, 5, 27), reservationTime, theme)));
        waitRepository.save(new Wait(LocalDateTime.of(2026, 5, 21, 10, 1), "fizz",
                new Slot(LocalDate.of(2026, 5, 27), reservationTime, theme)));
        waitRepository.save(new Wait(LocalDateTime.of(2026, 5, 21, 10, 2), "neo",
                new Slot(LocalDate.of(2026, 5, 27), reservationTime, theme)));

        List<Wait> waits = waitRepository.findAll();

        assertThat(waits.size()).isEqualTo(3);
    }

    @Test
    void calculateWaitingOrderTest() {
        Wait wait1 = waitRepository.save(new Wait(LocalDateTime.of(2026, 5, 21, 10, 0), "luke",
                new Slot(LocalDate.of(2026, 5, 27), reservationTime, theme)));
        Wait wait2 = waitRepository.save(new Wait(LocalDateTime.of(2026, 5, 21, 10, 1), "fizz",
                new Slot(LocalDate.of(2026, 5, 27), reservationTime, theme)));
        Wait wait3 = waitRepository.save(new Wait(LocalDateTime.of(2026, 5, 21, 10, 2), "neo",
                new Slot(LocalDate.of(2026, 5, 27), reservationTime, theme)));

        assertThat(waitRepository.calculateWaitingOrder(wait1.getReservationDate(), wait1.getTimeId(),
                wait1.getThemeId(), wait1.getId())).isEqualTo(1L);
        assertThat(waitRepository.calculateWaitingOrder(wait2.getReservationDate(), wait2.getTimeId(),
                wait2.getThemeId(), wait2.getId())).isEqualTo(2L);
        assertThat(waitRepository.calculateWaitingOrder(wait3.getReservationDate(), wait3.getTimeId(),
                wait3.getThemeId(), wait3.getId())).isEqualTo(3L);
    }

    @Test
    void deleteByIdTest() {
        Wait saved = waitRepository.save(new Wait(LocalDateTime.of(2026, 5, 21, 10, 0), "luke",
                new Slot(LocalDate.of(2026, 5, 27), reservationTime, theme)));

        waitRepository.deleteById(saved.getId());

        Assertions.assertThat(waitRepository.count()).isEqualTo(0);
    }
}
