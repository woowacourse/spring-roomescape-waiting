package roomescape.service;

import static java.time.Month.DECEMBER;
import static java.time.Month.JANUARY;

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
import roomescape.exception.reservation.DuplicatedReservationException;
import roomescape.exception.reservation.InvalidDateTimeReservationException;
import roomescape.service.dto.request.reservation.ReservationRequest;
import roomescape.service.dto.request.reservation.ReservationSearchCond;
import roomescape.service.dto.response.reservation.ReservationResponse;

@Transactional
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

    @Test
    @DisplayName("모든 예약에 대한 조회를 할 수 있다")
    void findAll_ShouldGetAllReservation() {
        // given
        LocalDate date = LocalDate.of(2023, 12, 11);
        ReservationTime reservationTime = new ReservationTime(LocalTime.of(1, 0));
        Theme theme = new Theme("name", "desc", "thum");
        Member member = new Member("name", "aa@aa.aa", "aa");
        reservationTimeRepository.save(reservationTime);
        themeRepository.save(theme);
        memberRepository.save(member);
        ReservationStatus status = new ReservationStatus(Status.RESERVED, 0);
        ReservationStatus status2 = new ReservationStatus(Status.WAITING, 1);
        ReservationStatus status3 = new ReservationStatus(Status.WAITING, 2);

        Reservation reservation1 = reservationRepository.save(
                new Reservation(date, reservationTime, theme, status, member));
        Reservation reservation2 = reservationRepository.save(
                new Reservation(date, reservationTime, theme, status2, member));
        Reservation reservation3 = reservationRepository.save(
                new Reservation(date, reservationTime, theme, status3, member));

        // when
        List<ReservationResponse> reservations = reservationService.findAllReservation();

        // then
        Assertions.assertThat(reservations).hasSize(3);
    }

    @Test
    @DisplayName("예약을 저장할 수 있다")
    void saveReservation_ShouldStoreReservationInfo() {
        // given
        ReservationTime time = new ReservationTime(LocalTime.of(1, 0));
        Member member = new Member("name", "email", "password");
        Theme theme = new Theme("a", "n", "t");

        reservationTimeRepository.save(time);
        memberRepository.save(member);
        themeRepository.save(theme);
        ReservationRequest request = new ReservationRequest(LocalDate.now().plusDays(1), member.getId(), time.getId(),
                theme.getId());
        // when
        reservationService.saveReservation(request);

        // when & then
        Assertions.assertThat(reservationRepository.findAll())
                .hasSize(1);
    }

    @Test
    @DisplayName("예약은 정책에 따라 저장 중 예외를 발생시킨다 - 과거에 대한 예약")
    void saveReservation_ShouldThrowException_WhenTryToBookPastTimeReservation() {
        // given
        ReservationTime time = new ReservationTime(LocalTime.of(1, 0));
        Member member = new Member("name", "email", "password");
        Theme theme = new Theme("a", "n", "t");

        reservationTimeRepository.save(time);
        memberRepository.save(member);
        themeRepository.save(theme);
        ReservationRequest request = new ReservationRequest(LocalDate.of(1998, 12, 11), member.getId(), time.getId(),
                theme.getId());
        // when & then
        Assertions.assertThatThrownBy(() -> reservationService.saveReservation(request))
                .isInstanceOf(InvalidDateTimeReservationException.class);
    }

    @Test
    @DisplayName("예약은 정책에 따라 저장 중 예외를 발생시킨다 - 중복된 예약 요청")
    void saveReservation_ShouldThrowException_WhenViolatePolicy() {
        // given
        ReservationTime time = new ReservationTime(LocalTime.of(1, 0));
        Member member = new Member("name", "email", "password");
        Theme theme = new Theme("a", "n", "t");

        reservationTimeRepository.save(time);
        memberRepository.save(member);
        themeRepository.save(theme);
        ReservationRequest request = new ReservationRequest(LocalDate.now().plusDays(1), member.getId(), time.getId(),
                theme.getId());
        reservationService.saveReservation(request);

        // when & then
        Assertions.assertThatThrownBy(() -> reservationService.saveReservation(request))
                .isInstanceOf(DuplicatedReservationException.class);
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
        ReservationStatus status = new ReservationStatus(Status.RESERVED, 0);
        ReservationStatus status1 = new ReservationStatus(Status.WAITING, 1);
        ReservationStatus status2 = new ReservationStatus(Status.WAITING, 2);
        ReservationStatus status3 = new ReservationStatus(Status.WAITING, 3);
        ReservationStatus status4 = new ReservationStatus(Status.WAITING, 4);
        ReservationStatus status5 = new ReservationStatus(Status.WAITING, 5);
        ReservationStatus status6 = new ReservationStatus(Status.WAITING, 6);

        Reservation reservation1 = new Reservation(LocalDate.of(2023, JANUARY, 1), savedTime, savedTheme1, status,
                savedMember1);
        Reservation reservation2 = new Reservation(LocalDate.of(2023, JANUARY, 2), savedTime, savedTheme1, status1,
                savedMember1);

        Reservation reservation3 = new Reservation(LocalDate.of(2023, JANUARY, 3), savedTime, savedTheme1, status2,
                savedMember1);
        Reservation reservation4 = new Reservation(LocalDate.of(2023, JANUARY, 2), savedTime, savedTheme2, status3,
                savedMember1);
        Reservation reservation5 = new Reservation(LocalDate.of(2022, DECEMBER, 31), savedTime, savedTheme1, status4,
                savedMember1);
        Reservation reservation6 = new Reservation(LocalDate.of(2023, JANUARY, 1), savedTime, savedTheme2, status5,
                savedMember2);

        Reservation savedReservation1 = reservationRepository.save(reservation1);
        Reservation savedReservation2 = reservationRepository.save(reservation2);
        Reservation savedReservation3 = reservationRepository.save(reservation3);
        Reservation savedReservation4 = reservationRepository.save(reservation4);
        Reservation savedReservation5 = reservationRepository.save(reservation5);
        Reservation savedReservation6 = reservationRepository.save(reservation6);

        ReservationSearchCond condition = new ReservationSearchCond(
                LocalDate.of(2023, JANUARY, 1),
                LocalDate.of(2023, JANUARY, 2),
                member1.getName(),
                theme1.getName());

        // when
        List<ReservationResponse> findReservations = reservationService.findAllReservationByConditions(condition);

        // then
        Assertions.assertThat(findReservations)
                .hasSize(1)
                .containsExactlyInAnyOrder(
                        new ReservationResponse(savedReservation1));
    }

    @Test
    @DisplayName("에약을 삭제할 수 있다")
    void deleteReservation_ShouldRemoveReservationInfo() {
        // given
        Theme theme = new Theme("name", "desc", "thumbnail");
        ReservationTime time = new ReservationTime(LocalTime.of(1, 0));
        Member member = new Member("aa", "aa@aa.aa", "aa");
        ReservationStatus status = new ReservationStatus(Status.RESERVED, 0);

        Reservation reservation = new Reservation(LocalDate.now().plusDays(1), time, theme, status, member);
        themeRepository.save(theme);
        reservationTimeRepository.save(time);
        memberRepository.save(member);
        reservationRepository.save(reservation);

        // when
        reservationService.deleteReservation(reservation.getId());

        // then
        Assertions.assertThat(reservationRepository.findAll())
                .isEmpty();
    }
}
