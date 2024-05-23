package roomescape.service;

import java.time.LocalDate;
import java.time.LocalTime;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import roomescape.domain.Member;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationTime;
import roomescape.domain.ReservationWait;
import roomescape.domain.Theme;
import roomescape.domain.repository.MemberRepository;
import roomescape.domain.repository.ReservationRepository;
import roomescape.domain.repository.ReservationTimeRepository;
import roomescape.domain.repository.ReservationWaitRepository;
import roomescape.domain.repository.ThemeRepository;
import roomescape.service.dto.request.wait.WaitRequest;

@Transactional
@SpringBootTest
class ReservationWaitServiceTest {
    @Autowired
    private MemberRepository memberRepository;
    @Autowired
    private ReservationRepository reservationRepository;
    @Autowired
    private ThemeRepository themeRepository;
    @Autowired
    private ReservationTimeRepository timeRepository;
    @Autowired
    private ReservationWaitRepository waitRepository;
    @Autowired
    private ReservationWaitService waitService;

    @Test
    @DisplayName("예약 대기를 생성한다")
    void saveReservationWait_ShouldStorePersistence() {
        // given
        Member member = new Member("aa", "aa@aa.aa", "aa");
        Theme theme = new Theme("n", "d", "th");
        ReservationTime time = new ReservationTime(LocalTime.of(12, 0));
        LocalDate date = LocalDate.of(2017, 12, 11);
        Reservation reservation = new Reservation(date, time, theme);

        Member savedMember = memberRepository.save(member);
        Theme savedTheme = themeRepository.save(theme);
        ReservationTime savedTime = timeRepository.save(time);
        reservationRepository.save(reservation);

        WaitRequest request = new WaitRequest(date, savedTime.getId(), savedTheme.getId());

        // then
        waitService.saveReservationWait(request, savedMember.getId());

        // then
        Assertions.assertThat(waitRepository.findAll())
                .hasSize(1);
    }

    @Test
    @DisplayName("예약이 존재하지 않는 예약대기 신청")
    void saveReservationWait_ShouldStorePersistence_WhenReservationDoesNotExists() {
        // given
        Member member = new Member("aa", "aa@aa.aa", "aa");
        Theme theme = new Theme("n", "d", "t");
        ReservationTime time = new ReservationTime(LocalTime.of(1, 0));
        memberRepository.save(member);
        themeRepository.save(theme);
        timeRepository.save(time);

        // when
        waitService.saveReservationWait(new WaitRequest(LocalDate.of(2017, 12, 11), time.getId(),
                theme.getId()), member.getId());

        // then
        Assertions.assertThat(waitRepository.findAll())
                .hasSize(1);

    }

    @Test
    @DisplayName("예약 대기를 삭제한다")
    void deleteReservation_ShouldRemoveReservationWaitPersistence() {
        // given
        Member member = new Member("aa", "aa@aa.aa", "aa");
        Theme theme = new Theme("n", "d", "t");
        ReservationTime time = new ReservationTime(LocalTime.of(1, 0));
        Reservation reservation = new Reservation(LocalDate.of(2023, 12, 11), time, theme);
        memberRepository.save(member);
        themeRepository.save(theme);
        timeRepository.save(time);
        reservationRepository.save(reservation);
        ReservationWait savedWait = waitRepository.save(new ReservationWait(member, reservation, 0));

        // when
        waitService.deleteReservationWait(savedWait.getId());

        // then
        Assertions.assertThat(waitRepository.findAll())
                .isEmpty();
    }
}
