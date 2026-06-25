package roomescape.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.jdbc.Sql;
import roomescape.domain.Reservation;
import roomescape.domain.Session;
import roomescape.domain.Theme;
import roomescape.domain.TimeSlot;
import roomescape.exception.TimeSlotNotFoundException;
import roomescape.repository.mapper.DomainRowMapperFactory;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatCode;

@JdbcTest
@Sql(scripts = "/test-setup.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
class JdbcTimeSlotRepositoryTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private JdbcTimeSlotRepository timeRepository;
    private JdbcThemeRepository themeRepository;
    private JdbcReservationRepository reservationRepository;
    private JdbcSessionRepository jdbcSessionRepository;

    @BeforeEach
    void setUp() {
        DomainRowMapperFactory factory = new DomainRowMapperFactory();
        timeRepository = new JdbcTimeSlotRepository(jdbcTemplate, factory);
        themeRepository = new JdbcThemeRepository(jdbcTemplate, factory);
        reservationRepository = new JdbcReservationRepository(jdbcTemplate, factory);
        jdbcSessionRepository = new JdbcSessionRepository(jdbcTemplate, factory);
    }

    @Test
    @DisplayName("예약 시간을 저장하고 영속화된 객체를 반환한다.")
    void save() {
        TimeSlot timeSlot = TimeSlot.transientOf(LocalTime.of(10, 0));
        TimeSlot savedTimeSlot = timeRepository.save(timeSlot);
        assertThat(savedTimeSlot.getId()).isPositive();
    }

    @Test
    @DisplayName("식별자로 예약 시간 객체를 조회한다.")
    void findById() {
        TimeSlot savedTimeSlot = timeRepository.save(TimeSlot.transientOf(LocalTime.of(10, 0)));
        Optional<TimeSlot> foundTimeSlot = timeRepository.findById(savedTimeSlot.getId());
        assertThat(foundTimeSlot).isPresent();
    }

    @Test
    @DisplayName("존재하는 예약 시간을 삭제한다.")
    void deleteExisting() {
        int defaultSize = timeRepository.findAll().size();
        TimeSlot savedTimeSlot = timeRepository.save(TimeSlot.transientOf(LocalTime.of(10, 0)));
        timeRepository.deleteById(savedTimeSlot.getId());
        assertThat(timeRepository.findAll().size() == defaultSize).isTrue();
    }

    @Test
    @DisplayName("존재하지 않는 예약 시간을 삭제해도 예외가 발생하지 않는다.")
    void deleteNonExisting() {
        assertThatCode(() -> timeRepository.deleteById(999L)).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("존재하는 예약 시간 정보를 수정한다.")
    void updateExisting() {
        TimeSlot savedTimeSlot = timeRepository.save(TimeSlot.transientOf(LocalTime.of(10, 0)));
        TimeSlot updateTime = new TimeSlot(savedTimeSlot.getId(), LocalTime.of(12, 0));
        assertThat(timeRepository.update(updateTime).getStartAt()).isEqualTo(LocalTime.of(12, 0));
    }

    @Test
    @DisplayName("존재하지 않는 예약 시간을 수정하면 예외가 발생한다.")
    void updateNonExisting() {
        TimeSlot updateTime = new TimeSlot(999L, LocalTime.of(12, 0));
        assertThatThrownBy(() -> timeRepository.update(updateTime)).isInstanceOf(TimeSlotNotFoundException.class);
    }

    @Test
    @DisplayName("예약이 존재하는 시간을 삭제할 수 없다.")
    void deleteTimeSlot_WithReservation() {
        TimeSlot savedTimeSlot = timeRepository.save(TimeSlot.transientOf(LocalTime.of(10, 0)));
        Theme savedTheme = themeRepository.save(Theme.transientOf("공포", "설명", "url"));
        Session savedSlot = jdbcSessionRepository.save(Session.transientOf(LocalDate.now(), savedTimeSlot, savedTheme));
        reservationRepository.save(Reservation.transientOf("브라운", savedSlot, 0L, null));
        assertThatThrownBy(() -> timeRepository.deleteById(savedTimeSlot.getId())).isInstanceOf(DataIntegrityViolationException.class);
    }
}
