package roomescape.reservation.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import roomescape.member.domain.Member;
import roomescape.member.domain.MemberName;
import roomescape.member.repository.MemberRepository;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationTime;
import roomescape.reservation.domain.Theme;

@JdbcTest
@Import({ReservationTimeRepository.class, ThemeRepository.class, ReservationRepository.class, MemberRepository.class})
class ReservationTimeRepositoryTest {

    @Autowired
    private ReservationTimeRepository reservationTimeRepository;

    @Autowired
    private ThemeRepository themeRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private ReservationRepository reservationRepository;

    @Test
    @DisplayName("id 로 엔티티를 찾는다.")
    void findByIdTest() {
        ReservationTime reservationTime = new ReservationTime(LocalTime.now());
        Long timeId = reservationTimeRepository.save(reservationTime);
        ReservationTime findReservationTime = reservationTimeRepository.findById(timeId).get();

        assertThat(findReservationTime.getId()).isEqualTo(timeId);
    }

    @Test
    @DisplayName("전체 엔티티를 조회한다.")
    void findAllTest() {
        ReservationTime reservationTime1 = new ReservationTime(LocalTime.now());
        ReservationTime reservationTime2 = new ReservationTime(LocalTime.now());
        reservationTimeRepository.save(reservationTime1);
        reservationTimeRepository.save(reservationTime2);
        List<ReservationTime> reservationTimes = reservationTimeRepository.findAll();

        assertThat(reservationTimes.size()).isEqualTo(2);
    }

    @Test
    @DisplayName("해당 시간을 참조하는 Reservation이 있는지 찾는다.")
    void findReservationInSameIdTest() {
        Long themeId = themeRepository.save(
                new Theme("공포", "무서운 테마", "https://i.pinimg.com/236x.jpg")
        );
        Theme theme = themeRepository.findById(themeId).get();

        Long timeId = reservationTimeRepository.save(new ReservationTime(LocalTime.now()));
        ReservationTime reservationTime = reservationTimeRepository.findById(timeId).get();

        Long memberId = memberRepository.save(new Member(new MemberName("호기"), "hogi@naver.com", "asd"));
        Member member = memberRepository.findById(memberId).get();

        reservationRepository.save(new Reservation(member, LocalDate.now(), theme, reservationTime));
        boolean exist = reservationTimeRepository.findReservationInSameId(timeId).isPresent();

        assertThat(exist).isTrue();
    }

    @Test
    @DisplayName("id를 받아 삭제한다.")
    void deleteTest() {
        ReservationTime reservationTime = new ReservationTime(LocalTime.now());
        Long timeId = reservationTimeRepository.save(reservationTime);
        reservationTimeRepository.delete(timeId);
        List<ReservationTime> reservationTimes = reservationTimeRepository.findAll();

        assertThat(reservationTimes.size()).isEqualTo(0);
    }
}
