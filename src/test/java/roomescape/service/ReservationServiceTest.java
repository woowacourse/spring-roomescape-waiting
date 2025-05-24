package roomescape.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.time.LocalDate;
import java.time.LocalTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import roomescape.dto.request.ReservationRequest;
import roomescape.entity.Member;
import roomescape.entity.Reservation;
import roomescape.entity.ReservationTime;
import roomescape.entity.Theme;
import roomescape.exception.custom.DuplicatedException;
import roomescape.exception.custom.InvalidInputException;
import roomescape.repository.jpa.JpaMemberRepository;
import roomescape.repository.jpa.JpaReservationRepository;
import roomescape.repository.jpa.JpaReservationTimeRepository;
import roomescape.repository.jpa.JpaThemeRepository;

@DataJpaTest
class ReservationServiceTest {

    @PersistenceContext
    private EntityManager em;
    @Autowired
    private JpaThemeRepository themeRepository;
    @Autowired
    private JpaReservationTimeRepository reservationTimeRepository;
    @Autowired
    private JpaMemberRepository memberRepository;
    @Autowired
    private JpaReservationRepository reservationRepository;

    private ReservationService reservationService;

    @BeforeEach
    void setUp() {
        reservationService = new ReservationService(
            reservationRepository,
            reservationTimeRepository,
            themeRepository,
            memberRepository);
    }

    @Test
    @DisplayName("예약 정보를 테마로 필터링하여 조회할 수 있다.")
    void findReservationsByFiltersAboutTheme() {
        Member member = saveMember(1L);
        Theme theme1 = saveTheme(1L);
        Theme theme2 = saveTheme(2L);
        ReservationTime time = saveTime(LocalTime.of(10, 0));
        LocalDate date = LocalDate.of(2025, 4, 28);

        em.flush();
        em.clear();

        Reservation reservation1 = new Reservation(member, date, time, theme1);
        Reservation reservation2 = new Reservation(member, date, time, theme2);
        reservationRepository.save(reservation1);
        reservationRepository.save(reservation2);

        assertThat(reservationService.findReservationsByFilters(
            theme1.getId(), null, null, null)).hasSize(1);
    }

    @Test
    @DisplayName("예약 정보를 사용자로 필터링하여 조회할 수 있다.")
    void findReservationsByFiltersAboutMember() {
        Member member1 = saveMember(1L);
        Member member2 = saveMember(2L);
        Theme theme = saveTheme(1L);
        ReservationTime time1 = saveTime(LocalTime.of(10, 0));
        ReservationTime time2 = saveTime(LocalTime.of(12, 0));
        LocalDate date = LocalDate.of(2025, 4, 28);

        em.flush();
        em.clear();

        Reservation reservation1 = new Reservation(member1, date, time1, theme);
        Reservation reservation2 = new Reservation(member2, date, time2, theme);
        reservationRepository.save(reservation1);
        reservationRepository.save(reservation2);

        assertThat(reservationService.findReservationsByFilters(
            null, member1.getId(), null, null)).hasSize(1);
    }

    @Test
    @DisplayName("예약 정보를 날짜로 필터링하여 조회할 수 있다.")
    void findReservationsByFiltersAboutDate() {
        Member member = saveMember(1L);
        Theme theme = saveTheme(1L);
        ReservationTime time = saveTime(LocalTime.of(10, 0));
        LocalDate date1 = LocalDate.of(2025, 4, 28);
        LocalDate date2 = LocalDate.of(2025, 4, 29);

        em.flush();
        em.clear();

        Reservation reservation1 = new Reservation(member, date1, time, theme);
        Reservation reservation2 = new Reservation(member, date2, time, theme);
        reservationRepository.save(reservation1);
        reservationRepository.save(reservation2);

        assertThat(reservationService.findReservationsByFilters(
            null, null, date1.minusDays(1), date1)).hasSize(1);
    }

    @Test
    @DisplayName("사용자는 현재 시간 이후의 예약만 추가할 수 있다.")
    void addReservationAfterNow() {
        Member member = saveMember(1L);
        Theme theme = saveTheme(1L);
        ReservationTime time = saveTime(LocalTime.of(10, 0));
        LocalDate date = LocalDate.of(5000, 4, 28);

        em.flush();
        em.clear();

        ReservationRequest request = new ReservationRequest(
            date, time.getId(), theme.getId(), null);

        assertThat(reservationService.addReservationAfterNow(member, request)).isNotNull();
    }

    @Test
    @DisplayName("사용자는 현재 시간 이전의 예약을 추가하면 예외가 발생한다.")
    void addReservationBeforeNow() {
        Member member = saveMember(1L);
        Theme theme = saveTheme(1L);
        ReservationTime time = saveTime(LocalTime.of(10, 0));
        LocalDate date = LocalDate.of(2000, 4, 28);

        em.flush();
        em.clear();

        ReservationRequest request = new ReservationRequest(
            date, time.getId(), theme.getId(), null);

        assertThatThrownBy(() -> reservationService.addReservationAfterNow(member, request))
            .isInstanceOf(InvalidInputException.class)
            .hasMessageContaining("과거 예약은 불가능");
    }

    @Test
    @DisplayName("관리자는 다른 사용자의 예약을 추가할 수 있다.")
    void addReservation() {
        Member member = saveMember(1L);
        Theme theme = saveTheme(1L);
        ReservationTime time = saveTime(LocalTime.of(10, 0));
        LocalDate date = LocalDate.of(2025, 4, 28);

        em.flush();
        em.clear();

        ReservationRequest request = new ReservationRequest(
            date, time.getId(), theme.getId(), member.getId());

        assertThat(reservationService.addReservation(request)).isNotNull();
    }

    @Test
    @DisplayName("관리자는 동일 정보를 가진 예약을 추가하면 예외가 발생한다.")
    void addReservationWithDuplicateReservation() {
        Member member = saveMember(1L);
        Theme theme = saveTheme(1L);
        ReservationTime time = saveTime(LocalTime.of(10, 0));
        LocalDate date = LocalDate.of(2025, 4, 28);

        em.flush();
        em.clear();

        Reservation reservation = new Reservation(member, date, time, theme);
        reservationRepository.save(reservation);

        ReservationRequest request = new ReservationRequest(
            date, time.getId(), theme.getId(), member.getId());

        assertThatThrownBy(() -> reservationService.addReservation(request))
            .isInstanceOf(DuplicatedException.class)
            .hasMessageContaining("reservation");
    }

    private Member saveMember(Long tmp) {
        Member member = Member.createUser("이름" + tmp, "이메일" + tmp, "비밀번호" + tmp);
        memberRepository.save(member);

        return member;
    }

    private Theme saveTheme(Long tmp) {
        Theme theme = new Theme("이름" + tmp, "설명" + tmp, "썸네일" + tmp);
        themeRepository.save(theme);

        return theme;
    }

    private ReservationTime saveTime(LocalTime reservationTime) {
        ReservationTime time = new ReservationTime(reservationTime);
        reservationTimeRepository.save(time);

        return time;
    }
}
