package roomescape.reservation.domain;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import roomescape.common.RepositoryTest;
import roomescape.member.domain.Member;
import roomescape.member.domain.MemberRepository;
import roomescape.member.persistence.MemberDao;
import roomescape.reservation.persistence.ReservationDao;
import roomescape.reservation.persistence.ReservationTimeDao;
import roomescape.reservation.persistence.ThemeDao;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.SoftAssertions.assertSoftly;
import static roomescape.TestFixture.*;

class ReservationRepositoryTest extends RepositoryTest {
    private ReservationRepository reservationRepository;

    private ReservationTime reservationTime;
    private Theme wootecoTheme;
    private Theme horrorTheme;
    private Member mia;
    private Member tommy;

    @BeforeEach
    void setUp() {
        this.reservationRepository = new ReservationDao(dataSource);

        ReservationTimeRepository reservationTimeRepository = new ReservationTimeDao(dataSource);
        ThemeRepository themeRepository = new ThemeDao(dataSource);
        MemberRepository memberRepository = new MemberDao(dataSource);
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
        Reservation reservation = MIA_RESERVATION(reservationTime, wootecoTheme, mia);

        // when
        Reservation savedReservation = reservationRepository.save(reservation);

        // then
        assertThat(savedReservation.getId()).isNotNull();
    }

    @Test
    @DisplayName("동일 시간대의 예약이 존재하는지 조회한다.")
    void existByDateAndTimeIdAndThemeId() {
        // given
        reservationRepository.save(MIA_RESERVATION(reservationTime, wootecoTheme, mia));

        // when
        boolean existByDateAndTimeIdAndThemeId = reservationRepository.existByDateAndTimeIdAndThemeId(
                MIA_RESERVATION_DATE, reservationTime.getId(), wootecoTheme.getId());

        // then
        assertThat(existByDateAndTimeIdAndThemeId).isTrue();
    }

    @Test
    @DisplayName("모든 예약 목록을 조회한다.")
    void findAll() {
        // given
        reservationRepository.save(MIA_RESERVATION(reservationTime, wootecoTheme, mia));

        // when
        List<Reservation> reservations = reservationRepository.findAll();

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
        reservationRepository.save(new Reservation(mia, MIA_RESERVATION_DATE, reservationTime, wootecoTheme));
        reservationRepository.save(new Reservation(mia, MIA_RESERVATION_DATE.plusDays(2), reservationTime, wootecoTheme));
        reservationRepository.save(new Reservation(tommy, MIA_RESERVATION_DATE, reservationTime, wootecoTheme));
        reservationRepository.save(new Reservation(mia, MIA_RESERVATION_DATE, reservationTime, horrorTheme));

        // when
        List<Reservation> reservations = reservationRepository.findAllByMemberIdAndThemeIdAndDateBetween(
                1L, 1L, MIA_RESERVATION_DATE, MIA_RESERVATION_DATE.plusDays(1));

        // then
        assertSoftly(softly -> {
            softly.assertThat(reservations).hasSize(1);
            softly.assertThat(reservations.get(0).getMemberId()).isEqualTo(1);
            softly.assertThat(reservations.get(0).getThemeId()).isEqualTo(1);
            softly.assertThat(reservations.get(0).getDate()).isEqualTo(MIA_RESERVATION_DATE);
        });
    }

    @Test
    @DisplayName("Id로 예약을 삭제한다.")
    void deleteById() {
        // given
        Long id = reservationRepository.save(MIA_RESERVATION(reservationTime, wootecoTheme, mia)).getId();

        // when
        reservationRepository.deleteById(id);

        // then
        List<Reservation> reservations = reservationRepository.findAll();
        assertThat(reservations).hasSize(0);
    }

    @Test
    @DisplayName("timeId에 해당하는 예약 건수를 조회한다.")
    void countByTimeId() {
        // given
        long timeId = 2L;

        // when
        int count = reservationRepository.countByTimeId(timeId);

        // then
        assertThat(count).isEqualTo(0);
    }

    @Test
    @DisplayName("날짜와 themeId로 예약 목록을 조회한다.")
    void findAllByDateAndThemeId() {
        // given
        reservationRepository.save(MIA_RESERVATION(reservationTime, wootecoTheme, mia));
        reservationRepository.save(MIA_RESERVATION(reservationTime, wootecoTheme, mia));

        // when
        List<Long> reservationsByDateAndThemeId = reservationRepository.findAllTimeIdsByDateAndThemeId(
                MIA_RESERVATION_DATE, wootecoTheme.getId());

        // then
        assertThat(reservationsByDateAndThemeId).hasSize(2);
    }
}
