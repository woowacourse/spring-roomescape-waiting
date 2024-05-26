package roomescape.reservation.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static roomescape.util.Fixture.HORROR_THEME;
import static roomescape.util.Fixture.KAKI;
import static roomescape.util.Fixture.RESERVATION_HOUR_10;
import static roomescape.util.Fixture.TODAY;

import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import roomescape.member.domain.Member;
import roomescape.member.repository.MemberRepository;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationStatus;
import roomescape.reservation.domain.ReservationTime;
import roomescape.reservation.domain.Theme;

@DataJpaTest
class ReservationTimeRepositoryTest {

    @Autowired
    private ReservationTimeRepository reservationTimeRepository;

    @Autowired
    private ThemeRepository themeRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private ReservationRepository reservationRepository;

    @DisplayName("id로 예약 시간을 조회한다.")
    @Test
    void findByIdTest() {
        ReservationTime hour10 = reservationTimeRepository.save(RESERVATION_HOUR_10);
        ReservationTime findReservationTime = reservationTimeRepository.findById(hour10.getId()).get();

        assertThat(findReservationTime.getStartAt()).isEqualTo(hour10.getStartAt());
    }

    @DisplayName("시간으로 예약 시간을 조회한다.")
    @Test
    void findByStartAt() {
        ReservationTime hour10 = reservationTimeRepository.save(RESERVATION_HOUR_10);
        ReservationTime findReservationTime = reservationTimeRepository.findByStartAt(hour10.getStartAt()).get();

        assertThat(findReservationTime.getStartAt()).isEqualTo(hour10.getStartAt());
    }

    @DisplayName("전체 엔티티를 조회한다.")
    @Test
    void findAllTest() {
        reservationTimeRepository.save(RESERVATION_HOUR_10);
        List<ReservationTime> reservationTimes = reservationTimeRepository.findAll();

        assertThat(reservationTimes.size()).isEqualTo(1);
    }

    @DisplayName("예약시간 id로 예약이 참조된 예약시간들을 찾는다.")
    @Test
    void findReservationInSameIdTest() {
        ReservationTime hour10 = reservationTimeRepository.save(RESERVATION_HOUR_10);

        Theme horrorTheme = themeRepository.save(HORROR_THEME);

        Member kaki = memberRepository.save(KAKI);

        reservationRepository.save(new Reservation(kaki, TODAY, horrorTheme, hour10, ReservationStatus.SUCCESS));

        boolean exist = !reservationTimeRepository.findReservationTimesThatReservationReferById(hour10.getId())
                .isEmpty();

        assertThat(exist).isTrue();
    }

    @DisplayName("id를 받아 예약 시간을 삭제한다.")
    @Test
    void deleteTest() {
        ReservationTime hour10 = reservationTimeRepository.save(RESERVATION_HOUR_10);

        reservationTimeRepository.deleteById(hour10.getId());

        List<ReservationTime> reservationTimes = reservationTimeRepository.findAll();

        assertThat(reservationTimes.size()).isEqualTo(0);
    }
}
