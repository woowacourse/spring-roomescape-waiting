package roomescape.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.jdbc.Sql;
import roomescape.domain.ReservationSlot;
import roomescape.domain.Theme;
import roomescape.domain.TimeSlot;
import roomescape.exception.DuplicateException;

@JdbcTest
@Sql(scripts = "/test-setup.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
class JdbcReservationSlotRepositoryTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private JdbcReservationSlotRepository reservationSlotRepository;
    private TimeSlot savedTimeSlot;
    private Theme savedTheme;

    @BeforeEach
    void setUp() {
        reservationSlotRepository = new JdbcReservationSlotRepository(jdbcTemplate);
        JdbcTimeSlotRepository timeSlotRepository = new JdbcTimeSlotRepository(jdbcTemplate);
        JdbcThemeRepository themeRepository = new JdbcThemeRepository(jdbcTemplate);

        savedTimeSlot = timeSlotRepository.save(new TimeSlot(LocalTime.of(10, 0)));
        savedTheme = themeRepository.save(new Theme("공포", "귀신의 집 탈출", "https://test.com"));
    }

    @Test
    @DisplayName("예약 슬롯을 저장하고 영속화된 객체를 반환한다.")
    void 예약_슬롯_저장() {
        ReservationSlot slot = new ReservationSlot(LocalDate.now().plusDays(1), savedTimeSlot, savedTheme);

        ReservationSlot savedSlot = reservationSlotRepository.save(slot);

        assertThat(savedSlot.getId()).isPositive();
        assertThat(savedSlot.getDate()).isEqualTo(slot.getDate());
        assertThat(savedSlot.getTimeSlot()).isEqualTo(savedTimeSlot);
        assertThat(savedSlot.getTheme()).isEqualTo(savedTheme);
    }

    @Test
    @DisplayName("식별자로 예약 슬롯을 조회한다.")
    void 식별자로_예약_슬롯_조회() {
        ReservationSlot savedSlot = reservationSlotRepository.save(
                new ReservationSlot(LocalDate.now().plusDays(1), savedTimeSlot, savedTheme)
        );

        Optional<ReservationSlot> foundSlot = reservationSlotRepository.findById(savedSlot.getId());

        assertThat(foundSlot).isPresent();
        assertThat(foundSlot.get().getDate()).isEqualTo(savedSlot.getDate());
    }

    @Test
    @DisplayName("날짜, 시간, 테마로 예약 슬롯을 조회한다.")
    void 날짜_시간_테마로_예약_슬롯_조회() {
        LocalDate date = LocalDate.now().plusDays(1);
        ReservationSlot savedSlot = reservationSlotRepository.save(
                new ReservationSlot(date, savedTimeSlot, savedTheme)
        );

        Optional<ReservationSlot> foundSlot = reservationSlotRepository.findByDateAndTimeIdAndThemeId(
                date,
                savedTimeSlot.getId(),
                savedTheme.getId()
        );

        assertThat(foundSlot).isPresent();
        assertThat(foundSlot.get().getId()).isEqualTo(savedSlot.getId());
    }

    @Test
    @DisplayName("같은 날짜, 시간, 테마의 예약 슬롯은 중복 저장할 수 없다.")
    void 예약_슬롯_중복_저장_예외_발생() {
        LocalDate date = LocalDate.now().plusDays(1);
        reservationSlotRepository.save(new ReservationSlot(date, savedTimeSlot, savedTheme));

        assertThatThrownBy(() -> reservationSlotRepository.save(new ReservationSlot(date, savedTimeSlot, savedTheme)))
                .isInstanceOf(DuplicateException.class);
    }
}
