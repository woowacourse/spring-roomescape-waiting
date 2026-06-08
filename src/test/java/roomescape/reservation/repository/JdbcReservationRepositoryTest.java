package roomescape.reservation.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.time.LocalTime;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.jdbc.core.JdbcTemplate;
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

    @DisplayName("이름과 예약 슬롯이 모두 일치하는 예약의 존재 여부를 조회합니다.")
    @Test
    void exists_by_name_and_schedule() {
        Long timeId = testHelper.insertReservationTime(LocalTime.of(9, 0));
        Long themeId = testHelper.insertTheme("theme name", "theme description", "theme img url");
        LocalDate date = LocalDate.of(2026, 5, 4);
        reservationRepository.save(Reservation.builder()
                .name("카야")
                .date(date)
                .themeId(themeId)
                .timeId(timeId)
                .build());

        Boolean exists = reservationRepository.existsByNameAndDateAndThemeAndTime(
                "카야",
                date,
                themeId,
                timeId
        );
        Boolean existsWithOtherName = reservationRepository.existsByNameAndDateAndThemeAndTime(
                "스타크",
                date,
                themeId,
                timeId
        );

        assertThat(exists).isTrue();
        assertThat(existsWithOtherName).isFalse();
    }
}
