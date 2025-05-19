package roomescape.reservation.service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import roomescape.common.exception.DataExistException;
import roomescape.common.exception.DataNotFoundException;
import roomescape.common.exception.PastDateException;
import roomescape.fake.FakeMemberRepository;
import roomescape.fake.FakeReservationRepository;
import roomescape.fake.FakeReservationTimeRepository;
import roomescape.fake.FakeThemeRepository;
import roomescape.member.domain.Member;
import roomescape.member.domain.Role;
import roomescape.member.repository.MemberRepositoryInterface;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationTime;
import roomescape.reservation.dto.AvailableReservationTime;
import roomescape.reservation.repository.ReservationRepositoryInterface;
import roomescape.reservation.repository.ReservationTimeRepositoryInterface;
import roomescape.theme.domain.Theme;
import roomescape.theme.repository.ThemeRepositoryInterface;

import static org.assertj.core.api.Assertions.assertThat;

class ReservationServiceTest {

    private final ReservationRepositoryInterface reservationRepository = new FakeReservationRepository();
    private final ReservationTimeRepositoryInterface reservationTimeRepository = new FakeReservationTimeRepository();
    private final ThemeRepositoryInterface themeRepository = new FakeThemeRepository();
    private final MemberRepositoryInterface memberRepository = new FakeMemberRepository();
    private final ReservationService reservationService = new ReservationService(
            reservationRepository,
            reservationTimeRepository,
            themeRepository
    );

    @Test
    void 예약_정보_목록을_조회한다() {
        // given
        final String themeName = "공포";
        final String description = "무섭다";
        final String thumbnail = "귀신사진";
        final Theme savedTheme = themeRepository.save(new Theme(themeName, description, thumbnail));

        final Member member = new Member("이스트", "east@email.com", "1234", Role.ADMIN);
        final Member savedMember = memberRepository.save(member);
        final LocalDate date = LocalDate.parse("2025-08-01");
        final LocalTime time = LocalTime.parse("20:00");
        final ReservationTime savedReservationTime1 = reservationTimeRepository.save(new ReservationTime(time));

        final Member member2 = new Member("머피", "muffy@email.com", "1234", Role.USER);
        final Member savedMember2 = memberRepository.save(member);
        final LocalDate date2 = LocalDate.parse("2025-08-01");
        final LocalTime time2 = LocalTime.parse("21:00");
        final ReservationTime savedReservationTime2 = reservationTimeRepository.save(new ReservationTime(time2));

        reservationRepository.save(
                new Reservation(
                        savedMember,
                        date,
                        savedReservationTime1,
                        savedTheme
                )
        );

        reservationRepository.save(
                new Reservation(
                        savedMember2,
                        date2,
                        savedReservationTime2,
                        savedTheme
                )
        );

        // when
        final List<Reservation> reservations = reservationService.findAll();

        // then
        assertThat(reservations.size()).isEqualTo(2);
    }

    @Test
    void 예약_정보를_저장한다() {
        // given
        final Member member = new Member("이스트", "east@email.com", "1234", Role.ADMIN);
        final Member savedMember = memberRepository.save(member);
        final LocalTime time = LocalTime.parse("20:00");
        final LocalDate date = LocalDate.parse("2025-11-28");
        final ReservationTime savedTime = reservationTimeRepository.save(new ReservationTime(time));

        final String themeName = "공포";
        final String description = "무섭다";
        final String thumbnail = "귀신사진";
        final Theme savedTheme = themeRepository.save(new Theme(themeName, description, thumbnail));

        // when & then
        Assertions.assertThatCode(
                        () -> reservationService.save(savedMember, date, savedTime.getId(), savedTheme.getId()))
                .doesNotThrowAnyException();
    }

    @Test
    void 예약_정보를_삭제한다() {
        // given
        final Member member = new Member("이스트", "east@email.com", "1234", Role.ADMIN);
        final Member savedMember = memberRepository.save(member);
        final LocalTime time = LocalTime.parse("20:00");
        final LocalDate date = LocalDate.parse("2025-11-28");
        final ReservationTime savedTime = reservationTimeRepository.save(new ReservationTime(time));

        final String themeName = "공포";
        final String description = "무섭다";
        final String thumbnail = "귀신사진";
        final Theme savedTheme = themeRepository.save(new Theme(themeName, description, thumbnail));

        Reservation savedReservation = reservationRepository.save(new Reservation(
                savedMember,
                date,
                savedTime,
                savedTheme
        ));

        // when & then
        Assertions.assertThatCode(() -> reservationService.deleteById(savedReservation.getId()))
                .doesNotThrowAnyException();
    }

    @Test
    void 이용가능한_예약_시간을_조회한다() {
        // given
        final Member member = new Member("이스트", "east@email.com", "1234", Role.ADMIN);
        final Member savedMember = memberRepository.save(member);
        final LocalTime time = LocalTime.parse("20:00");
        final LocalDate date = LocalDate.parse("2025-11-28");
        final ReservationTime savedTime = reservationTimeRepository.save(new ReservationTime(time));

        final String themeName = "공포";
        final String description = "무섭다";
        final String thumbnail = "귀신사진";
        final Theme savedTheme = themeRepository.save(new Theme(themeName, description, thumbnail));

        reservationRepository.save(new Reservation(
                savedMember,
                date,
                savedTime,
                savedTheme
        ));

        final LocalTime time2 = LocalTime.parse("21:00");
        reservationTimeRepository.save(new ReservationTime(time2));

        // when
        final long count =
                reservationService.findAvailableReservationTimes(date, savedTheme.getId())
                        .stream()
                        .filter(AvailableReservationTime::alreadyBooked)
                        .count();

        // then
        assertThat(count).isEqualTo(1);
    }

