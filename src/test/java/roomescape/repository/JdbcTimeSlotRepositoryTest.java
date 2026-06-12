package roomescape.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatCode;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.jdbc.Sql;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationSlot;
import roomescape.domain.ReservationStatus;
import roomescape.domain.Theme;
import roomescape.domain.TimeSlot;

@JdbcTest
@Sql(scripts = "/test-setup.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
class JdbcTimeSlotRepositoryTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private JdbcTimeSlotRepository timeRepository;
    private JdbcThemeRepository themeRepository;
    private JdbcReservationRepository reservationRepository;
    private JdbcReservationSlotRepository reservationSlotRepository;

    @BeforeEach
    void setUp() {
        timeRepository = new JdbcTimeSlotRepository(jdbcTemplate);
        themeRepository = new JdbcThemeRepository(jdbcTemplate);
        reservationRepository = new JdbcReservationRepository(jdbcTemplate);
        reservationSlotRepository = new JdbcReservationSlotRepository(jdbcTemplate);
    }

    @Test
    @DisplayName("예약 시간을 저장하고 영속화된 객체를 반환한다.")
    void 예약_시간_저장() {
        TimeSlot timeSlot = new TimeSlot(LocalTime.of(10, 0));
        TimeSlot savedTimeSlot = timeRepository.save(timeSlot);
        assertThat(savedTimeSlot.getId()).isPositive();
    }

    @Test
    @DisplayName("식별자로 예약 시간 객체를 조회한다.")
    void 식별자로_예약_시간_조회() {
        TimeSlot savedTimeSlot = timeRepository.save(new TimeSlot(LocalTime.of(10, 0)));
        Optional<TimeSlot> foundTimeSlot = timeRepository.findById(savedTimeSlot.getId());
        assertThat(foundTimeSlot).isPresent();
        assertThat(foundTimeSlot.get().getStartAt()).isEqualTo(LocalTime.of(10, 0));
    }

    @Test
    @DisplayName("존재하는 예약 시간을 삭제한다.")
    void 존재하는_예약_시간_삭제() {
        int defaultSize = timeRepository.findAll().size();
        TimeSlot savedTimeSlot = timeRepository.save(new TimeSlot(LocalTime.of(10, 0)));
        timeRepository.deleteById(savedTimeSlot.getId());
        assertThat(timeRepository.findAll().size() == defaultSize).isTrue();
    }

    @Test
    @DisplayName("존재하지 않는 예약 시간을 삭제해도 예외가 발생하지 않는다.")
    void 존재하지_않는_예약_시간_삭제() {
        assertThatCode(() -> timeRepository.deleteById(999L))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("예약이 존재하는 시간을 삭제할 수 없다.")
    void 예약이_존재하는_시간_삭제_예외_발생() {
        TimeSlot savedTimeSlot = timeRepository.save(new TimeSlot(LocalTime.of(10, 0)));
        Theme savedTheme = themeRepository.save(new Theme("공포", "설명", "url"));
        ReservationSlot slot = reservationSlotRepository.save(
                new ReservationSlot(LocalDate.now().plusDays(1), savedTimeSlot, savedTheme));
        reservationRepository.save(
                new Reservation(null, "브라운", slot, LocalDateTime.now(), ReservationStatus.RESERVED));

        assertThatThrownBy(() -> timeRepository.deleteById(savedTimeSlot.getId()))
                .isInstanceOf(DataIntegrityViolationException.class);
    }
}
