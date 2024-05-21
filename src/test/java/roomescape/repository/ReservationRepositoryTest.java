package roomescape.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import roomescape.domain.member.Member;
import roomescape.domain.reservation.Reservation;
import roomescape.domain.reservation.ReservationTime;
import roomescape.domain.theme.Theme;
import roomescape.dto.reservation.AvailableReservationTimeSearch;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static roomescape.TestFixture.DATE_MAY_EIGHTH;
import static roomescape.TestFixture.DATE_MAY_NINTH;
import static roomescape.TestFixture.MEMBER_BROWN;
import static roomescape.TestFixture.RESERVATION_TIME_SIX;
import static roomescape.TestFixture.THEME_HORROR;

@DataJpaTest
class ReservationRepositoryTest {

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private ReservationTimeRepository reservationTimeRepository;

    @Autowired
    private ThemeRepository themeRepository;

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private TestEntityManager testEntityManager;

    private Member member;
    private ReservationTime reservationTime;
    private Theme theme;
    private Reservation reservation;

    @BeforeEach
    void setUp() {
        member = memberRepository.save(MEMBER_BROWN());
        reservationTime = reservationTimeRepository.save(RESERVATION_TIME_SIX());
        theme = themeRepository.save(THEME_HORROR());
        reservation = reservationRepository.save(new Reservation(member, DATE_MAY_EIGHTH, reservationTime, theme));
        testEntityManager.clear();
    }

    @Test
    @DisplayName("예약을 저장한다.")
    void save() {
        // given
        final Reservation reservation = new Reservation(member, DATE_MAY_NINTH, reservationTime, theme);

        // when
        final Reservation actual = reservationRepository.save(reservation);

        // then
        assertThat(actual.getId()).isNotNull();
    }

    @Test
    @DisplayName("검색 조건에 따른 예약 목록을 조회한다.")
    void findAllByFilterParameter() {
        // when
        final List<Reservation> actual = reservationRepository.findByTheme_IdAndMember_IdAndDateBetween(
                theme.getId(), member.getId(), DATE_MAY_EIGHTH, DATE_MAY_NINTH
        );

        // then
        assertThat(actual).hasSize(1);
    }

    @Test
    @DisplayName("동일 시간대의 예약 건수를 조회한다.")
    void countByDateAndTime() {
        // when
        final int actual = reservationRepository.countByDateAndTime_IdAndTheme_Id(
                DATE_MAY_EIGHTH, reservationTime.getId(), theme.getId()
        );

        // then
        assertThat(actual).isEqualTo(1);
    }

    @Test
    @DisplayName("Id에 해당하는 예약이 존재하면 true를 반환한다.")
    void returnTrueWhenExistById() {
        // when
        final boolean actual = reservationRepository.existsById(reservation.getId());

        // then
        assertThat(actual).isTrue();
    }

    @Test
    @DisplayName("Id에 해당하는 예약이 존재하지 않으면 false를 반환한다.")
    void returnFalseWhenNotExistById() {
        // given
        final Long id = 0L;

        // when
        final boolean actual = reservationRepository.existsById(id);

        // then
        assertThat(actual).isFalse();
    }

    @Test
    @DisplayName("Id에 해당하는 예약을 삭제한다.")
    void deleteById() {
        // when
        reservationRepository.deleteById(reservation.getId());

        // then
        final List<Reservation> actual = reservationRepository.findAll();
        assertThat(actual).doesNotContain(reservation);
    }

    @Test
    @DisplayName("timeId에 해당하는 예약 건수를 조회한다.")
    void countByTimeId() {
        // given
        final long timeId = 2L;

        // when
        final int actual = reservationRepository.countByTime_Id(timeId);

        // then
        assertThat(actual).isEqualTo(0);
    }

    @Test
    @DisplayName("예약 가능한 시간 목록을 조회한다.")
    void findAllByDateAndThemeId() {
        // given
        AvailableReservationTimeSearch condition = new AvailableReservationTimeSearch(
                DATE_MAY_EIGHTH, theme.getId()
        );

        // when
        final List<Long> actual = reservationRepository.findTimeIds(condition);

        // then
        assertThat(actual).hasSize(1);
    }

    @Test
    @DisplayName("멤버 Id에 해당하는 예약을 조회한다.")
    void findByMember_Id() {
        List<Reservation> reservations = reservationRepository.findByMember_Id(member.getId());

        assertThat(reservations).contains(reservation);
    }

    @Test
    @DisplayName("모든 예약을 조회한다.")
    void findAll() {
        List<Reservation> reservations = reservationRepository.findAll();

        assertThat(reservations).contains(reservation);
    }
}
