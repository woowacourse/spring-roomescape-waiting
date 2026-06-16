package roomescape.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import roomescape.domain.reservation.Reservation;
import roomescape.domain.reservationslot.ReservationSlot;
import roomescape.repository.reservation.ReservationRepository;
import roomescape.domain.reservationtime.ReservationTime;
import roomescape.repository.reservationtime.ReservationTimeRepository;
import roomescape.repository.reservationslot.ReservationSlotRepository;
import roomescape.domain.theme.Theme;
import roomescape.repository.theme.ThemeRepository;

@SpringBootTest
class JdbcReservationRepositoryTest {

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private ReservationSlotRepository reservationSlotRepository;

    @Autowired
    private ReservationTimeRepository reservationTimeRepository;

    @Autowired
    private ThemeRepository themeRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setup() {
        clearTables();
    }

    @Test
    @DisplayName("예약 저장")
    void reservation_save_test() {
        //given
        String name = "쿠다";
        LocalDate date = LocalDate.parse("2023-08-05");
        LocalTime time = LocalTime.parse("10:00");
        LocalDateTime createdAt = LocalDateTime.parse("2023-08-04T12:00:00");
        Theme theme = createTheme("미술관의 밤");

        ReservationTime reservationTime = reservationTimeRepository.save(ReservationTime.createNew(time));

        ReservationSlot slot = reservationSlotRepository.save(new ReservationSlot(date, theme, reservationTime));
        Reservation reservation = Reservation.reserve(name, slot, createdAt);
        //when
        Reservation result = reservationRepository.save(reservation);
        Reservation saved = reservationRepository.findById(result.getId())
                .orElseThrow();

        // then
        assertThat(result).isEqualTo(saved);
        assertThat(saved.getCreatedAt()).isEqualTo(createdAt);
    }

    @Test
    @DisplayName("예약 저장 중복 예외")
    void reservation_save_duplicate_test() {
        // given
        LocalDate date = LocalDate.parse("2026-08-06");
        LocalTime time = LocalTime.parse("10:00");
        Theme theme = createTheme("미술관의 밤");

        ReservationTime reservationTime = reservationTimeRepository.save(ReservationTime.createNew(time));

        // when & then
        assertThrows(PersistenceConflictException.class, () -> {
            ReservationSlot slot = reservationSlotRepository.save(new ReservationSlot(date, theme, reservationTime));
            reservationRepository.save(Reservation.reserve("쿠다", slot, LocalDateTime.now()));
            reservationRepository.save(Reservation.reserve("아루", slot, LocalDateTime.now()));
        });
    }

    @Test
    @DisplayName("예약 전체 조회")
    void reservation_findAll_test() {
        //given
        LocalDate date = LocalDate.parse("2026-08-06");
        LocalTime time = LocalTime.parse("10:00");
        Theme theme = createTheme("미술관의 밤");

        ReservationTime reservationTime = reservationTimeRepository.save(ReservationTime.createNew(time));
        ReservationSlot slot = reservationSlotRepository.save(new ReservationSlot(date, theme, reservationTime));
        reservationRepository.save(Reservation.reserve("쿠다", slot, LocalDateTime.now()));

        //when
        List<Reservation> reservations = reservationRepository.findAll();

        //then
        assertThat(reservations.size()).isEqualTo(1);
    }

    @Test
    @DisplayName("예약 삭제")
    void reservation_delete_test() {
        // given
        LocalDate date = LocalDate.parse("2026-08-06");
        LocalTime time = LocalTime.parse("10:00");
        Theme theme = createTheme("미술관의 밤");

        ReservationTime reservationTime = reservationTimeRepository.save(ReservationTime.createNew(time));

        ReservationSlot slot = reservationSlotRepository.save(new ReservationSlot(date, theme, reservationTime));
        reservationRepository.save(Reservation.reserve("쿠다", slot, LocalDateTime.now()));

        int beforeSize = reservationRepository.findAll().size();

        Reservation reservation = reservationRepository.findAll()
                .stream()
                .findFirst()
                .orElseThrow();

        // when
        reservationRepository.delete(reservation);

        // then
        int afterSize = reservationRepository.findAll().size();

        assertThat(afterSize).isEqualTo(beforeSize - 1);
    }

