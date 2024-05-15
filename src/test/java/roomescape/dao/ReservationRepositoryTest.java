package roomescape.dao;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import roomescape.domain.reservation.Reservation;
import roomescape.domain.reservation.ReservationDate;
import roomescape.domain.reservation.ReservationTime;
import roomescape.domain.reservation.Theme;
import roomescape.domain.user.Member;
import roomescape.fixture.MemberFixture;
import roomescape.fixture.ReservationTimeFixture;
import roomescape.fixture.ThemeFixture;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class ReservationRepositoryTest {
    @Autowired
    ReservationRepository sut;
    @Autowired
    ThemeRepository themeRepository;
    @Autowired
    MemberRepository memberRepository;
    @Autowired
    ReservationTimeRepository timeRepository;

    private Theme theme;
    private Member member;
    private ReservationTime time;

    @BeforeEach
    void setUp() {
        sut.deleteAll();
        theme = themeRepository.save(ThemeFixture.getDomain());
        member = memberRepository.save(MemberFixture.getDomain());
        time = timeRepository.save(ReservationTimeFixture.getDomain());
    }

    @Test
    void create() {
        final var reservation = sut.save(Reservation.fromComplete(null, "2024-10-10", time, theme, member));
        assertThat(reservation).isNotNull();
    }

    @Test
    void delete() {
        final var reservation = sut.save(Reservation.fromComplete(null, "2024-10-10", time, theme, member));
        sut.delete(reservation);
        final var result = sut.findById(reservation.getId());
        assertThat(result).isNotPresent();
    }

    @Test
    void getAll() {
        sut.save(Reservation.fromComplete(null, "2024-10-10", time, theme, member));
        sut.save(Reservation.fromComplete(null, "2024-10-11", time, theme, member));

        final var result = sut.findAll();
        assertThat(result).hasSize(2);
    }

    @Test
    void return_true_when_exist() {
        sut.save(Reservation.fromComplete(null, "2024-10-10", time, theme, member));
        final var result = sut.existsByThemeId(theme.getId());
        assertThat(result).isTrue();
    }

    @Test
    void return_false_when_not_exist() {
        sut.save(Reservation.fromComplete(null, "2024-10-10", time, theme, member));
        final var result = sut.existsByThemeId(-1L);
        assertThat(result).isFalse();
    }

    @Test
    void return_true_existsByReservationDateAndTimeId() {
        sut.save(Reservation.fromComplete(null, "2024-10-10", time, theme, member));
        final var result =
                sut.existsByDateAndTimeId(ReservationDate.from("2024-10-10"), time.getId());
        assertThat(result).isTrue();
    }

    @Test
    void return_false_not_existsByReservationDateAndTimeId() {
        sut.save(Reservation.fromComplete(null, "2024-10-10", time, theme, member));
        final var result =
                sut.existsByDateAndTimeId(ReservationDate.from("2024-10-09"), time.getId());
        assertThat(result).isFalse();
    }
    @Test
    void get_reservation_themeId_memberId_between_reservationDate(){
        final var newMember = memberRepository.save(MemberFixture.getDomain("alphaka@naver.com"));
        sut.save(Reservation.fromComplete(null, "2024-10-08", time, theme, member));
        sut.save(Reservation.fromComplete(null, "2024-10-09", time, theme, member));
        sut.save(Reservation.fromComplete(null, "2024-10-10", time, theme, member));
        sut.save(Reservation.fromComplete(null, "2024-10-11", time, theme, newMember));
        final var result =
                sut.getReservationByThemeIdAndMemberIdAndDateBetween(theme.getId(),member.getId(),
                        ReservationDate.from("2024-10-10"),ReservationDate.from("2024-10-11"));

        assertThat(result).hasSize(1);
    }

    @Test
    void find_all_by_member_id(){
        final var newMember = memberRepository.save(MemberFixture.getDomain("alphaka@naver.com"));
        sut.save(Reservation.fromComplete(null, "2024-10-08", time, theme, member));
        sut.save(Reservation.fromComplete(null, "2024-10-09", time, theme, member));
        sut.save(Reservation.fromComplete(null, "2024-10-10", time, theme, newMember));

        assertThat(sut.findAllByMemberId(member.getId()))
                .hasSize(2);
    }
}
