package roomescape.reservation.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static roomescape.common.Constant.예약날짜_내일;
import static roomescape.member.role.Role.ADMIN;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageRequest;
import roomescape.member.domain.Email;
import roomescape.member.domain.Member;
import roomescape.member.domain.Name;
import roomescape.member.domain.Password;
import roomescape.member.repository.MemberRepository;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationStatus;
import roomescape.theme.domain.Theme;
import roomescape.theme.repository.ThemeRepository;
import roomescape.time.domain.ReservationTime;
import roomescape.time.repository.ReservationTimeRepository;

@DataJpaTest
class ReservationJpaRepositoryTest {

    @Autowired
    ReservationRepository reservationRepository;

    @Autowired
    MemberRepository memberRepository;

    @Autowired
    ReservationTimeRepository reservationTimeRepository;

    @Autowired
    ThemeRepository themeRepository;

    private Member member;
    private ReservationTime reservationTime;
    private Theme theme;
    private Reservation reservation;

    @BeforeEach
    void setUp() {
        member = memberRepository.save(
                new Member(new Name("매트"), new Email("matt@kakao.com"), new Password("1234"), ADMIN));
        reservationTime = reservationTimeRepository.save(
                ReservationTime.create(LocalTime.of(10, 0))
        );
        theme = themeRepository.save(new Theme("공포", "ss", "ss"));
        reservation = reservationRepository.save(
                Reservation.create(예약날짜_내일.getDate(), reservationTime, theme, member, ReservationStatus.RESERVATION));
        reservationRepository.save(
                Reservation.create(LocalDate.now().minusDays(1L), reservationTime, theme, member,
                        ReservationStatus.RESERVATION));
    }

    @Test
    void 선택한_옵션별_예약을_조회한다() {
        //when
        List<Reservation> reservations = reservationRepository.findByFilter(member.getId(), theme.getId(),
                LocalDate.now(),
                LocalDate.now().plusDays(2));

        //then
        assertThat(reservations.size()).isEqualTo(1);

    }

    @Test
    void 멤버별_예약을_조회한다() {
        //when
        List<Reservation> allByMemberId = reservationRepository.findAllByMemberId(member.getId());

        //then
        assertThat(allByMemberId.contains(reservation)).isTrue();
    }

    @Test
    void 인기_테마_10개를_조회한다() {
        //given
        Theme theme1 = themeRepository.save(new Theme("웃음", "aa", "aa"));
        Theme theme2 = themeRepository.save(new Theme("슬픔", "bb", "bb"));
        reservationRepository.save(Reservation.create(LocalDate.now().minusDays(2L), reservationTime, theme, member,
                ReservationStatus.RESERVATION));
        reservationRepository.save(Reservation.create(LocalDate.now().minusDays(3L), reservationTime, theme, member,
                ReservationStatus.RESERVATION));
        reservationRepository.save(Reservation.create(LocalDate.now().minusDays(4L), reservationTime, theme, member,
                ReservationStatus.RESERVATION));
        reservationRepository.save(Reservation.create(LocalDate.now().minusDays(5L), reservationTime, theme, member,
                ReservationStatus.RESERVATION));
        reservationRepository.save(Reservation.create(LocalDate.now().minusDays(6L), reservationTime, theme1, member,
                ReservationStatus.RESERVATION));
        reservationRepository.save(Reservation.create(LocalDate.now().minusDays(7L), reservationTime, theme1, member,
                ReservationStatus.RESERVATION));
        reservationRepository.save(Reservation.create(LocalDate.now().minusDays(8L), reservationTime, theme, member,
                ReservationStatus.RESERVATION));
        reservationRepository.save(Reservation.create(LocalDate.now().minusDays(9L), reservationTime, theme, member,
                ReservationStatus.RESERVATION));
        reservationRepository.save(Reservation.create(LocalDate.now().minusDays(10L), reservationTime, theme, member,
                ReservationStatus.RESERVATION));
        reservationRepository.save(Reservation.create(LocalDate.now().minusDays(11L), reservationTime, theme2, member,
                ReservationStatus.RESERVATION));
        reservationRepository.save(Reservation.create(LocalDate.now().minusDays(12L), reservationTime, theme2, member,
                ReservationStatus.RESERVATION));

        // when
        LocalDate endDate = LocalDate.now().minusDays(1L);
        LocalDate startDate = LocalDate.now().minusDays(7L);

        List<Theme> recentReservations = reservationRepository.findTopThemesByReservationCount(startDate,
                endDate, PageRequest.of(0, 10));

        //then
        assertThat(recentReservations).containsExactly(
                theme, theme1
        );
    }

    @Test
    void 같은_날짜_같은_시간에_중복_예약을_허용하지_않는다() {
        //when - then
        assertThatThrownBy(() ->
                reservationRepository.save(
                        Reservation.create(LocalDate.now().minusDays(1L), reservationTime, theme, member,
                                ReservationStatus.RESERVATION)))
                .isInstanceOf(DataIntegrityViolationException.class);

    }

}
