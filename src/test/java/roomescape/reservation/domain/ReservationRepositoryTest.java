package roomescape.reservation.domain;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import roomescape.common.RepositoryTest;
import roomescape.member.domain.Member;
import roomescape.member.domain.MemberRepository;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.SoftAssertions.assertSoftly;
import static roomescape.TestFixture.HORROR_THEME;
import static roomescape.TestFixture.MIA_RESERVATION;
import static roomescape.TestFixture.MIA_RESERVATION_DATE;
import static roomescape.TestFixture.MIA_RESERVATION_TIME;
import static roomescape.TestFixture.TOMMY_RESERVATION;
import static roomescape.TestFixture.TOMMY_RESERVATION_DATE;
import static roomescape.TestFixture.USER_MIA;
import static roomescape.TestFixture.USER_TOMMY;
import static roomescape.TestFixture.WOOTECO_THEME;
import static roomescape.TestFixture.WOOTECO_THEME_NAME;
import static roomescape.reservation.domain.ReservationStatus.BOOKING;
import static roomescape.reservation.domain.ReservationStatus.WAITING;

class ReservationRepositoryTest extends RepositoryTest {
    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private ReservationTimeRepository reservationTimeRepository;

    @Autowired
    private ThemeRepository themeRepository;

    @Autowired
    private MemberRepository memberRepository;

    private ReservationTime reservationTime;
    private Theme wootecoTheme;
    private Theme horrorTheme;
    private Member mia;
    private Member tommy;

    @BeforeEach
    void setUp() {
        this.reservationTime = reservationTimeRepository.save(new ReservationTime(MIA_RESERVATION_TIME));
        this.wootecoTheme = themeRepository.save(WOOTECO_THEME());
        this.horrorTheme = themeRepository.save(HORROR_THEME());
        this.mia = memberRepository.save(USER_MIA());
        this.tommy = memberRepository.save(USER_TOMMY());
    }

    @Test
    @DisplayName("예약을 저장한다.")
    void save() {
        // given
        Reservation reservation = MIA_RESERVATION(reservationTime, wootecoTheme, mia, BOOKING);

        // when
        Reservation savedReservation = reservationRepository.save(reservation);

        // then
        assertThat(savedReservation.getId()).isNotNull();
    }

    @Test
    @DisplayName("모든 예약 목록을 조회한다.")
    void findAllWithDetails() {
        // given
        reservationRepository.save(MIA_RESERVATION(reservationTime, wootecoTheme, mia, BOOKING));

        // when
        List<Reservation> reservations = reservationRepository.findAllWithDetails();

        // then
        assertSoftly(softly -> {
            softly.assertThat(reservations).hasSize(1)
                    .extracting(Reservation::getTheme)
                    .extracting(Theme::getName)
                    .containsExactly(WOOTECO_THEME_NAME);
            softly.assertThat(reservations).extracting(Reservation::getTime)
                    .extracting(ReservationTime::getStartAt)
                    .containsExactly(MIA_RESERVATION_TIME);
        });
    }

    @Test
    @DisplayName("예약자, 테마, 날짜로 예약 목록을 조회한다.")
    void findAllByMemberIdAndThemeIdAndDateBetween() {
        // given
        reservationRepository.save(new Reservation(mia, MIA_RESERVATION_DATE, reservationTime, wootecoTheme, BOOKING));
        reservationRepository.save(new Reservation(mia, MIA_RESERVATION_DATE.plusDays(2), reservationTime, wootecoTheme, BOOKING));
        reservationRepository.save(new Reservation(tommy, MIA_RESERVATION_DATE, reservationTime, wootecoTheme, BOOKING));
        reservationRepository.save(new Reservation(mia, MIA_RESERVATION_DATE, reservationTime, horrorTheme, BOOKING));

        // when
        List<Reservation> reservations = reservationRepository.findAllByMemberAndThemeAndDateBetween(
                mia, wootecoTheme, MIA_RESERVATION_DATE, MIA_RESERVATION_DATE.plusDays(1));

        // then
        assertSoftly(softly -> {
            softly.assertThat(reservations).hasSize(1);
            softly.assertThat(reservations.get(0).getMember().getId()).isEqualTo(mia.getId());
            softly.assertThat(reservations.get(0).getTheme().getId()).isEqualTo(wootecoTheme.getId());
            softly.assertThat(reservations.get(0).getDate()).isEqualTo(MIA_RESERVATION_DATE);
        });
    }

