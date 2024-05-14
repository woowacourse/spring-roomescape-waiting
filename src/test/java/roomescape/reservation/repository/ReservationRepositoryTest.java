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
import roomescape.reservation.dto.ReservationSearchRequest;

@JdbcTest
@Import({ReservationRepository.class, ThemeRepository.class, ReservationTimeRepository.class, MemberRepository.class})
public class ReservationRepositoryTest {

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private ThemeRepository themeRepository;

    @Autowired
    private ReservationTimeRepository reservationTimeRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Test
    @DisplayName("DB 조회 테스트")
    void findAllTest() {
        Long timeId = reservationTimeRepository.save(new ReservationTime(LocalTime.now()));
        ReservationTime reservationTime = reservationTimeRepository.findById(timeId).get();

        Long themeId = themeRepository.save(
                new Theme("공포", "무서운 테마", "https://i.pinimg.com/236x.jpg")
        );
        Theme theme = themeRepository.findById(themeId).get();

        Long memberId = memberRepository.save(new Member(new MemberName("호기"), "hogi@naver.com", "asd"));
        Member member = memberRepository.findById(memberId).get();

        reservationRepository.save(new Reservation(member, LocalDate.now(), theme, reservationTime));

        List<Reservation> reservations = reservationRepository.findAll();

        assertThat(reservations.size()).isEqualTo(1);
    }

    @Test
    @DisplayName("id 값을 받아 Reservation 반환")
    void findByIdTest() {
        Long timeId = reservationTimeRepository.save(new ReservationTime(LocalTime.now()));
        ReservationTime reservationTime = reservationTimeRepository.findById(timeId).get();

        Long themeId = themeRepository.save(
                new Theme("공포", "무서운 테마", "https://i.pinimg.com/236x.jpg")
        );
        Theme theme = themeRepository.findById(themeId).get();

        Long memberId = memberRepository.save(new Member(new MemberName("호기"), "hogi@naver.com", "asd"));
        Member member = memberRepository.findById(memberId).get();

        Long reservationId = reservationRepository.save(
                new Reservation(member, LocalDate.now(), theme, reservationTime));
        Reservation findReservation = reservationRepository.findById(reservationId).get();

        assertThat(findReservation.getId()).isEqualTo(reservationId);
    }

    @Test
    @DisplayName("날짜와 테마 아이디로 예약 시간 아이디들을 조회한다.")
    void findTimeIdsByDateAndThemeIdTest() {
        Long timeId = reservationTimeRepository.save(new ReservationTime(LocalTime.now()));
        ReservationTime reservationTime = reservationTimeRepository.findById(timeId).get();

        Long themeId = themeRepository.save(
                new Theme("공포", "무서운 테마", "https://i.pinimg.com/236x.jpg")
        );
        Theme theme = themeRepository.findById(themeId).get();

        Long memberId = memberRepository.save(new Member(new MemberName("호기"), "hogi@naver.com", "asd"));
        Member member = memberRepository.findById(memberId).get();

        Reservation reservation = new Reservation(member, LocalDate.now(), theme, reservationTime);
        reservationRepository.save(reservation);

        List<Long> timeIds = reservationRepository.findTimeIdsByDateAndThemeId(reservation.getDate(), themeId);

        assertThat(timeIds).containsExactly(timeId);
    }

    @Test
    @DisplayName("이미 저장된 예약일 경우 true를 반환한다.")
    void existReservationTest() {
        Long themeId = themeRepository.save(
                new Theme("공포", "무서운 테마", "https://i.pinimg.com/236x.jpg")
        );
        Theme theme = themeRepository.findById(themeId).get();

        Long timeId = reservationTimeRepository.save(new ReservationTime(LocalTime.parse("10:00")));
        ReservationTime reservationTime = reservationTimeRepository.findById(timeId).get();

        Long memberId = memberRepository.save(new Member(new MemberName("호기"), "hogi@naver.com", "asd"));
        Member member = memberRepository.findById(memberId).get();

        Long reservationId = reservationRepository.save(
                new Reservation(member, LocalDate.now(), theme, reservationTime));
        Reservation findReservation = reservationRepository.findById(reservationId).get();

        boolean exist = reservationRepository.existReservation(findReservation);

        assertThat(exist).isTrue();
    }

    @Test
    @DisplayName("회원 아이디, 테마 아이디와 기간이 일치하는 Reservation을 반환한다.")
    void findAllByThemeIdAndMemberIdBetweenStartAndEnd() {
        Long timeId = reservationTimeRepository.save(new ReservationTime(LocalTime.now()));
        ReservationTime reservationTime = reservationTimeRepository.findById(timeId).get();

        Long themeId = themeRepository.save(
                new Theme("공포", "무서운 테마", "https://i.pinimg.com/236x.jpg")
        );
        Theme theme = themeRepository.findById(themeId).get();

        Long memberId = memberRepository.save(new Member(new MemberName("호기"), "hogi@naver.com", "asd"));
        Member member = memberRepository.findById(memberId).get();

        LocalDate tomorrow = LocalDate.now().plusDays(1);
        LocalDate oneWeekLater = LocalDate.now().plusWeeks(1);
        reservationRepository.save(new Reservation(member, tomorrow, theme, reservationTime));
        reservationRepository.save(new Reservation(member, oneWeekLater, theme, reservationTime));

        ReservationSearchRequest reservationSearchRequest = new ReservationSearchRequest(themeId, memberId,
                LocalDate.now(), tomorrow);
        List<Reservation> reservations = reservationRepository.findAllByThemeIdAndMemberIdBetweenStartAndEnd(
                reservationSearchRequest);

        assertThat(reservations).hasSize(1);
    }

    @Test
    @DisplayName("DB 삭제 테스트")
    void deleteTest() {
        Long timeId = reservationTimeRepository.save(new ReservationTime(LocalTime.now()));
        ReservationTime reservationTime = reservationTimeRepository.findById(timeId).get();

        Long themeId = themeRepository.save(
                new Theme("공포", "무서운 테마", "https://i.pinimg.com/236x.jpg")
        );
        Theme theme = themeRepository.findById(themeId).get();

        Long memberId = memberRepository.save(new Member(new MemberName("호기"), "hogi@naver.com", "asd"));
        Member member = memberRepository.findById(memberId).get();

        Long reservationId = reservationRepository.save(
                new Reservation(member, LocalDate.now(), theme, reservationTime));
        reservationRepository.delete(reservationId);
        List<Reservation> reservations = reservationRepository.findAll();

        assertThat(reservations.size()).isEqualTo(0);
    }
}
