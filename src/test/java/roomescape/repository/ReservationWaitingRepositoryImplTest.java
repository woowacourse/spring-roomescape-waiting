package roomescape.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.junit.jupiter.api.Assertions.assertAll;

import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import roomescape.domain.Member;
import roomescape.domain.ReservationTheme;
import roomescape.domain.ReservationTime;
import roomescape.domain.ReservationWaiting;
import roomescape.repository.jpa.MemberJpaRepository;
import roomescape.repository.jpa.ReservationThemeJpaRepository;
import roomescape.repository.jpa.ReservationTimeJpaRepository;

@DataJpaTest
@Import(ReservationWaitingRepositoryImpl.class)
class ReservationWaitingRepositoryImplTest {

    @Autowired
    private ReservationWaitingRepository reservationWaitingRepository;

    @Autowired
    private ReservationThemeJpaRepository themeJpaRepository;

    @Autowired
    private MemberJpaRepository memberJpaRepository;

    @Autowired
    private ReservationTimeJpaRepository timeJpaRepository;


    @Test
    void saveTest() {
        //given
        final ReservationTime reservationTime = timeJpaRepository.findById(1L).orElse(null);
        final ReservationTheme reservationTheme = themeJpaRepository.findById(1L).orElse(null);
        final Member member = memberJpaRepository.findById(1L).orElse(null);
        final ReservationWaiting reservationWaiting = new ReservationWaiting(member, LocalDate.now(), reservationTime,
                reservationTheme);

        //when
        final ReservationWaiting expected = reservationWaitingRepository.save(reservationWaiting);

        //then
        assertAll(
                () -> assertThat(expected.getTheme().getName()).isEqualTo(reservationTheme.getName()),
                () -> assertThat(expected.getMember().getName()).isEqualTo(member.getName())
        );
    }

    @Test
    void findByThemeIdAndTimeIdAndDateTest() {
        //given
        final ReservationTime reservationTime = timeJpaRepository.findById(1L).orElse(null);
        final ReservationTheme reservationTheme = themeJpaRepository.findById(1L).orElse(null);
        final Member member = memberJpaRepository.findById(1L).orElse(null);
        final ReservationWaiting reservationWaiting = new ReservationWaiting(member, LocalDate.of(2025, 5,23), reservationTime,
                reservationTheme);
        final ReservationWaiting saved = reservationWaitingRepository.save(reservationWaiting);

        //when
        final ReservationWaiting expected = reservationWaitingRepository.findByThemeIdAndTimeIdAndDate(
                reservationTheme.getId(), reservationTime.getId(), LocalDate.of(2025, 5, 23)).get();


        //then
        assertThat(expected.getId()).isEqualTo(saved.getId());
    }

    @Test
    void deleteByIdTest() {
        //given
        final ReservationTime reservationTime = timeJpaRepository.findById(1L).orElse(null);
        final ReservationTheme reservationTheme = themeJpaRepository.findById(1L).orElse(null);
        final Member member = memberJpaRepository.findById(1L).orElse(null);
        final ReservationWaiting reservationWaiting = new ReservationWaiting(member, LocalDate.of(2025, 5,23), reservationTime,
                reservationTheme);
        final ReservationWaiting saved = reservationWaitingRepository.save(reservationWaiting);

        //should
        assertThatCode(() -> reservationWaitingRepository.deleteById(saved.getId())).doesNotThrowAnyException();
    }

    @Test
    void existsByIdTest() {
        //given
        final ReservationTime reservationTime = timeJpaRepository.findById(1L).orElse(null);
        final ReservationTheme reservationTheme = themeJpaRepository.findById(1L).orElse(null);
        final Member member = memberJpaRepository.findById(1L).orElse(null);
        final ReservationWaiting reservationWaiting = new ReservationWaiting(member, LocalDate.of(2025, 5,23), reservationTime,
                reservationTheme);
        final ReservationWaiting saved = reservationWaitingRepository.save(reservationWaiting);

        //when
        final boolean expected = reservationWaitingRepository.existsById(saved.getId());

        //then
        assertThat(expected).isTrue();
    }

    @Test
    void existsByMemberIdAndThemeIdAndTimeIdAndDateTest() {
        //given
        final ReservationTime reservationTime = timeJpaRepository.findById(1L).orElse(null);
        final ReservationTheme reservationTheme = themeJpaRepository.findById(1L).orElse(null);
        final Member member = memberJpaRepository.findById(1L).orElse(null);
        final ReservationWaiting reservationWaiting = new ReservationWaiting(member, LocalDate.of(2025, 5,23), reservationTime,
                reservationTheme);
        reservationWaitingRepository.save(reservationWaiting);

        //when
        final boolean expected = reservationWaitingRepository.existsByMemberIdAndThemeIdAndTimeIdAndDate(member.getId(),
                reservationTheme.getId(), reservationTime.getId(), LocalDate.of(2025, 5, 23));

        //then
        assertThat(expected).isTrue();
    }

    @Test
    void findByMemberIdTest() {
        //given
        final ReservationTime reservationTime = timeJpaRepository.findById(1L).orElse(null);
        final ReservationTheme reservationTheme = themeJpaRepository.findById(1L).orElse(null);
        final Member member = memberJpaRepository.findById(1L).orElse(null);
        final ReservationWaiting reservationWaiting = new ReservationWaiting(member, LocalDate.of(2025, 5,23), reservationTime,
                reservationTheme);
        reservationWaitingRepository.save(reservationWaiting);

        //when
        final List<ReservationWaiting> expected = reservationWaitingRepository.findByMemberId(member.getId());

        //then
        assertThat(expected).hasSizeGreaterThanOrEqualTo(1);
    }

    @Test
    void findWaitingOrderByIdTest() {
        //given
        final ReservationTime reservationTime1 = timeJpaRepository.findById(1L).orElse(null);
        final ReservationTheme reservationTheme1 = themeJpaRepository.findById(1L).orElse(null);
        final Member member1 = memberJpaRepository.findById(1L).orElse(null);
        final ReservationWaiting reservationWaiting1 = new ReservationWaiting(member1, LocalDate.of(2025, 5,23), reservationTime1,
                reservationTheme1);

        final ReservationTime reservationTime2 = timeJpaRepository.findById(1L).orElse(null);
        final ReservationTheme reservationTheme2 = themeJpaRepository.findById(1L).orElse(null);
        final Member member2 = memberJpaRepository.findById(2L).orElse(null);
        final ReservationWaiting reservationWaiting2 = new ReservationWaiting(member2, LocalDate.of(2025, 5,23), reservationTime2,
                reservationTheme2);
        reservationWaitingRepository.save(reservationWaiting1);
        reservationWaitingRepository.save(reservationWaiting2);

        //when
        final int expected1 = reservationWaitingRepository.findWaitingOrderById(reservationWaiting1.getId());
        final int expected2 = reservationWaitingRepository.findWaitingOrderById(reservationWaiting2.getId());

        //then
        assertAll(
                () -> assertThat(expected1).isEqualTo(1),
                () -> assertThat(expected2).isEqualTo(2)
        );
    }
}
