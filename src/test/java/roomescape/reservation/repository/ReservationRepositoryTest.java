package roomescape.reservation.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static roomescape.util.Fixture.HORROR_DESCRIPTION;
import static roomescape.util.Fixture.HORROR_THEME_NAME;
import static roomescape.util.Fixture.HOUR_10;
import static roomescape.util.Fixture.JOJO_EMAIL;
import static roomescape.util.Fixture.JOJO_NAME;
import static roomescape.util.Fixture.JOJO_PASSWORD;
import static roomescape.util.Fixture.KAKI_EMAIL;
import static roomescape.util.Fixture.KAKI_NAME;
import static roomescape.util.Fixture.KAKI_PASSWORD;
import static roomescape.util.Fixture.THUMBNAIL;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import roomescape.member.domain.Member;
import roomescape.member.domain.MemberName;
import roomescape.member.repository.MemberRepository;
import roomescape.reservation.domain.Description;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationTime;
import roomescape.reservation.domain.Status;
import roomescape.reservation.domain.Theme;
import roomescape.reservation.domain.ThemeName;
import roomescape.reservation.dto.request.ReservationSearchCondRequest;

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

    @DisplayName("전체 예약 목록을 조회한다.")
    @Test
    void findAllTest() {
        ReservationTime reservationTime = reservationTimeRepository.save(new ReservationTime(LocalTime.parse(HOUR_10)));

        Theme theme = themeRepository.save(
                new Theme(
                        new ThemeName(HORROR_THEME_NAME),
                        new Description(HORROR_DESCRIPTION),
                        THUMBNAIL
                )
        );

        Member member = memberRepository.save(new Member(new MemberName(KAKI_NAME), KAKI_EMAIL, KAKI_PASSWORD));

        reservationRepository.save(new Reservation(member, LocalDate.now(), theme, reservationTime, Status.SUCCESS));

        List<Reservation> reservations = reservationRepository.findAll();

        assertThat(reservations.size()).isEqualTo(1);
    }

    @DisplayName("회원 id로 예약 목록을 조회한다.")
    @Test
    void findAllByMemberId() {
        ReservationTime reservationTime = reservationTimeRepository.save(new ReservationTime(LocalTime.parse(HOUR_10)));

        Theme theme = themeRepository.save(
                new Theme(
                        new ThemeName(HORROR_THEME_NAME),
                        new Description(HORROR_DESCRIPTION),
                        THUMBNAIL
                )
        );

        Member kaki = memberRepository.save(new Member(new MemberName(KAKI_NAME), KAKI_EMAIL, KAKI_PASSWORD));
        Member jojo = memberRepository.save(new Member(new MemberName(JOJO_NAME), JOJO_EMAIL, JOJO_PASSWORD));

        reservationRepository.save(new Reservation(kaki, LocalDate.now(), theme, reservationTime, Status.SUCCESS));
        reservationRepository.save(new Reservation(jojo, LocalDate.now(), theme, reservationTime, Status.SUCCESS));

        List<Reservation> reservations = reservationRepository.findAllByMemberId(kaki.getId());

        assertThat(reservations.size()).isEqualTo(1);
    }

    @DisplayName("id 값을 받아 Reservation 반환")
    @Test
    void findByIdTest() {
        ReservationTime reservationTime = reservationTimeRepository.save(new ReservationTime(LocalTime.parse(HOUR_10)));

        Theme theme = themeRepository.save(
                new Theme(
                        new ThemeName(HORROR_THEME_NAME),
                        new Description(HORROR_DESCRIPTION),
                        THUMBNAIL
                )
        );

        Member member = memberRepository.save(new Member(new MemberName(KAKI_NAME), KAKI_EMAIL, KAKI_PASSWORD));

        Reservation savedReservation = reservationRepository.save(
                new Reservation(member, LocalDate.now(), theme, reservationTime, Status.SUCCESS));
        Reservation findReservation = reservationRepository.findById(savedReservation.getId()).get();

        assertThat(findReservation.getMember().getEmail()).isEqualTo(savedReservation.getMember().getEmail());
    }

    @DisplayName("날짜와 테마 아이디로 예약 시간 아이디들을 조회한다.")
    @Test
    void findTimeIdsByDateAndThemeIdTest() {
        ReservationTime reservationTime = reservationTimeRepository.save(new ReservationTime(LocalTime.parse(HOUR_10)));

        Theme theme = themeRepository.save(
                new Theme(
                        new ThemeName(HORROR_THEME_NAME),
                        new Description(HORROR_DESCRIPTION),
                        THUMBNAIL
                )
        );

        Member member = memberRepository.save(new Member(new MemberName(KAKI_NAME), KAKI_EMAIL, KAKI_PASSWORD));

        Reservation savedReservation = reservationRepository.save(
                new Reservation(member, LocalDate.now(), theme, reservationTime, Status.SUCCESS));

        List<Long> timeIds = reservationRepository.findTimeIdsByDateAndThemeId(savedReservation.getDate(),
                theme.getId());

        assertThat(timeIds).containsExactly(reservationTime.getId());
    }

    @DisplayName("같은 테마, 날짜, 시간에 예약이 있을 경우 true를 반환한다.")
    @Test
    void existReservationTest() {
        ReservationTime reservationTime = reservationTimeRepository.save(new ReservationTime(LocalTime.parse(HOUR_10)));

        Theme theme = themeRepository.save(
                new Theme(
                        new ThemeName(HORROR_THEME_NAME),
                        new Description(HORROR_DESCRIPTION),
                        THUMBNAIL
                )
        );

        Member member = memberRepository.save(new Member(new MemberName(KAKI_NAME), KAKI_EMAIL, KAKI_PASSWORD));

        Reservation savedReservation = reservationRepository.save(
                new Reservation(member, LocalDate.now(), theme, reservationTime, Status.SUCCESS));

        boolean exist = reservationRepository.existsByDateAndReservationTimeStartAtAndTheme(
                savedReservation.getDate(),
                savedReservation.getStartAt(),
                savedReservation.getTheme()
        );

        assertThat(exist).isTrue();
    }

    @DisplayName("회원 아이디, 테마 아이디와 기간이 일치하는 Reservation을 반환한다.")
    @Test
    void findAllByThemeIdAndMemberIdAndBetweenStartDateAndEndDate() {
        ReservationTime reservationTime = reservationTimeRepository.save(new ReservationTime(LocalTime.parse(HOUR_10)));

        Theme theme = themeRepository.save(
                new Theme(
                        new ThemeName(HORROR_THEME_NAME),
                        new Description(HORROR_DESCRIPTION),
                        THUMBNAIL
                )
        );

        Member member = memberRepository.save(new Member(new MemberName(KAKI_NAME), KAKI_EMAIL, KAKI_PASSWORD));

        LocalDate tomorrow = LocalDate.now().plusDays(1);
        LocalDate oneWeekLater = LocalDate.now().plusWeeks(1);
        reservationRepository.save(new Reservation(member, tomorrow, theme, reservationTime, Status.SUCCESS));
        reservationRepository.save(new Reservation(member, oneWeekLater, theme, reservationTime, Status.SUCCESS));

        ReservationSearchCondRequest request = new ReservationSearchCondRequest(
                theme.getId(),
                member.getId(),
                LocalDate.now(),
                tomorrow
        );

        List<Reservation> reservations = reservationRepository.findAllByThemeIdAndMemberIdAndDateBetween(
                request.themeId(),
                request.memberId(),
                request.dateFrom(),
                request.dateTo()
        );

        assertThat(reservations).hasSize(1);
    }

    @DisplayName("예약 삭제 테스트")
    @Test
    void deleteTest() {
        ReservationTime reservationTime = reservationTimeRepository.save(new ReservationTime(LocalTime.parse(HOUR_10)));

        Theme theme = themeRepository.save(
                new Theme(
                        new ThemeName(HORROR_THEME_NAME),
                        new Description(HORROR_DESCRIPTION),
                        THUMBNAIL
                )
        );

        Member member = memberRepository.save(new Member(new MemberName(KAKI_NAME), KAKI_EMAIL, KAKI_PASSWORD));

        Reservation savedReservation = reservationRepository.save(
                new Reservation(member, LocalDate.now(), theme, reservationTime, Status.SUCCESS)
        );
        reservationRepository.deleteById(savedReservation.getId());

        List<Reservation> reservations = reservationRepository.findAll();

        assertThat(reservations.size()).isEqualTo(0);
    }
}
