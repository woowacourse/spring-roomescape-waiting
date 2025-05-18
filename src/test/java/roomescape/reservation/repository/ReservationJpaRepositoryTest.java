package roomescape.reservation.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static roomescape.common.Constant.예약날짜_내일;
import static roomescape.member.role.Role.ADMIN;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import roomescape.member.domain.Email;
import roomescape.member.domain.Member;
import roomescape.member.domain.Name;
import roomescape.member.domain.Password;
import roomescape.member.service.MemberRepository;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationStatus;
import roomescape.reservation.service.ReservationRepository;
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
    void 최근_일주일_예약을_가져온다() {
        //given
        reservationRepository.save(Reservation.create(LocalDate.now().minusDays(2L), reservationTime, theme, member,
                ReservationStatus.RESERVATION));
        reservationRepository.save(Reservation.create(LocalDate.now().minusDays(3L), reservationTime, theme, member,
                ReservationStatus.RESERVATION));
        reservationRepository.save(Reservation.create(LocalDate.now().minusDays(4L), reservationTime, theme, member,
                ReservationStatus.RESERVATION));
        reservationRepository.save(Reservation.create(LocalDate.now().minusDays(5L), reservationTime, theme, member,
                ReservationStatus.RESERVATION));
        reservationRepository.save(Reservation.create(LocalDate.now().minusDays(6L), reservationTime, theme, member,
                ReservationStatus.RESERVATION));
        reservationRepository.save(Reservation.create(LocalDate.now().minusDays(7L), reservationTime, theme, member,
                ReservationStatus.RESERVATION));
        reservationRepository.save(Reservation.create(LocalDate.now().minusDays(8L), reservationTime, theme, member,
                ReservationStatus.RESERVATION));
        reservationRepository.save(Reservation.create(LocalDate.now().minusDays(9L), reservationTime, theme, member,
                ReservationStatus.RESERVATION));
        reservationRepository.save(Reservation.create(LocalDate.now().minusDays(10L), reservationTime, theme, member,
                ReservationStatus.RESERVATION));
        reservationRepository.save(Reservation.create(LocalDate.now().minusDays(11L), reservationTime, theme, member,
                ReservationStatus.RESERVATION));
        reservationRepository.save(Reservation.create(LocalDate.now().minusDays(12L), reservationTime, theme, member,
                ReservationStatus.RESERVATION));

        LocalDate endDate = LocalDate.now().minusDays(1L);
        LocalDate startDate = LocalDate.now().minusDays(7L);

        //when
        List<Reservation> reservations = reservationRepository.findAllByReservationDateBetween(startDate, endDate);

        //then
        assertThat(reservations.size()).isEqualTo(7);
    }

    @Test
    void 멤버별_예약을_조회한다() {
        //when
        List<Reservation> allByMemberId = reservationRepository.findAllByMemberId(member.getId());

        //then
        assertThat(allByMemberId.contains(reservation)).isTrue();
    }

}