    @Test
    @DisplayName("예약 슬롯으로 예약을 조회한다")
    void findBySlot() {
        // given
        LocalDate date = LocalDate.parse("2026-08-06");
        Theme theme = createTheme("미술관의 밤");
        ReservationTime reservationTime = reservationTimeRepository.save(
                ReservationTime.createNew(LocalTime.parse("10:00"))
        );
        ReservationSlot slot = reservationSlotRepository.save(new ReservationSlot(date, theme, reservationTime));
        Reservation saved = reservationRepository.save(Reservation.reserve("쿠다", slot, LocalDateTime.now()));

        // when
        Reservation found = reservationRepository.findBySlot(new ReservationSlot(date, theme, reservationTime))
                .orElseThrow();

        // then
        assertThat(found).isEqualTo(saved);
    }

    @Test
    @DisplayName("날짜와 테마로 예약 목록을 조회한다")
    void findByDateAndTheme() {
        // given
        LocalDate date = LocalDate.parse("2026-08-06");
        Theme theme = createTheme("미술관의 밤");
        Theme otherTheme = createTheme("잠실의 밤");
        ReservationTime ten = reservationTimeRepository.save(ReservationTime.createNew(LocalTime.parse("10:00")));
        ReservationTime eleven = reservationTimeRepository.save(ReservationTime.createNew(LocalTime.parse("11:00")));
        ReservationSlot slot = reservationSlotRepository.save(new ReservationSlot(date, theme, ten));
        ReservationSlot otherTimeSlot = reservationSlotRepository.save(new ReservationSlot(date, theme, eleven));
        ReservationSlot otherThemeSlot = reservationSlotRepository.save(new ReservationSlot(date, otherTheme, ten));
        Reservation expected = reservationRepository.save(Reservation.reserve("쿠다", slot, LocalDateTime.now()));
        Reservation sameThemeReservation = reservationRepository.save(
                Reservation.reserve("아루", otherTimeSlot, LocalDateTime.now())
        );
        reservationRepository.save(Reservation.reserve("도기", otherThemeSlot, LocalDateTime.now()));

        // when
        List<Reservation> reservations = reservationRepository.findByDateAndTheme(date, theme);

        // then
        assertThat(reservations).containsExactlyInAnyOrder(expected, sameThemeReservation);
    }

    @Test
    @DisplayName("예약 시간으로 예약 존재 여부를 확인한다")
    void existsByTime() {
        // given
        LocalDate date = LocalDate.parse("2026-08-06");
        Theme theme = createTheme("미술관의 밤");
        ReservationTime reservedTime = reservationTimeRepository.save(
                ReservationTime.createNew(LocalTime.parse("10:00"))
        );
        ReservationTime emptyTime = reservationTimeRepository.save(
                ReservationTime.createNew(LocalTime.parse("11:00"))
        );
        ReservationSlot slot = reservationSlotRepository.save(new ReservationSlot(date, theme, reservedTime));
        reservationRepository.save(Reservation.reserve("쿠다", slot, LocalDateTime.now()));

        // when & then
        assertThat(reservationRepository.existsByTime(reservedTime)).isTrue();
        assertThat(reservationRepository.existsByTime(emptyTime)).isFalse();
    }

    private void clearTables() {
        jdbcTemplate.update("DELETE FROM reservation");
        jdbcTemplate.update("DELETE FROM reservation_slot");
        jdbcTemplate.update("DELETE FROM reservation_time");
        jdbcTemplate.update("DELETE FROM theme");
        jdbcTemplate.update("ALTER TABLE reservation ALTER COLUMN id RESTART WITH 1");
        jdbcTemplate.update("ALTER TABLE reservation_slot ALTER COLUMN id RESTART WITH 1");
        jdbcTemplate.update("ALTER TABLE reservation_time ALTER COLUMN id RESTART WITH 1");
        jdbcTemplate.update("ALTER TABLE theme ALTER COLUMN id RESTART WITH 1");
    }

    private Theme createTheme(final String name) {
        return themeRepository.save(
                Theme.createNew(name, "추리 테마", "https://example.com/theme.png")
        );
    }

}
