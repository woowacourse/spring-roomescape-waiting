package roomescape.reservation.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import roomescape.fixture.MemberFixture;
import roomescape.fixture.ReservationTimeFixture;
import roomescape.fixture.ThemeFixture;
import roomescape.member.domain.Member;
import roomescape.member.repository.MemberRepository;
import roomescape.reservation.model.Reservation;
import roomescape.reservationtime.model.ReservationTime;
import roomescape.reservationtime.repository.ReservationTimeRepository;
import roomescape.theme.model.Theme;
import roomescape.theme.repository.ThemeRepository;
import roomescape.util.JpaRepositoryTest;

@JpaRepositoryTest
class ReservationRepositoryTest {

    @Autowired
    private ReservationRepository reservationRepository;
    @Autowired
    private ReservationTimeRepository reservationTimeRepository;
    @Autowired
    private ThemeRepository themeRepository;
    @Autowired
    private MemberRepository memberRepository;

    @Test
    @DisplayName("저장된 모든 예약을 조회한다.")
    void findAll() {
        LocalDate date = LocalDate.parse("2024-11-11");
        Member member = memberRepository.save(MemberFixture.getOne());
        ReservationTime reservationTime = reservationTimeRepository.save(ReservationTimeFixture.getOne());
        Theme theme = themeRepository.save(ThemeFixture.getOne());
        Reservation reservation1 = reservationRepository.save(
                new Reservation(member, date, reservationTime, theme));
        Reservation reservation2 = reservationRepository.save(
                new Reservation(member, date.plusDays(1), reservationTime, theme));

        assertThat(reservationRepository.findAll())
                .containsExactlyInAnyOrder(reservation1, reservation2);
    }

    @Test
    @DisplayName("동일한 회원인 모든 예약을 조회한다.")
    void findAllByMemberId() {
        LocalDate date = LocalDate.parse("2024-11-11");

        Member member = memberRepository.save(MemberFixture.getOne());
        ReservationTime reservationTime = reservationTimeRepository.save(ReservationTimeFixture.getOne());
        Theme theme = themeRepository.save(ThemeFixture.getOne());
        Reservation reservation1 = reservationRepository.save(
                new Reservation(member, date, reservationTime, theme));
        Reservation reservation2 = reservationRepository.save(
                new Reservation(member, date.plusDays(1), reservationTime, theme));
        Reservation reservation3 = reservationRepository.save(
                new Reservation(member, date.plusDays(2), reservationTime, theme));

        assertThat(reservationRepository.findAllByMemberId(member.getId()))
                .containsExactlyInAnyOrder(reservation1, reservation2, reservation3);
    }

    @Test
    @DisplayName("동일한 날짜, 테마의 모든 예약을 조회한다.")
    void findAllByDateAndThemeId() {
        LocalDate date = LocalDate.parse("2024-11-11");

        Member member = memberRepository.save(MemberFixture.getOne());
        ReservationTime reservationTime = reservationTimeRepository.save(ReservationTimeFixture.getOne());
        Theme theme = themeRepository.save(ThemeFixture.getOne());
        Reservation reservation1 = reservationRepository.save(
                new Reservation(member, date, reservationTime, theme));
        Reservation reservation2 = reservationRepository.save(
                new Reservation(member, date, reservationTime, theme));
        Reservation reservation3 = reservationRepository.save(
                new Reservation(member, date.plusDays(2), reservationTime, theme));

        assertThat(
                reservationRepository.findAllByDateAndThemeId(
                        date,
                        reservation1.getTheme().getId()))
                .containsExactlyInAnyOrder(reservation1, reservation2);
    }

    @Test
    @DisplayName("동일한 회원, 테마, 해당하는 기간 내의 모든 예약을 조회한다.")
    void findAllByThemeIdAndMemberIdAndDateBetween() {
        LocalDate startDate = LocalDate.parse("2024-11-11");

        Member member = memberRepository.save(MemberFixture.getOne());
        ReservationTime reservationTime = reservationTimeRepository.save(ReservationTimeFixture.getOne());
        Theme theme = themeRepository.save(ThemeFixture.getOne());
        Reservation reservation1 = reservationRepository.save(
                new Reservation(member, startDate, reservationTime, theme));
        Reservation reservation2 = reservationRepository.save(
                new Reservation(member, startDate.plusDays(1), reservationTime, theme));
        Reservation reservation3 = reservationRepository.save(
                new Reservation(member, startDate.plusDays(2), reservationTime, theme));

        assertThat(
                reservationRepository.findAllByThemeIdAndMemberIdAndDateBetween(
                        theme.getId(),
                        member.getId(),
                        startDate,
                        startDate.plusDays(1)))
                .containsExactlyInAnyOrder(reservation1, reservation2);
    }

