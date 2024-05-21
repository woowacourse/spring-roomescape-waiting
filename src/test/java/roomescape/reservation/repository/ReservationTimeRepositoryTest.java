package roomescape.reservation.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static roomescape.util.Fixture.HORROR_DESCRIPTION;
import static roomescape.util.Fixture.HORROR_THEME_NAME;
import static roomescape.util.Fixture.HOUR_10;
import static roomescape.util.Fixture.KAKI_EMAIL;
import static roomescape.util.Fixture.KAKI_NAME;
import static roomescape.util.Fixture.KAKI_PASSWORD;
import static roomescape.util.Fixture.THUMBNAIL;
import static roomescape.util.Fixture.TODAY;

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
        ReservationTime savedReservationTime = reservationTimeRepository.save(new ReservationTime(HOUR_10));
        ReservationTime findReservationTime = reservationTimeRepository.findById(savedReservationTime.getId()).get();

        assertThat(findReservationTime.getStartAt()).isEqualTo(HOUR_10);
    }

    @DisplayName("전체 엔티티를 조회한다.")
    @Test
    void findAllTest() {
        reservationTimeRepository.save(new ReservationTime(HOUR_10));

        List<ReservationTime> reservationTimes = reservationTimeRepository.findAll();

        assertThat(reservationTimes.size()).isEqualTo(1);
    }

    @DisplayName("예약시간 id로 예약이 참조된 예약시간들을 찾는다.")
    @Test
    void findReservationInSameIdTest() {
        ReservationTime reservationTime = reservationTimeRepository.save(new ReservationTime(HOUR_10));

        Theme theme = themeRepository.save(
                new Theme(
                        new ThemeName(HORROR_THEME_NAME),
                        new Description(HORROR_DESCRIPTION),
                        THUMBNAIL
                )
        );

        Member member = memberRepository.save(Member.createMemberByUserRole(new MemberName(KAKI_NAME), KAKI_EMAIL, KAKI_PASSWORD));

        reservationRepository.save(new Reservation(member, TODAY, theme, reservationTime, Status.SUCCESS));

        boolean exist = !reservationTimeRepository.findReservationTimesThatReservationReferById(reservationTime.getId())
                .isEmpty();

        assertThat(exist).isTrue();
    }

    @DisplayName("id를 받아 예약 시간을 삭제한다.")
    @Test
    void deleteTest() {
        ReservationTime savedReservationTime = reservationTimeRepository.save(new ReservationTime(HOUR_10));

        reservationTimeRepository.deleteById(savedReservationTime.getId());

        List<ReservationTime> reservationTimes = reservationTimeRepository.findAll();

        assertThat(reservationTimes.size()).isEqualTo(0);
    }
}
