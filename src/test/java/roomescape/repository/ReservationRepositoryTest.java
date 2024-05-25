package roomescape.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import roomescape.domain.member.Member;
import roomescape.domain.reservation.Reservation;
import roomescape.domain.reservation.ReservationStatus;
import roomescape.domain.reservation.ReservationTime;
import roomescape.domain.theme.Theme;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static roomescape.TestFixture.*;

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

    private Member member;
    private ReservationTime reservationTime;
    private Theme theme;
    private Reservation reservation;

    @BeforeEach
    void setUp() {
        member = memberRepository.save(MEMBER_MIA());
        reservationTime = reservationTimeRepository.save(RESERVATION_TIME_SIX());
        theme = themeRepository.save(THEME_HORROR());
        reservation = reservationRepository.save(new Reservation(member, LocalDate.parse(DATE_MAY_EIGHTH), reservationTime, theme, ReservationStatus.RESERVED));
    }

    @Test
    @DisplayName("예약을 저장한다.")
    void save() {
        // given
        final Reservation reservation = new Reservation(member, LocalDate.parse(DATE_MAY_NINTH), reservationTime, theme, ReservationStatus.RESERVED);

        // when
        final Reservation actual = reservationRepository.save(reservation);

        // then
        assertThat(actual.getId()).isNotNull();
    }

    @Test
    @DisplayName("검색 조건에 따른 예약 목록을 조회한다.")
    void findAllByFilterParameter() {
        // when
        final List<Reservation> actual = reservationRepository.findByThemeIdAndMemberIdAndDateBetweenAndStatus(
                theme.getId(), member.getId(),
                LocalDate.parse(DATE_MAY_EIGHTH), LocalDate.parse(DATE_MAY_NINTH), ReservationStatus.RESERVED
        );

        // then
        assertThat(actual).hasSize(1);
    }

    @Test
    @DisplayName("동일 시간대의 예약 건수를 조회한다.")
    void countByDateAndTime() {
        // when
        final int actual = reservationRepository.countByDateAndTimeIdAndThemeId(
                LocalDate.parse(DATE_MAY_EIGHTH), reservationTime.getId(), theme.getId()
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
        final int actual = reservationRepository.countByTimeId(timeId);

        // then
        assertThat(actual).isEqualTo(0);
    }

    @Test
    @DisplayName("날짜와 테마 Id에 해당하는 예약 목록을 조회한다.")
    void findAllByDateAndThemeId() {
        // when
        final List<Reservation> actual = reservationRepository.findByDateAndThemeId(
                LocalDate.parse(DATE_MAY_EIGHTH), theme.getId());

        // then
        assertThat(actual).hasSize(1);
    }

    @Test
    @DisplayName("특정 사용자의 예약 목록 및 대기 목록을 조회한다.")
    void findByReservationsMemberId() {
        final Long memberId = member.getId();
        reservationRepository.save(new Reservation(member, LocalDate.parse(DATE_MAY_EIGHTH), reservationTime, theme, ReservationStatus.WAITING));

        final List<Reservation> actual = reservationRepository.findByMemberId(memberId);

        assertAll(
                () -> assertThat(actual).hasSize(2),
                () -> assertThat(actual.get(0).getStatus()).isEqualTo(ReservationStatus.RESERVED),
                () -> assertThat(actual.get(1).getStatus()).isEqualTo(ReservationStatus.WAITING)
        );
    }
    
    @ParameterizedTest
    @EnumSource(ReservationStatus.class)
    @DisplayName("예약 상태에 따른 예약 목록을 조회한다.")
    void findByStatus(final ReservationStatus status) {
        final List<Reservation> actual = reservationRepository.findByStatus(status);

        assertThat(actual).allSatisfy(reservation -> assertThat(reservation.getStatus()).isEqualTo(status));
    }

    @Test
    @DisplayName("테마, 날짜, 시간에 해당하는 예약이 있는지 확인한다.")
    void existsByThemeAndDateAndTimeAndStatus() {
        final boolean actual = reservationRepository.existsByThemeAndDateAndTimeAndStatus(
                theme, LocalDate.parse(DATE_MAY_EIGHTH), reservationTime, ReservationStatus.RESERVED);

        assertThat(actual).isTrue();
    }

    @Test
    @DisplayName("테마, 날짜, 시간, 멤버에 해당하는 예약이 있는지 확인한다.")
    void existsByThemeAndDateAndTimeAndStatusAndMember() {
        final boolean actual = reservationRepository.existsByThemeAndDateAndTimeAndStatusAndMember(
                theme, LocalDate.parse(DATE_MAY_EIGHTH), reservationTime, ReservationStatus.RESERVED, member);

        assertThat(actual).isTrue();
    }
}
