package roomescape.reservation.service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import roomescape.common.exception.DataExistException;
import roomescape.common.exception.DataNotFoundException;
import roomescape.common.exception.PastDateException;
import roomescape.common.exception.ReservationNotAllowedException;
import roomescape.common.exception.WaitingNotAllowedException;
import roomescape.fake.FakeMemberRepository;
import roomescape.fake.FakeReservationRepository;
import roomescape.fake.FakeReservationTimeRepository;
import roomescape.fake.FakeThemeRepository;
import roomescape.fake.FakeWaitingRepository;
import roomescape.member.domain.Member;
import roomescape.member.domain.Role;
import roomescape.member.repository.MemberRepositoryInterface;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationTime;
import roomescape.reservation.domain.Waiting;
import roomescape.reservation.dto.AvailableReservationTime;
import roomescape.reservation.repository.ReservationRepositoryInterface;
import roomescape.reservation.repository.ReservationTimeRepositoryInterface;
import roomescape.reservation.repository.WaitingRepositoryInterface;
import roomescape.theme.domain.Theme;
import roomescape.theme.repository.ThemeRepositoryInterface;

import static org.assertj.core.api.Assertions.assertThat;

class ReservationServiceTest {

    private final ReservationRepositoryInterface reservationRepository = new FakeReservationRepository();
    private final ReservationTimeRepositoryInterface reservationTimeRepository = new FakeReservationTimeRepository();
    private final ThemeRepositoryInterface themeRepository = new FakeThemeRepository();
    private final MemberRepositoryInterface memberRepository = new FakeMemberRepository();
    private final WaitingRepositoryInterface waitingRepository = new FakeWaitingRepository();
    private final ReservationService reservationService = new ReservationService(
            reservationRepository,
            reservationTimeRepository,
            themeRepository,
            waitingRepository
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

        final Reservation savedReservation = reservationRepository.save(new Reservation(
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
    void 예약_정보를_삭제하면_첫번째_대기가_예약이_된다() {
        // given
        final Member member = new Member("이스트", "east@email.com", "1234", Role.ADMIN);
        final Member member2 = new Member("우가", "wooga@gmail.com", "1234", Role.USER);
        final Member savedMember = memberRepository.save(member);
        final Member savedMember2 = memberRepository.save(member2);
        final LocalTime time = LocalTime.parse("20:00");
        final LocalDate date = LocalDate.parse("2025-11-28");
        final ReservationTime savedTime = reservationTimeRepository.save(new ReservationTime(time));

        final String themeName = "공포";
        final String description = "무섭다";
        final String thumbnail = "귀신사진";
        final Theme savedTheme = themeRepository.save(new Theme(themeName, description, thumbnail));

        final Reservation savedReservation = reservationRepository.save(new Reservation(
                savedMember,
                date,
                savedTime,
                savedTheme
        ));
        
        waitingRepository.save(new Waiting(
                savedMember2,
                savedTime,
                savedTheme,
                date
        ));

        // when
        reservationService.deleteById(savedReservation.getId());

        // then
        Assertions.assertThat(reservationRepository.findByMember(savedMember2).getFirst().getMember())
                .isEqualTo(savedMember2);
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

        // when & then
        Assertions.assertThatThrownBy(
                        () -> reservationService.save(member, date, savedTime.getId(), savedTheme.getId()))
                .isInstanceOf(PastDateException.class);
    }

    @Test
    void 예약_정보를_저장할_때_대기가_존재하면_예외가_발생한다() {
        // given
        final Member member = new Member("이스트", "east@email.com", "1234", Role.ADMIN);
        memberRepository.save(member);
        final LocalTime time = LocalTime.parse("20:00");
        final LocalDate date = LocalDate.parse("2026-11-28");
        final ReservationTime savedTime = reservationTimeRepository.save(new ReservationTime(time));

        final String themeName = "공포";
        final String description = "무섭다";
        final String thumbnail = "귀신사진";
        final Theme savedTheme = themeRepository.save(new Theme(themeName, description, thumbnail));

        waitingRepository.save(new Waiting(member, savedTime, savedTheme, date));

        // when & then
        Assertions.assertThatThrownBy(
                        () -> reservationService.save(member, date, savedTime.getId(), savedTheme.getId()))
                .isInstanceOf(ReservationNotAllowedException.class);
    }

    @Test
    void 예약_정보를_저장할_때_이미_예약이_있으면_예외가_발생한다() {
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
                        savedMember,
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

    @Test
    void 예약_대기_저장() {
        // given
        final Member savedMember = memberRepository.save(new Member("우가", "wooga@gmail.com", "1234", Role.USER));
        final LocalTime time = LocalTime.parse("20:00");
        final LocalDate date = LocalDate.parse("2026-11-28");
        final ReservationTime savedTime = reservationTimeRepository.save(new ReservationTime(time));

        final String themeName = "공포";
        final String description = "무섭다";
        final String thumbnail = "귀신사진";
        final Theme savedTheme = themeRepository.save(new Theme(themeName, description, thumbnail));

        reservationRepository.save(
                new Reservation(savedMember, date, savedTime, savedTheme)
        );
        //when
        final Waiting savedWaiting = reservationService.createWaitingReservation(
                savedMember,
                date,
                savedTime.getId(),
                savedTheme.getId()
        );

        //then
        Assertions.assertThat(savedWaiting.getId()).isEqualTo(1);
    }

    @Test
    void 예약_대기_저장할_때_예약이_존재하지_않으면_예외_발생() {
        // given
        final Member savedMember = memberRepository.save(new Member("우가", "wooga@gmail.com", "1234", Role.USER));
        final LocalTime time = LocalTime.parse("20:00");
        final LocalDate date = LocalDate.parse("2026-11-28");
        final ReservationTime savedTime = reservationTimeRepository.save(new ReservationTime(time));

        final String themeName = "공포";
        final String description = "무섭다";
        final String thumbnail = "귀신사진";
        final Theme savedTheme = themeRepository.save(new Theme(themeName, description, thumbnail));

        //when & then
        Assertions.assertThatThrownBy(
                        () -> reservationService.createWaitingReservation(savedMember, date, savedTime.getId(),
                                savedTheme.getId()))
                .isInstanceOf(WaitingNotAllowedException.class);
    }

    @Test
    void 멤버를_기준으로_예약_대기_찾기() {
        // given
        final Member savedMember = memberRepository.save(new Member("우가", "wooga@gmail.com", "1234", Role.USER));
        final LocalTime time = LocalTime.parse("20:00");
        final LocalDate date = LocalDate.parse("2026-11-28");
        final ReservationTime savedTime = reservationTimeRepository.save(new ReservationTime(time));

        final String themeName = "공포";
        final String description = "무섭다";
        final String thumbnail = "귀신사진";
        final Theme savedTheme = themeRepository.save(new Theme(themeName, description, thumbnail));

        final Waiting savedWaiting = waitingRepository.save(new Waiting(savedMember, savedTime, savedTheme, date));

        // when
        final List<Waiting> waitings = reservationService.findWaitingByMember(savedMember);

        // then
        Assertions.assertThat(waitings).containsExactly(savedWaiting);
    }

    @Test
    void 아이디를_기준으로_예약_대기_삭제() {
        // given
        final Member savedMember = memberRepository.save(new Member("우가", "wooga@gmail.com", "1234", Role.USER));
        final LocalTime time = LocalTime.parse("20:00");
        final LocalDate date = LocalDate.parse("2026-11-28");
        final ReservationTime savedTime = reservationTimeRepository.save(new ReservationTime(time));

        final String themeName = "공포";
        final String description = "무섭다";
        final String thumbnail = "귀신사진";
        final Theme savedTheme = themeRepository.save(new Theme(themeName, description, thumbnail));

        final Waiting savedWaiting = waitingRepository.save(new Waiting(savedMember, savedTime, savedTheme, date));

        // when
        reservationService.deleteWaitingById(savedWaiting.getId());

        // then
        Assertions.assertThat(waitingRepository.findByMember(savedMember))
                .doesNotContain(savedWaiting);
    }

    @Test
    void 아이디를_기준으로_예약_대기_삭제_예외_발생() {

        // when & then
        Assertions.assertThatThrownBy(() -> reservationService.deleteWaitingById(Long.MAX_VALUE))
                .isInstanceOf(DataNotFoundException.class);
    }

    @Test
    void 예약_대기_정보에_따른_순번_조회() {
        // given
        final Member savedMember = memberRepository.save(new Member("우가", "wooga@gmail.com", "1234", Role.USER));
        final LocalTime time = LocalTime.parse("20:00");
        final LocalDate date = LocalDate.parse("2026-11-28");
        final ReservationTime savedTime = reservationTimeRepository.save(new ReservationTime(time));

        final String themeName = "공포";
        final String description = "무섭다";
        final String thumbnail = "귀신사진";
        final Theme savedTheme = themeRepository.save(new Theme(themeName, description, thumbnail));

        waitingRepository.save(new Waiting(savedMember, savedTime, savedTheme, date));
        waitingRepository.save(new Waiting(savedMember, savedTime, savedTheme, date));
        waitingRepository.save(new Waiting(savedMember, savedTime, savedTheme, date));
        waitingRepository.save(new Waiting(savedMember, savedTime, savedTheme, date));
        final Waiting savedWaiting = waitingRepository.save(new Waiting(savedMember, savedTime, savedTheme, date));

        // when
        final long count = reservationService.getRankInWaiting(savedWaiting);

        // then
        Assertions.assertThat(count).isEqualTo(5);
    }

    @Test
    void 예약_대기_정보에서_앞_번호가_사라지면_번호_당겨지는_순번_조회() {
        // given
        final Member savedMember = memberRepository.save(new Member("우가", "wooga@gmail.com", "1234", Role.USER));
        final LocalTime time = LocalTime.parse("20:00");
        final LocalDate date = LocalDate.parse("2026-11-28");
        final ReservationTime savedTime = reservationTimeRepository.save(new ReservationTime(time));

        final String themeName = "공포";
        final String description = "무섭다";
        final String thumbnail = "귀신사진";
        final Theme savedTheme = themeRepository.save(new Theme(themeName, description, thumbnail));

        waitingRepository.save(new Waiting(savedMember, savedTime, savedTheme, date));
        waitingRepository.save(new Waiting(savedMember, savedTime, savedTheme, date));
        final Waiting savedWaiting1 = waitingRepository.save(new Waiting(savedMember, savedTime, savedTheme, date));
        waitingRepository.save(new Waiting(savedMember, savedTime, savedTheme, date));
        final Waiting savedWaiting2 = waitingRepository.save(new Waiting(savedMember, savedTime, savedTheme, date));

        waitingRepository.deleteById(savedWaiting1.getId());

        // when
        final long count = reservationService.getRankInWaiting(savedWaiting2);

        // then
        Assertions.assertThat(count).isEqualTo(4);
    }
}