    @Test
    @DisplayName("동일한 날짜와 예약 시간, 테마의 예약이 존재할 경우, 참을 반환한다.")
    void existsByDateAndReservationTimeIdAndThemeId() {
        LocalDate date = LocalDate.parse("2024-11-11");
        Member member = memberRepository.save(MemberFixture.getOne());
        ReservationTime reservationTime = reservationTimeRepository.save(ReservationTimeFixture.getOne());
        Theme theme = themeRepository.save(ThemeFixture.getOne());
        reservationRepository.save(new Reservation(member, date, reservationTime, theme));

        assertThat(
                reservationRepository.existsByDateAndReservationTimeIdAndThemeId(
                        date,
                        reservationTime.getId(),
                        theme.getId()))
                .isTrue();
    }

    @Test
    @DisplayName("동일한 날짜와 예약 시간, 테마의 예약이 존재하지 않을 경우, 거짓을 반환한다.")
    void existsByDateAndReservationTimeIdAndThemeId_WhenNotExists() {
        LocalDate date = LocalDate.parse("2024-11-11");
        Member member = memberRepository.save(MemberFixture.getOne());
        ReservationTime reservationTime = reservationTimeRepository.save(ReservationTimeFixture.getOne());
        Theme theme = themeRepository.save(ThemeFixture.getOne());
        reservationRepository.save(new Reservation(member, date, reservationTime, theme));

        assertThat(
                reservationRepository.existsByDateAndReservationTimeIdAndThemeId(
                        date.minusDays(1),
                        reservationTime.getId() + 1,
                        theme.getId() + 1))
                .isFalse();
    }

    @Test
    @DisplayName("동일한 시간의 예약이 존재할 경우, 참을 반환한다.")
    void existsByReservationTimeId() {
        Member member = memberRepository.save(MemberFixture.getOne());
        ReservationTime reservationTime = reservationTimeRepository.save(ReservationTimeFixture.getOne());
        Theme theme = themeRepository.save(ThemeFixture.getOne());
        reservationRepository.save(new Reservation(member, LocalDate.parse("2024-11-11"), reservationTime, theme));

        assertThat(reservationRepository.existsByReservationTimeId(reservationTime.getId())).isTrue();
    }

    @Test
    @DisplayName("동일한 시간의 예약이 존재하지 않을 경우, 거짓을 반환한다.")
    void existsByReservationTimeId_WhenNotExists() {
        Member member = memberRepository.save(MemberFixture.getOne());
        ReservationTime reservationTime = reservationTimeRepository.save(ReservationTimeFixture.getOne());
        Theme theme = themeRepository.save(ThemeFixture.getOne());
        reservationRepository.save(new Reservation(member, LocalDate.parse("2024-11-11"), reservationTime, theme));

        assertThat(reservationRepository.existsByReservationTimeId(reservationTime.getId() + 1)).isFalse();
    }

    @Test
    @DisplayName("동일한 테마인 예약이 존재할 경우, 참을 반환한다.")
    void existsByThemeId() {
        Member member = memberRepository.save(MemberFixture.getOne());
        ReservationTime reservationTime = reservationTimeRepository.save(ReservationTimeFixture.getOne());
        Theme theme = themeRepository.save(ThemeFixture.getOne());
        reservationRepository.save(new Reservation(member, LocalDate.parse("2024-11-11"), reservationTime, theme));

        assertThat(reservationRepository.existsByThemeId(theme.getId())).isTrue();
    }

    @Test
    @DisplayName("동일한 테마인 예약이 존재할 경우, 거짓을 반환한다.")
    void existsByThemeId_WhenNotExists() {
        Member member = memberRepository.save(MemberFixture.getOne());
        ReservationTime reservationTime = reservationTimeRepository.save(ReservationTimeFixture.getOne());
        Theme theme = themeRepository.save(ThemeFixture.getOne());
        reservationRepository.save(new Reservation(member, LocalDate.parse("2024-11-11"), reservationTime, theme));

        assertThat(reservationRepository.existsByThemeId(theme.getId() + 1)).isFalse();
    }
}
