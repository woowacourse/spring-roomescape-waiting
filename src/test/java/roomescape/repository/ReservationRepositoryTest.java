package roomescape.repository;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import roomescape.domain.Member;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationTime;
import roomescape.domain.Slot;
import roomescape.domain.Theme;

@DataJpaTest
public class ReservationRepositoryTest {

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private ReservationTimeRepository reservationTimeRepository;

    @Autowired
    private ThemeRepository themeRepository;

    @Autowired
    private MemberRepository memberRepository;

    private ReservationTime reservationTime1;
    private ReservationTime reservationTime2;
    private ReservationTime reservationTime3;
    private Theme theme;
    private Member member;

    @BeforeEach
    void beforeEach() {
        reservationTime1 = reservationTimeRepository.save(new ReservationTime(LocalTime.of(10, 0)));
        reservationTime2 = reservationTimeRepository.save(new ReservationTime(LocalTime.of(11, 0)));
        reservationTime3 = reservationTimeRepository.save(new ReservationTime(LocalTime.of(12, 0)));
        theme = themeRepository.save(new Theme("방탈출1", "방탈출1 설명", "url.jpg"));
        member = memberRepository.save(new Member("fizz"));
    }

    @Test
    void saveTest() {
        Reservation reservationWithoutId = new Reservation(member,
                new Slot(LocalDate.of(2026, 5, 2), reservationTime1, theme));

        Reservation reservation = reservationRepository.save(reservationWithoutId);

        assertThat(reservation.getId()).isNotNull();
    }

    @Test
    void findByIdTest() {
        Reservation saved = reservationRepository.save(new Reservation(member,
                new Slot(LocalDate.of(2026, 5, 2), reservationTime1, theme)));

        Reservation reservation = reservationRepository.findById(saved.getId()).get();

        assertThat(reservation.getName()).isEqualTo("fizz");
        assertThat(reservation.getDate()).isEqualTo(LocalDate.of(2026, 5, 2));
        assertThat(reservation.getTime().getId()).isEqualTo(reservationTime1.getId());
        assertThat(reservation.getTheme().getId()).isEqualTo(theme.getId());
    }

    @Test
    void findByNameTest() {
        Member tree = memberRepository.save(new Member("tree"));

        reservationRepository.save(new Reservation(member,
                new Slot(LocalDate.of(2026, 5, 2), reservationTime1, theme)));
        reservationRepository.save(new Reservation(tree,
                new Slot(LocalDate.of(2026, 5, 2), reservationTime2, theme)));
        reservationRepository.save(new Reservation(member,
                new Slot(LocalDate.of(2026, 5, 2), reservationTime3, theme)));

        List<Reservation> reservations = reservationRepository.findByMember_Name("fizz");

        assertThat(reservations.size()).isEqualTo(2);
        assertThat(reservations.get(0).getName()).isEqualTo("fizz");
        assertThat(reservations.get(1).getName()).isEqualTo("fizz");

        assertThat(reservationRepository.findByMember_Name("user").size()).isEqualTo(0);
    }

    @Test
    void findAllTest() {
        reservationRepository.save(new Reservation(member,
                new Slot(LocalDate.of(2026, 5, 2), reservationTime1, theme)));
        reservationRepository.save(new Reservation(member,
                new Slot(LocalDate.of(2026, 5, 2), reservationTime2, theme)));

        List<Reservation> reservations = reservationRepository.findAll();

        assertThat(reservations.size()).isEqualTo(2);
    }

    @Test
    void deleteByIdTest() {
        Reservation saved = reservationRepository.save(new Reservation(member,
                new Slot(LocalDate.of(2026, 5, 2), reservationTime1, theme)));

        reservationRepository.deleteById(saved.getId());

        Assertions.assertThat(reservationRepository.count()).isEqualTo(0);
    }

    @Test
    void existsByTimeIdTest() {
        ReservationTime extraTime = reservationTimeRepository.save(new ReservationTime(LocalTime.of(13, 0)));

        boolean exist = reservationRepository.existsBySlot_Time_Id(extraTime.getId());
        assertThat(exist).isFalse();

        reservationRepository.save(new Reservation(member,
                new Slot(LocalDate.of(2026, 5, 2), extraTime, theme)));

        exist = reservationRepository.existsBySlot_Time_Id(extraTime.getId());
        assertThat(exist).isTrue();
    }

    @Test
    void existsByThemeIdTest() {
        Theme extraTheme = themeRepository.save(new Theme("방탈출2", "방탈출2 설명", "url.jpg"));

        boolean exist = reservationRepository.existsBySlot_Theme_Id(extraTheme.getId());
        assertThat(exist).isFalse();

        reservationRepository.save(new Reservation(member,
                new Slot(LocalDate.of(2026, 5, 2), reservationTime1, extraTheme)));

        exist = reservationRepository.existsBySlot_Theme_Id(extraTheme.getId());
        assertThat(exist).isTrue();
    }

    @Test
    void findByDateAndTimeIdAndThemeIdTest() {
        reservationRepository.save(new Reservation(member,
                new Slot(LocalDate.of(2026, 5, 2), reservationTime1, theme)));

        Optional<Reservation> slot = reservationRepository.findBySlot(
                LocalDate.of(2026, 5, 2), reservationTime1.getId(), theme.getId());

        assertThat(slot).isNotEmpty();
        assertThat(slot.get().getDate()).isEqualTo(LocalDate.of(2026, 5, 2));
        assertThat(slot.get().getName()).isEqualTo("fizz");
        assertThat(slot.get().getTime().getId()).isEqualTo(reservationTime1.getId());
        assertThat(slot.get().getTheme().getId()).isEqualTo(theme.getId());
    }
}
