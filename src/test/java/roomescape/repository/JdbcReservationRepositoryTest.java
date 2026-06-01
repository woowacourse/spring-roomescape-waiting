package roomescape.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatCode;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.jdbc.Sql;
import roomescape.domain.Reservation;
import roomescape.domain.Theme;
import roomescape.domain.TimeSlot;

@JdbcTest
@Sql(scripts = "/test-setup.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
class JdbcReservationRepositoryTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private JdbcReservationRepository jdbcReservationRepository;
    private TimeSlot savedTimeSlot;
    private Theme savedTheme;

    @BeforeEach
    void setUp() {
        jdbcReservationRepository = new JdbcReservationRepository(jdbcTemplate);
        insertDependencyData();
    }

    private void insertDependencyData() {
        JdbcTimeSlotRepository timeRepository = new JdbcTimeSlotRepository(jdbcTemplate);
        JdbcThemeRepository themeRepository = new JdbcThemeRepository(jdbcTemplate);
        savedTimeSlot = timeRepository.save(new TimeSlot(1L, LocalTime.of(10, 0)));
        savedTheme = themeRepository.save(new Theme(1L, "공포", "귀신의 집 탈출", "https://test.com"));
    }

    @Test
    @DisplayName("예약을 저장하고 영속화된 객체를 반환한다.")
    void 예약_저장() {
        Reservation reservation = new Reservation(null, "브라운", LocalDate.now(), savedTimeSlot, savedTheme);
        Reservation savedReservation = jdbcReservationRepository.save(reservation);
        assertThat(savedReservation.getId()).isPositive();
    }

    @Test
    @DisplayName("식별자로 예약 객체를 조회한다.")
    void 식별자로_예약_조회() {
        Reservation savedReservation = jdbcReservationRepository.save(new Reservation(null,
                "브라운",
                LocalDate.now(),
                savedTimeSlot,
                savedTheme
        ));
        Optional<Reservation> foundReservation = jdbcReservationRepository.findById(savedReservation.getId());
        assertThat(foundReservation).isPresent();
        assertThat(foundReservation.get().getName()).isEqualTo("브라운");
    }

    @Test
    @DisplayName("모든 예약 객체 목록을 조회한다.")
    void 전체_예약_조회() {
        jdbcReservationRepository.save(new Reservation(null,
                "브라운",
                LocalDate.now(), savedTimeSlot,
                savedTheme
        ));
        List<Reservation> reservations = jdbcReservationRepository.findAll();
        assertThat(reservations).hasSize(1);
    }

    @Test
    @DisplayName("식별자로 예약을 삭제한다.")
    void 식별자로_예약_삭제() {
        Reservation savedReservation = jdbcReservationRepository.save(new Reservation(null,
                "브라운",
                LocalDate.now(),
                savedTimeSlot,
                savedTheme
        ));
        jdbcReservationRepository.deleteById(savedReservation.getId());
        assertThat(jdbcReservationRepository.findAll()).isEmpty();
    }

    @Test
    @DisplayName("특정 날짜, 시간, 테마에 해당하는 예약이 이미 존재하면 해당 예약을 반환한다.")
    void 날짜_시간_테마로_예약_조회() {
        Reservation reservation = new Reservation(null, "브라운", LocalDate.now(), savedTimeSlot, savedTheme);
        jdbcReservationRepository.save(reservation);
        Optional<Reservation> existingReservation = jdbcReservationRepository.findByDateAndTimeIdAndThemeId(
                LocalDate.now(),
                savedTimeSlot.getId(),
                savedTheme.getId()
        );

        assertThat(existingReservation).isPresent();
        assertThat(existingReservation.get().getName()).isEqualTo("브라운");
    }

    @Test
    @DisplayName("존재하는 예약을 변경 불가능한 날짜, 시간, 테마으로 수정 시도 시 예외가 발생한다.")
    void 중복_날짜_시간_테마_예약_수정_예외_발생() {
        jdbcReservationRepository.save(new Reservation(null,
                "브라운",
                LocalDate.now(),
                savedTimeSlot,
                savedTheme
        ));

        Reservation newReservation = jdbcReservationRepository.save(
                new Reservation(null,
                        "네오",
                        LocalDate.now().plusDays(7),
                        savedTimeSlot,
                        savedTheme
                )
        );

        Reservation updateReservation = new Reservation(
                newReservation.getId(),
                "네오",
                LocalDate.now(),
                savedTimeSlot,
                savedTheme
        );

        assertThatThrownBy(
                () -> jdbcReservationRepository.update(updateReservation))
                .isInstanceOf(DuplicateKeyException.class);
    }

    @Test
    @DisplayName("존재하는 예약을 삭제한다.")
    void 존재하는_예약_삭제() {
        Reservation saved = jdbcReservationRepository.save(
                new Reservation(null, "브라운", LocalDate.now(), savedTimeSlot, savedTheme));
        jdbcReservationRepository.deleteById(saved.getId());
        assertThat(jdbcReservationRepository.findAll()).isEmpty();
    }

    @Test
    @DisplayName("존재하지 않는 예약을 삭제해도 예외가 발생하지 않는다.")
    void 존재하지_않는_예약_삭제() {
        assertThatCode(() -> jdbcReservationRepository.deleteById(999L))
                .doesNotThrowAnyException();
    }
}
