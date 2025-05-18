package roomescape.repository;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.persistence.EntityManager;
import java.time.LocalDate;
import java.time.LocalTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.annotation.DirtiesContext;
import roomescape.entity.Member;
import roomescape.entity.Reservation;
import roomescape.entity.ReservationTime;
import roomescape.entity.Theme;

@DataJpaTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class JpaReservationRepositoryTest {

    @Autowired
    private EntityManager em;
    @Autowired
    private JpaThemeRepository jpaThemeRepository;
    @Autowired
    private JpaReservationTimeRepository jpaReservationTimeRepository;
    @Autowired
    private JpaReservationRepository jpaReservationRepository;
    @Autowired
    private JpaMemberRepository jpaMemberRepository;


    @Test
    @DisplayName("해당 날짜, 시간, 테마로 해당 예약이 있다면 true를 반환한다.")
    void existReservationByDateTimeAndTheme() {
        Member member = Member.createUser("이름", "이메일", "비밀번호");
        Theme theme = new Theme("이름", "설명", "썸네일");
        ReservationTime time = new ReservationTime(LocalTime.of(10, 0), null);
        LocalDate date = LocalDate.of(2025, 4, 28);

        Reservation reservation = new Reservation(member, date, time, theme);

        jpaThemeRepository.save(theme);
        jpaMemberRepository.save(member);
        jpaReservationTimeRepository.save(time);
        jpaReservationRepository.save(reservation);

        em.flush();
        em.clear();

        assertThat(jpaReservationRepository.existsByDateAndTimeIdAndThemeId(date, 1L, 1L))
            .isTrue();
    }

    @Test
    @DisplayName("해당 날짜, 시간, 테마로 해당 예약이 없다면 false 반환한다.")
    void notExistReservationByDateTimeAndTheme() {
        Member member = Member.createUser("이름", "이메일", "비밀번호");
        Theme theme = new Theme("이름", "설명", "썸네일");
        ReservationTime time = new ReservationTime(LocalTime.of(10, 0), null);
        LocalDate date = LocalDate.of(2025, 4, 28);

        Reservation reservation = new Reservation(member, date, time, theme);

        jpaThemeRepository.save(theme);
        jpaMemberRepository.save(member);
        jpaReservationTimeRepository.save(time);
        jpaReservationRepository.save(reservation);

        em.flush();
        em.clear();

        assertThat(jpaReservationRepository.existsByDateAndTimeIdAndThemeId(date, 1L, 2L))
            .isFalse();
    }
}
