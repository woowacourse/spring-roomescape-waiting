package roomescape.reservation.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Optional;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.jdbc.core.JdbcTemplate;
import roomescape.global.NotFoundException;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.repository.ReservationRepository;
import roomescape.reservation.infra.JdbcReservationRepository;
import roomescape.support.RepositoryTestHelper;

@JdbcTest
class JdbcReservationRepositoryTest {

    ReservationRepository reservationRepository;
    RepositoryTestHelper testHelper;
    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() {
        reservationRepository = new JdbcReservationRepository(jdbcTemplate);
        testHelper = new RepositoryTestHelper(jdbcTemplate);
    }

    @DisplayName("사용자의 방탈출 예약 시간 추가를 테스트합니다.")
    @Test
    void save_user_reservation() {
        Long timeId = testHelper.insertReservationTime(LocalTime.of(9, 0));
        Long themeId = testHelper.insertTheme("theme name", "theme description", "theme img url");

        Reservation reservation = Reservation.builder()
                .name("name")
                .date(LocalDate.of(2026, 5, 4))
                .themeId(themeId)
                .timeId(timeId)
                .build();

        Reservation savedReservation = reservationRepository.save(reservation);

        SoftAssertions.assertSoftly(assertSoftly -> {
            assertSoftly.assertThat(savedReservation.getName()).isEqualTo("name");
            assertSoftly.assertThat(savedReservation.getDate()).isEqualTo(LocalDate.of(2026, 5, 4));
            assertSoftly.assertThat(savedReservation.getThemeId()).isEqualTo(themeId);
            assertSoftly.assertThat(savedReservation.getTimeId()).isEqualTo(timeId);
        });
    }

    @DisplayName("예약의 날짜와 시간을 변경할 수 있다.")
    @Test
    void update_reservation() {
        Long timeId = testHelper.insertReservationTime(LocalTime.of(9, 0));
        Long newTimeId = testHelper.insertReservationTime(LocalTime.of(10, 0));
        Long themeId = testHelper.insertTheme("theme name", "theme description", "theme img url");
        Reservation saved = reservationRepository.save(Reservation.builder()
                .name("타스")
                .date(LocalDate.of(2026, 5, 4))
                .themeId(themeId)
                .timeId(timeId)
                .build());

        reservationRepository.update(saved.update(LocalDate.of(2026, 5, 5), newTimeId));

        Optional<Reservation> updated = reservationRepository.findById(saved.getId());
        assertThat(updated).isPresent();
        assertThat(updated.get().getDate()).isEqualTo(LocalDate.of(2026, 5, 5));
        assertThat(updated.get().getTimeId()).isEqualTo(newTimeId);
    }

    @DisplayName("존재하지 않는 예약 id로 변경 시 예외가 발생할 수 있다.")
    @Test
    void update_reservation_not_found() {
        assertThatThrownBy(() -> reservationRepository.update(Reservation.builder()
                .id(999L)
                .name("타스")
                .date(LocalDate.of(2026, 5, 4))
                .themeId(1L)
                .timeId(1L)
                .build()))
                .isExactlyInstanceOf(NotFoundException.class)
                .hasMessage("해당하는 ID(999)의 예약이 존재하지 않습니다.");
    }

}
