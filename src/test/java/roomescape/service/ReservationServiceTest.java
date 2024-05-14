package roomescape.service;

import static java.time.Month.DECEMBER;
import static java.time.Month.JANUARY;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import roomescape.domain.Member;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationTime;
import roomescape.domain.Theme;
import roomescape.domain.repository.MemberRepository;
import roomescape.domain.repository.ReservationRepository;
import roomescape.domain.repository.ReservationTimeRepository;
import roomescape.domain.repository.ThemeRepository;
import roomescape.web.dto.request.reservation.ReservationSearchCond;
import roomescape.web.dto.response.reservation.ReservationResponse;

@SpringBootTest
class ReservationServiceTest {
    @Autowired
    private ReservationService reservationService;
    @Autowired
    private ThemeRepository themeRepository;
    @Autowired
    private ReservationTimeRepository reservationTimeRepository;
    @Autowired
    private MemberRepository memberRepository;
    @Autowired
    private ReservationRepository reservationRepository;

    @AfterEach
    void tearDown() {
        reservationRepository.deleteAll();
        reservationTimeRepository.deleteAll();
        themeRepository.deleteAll();
        memberRepository.deleteAll();
    }

    @Test
    @DisplayName("검색 조건에 맞는 예약을 조회할 수 있다")
    void findAllReservationByCondition_ShouldGetReservation_WhenConditionIsCorrect() {
        // given
        Theme theme1 = new Theme("theme_name", "desc", "thumbnail");
        Theme theme2 = new Theme("theme_name2", "desc", "thumbnail");
        Theme savedTheme1 = themeRepository.save(theme1);
        Theme savedTheme2 = themeRepository.save(theme2);

        ReservationTime time = new ReservationTime(LocalTime.of(1, 0));
        ReservationTime savedTime = reservationTimeRepository.save(time);

        Member member1 = new Member("name1", "email", "password");
        Member member2 = new Member("name2", "email", "password");
        Member savedMember1 = memberRepository.save(member1);
        Member savedMember2 = memberRepository.save(member2);

        Reservation reservation1 = new Reservation(LocalDate.of(2023, JANUARY, 1), savedTime, savedTheme1,
                savedMember1);
        Reservation reservation2 = new Reservation(LocalDate.of(2023, JANUARY, 2), savedTime, savedTheme1,
                savedMember1);

        Reservation reservation3 = new Reservation(LocalDate.of(2023, JANUARY, 3), savedTime, savedTheme1,
                savedMember1);
        Reservation reservation4 = new Reservation(LocalDate.of(2023, JANUARY, 2), savedTime, savedTheme2,
                savedMember1);
        Reservation reservation5 = new Reservation(LocalDate.of(2022, DECEMBER, 31), savedTime, savedTheme1,
                savedMember1);
        Reservation reservation6 = new Reservation(LocalDate.of(2023, JANUARY, 1), savedTime, savedTheme1,
                savedMember2);
        Reservation savedReservation1 = reservationRepository.save(reservation1);
        Reservation savedReservation2 = reservationRepository.save(reservation2);
        reservationRepository.save(reservation3);
        reservationRepository.save(reservation4);
        reservationRepository.save(reservation5);
        reservationRepository.save(reservation6);

        ReservationSearchCond condition = new ReservationSearchCond(LocalDate.of(2023, JANUARY, 1),
                LocalDate.of(2023, JANUARY, 2), member1.getName(), theme1.getName()
        );

        // when
        List<ReservationResponse> findReservations = reservationService.findAllReservationByConditions(
                condition);

        // then
        Assertions.assertThat(findReservations)
                .hasSize(2)
                .containsExactlyInAnyOrder(
                        ReservationResponse.from(savedReservation1),
                        ReservationResponse.from(savedReservation2)
                );

    }

}
