package roomescape.service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import roomescape.domain.Member;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationStatus;
import roomescape.domain.ReservationStatus.Status;
import roomescape.domain.ReservationTime;
import roomescape.domain.Theme;
import roomescape.domain.repository.MemberRepository;
import roomescape.domain.repository.ReservationRepository;
import roomescape.domain.repository.ReservationTimeRepository;
import roomescape.domain.repository.ThemeRepository;
import roomescape.service.dto.response.theme.ThemeResponse;

@Transactional
@SpringBootTest
class ThemeServiceTest {
    @Autowired
    private ThemeService themeService;
    @Autowired
    private ThemeRepository themeRepository;
    @Autowired
    private ReservationRepository reservationRepository;
    @Autowired
    private ReservationTimeRepository reservationTimeRepository;
    @Autowired
    private MemberRepository memberRepository;

    @Test
    @DisplayName("랭킹 순으로 조회할 수 있다")
    void findAllPopularTheme_ShouldReturnTrendingThemes() {
        // given
        Theme savedTheme1 = themeRepository.save(new Theme("name1", "description", "thumbnail"));
        Theme savedTheme2 = themeRepository.save(new Theme("name2", "description", "thumbnail"));
        Theme savedTheme3 = themeRepository.save(new Theme("name3", "description", "thumbnail"));

        ReservationTime savedReservationTime = reservationTimeRepository.save(new ReservationTime(LocalTime.of(1, 0)));
        ReservationStatus status1 = new ReservationStatus(Status.RESERVED, 0);
        ReservationStatus status2 = new ReservationStatus(Status.WAITING, 1);
        ReservationStatus status3 = new ReservationStatus(Status.WAITING, 2);

        Member member = new Member("aa", "aa@aa.aa", "aa");
        memberRepository.save(member);

        creatReservation(1, savedReservationTime, savedTheme1, status1, member);
        creatReservation(2, savedReservationTime, savedTheme1, status1, member);
        creatReservation(3, savedReservationTime, savedTheme1, status1, member);

        creatReservation(1, savedReservationTime, savedTheme2, status2, member);
        creatReservation(2, savedReservationTime, savedTheme2, status2, member);

        creatReservation(1, savedReservationTime, savedTheme3, status3, member);

        // when
        List<ThemeResponse> popularTheme = themeService.findAllPopularTheme();

        // then
        Assertions.assertThat(popularTheme)
                .hasSize(3)
                .extracting("name")
                .containsExactly("name1", "name2", "name3");
    }

    @Test
    @DisplayName("랭킹은 최대 10개까지 조회할 수 있다")
    void findAllPopularTheme_ShouldReturnMax10TrendingThemes() {
        // given
        Theme savedTheme1 = themeRepository.save(new Theme("name1", "description", "thumbnail"));
        Theme savedTheme2 = themeRepository.save(new Theme("name2", "description", "thumbnail"));
        Theme savedTheme3 = themeRepository.save(new Theme("name3", "description", "thumbnail"));
        Theme savedTheme4 = themeRepository.save(new Theme("name4", "description", "thumbnail"));
        Theme savedTheme5 = themeRepository.save(new Theme("name5", "description", "thumbnail"));
        Theme savedTheme6 = themeRepository.save(new Theme("name6", "description", "thumbnail"));
        Theme savedTheme7 = themeRepository.save(new Theme("name7", "description", "thumbnail"));
        Theme savedTheme8 = themeRepository.save(new Theme("name8", "description", "thumbnail"));
        Theme savedTheme9 = themeRepository.save(new Theme("name9", "description", "thumbnail"));
        Theme savedTheme10 = themeRepository.save(new Theme("name10", "description", "thumbnail"));
        Theme savedTheme11 = themeRepository.save(new Theme("name11", "description", "thumbnail"));

        ReservationTime savedReservationTime = reservationTimeRepository.save(new ReservationTime(LocalTime.of(1, 0)));

        Member member = new Member("aa", "aa@aa.aa", "aa");
        memberRepository.save(member);

        ReservationStatus status1 = new ReservationStatus(Status.RESERVED, 0);
        ReservationStatus status2 = new ReservationStatus(Status.WAITING, 1);
        ReservationStatus status3 = new ReservationStatus(Status.WAITING, 2);
        ReservationStatus status4 = new ReservationStatus(Status.WAITING, 3);
        ReservationStatus status5 = new ReservationStatus(Status.WAITING, 4);
        ReservationStatus status6 = new ReservationStatus(Status.WAITING, 5);
        ReservationStatus status7 = new ReservationStatus(Status.WAITING, 6);
        ReservationStatus status8 = new ReservationStatus(Status.WAITING, 7);
        ReservationStatus status9 = new ReservationStatus(Status.WAITING, 8);
        ReservationStatus status10 = new ReservationStatus(Status.WAITING, 9);
        ReservationStatus status11 = new ReservationStatus(Status.WAITING, 10);

        creatReservation(1, savedReservationTime, savedTheme1, status1, member);
        creatReservation(1, savedReservationTime, savedTheme2, status2, member);
        creatReservation(1, savedReservationTime, savedTheme3, status3, member);
        creatReservation(1, savedReservationTime, savedTheme4, status4, member);
        creatReservation(1, savedReservationTime, savedTheme5, status5, member);
        creatReservation(1, savedReservationTime, savedTheme6, status6, member);
        creatReservation(1, savedReservationTime, savedTheme7, status7, member);
        creatReservation(1, savedReservationTime, savedTheme8, status8, member);
        creatReservation(1, savedReservationTime, savedTheme9, status9, member);
        creatReservation(1, savedReservationTime, savedTheme10, status10, member);
        creatReservation(1, savedReservationTime, savedTheme11, status11, member);

        // when
        List<ThemeResponse> popularTheme = themeService.findAllPopularTheme();

        // then
        Assertions.assertThat(popularTheme)
                .hasSize(10);
    }

    private void creatReservation(int day,
                                  ReservationTime reservationTime,
                                  Theme theme,
                                  ReservationStatus status,
                                  Member member) {
        reservationRepository.save(
                new Reservation(LocalDate.now().minusDays(day), reservationTime, theme, status, member));
    }
}