    @Test
    @DisplayName("Id로 예약을 삭제한다.")
    void deleteById() {
        // given
        Long id = reservationRepository.save(MIA_RESERVATION(reservationTime, wootecoTheme, mia, BOOKING)).getId();

        // when
        reservationRepository.deleteById(id);

        // then
        List<Reservation> reservations = reservationRepository.findAll();
        assertThat(reservations).hasSize(0);
    }

    @Test
    @DisplayName("timeId에 해당하는 예약 건수를 조회한다.")
    void countByTimeId() {
        //giv
        // when
        int count = reservationRepository.countByTime(reservationTime);

        // then
        assertThat(count).isEqualTo(0);
    }

    @Test
    @DisplayName("날짜와 themeId로 예약 목록을 조회한다.")
    void findAllByDateAndThemeId() {
        // given
        reservationRepository.save(MIA_RESERVATION(reservationTime, wootecoTheme, mia, BOOKING));
        reservationRepository.save(MIA_RESERVATION(reservationTime, wootecoTheme, mia, BOOKING));

        // when
        List<Long> reservationsByDateAndThemeId = reservationRepository.findAllTimeIdsByDateAndTheme(
                MIA_RESERVATION_DATE, wootecoTheme);

        // then
        assertThat(reservationsByDateAndThemeId).hasSize(2);
    }

    @Test
    @DisplayName("사용자의 예약 목록을 조회한다.")
    void findAllByMemberAndStatusWithDetails() {
        //given
        reservationRepository.save(MIA_RESERVATION(reservationTime, wootecoTheme, mia, BOOKING));
        reservationRepository.save(MIA_RESERVATION(reservationTime, wootecoTheme, mia, BOOKING));
        reservationRepository.save(TOMMY_RESERVATION(reservationTime, wootecoTheme, tommy, BOOKING));

        //when
        List<Reservation> reservations = reservationRepository.findAllByMemberAndStatusWithDetails(mia, BOOKING);

        //then
        assertThat(reservations).hasSize(2);
    }

    @Test
    @DisplayName("사용자의 대기 예약 목록을 이전 대기 예약 개수와 함께 조회한다.")
    void findWaitingReservationsByMemberWithDetails() {
        // given
        reservationRepository.save(new Reservation(tommy, MIA_RESERVATION_DATE, reservationTime, wootecoTheme, BOOKING));
        reservationRepository.save(new Reservation(tommy, MIA_RESERVATION_DATE, reservationTime, wootecoTheme, WAITING));
        reservationRepository.save(new Reservation(mia, MIA_RESERVATION_DATE, reservationTime, wootecoTheme, WAITING));

        reservationRepository.save(new Reservation(tommy, TOMMY_RESERVATION_DATE, reservationTime, wootecoTheme, BOOKING));
        reservationRepository.save(new Reservation(mia, TOMMY_RESERVATION_DATE, reservationTime, wootecoTheme, WAITING));

        // when
        List<WaitingReservation> waitingReservations =
                reservationRepository.findWaitingReservationsByMemberWithDetails(mia);

        // then
        assertThat(waitingReservations).hasSize(2)
                .extracting(WaitingReservation::getPreviousCount)
                .contains(0L, 1L);
    }

    @Test
    @DisplayName("동일한 날짜, 시간, 테마의 첫 번째 예약을 조회한다.")
    void findFirstByDateAndTimeAndTheme() {
        // given
        Reservation firstReservation = reservationRepository.save(
                new Reservation(tommy, MIA_RESERVATION_DATE, reservationTime, wootecoTheme, BOOKING));
        Reservation secondReservation = reservationRepository.save(
                new Reservation(mia, MIA_RESERVATION_DATE, reservationTime, wootecoTheme, WAITING));

        // when
        Optional<Reservation> foundReservation = reservationRepository.findFirstByDateAndTimeAndTheme(
                MIA_RESERVATION_DATE, reservationTime, wootecoTheme);

        // then
        Long expectedReservationId = firstReservation.getId();
        Long actualReservationId = foundReservation.get().getId();
        assertThat(actualReservationId).isEqualTo(expectedReservationId);
    }

    @Test
    @DisplayName("동일 시간대의 예약이 존재하는지 조회한다.")
    void existByDateAndTimeIdAndThemeId() {
        // given
        reservationRepository.save(MIA_RESERVATION(reservationTime, wootecoTheme, mia, BOOKING));

        // when
        boolean existByDateAndTimeIdAndThemeId = reservationRepository.existsByDateAndTimeAndTheme(
                MIA_RESERVATION_DATE, reservationTime, wootecoTheme);

        // then
        assertThat(existByDateAndTimeIdAndThemeId).isTrue();
    }
}
