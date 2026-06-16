package roomescape.dao;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.TestPropertySource;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationStatus;
import roomescape.domain.ReservationTime;
import roomescape.domain.Theme;

@DataJpaTest
@TestPropertySource(properties = {
        "spring.jpa.hibernate.ddl-auto=none",
        "spring.sql.init.mode=always"
})
class ReservationRepositoryTest {

    @Autowired private ReservationRepository reservationRepository;
    @Autowired private ReservationTimeRepository reservationTimeRepository;
    @Autowired private ThemeRepository themeRepository;

    private ReservationTime time() {
        return reservationTimeRepository.findById(1L).orElseThrow();
    }

    private Theme theme() {
        return themeRepository.findById(1L).orElseThrow();
    }

    @Test
    void findAllByStatus_CONFIRMED_페이징_조회() {
        PageRequest page = PageRequest.of(0, 5, Sort.by(Sort.Direction.DESC, "date", "id"));
        List<Reservation> result = reservationRepository.findAllByStatus(ReservationStatus.CONFIRMED, page);

        assertThat(result).isNotEmpty();
        assertThat(result).allMatch(r -> r.getStatus() == ReservationStatus.CONFIRMED);
    }

    @Test
    void countByStatus_CONFIRMED_건수() {
        long count = reservationRepository.countByStatus(ReservationStatus.CONFIRMED);

        assertThat(count).isPositive();
    }

    @Test
    void existsByTime_Id_사용중이면_true() {
        assertThat(reservationRepository.existsByTime_Id(3L)).isTrue();
    }

    @Test
    void existsByTime_Id_미사용이면_false() {
        assertThat(reservationRepository.existsByTime_Id(12L)).isFalse();
    }

    @Test
    void existsByTheme_Id_사용중이면_true() {
        assertThat(reservationRepository.existsByTheme_Id(1L)).isTrue();
    }

    @Test
    void existsByTheme_Id_미사용이면_false() {
        assertThat(reservationRepository.existsByTheme_Id(99L)).isFalse();
    }

    @Test
    void existsByDateAndTime_IdAndTheme_IdAndStatus_존재하면_true() {
        assertThat(reservationRepository.existsByDateAndTime_IdAndTheme_IdAndStatus(
                LocalDate.of(2026, 4, 29), 3L, 1L, ReservationStatus.CONFIRMED)).isTrue();
    }

    @Test
    void existsByDateAndTime_IdAndTheme_IdAndStatus_없으면_false() {
        assertThat(reservationRepository.existsByDateAndTime_IdAndTheme_IdAndStatus(
                LocalDate.of(2026, 12, 31), 3L, 1L, ReservationStatus.CONFIRMED)).isFalse();
    }

    @Test
    void findByNameAndStatus_이름과_상태로_조회() {
        List<Reservation> result = reservationRepository.findByNameAndStatus("김철수", ReservationStatus.CONFIRMED);

        assertThat(result).isNotEmpty();
        assertThat(result).allMatch(r -> r.getName().equals("김철수"));
    }

    @Test
    void findByIdAndStatus_WAITING_상태만_반환() {
        Reservation waiting = reservationRepository.save(
                new Reservation("브리", LocalDate.of(2026, 12, 31), LocalDateTime.now(), time(), theme(), ReservationStatus.WAITING));

        assertThat(reservationRepository.findByIdAndStatus(waiting.getId(), ReservationStatus.WAITING)).isPresent();
        assertThat(reservationRepository.findByIdAndStatus(1L, ReservationStatus.WAITING)).isEmpty();
    }

    @Test
    void findFirstByDate_첫번째_대기자_반환() {
        LocalDate date = LocalDate.of(2026, 12, 31);
        Reservation first = reservationRepository.save(
                new Reservation("브리", date, LocalDateTime.of(2026, 12, 1, 9, 0), time(), theme(), ReservationStatus.WAITING));
        reservationRepository.save(
                new Reservation("이영희", date, LocalDateTime.of(2026, 12, 1, 10, 0), time(), theme(), ReservationStatus.WAITING));

        Optional<Reservation> found = reservationRepository.findFirstByDateAndTime_IdAndTheme_IdAndStatusOrderByCreatedAtAscIdAsc(
                date, 1L, 1L, ReservationStatus.WAITING);

        assertThat(found).isPresent();
        assertThat(found.get().getId()).isEqualTo(first.getId());
    }

    @Test
    void countWaitingBefore_대기_순번_계산() {
        LocalDate date = LocalDate.of(2026, 12, 31);
        LocalDateTime firstTime = LocalDateTime.of(2026, 12, 1, 9, 0);
        LocalDateTime secondTime = LocalDateTime.of(2026, 12, 1, 10, 0);
        reservationRepository.save(new Reservation("브리", date, firstTime, time(), theme(), ReservationStatus.WAITING));
        Reservation second = reservationRepository.save(new Reservation("이영희", date, secondTime, time(), theme(), ReservationStatus.WAITING));

        long before = reservationRepository.countWaitingBefore(date, 1L, 1L, ReservationStatus.WAITING, secondTime, second.getId());

        assertThat(before).isEqualTo(1L);
    }

    @Test
    void updateStatus_WAITING에서_CONFIRMED로_전환() {
        Reservation waiting = reservationRepository.save(
                new Reservation("브리", LocalDate.of(2026, 12, 31), LocalDateTime.now(), time(), theme(), ReservationStatus.WAITING));

        reservationRepository.updateStatus(waiting.getId(), ReservationStatus.CONFIRMED);

        Reservation updated = reservationRepository.findById(waiting.getId()).orElseThrow();
        assertThat(updated.getStatus()).isEqualTo(ReservationStatus.CONFIRMED);
    }

    @Test
    void updateDateAndTime_날짜와_시간_수정() {
        Reservation saved = reservationRepository.save(
                new Reservation("브라운", LocalDate.of(2026, 12, 31), LocalDateTime.now(), time(), theme()));
        ReservationTime newTime = reservationTimeRepository.findById(2L).orElseThrow();
        LocalDate newDate = LocalDate.of(2027, 1, 10);

        reservationRepository.updateDateAndTime(saved.getId(), newDate, newTime);

        Reservation updated = reservationRepository.findById(saved.getId()).orElseThrow();
        assertThat(updated.getDate()).isEqualTo(newDate);
        assertThat(updated.getTime().getId()).isEqualTo(2L);
    }
}
