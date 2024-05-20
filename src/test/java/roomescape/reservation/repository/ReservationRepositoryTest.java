package roomescape.reservation.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import roomescape.member.domain.Member;
import roomescape.member.repository.MemberRepository;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationTime;
import roomescape.reservation.domain.Theme;
import roomescape.reservation.dto.ReservationSearchRequest;

@DataJpaTest
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
        Long timeId = reservationTimeRepository.save(new ReservationTime(LocalTime.now())).getId();
        ReservationTime reservationTime = reservationTimeRepository.findById(timeId).get();

        Long themeId = themeRepository.save(
                new Theme("공포", "무서운 테마", "https://i.pinimg.com/236x.jpg")
        ).getId();
        Theme theme = themeRepository.findById(themeId).get();

        Long memberId = memberRepository.save(new Member("호기", "hogi@naver.com", "asd")).getId();
        Member member = memberRepository.findById(memberId).get();

        reservationRepository.save(new Reservation(member, LocalDate.now(), theme, reservationTime));

        List<Reservation> reservations = reservationRepository.findAll();

        assertThat(reservations.size()).isEqualTo(1);
    }

    @Test
    @DisplayName("id 값을 받아 Reservation 반환")
    void findByIdTest() {
        Long timeId = reservationTimeRepository.save(new ReservationTime(LocalTime.now())).getId();
        ReservationTime reservationTime = reservationTimeRepository.findById(timeId).get();

        Long themeId = themeRepository.save(
                new Theme("공포", "무서운 테마", "https://i.pinimg.com/236x.jpg")
        ).getId();
        Theme theme = themeRepository.findById(themeId).get();

        Long memberId = memberRepository.save(new Member("호기", "hogi@naver.com", "asd")).getId();
        Member member = memberRepository.findById(memberId).get();

        Long reservationId = reservationRepository.save(
                new Reservation(member, LocalDate.now(), theme, reservationTime)).getId();
        Reservation findReservation = reservationRepository.findById(reservationId).get();

        assertThat(findReservation.getId()).isEqualTo(reservationId);
    }

    @Test
    @DisplayName("날짜와 테마 아이디로 예약 시간 아이디들을 조회한다.")
    void findTimeIdsByDateAndThemeIdTest() {
        Long timeId = reservationTimeRepository.save(new ReservationTime(LocalTime.now())).getId();
        ReservationTime reservationTime = reservationTimeRepository.findById(timeId).get();

        Long themeId = themeRepository.save(
                new Theme("공포", "무서운 테마", "https://i.pinimg.com/236x.jpg")
        ).getId();
        Theme theme = themeRepository.findById(themeId).get();

        Long memberId = memberRepository.save(new Member("호기", "hogi@naver.com", "asd")).getId();
        Member member = memberRepository.findById(memberId).get();

        Reservation reservation = new Reservation(member, LocalDate.now(), theme, reservationTime);
        reservationRepository.save(reservation);

        List<Long> timeIds = reservationRepository.findIdByReservationsDateAndThemeId(reservation.getDate(),
                themeId);

        assertThat(timeIds).containsExactly(timeId);
    }

    @Test
    @DisplayName("이미 저장된 예약일 경우 true를 반환한다.")
    void existReservationTest() {
        Long themeId = themeRepository.save(
                new Theme("공포", "무서운 테마", "https://i.pinimg.com/236x.jpg")
        ).getId();
        Theme theme = themeRepository.findById(themeId).get();

        Long timeId = reservationTimeRepository.save(new ReservationTime(LocalTime.parse("10:00"))).getId();
        ReservationTime reservationTime = reservationTimeRepository.findById(timeId).get();

        Long memberId = memberRepository.save(new Member("호기", "hogi@naver.com", "asd")).getId();
        Member member = memberRepository.findById(memberId).get();

        Long reservationId = reservationRepository.save(
                new Reservation(member, LocalDate.now(), theme, reservationTime)).getId();
        Reservation findReservation = reservationRepository.findById(reservationId).get();

        boolean exist = reservationRepository.existsByDateAndReservationTimeStartAt(findReservation.getDate(),
                findReservation.getTime()
                        .getStartAt());

        assertThat(exist).isTrue();
    }

    @Test
    @DisplayName("회원 아이디, 테마 아이디와 기간이 일치하는 Reservation을 반환한다.")
    void findAllByThemeIdAndMemberIdBetweenStartAndEnd() {
        ReservationTime reservationTime = reservationTimeRepository.save(new ReservationTime(LocalTime.now()));
        Theme theme = themeRepository.save(
                new Theme("공포", "무서운 테마", "https://i.pinimg.com/236x.jpg")
        );
        Member member = memberRepository.save(new Member("호기", "hogi@naver.com", "asd"));

        LocalDate tomorrow = LocalDate.now().plusDays(1);
        LocalDate oneWeekLater = LocalDate.now().plusWeeks(1);
        reservationRepository.save(new Reservation(member, tomorrow, theme, reservationTime));
        reservationRepository.save(new Reservation(member, oneWeekLater, theme, reservationTime));

        ReservationSearchRequest reservationSearchRequest = new ReservationSearchRequest(theme.getId(), member.getId(),
                LocalDate.now().minusDays(1), tomorrow);

        List<Reservation> reservations = reservationRepository.findAllByMemberAndThemeAndDateBetween(
                member, theme, reservationSearchRequest.dateFrom(), reservationSearchRequest.dateTo());

        assertThat(reservations).hasSize(1);
    }

    @Test
    @DisplayName("한 맴버가 예약한 목록을 반환한다.")
    void findAllByMemberIdTest() {
        Member member = memberRepository.save(new Member("hogi", "hoho@naver.com", "1234"));
        Theme theme = themeRepository.save(new Theme("a", "a", "a"));
        ReservationTime time = reservationTimeRepository.save(new ReservationTime(LocalTime.now()));
        reservationRepository.save(new Reservation(member, LocalDate.now(), theme, time));

        List<Reservation> reservations = reservationRepository.findAllByMemberId(member.getId());

        assertThat(reservations.size()).isEqualTo(1);
    }

    @Test
    @DisplayName("DB 삭제 테스트")
    void deleteTest() {
        Long timeId = reservationTimeRepository.save(new ReservationTime(LocalTime.now())).getId();
        ReservationTime reservationTime = reservationTimeRepository.findById(timeId).get();

        Long themeId = themeRepository.save(
                new Theme("공포", "무서운 테마", "https://i.pinimg.com/236x.jpg")
        ).getId();
        Theme theme = themeRepository.findById(themeId).get();

        Long memberId = memberRepository.save(new Member("호기", "hogi@naver.com", "asd")).getId();
        Member member = memberRepository.findById(memberId).get();

        Long reservationId = reservationRepository.save(
                new Reservation(member, LocalDate.now(), theme, reservationTime)).getId();
        reservationRepository.deleteById(reservationId);
        List<Reservation> reservations = reservationRepository.findAll();

        assertThat(reservations.size()).isEqualTo(0);
    }

    @Test
    void findByMemberAndThemeAndDateBetween() {
        Theme theme = new Theme("공포", "호러 방탈출", "http://asdf.jpg");
        themeRepository.save(theme);
        LocalTime localTime = LocalTime.parse("10:00");
        ReservationTime reservationTime = new ReservationTime(localTime);
        reservationTimeRepository.save(reservationTime);
        Member member = new Member("마크", "mark@woowa.com", "1234");
        memberRepository.save(member);

        LocalDate localDate = LocalDate.now().plusYears(1);
        Reservation reservation = new Reservation(member, localDate, theme, reservationTime);
        reservationRepository.save(reservation);

        List<Reservation> reservations = reservationRepository.findAllByMemberAndThemeAndDateBetween(member, theme, localDate, localDate);
        assertThat(reservations.size()).isEqualTo(1);
    }
}
