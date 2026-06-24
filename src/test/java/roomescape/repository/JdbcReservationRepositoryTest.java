package roomescape.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.jdbc.Sql;
import roomescape.domain.Reservation;
import roomescape.domain.Session;
import roomescape.domain.Theme;
import roomescape.domain.TimeSlot;
import roomescape.repository.mapper.DomainRowMapperFactory;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatCode;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

@JdbcTest
@Sql(scripts = "/test-setup.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
class JdbcReservationRepositoryTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private JdbcReservationRepository jdbcReservationRepository;
    private JdbcSessionRepository jdbcSessionRepository;
    private TimeSlot savedTimeSlot;
    private Theme savedTheme;

    @BeforeEach
    void setUp() {
        DomainRowMapperFactory factory = new DomainRowMapperFactory();
        jdbcReservationRepository = new JdbcReservationRepository(jdbcTemplate, factory);
        jdbcSessionRepository = new JdbcSessionRepository(jdbcTemplate, factory);
        insertDependencyData(factory);
    }

    private void insertDependencyData(DomainRowMapperFactory factory) {
        JdbcTimeSlotRepository timeRepo = new JdbcTimeSlotRepository(jdbcTemplate, factory);
        JdbcThemeRepository themeRepo = new JdbcThemeRepository(jdbcTemplate, factory);
        savedTimeSlot = timeRepo.save(new TimeSlot(1L, LocalTime.of(10, 0)));
        savedTheme = themeRepo.save(new Theme(1L, "공포", "귀신의 집 탈출", "https://test.com"));
    }

    private Session createSavedSlot() {
        return jdbcSessionRepository.save(Session.transientOf(LocalDate.now(), savedTimeSlot, savedTheme));
    }

    @Test
    @DisplayName("예약을 저장하고 영속화된 객체를 반환한다.")
    void save() {
        Reservation reservation = Reservation.transientOf("브라운", createSavedSlot(), 0L, null);
        Reservation savedReservation = jdbcReservationRepository.save(reservation);
        assertThat(savedReservation.getId()).isPositive();
    }

    @Test
    @DisplayName("식별자로 예약 객체를 조회한다.")
    void findById() {
        Reservation savedReservation = jdbcReservationRepository.save(Reservation.transientOf("브라운", createSavedSlot(), 0L, null));
        Optional<Reservation> foundReservation = jdbcReservationRepository.findById(savedReservation.getId());
        assertThat(foundReservation).isPresent();
        assertThat(foundReservation.get().getName()).isEqualTo("브라운");
    }

    @Test
    @DisplayName("모든 예약 객체 목록을 조회한다.")
    void findAll() {
        jdbcReservationRepository.save(Reservation.transientOf("브라운", createSavedSlot(), 0L, null));
        List<Reservation> reservations = jdbcReservationRepository.findAll();
        assertThat(reservations).hasSize(1);
    }

    @Test
    @DisplayName("식별자로 예약을 삭제한다.")
    void deleteById() {
        Reservation savedReservation = jdbcReservationRepository.save(Reservation.transientOf("브라운", createSavedSlot(), 0L, null));
        jdbcReservationRepository.deleteById(savedReservation.getId());
        assertThat(jdbcReservationRepository.findAll()).isEmpty();
    }

    @Test
    @DisplayName("특정 날짜, 시간, 테마에 해당하는 예약이 이미 존재하면 해당 예약을 반환한다.")
    void findByDateAndTimeIdAndThemeId() {
        Session session = createSavedSlot();
        System.out.println(session.getDate());
        System.out.println(session.getTheme());
        jdbcReservationRepository.save(Reservation.transientOf("브라운", session, 0L, null));
        Optional<Reservation> existingReservation = jdbcReservationRepository.findByDateAndTimeIdAndThemeId(
                LocalDate.now(), savedTimeSlot.getId(), savedTheme.getId()
        );
        assertThat(existingReservation).isPresent();
    }

    @Test
    @DisplayName("존재하는 예약을 변경 불가능한 날짜, 시간, 테마으로 수정 시도 시 예외가 발생한다.")
    void updateByDuplicatedDateAndTimeIdAndThemeId() {
        jdbcReservationRepository.save(Reservation.transientOf("브라운", createSavedSlot(), 0L, null));
        Session otherSlot = jdbcSessionRepository.save(Session.transientOf(LocalDate.now().plusDays(7), savedTimeSlot, savedTheme));
        Reservation newReservation = jdbcReservationRepository.save(Reservation.transientOf("네오", otherSlot, 0L, null));
        Session firstSlot = jdbcSessionRepository.findByDateAndTimeIdAndThemeId(LocalDate.now(), savedTimeSlot.getId(), savedTheme.getId()).get();
        Reservation updateReservation = new Reservation(newReservation.getId(), "네오", firstSlot, 0L, null);
        assertThatThrownBy(() -> jdbcReservationRepository.update(updateReservation)).isInstanceOf(DuplicateKeyException.class);
    }

    @Test
    @DisplayName("존재하는 예약을 삭제한다.")
    void deleteExisting() {
        Reservation saved = jdbcReservationRepository.save(Reservation.transientOf("브라운", createSavedSlot(), 0L, null));
        jdbcReservationRepository.deleteById(saved.getId());
        assertThat(jdbcReservationRepository.findAll()).isEmpty();
    }

    @Test
    @DisplayName("존재하지 않는 예약을 삭제해도 예외가 발생하지 않는다.")
    void deleteNonExisting() {
        assertThatCode(() -> jdbcReservationRepository.deleteById(999L)).doesNotThrowAnyException();
    }
}