    @Test
    void 예약_정보를_저장할_때_과거_시간이면_예외가_발생한다() {
        // given
        final Member member = new Member("이스트", "east@email.com", "1234", Role.ADMIN);
        memberRepository.save(member);
        final LocalTime time = LocalTime.parse("20:00");
        final LocalDate date = LocalDate.parse("2024-11-28");
        final ReservationTime savedTime = reservationTimeRepository.save(new ReservationTime(time));

        final String themeName = "공포";
        final String description = "무섭다";
        final String thumbnail = "귀신사진";
        final Theme savedTheme = themeRepository.save(new Theme(themeName, description, thumbnail));

        reservationRepository.save(
                new Reservation(
                        member,
                        date,
                        savedTime,
                        savedTheme
                )
        );

        // when & then
        Assertions.assertThatThrownBy(
                        () -> reservationService.save(member, date, savedTime.getId(), savedTheme.getId()))
                .isInstanceOf(PastDateException.class);
    }

    @Test
    void 예약_정보를_저장할_때_이미_예약된_시간이면_예외가_발생한다() {
        // given
        final Member member = new Member("이스트", "east@email.com", "1234", Role.ADMIN);
        memberRepository.save(member);
        final LocalTime time = LocalTime.parse("20:00");
        final LocalDate date = LocalDate.parse("2025-11-28");
        final ReservationTime savedTime = reservationTimeRepository.save(new ReservationTime(time));

        final String themeName = "공포";
        final String description = "무섭다";
        final String thumbnail = "귀신사진";
        final Theme savedTheme = themeRepository.save(new Theme(themeName, description, thumbnail));

        reservationRepository.save(
                new Reservation(
                        member,
                        date,
                        savedTime,
                        savedTheme
                )
        );

        // when & then
        Assertions.assertThatThrownBy(
                        () -> reservationService.save(member, date, savedTime.getId(), savedTheme.getId()))
                .isInstanceOf(DataExistException.class);
    }


    @Test
    void 예약_정보를_저장할_때_예약시간이_존재하지않으면_예외가_발생한다() {
        // given
        final Member member = new Member("이스트", "east@email.com", "1234", Role.ADMIN);
        final Member savedMember = memberRepository.save(member);
        final LocalDate date = LocalDate.parse("2025-11-28");
        final Long timeId = Long.MAX_VALUE;

        final String themeName = "공포";
        final String description = "무섭다";
        final String thumbnail = "귀신사진";
        final Theme savedTheme = themeRepository.save(new Theme(themeName, description, thumbnail));

        // when & then
        Assertions.assertThatThrownBy(() -> reservationService.save(savedMember, date, timeId, savedTheme.getId()))
                .isInstanceOf(DataNotFoundException.class);
    }

    @Test
    void 한_테마의_날짜와_시간이_중복_될_수_없다() {
        // given
        final Member member = new Member("이스트", "east@email.com", "1234", Role.ADMIN);
        final Member savedMember = memberRepository.save(member);
        final LocalTime time = LocalTime.parse("20:00");
        final LocalDate date = LocalDate.parse("2025-11-28");
        final ReservationTime savedTime = reservationTimeRepository.save(new ReservationTime(time));

        final String themeName = "공포";
        final String description = "무섭다";
        final String thumbnail = "귀신사진";
        final Theme savedTheme = themeRepository.save(new Theme(themeName, description, thumbnail));

        reservationRepository.save(
                new Reservation(
                        member,
                        date,
                        savedTime,
                        savedTheme
                )
        );

        // when & then
        Assertions.assertThatThrownBy(() -> reservationService.save(
                        new Member(
                                2L,
                                "WooGa",
                                "bowook316@gmail.com",
                                "1234",
                                Role.USER
                        ), date, savedTime.getId(), savedTheme.getId()))
                .isInstanceOf(DataExistException.class);
    }

    @Test
    void 멤버_기준으로_예약_정보_가져오기() {
        // given
        final Member member = new Member("이스트", "email@email.com", "1234", Role.ADMIN);
        final Member savedMember = memberRepository.save(member);
        final LocalTime time = LocalTime.parse("20:00");
        final LocalDate date = LocalDate.parse("2025-11-28");
        final ReservationTime savedTime = reservationTimeRepository.save(new ReservationTime(time));
        final String themeName = "공포";
        final String description = "무섭다";
        final String thumbnail = "귀신사진";
        final Theme savedTheme = themeRepository.save(new Theme(themeName, description, thumbnail));
        reservationRepository.save(
                new Reservation(
                        savedMember,
                        date,
                        savedTime,
                        savedTheme
                )
        );

        // when
        final List<Reservation> reservations = reservationService.findByMember(savedMember);

        // then
        assertThat(reservations.size()).isEqualTo(1);
    }
}
