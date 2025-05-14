package roomescape.repository.jpa;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import roomescape.entity.Member;
import roomescape.entity.Reservation;
import roomescape.entity.ReservationTime;
import roomescape.entity.Theme;

@DataJpaTest
class JpaReservationTimeRepositoryTest {

    @Autowired
    private JpaReservationTimeRepository jpaReservationTimeRepository;
    @Autowired
    private JpaThemeRepository jpaThemeRepository;
    @Autowired
    private JpaReservationRepository jpaReservationRepository;

    @Test
    @DisplayName("주어진 시간과 테마의 예약 시간과 현재 예약 여부를 함께 조회한다")
    void findAllTimesWithBooked() {
        Member member = Member.createUser("이름", "이메일", "비밀번호");
        Theme theme = new Theme("이름", "설명", "썸네일");
        ReservationTime time = new ReservationTime(LocalTime.of(10, 0), null);
        LocalDate date = LocalDate.of(2025, 4, 28);

        Reservation reservation = new Reservation(member, date, time, theme);

        jpaThemeRepository.save(theme);
        jpaReservationTimeRepository.save(time);
        jpaReservationRepository.save(reservation);

        List<ReservationTime> times = jpaReservationTimeRepository.findAllTimesWithBooked(date, 1L);

        assertThat(times.getFirst().getAlreadyBooked()).isTrue();
    }


    @Test
    @DisplayName("해당 시간이 있다면 true를 반환한다.")
    void existTimeByStartAt() {
        LocalTime time = LocalTime.of(10, 0);

        jpaReservationTimeRepository.save(new ReservationTime(time, false));

        assertThat(jpaReservationTimeRepository.existsByStartAt(time)).isTrue();
    }

    @Test
    @DisplayName("해당 시간이 없다면 false를 반환한다.")
    void notExistTimeByStartAt() {
        LocalTime time = LocalTime.of(11, 0);

        assertThat(jpaReservationTimeRepository.existsByStartAt(time)).isFalse();
    }
}
