package roomescape.repository.jpa;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.time.LocalDate;
import java.time.LocalTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import roomescape.entity.Member;
import roomescape.entity.Reservation;
import roomescape.entity.ReservationTime;
import roomescape.entity.Theme;

@DataJpaTest
class JpaReservationRepositoryTest {

    @PersistenceContext
    private EntityManager em;
    @Autowired
    private JpaThemeRepository themeRepository;
    @Autowired
    private JpaReservationTimeRepository reservationTimeRepository;
    @Autowired
    private JpaReservationRepository reservationRepository;
    @Autowired
    private JpaMemberRepository memberRepository;

    @Test
    @DisplayName("사용자로 예약을 찾을 수 있다.")
    void findByMemberId() {
        Member member = saveMember(1L);
        Theme theme = saveTheme(1L);
        ReservationTime time = saveTime(LocalTime.of(10, 0));
        LocalDate date = LocalDate.of(2025, 4, 28);

        em.flush();
        em.clear();

        Reservation reservation = new Reservation(member, date, time, theme);
        reservationRepository.save(reservation);

        assertThat(reservationRepository.findByMemberId(member.getId())).hasSize(1);
    }

    @Test
    @DisplayName("해당 날짜와 테마에 해당하는 예약을 찾을 수 있다.")
    void findByDateAndThemeId() {
        Member member = saveMember(1L);
        Theme theme = saveTheme(1L);
        ReservationTime time = saveTime(LocalTime.of(10, 0));
        LocalDate date = LocalDate.of(2025, 4, 28);

        em.flush();
        em.clear();

        Reservation reservation = new Reservation(member, date, time, theme);
        reservationRepository.save(reservation);

        assertThat(reservationRepository.findByDateAndThemeId(date, theme.getId())).hasSize(1);
    }

    @Test
    @DisplayName("테마를 필터링하여 예약을 조회할 수 있다.")
    void findByFiltersForTheme() {
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

        assertThat(reservationRepository.findByFilters(
            theme1.getId(), null, null, null)).hasSize(1);
    }

    @Test
    @DisplayName("테마와 사용자를 필터링하여 예약을 조회할 수 있다.")
    void findByFiltersForThemeAndMember() {
        Member member1 = saveMember(1L);
        Member member2 = saveMember(2L);
        Theme theme1 = saveTheme(1L);
        Theme theme2 = saveTheme(2L);
        ReservationTime time = saveTime(LocalTime.of(10, 0));
        LocalDate date = LocalDate.of(2025, 4, 28);

        em.flush();
        em.clear();

        Reservation reservation1 = new Reservation(member1, date, time, theme1);
        Reservation reservation2 = new Reservation(member2, date, time, theme2);
        reservationRepository.save(reservation1);
        reservationRepository.save(reservation2);

        assertThat(reservationRepository.findByFilters(
            theme1.getId(), member1.getId(), null, null)).hasSize(1);
    }

    @Test
    @DisplayName("해당 날짜, 시간, 테마로 예약이 있다면 true를 반환한다.")
    void existReservationByDateTimeAndTheme() {
        Member member = saveMember(1L);
        Theme theme = saveTheme(1L);
        ReservationTime time = saveTime(LocalTime.of(10, 0));
        LocalDate date = LocalDate.of(2025, 4, 28);

        em.flush();
        em.clear();

        Reservation reservation = new Reservation(member, date, time, theme);
        reservationRepository.save(reservation);

        assertThat(reservationRepository.existsByDateAndTimeIdAndThemeIdAndMemberId(
            date, time.getId(), theme.getId(), member.getId())).isTrue();
    }

    @Test
    @DisplayName("해당 날짜, 시간, 테마로 예약이 없다면 false 반환한다.")
    void notExistReservationByDateTimeAndTheme() {
        Member member = saveMember(1L);
        Theme theme = saveTheme(1L);
        ReservationTime time = saveTime(LocalTime.of(10, 0));
        LocalDate date = LocalDate.of(2025, 4, 28);

        em.flush();
        em.clear();

        Reservation reservation = new Reservation(member, date, time, theme);
        reservationRepository.save(reservation);

        assertThat(reservationRepository.existsByDateAndTimeIdAndThemeIdAndMemberId(
            date, time.getId(), theme.getId() + 1, member.getId())).isFalse();
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
