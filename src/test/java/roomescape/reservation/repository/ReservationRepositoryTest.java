package roomescape.reservation.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static roomescape.util.Fixture.HORROR_THEME;
import static roomescape.util.Fixture.JOJO;
import static roomescape.util.Fixture.KAKI;
import static roomescape.util.Fixture.RESERVATION_HOUR_10;
import static roomescape.util.Fixture.TODAY;
import static roomescape.util.Fixture.TOMORROW;

import java.time.LocalDate;
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
import roomescape.reservation.dto.ReservationSearchConditionRequest;

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
        ReservationTime hour10 = reservationTimeRepository.save(RESERVATION_HOUR_10);

        Theme horrorTheme = themeRepository.save(HORROR_THEME);

        Member kaki = memberRepository.save(KAKI);

        reservationRepository.save(new Reservation(kaki, TODAY, horrorTheme, hour10, ReservationStatus.SUCCESS));

        List<Reservation> reservations = reservationRepository.findAll();

        assertThat(reservations.size()).isEqualTo(1);
    }

    @DisplayName("회원 id로 예약 목록을 조회한다.")
    @Test
    void findAllByMemberId() {
        ReservationTime hour10 = reservationTimeRepository.save(RESERVATION_HOUR_10);

        Theme horrorTheme = themeRepository.save(HORROR_THEME);

        Member kaki = memberRepository.save(KAKI);
        Member jojo = memberRepository.save(JOJO);

        reservationRepository.save(new Reservation(kaki, TODAY, horrorTheme, hour10, ReservationStatus.SUCCESS));
        reservationRepository.save(new Reservation(jojo, TODAY, horrorTheme, hour10, ReservationStatus.SUCCESS));

        List<Reservation> reservations = reservationRepository.findAllByMemberIdFromDateOrderByDateAscTimeStartAtAscCreatedAtAsc(kaki.getId(), TODAY);

        assertThat(reservations.size()).isEqualTo(1);
    }

    @DisplayName("id 값을 받아 Reservation 반환")
    @Test
    void findByIdTest() {
        ReservationTime hour10 = reservationTimeRepository.save(RESERVATION_HOUR_10);

        Theme horrorTheme = themeRepository.save(HORROR_THEME);

        Member kaki = memberRepository.save(KAKI);

        Reservation savedReservation = reservationRepository.save(
                new Reservation(kaki, TODAY, horrorTheme, hour10, ReservationStatus.SUCCESS)
        );
        Reservation findReservation = reservationRepository.findById(savedReservation.getId()).get();

        assertThat(findReservation.getMember().getEmail()).isEqualTo(savedReservation.getMember().getEmail());
    }

    @DisplayName("예약 대기 순서를 반환한다.")
    @Test
    void countWaitingRankBy() {
        ReservationTime hour10 = reservationTimeRepository.save(RESERVATION_HOUR_10);

        Theme horrorTheme = themeRepository.save(HORROR_THEME);

        Member kaki = memberRepository.save(KAKI);
        Member jojo = memberRepository.save(JOJO);

        reservationRepository.save(new Reservation(kaki, TODAY, horrorTheme, hour10, ReservationStatus.WAIT));

        Reservation jojoReservation = reservationRepository.save(
                new Reservation(jojo, TODAY, horrorTheme, hour10, ReservationStatus.WAIT)
        );

        int jojoRank = reservationRepository.countWaitingRankBy(TODAY, horrorTheme.getId(), hour10.getId(), jojoReservation.getCreatedAt());

        assertThat(jojoRank).isEqualTo(2);
    }

    @DisplayName("예약 대기 상태인 첫 번째 예약을 반환한다.")
    @Test
    void findFirstWaitingReservationBy() {
        ReservationTime hour10 = reservationTimeRepository.save(RESERVATION_HOUR_10);

        Theme horrorTheme = themeRepository.save(HORROR_THEME);

        Member kaki = memberRepository.save(KAKI);
        Member jojo = memberRepository.save(JOJO);

        Reservation kakiReservation = reservationRepository.save(
                new Reservation(kaki, TODAY, horrorTheme, hour10, ReservationStatus.WAIT)
        );
        reservationRepository.save(new Reservation(jojo, TODAY, horrorTheme, hour10, ReservationStatus.WAIT));

        Reservation firstWaitingReservation = reservationRepository.findFirstWaitingReservationBy(TODAY, hour10.getId(), horrorTheme.getId()).get();

        assertThat(firstWaitingReservation.getId()).isEqualTo(kakiReservation.getId());
    }

    @DisplayName("날짜와 테마 아이디로 예약 시간 아이디들을 조회한다.")
    @Test
    void findTimeIdsByDateAndThemeIdTest() {
        ReservationTime hour10 = reservationTimeRepository.save(RESERVATION_HOUR_10);

        Theme horrorTheme = themeRepository.save(HORROR_THEME);

        Member kaki = memberRepository.save(KAKI);

        Reservation savedReservation = reservationRepository.save(
                new Reservation(kaki, TODAY, horrorTheme, hour10, ReservationStatus.SUCCESS)
        );

        List<Long> timeIds = reservationRepository.findTimeIdsByDateAndThemeId(savedReservation.getDate(), horrorTheme.getId());

        assertThat(timeIds).containsExactly(hour10.getId());
    }

    @DisplayName("회원 아이디, 날짜, 시간 조간에 해당하는 예약의 상태들을 조회한다.")
    @Test
    void findStatusesByMemberIdAndDateAndReservationTimeStartAt() {
        ReservationTime hour10 = reservationTimeRepository.save(RESERVATION_HOUR_10);

        Theme horrorTheme = themeRepository.save(HORROR_THEME);

        Member kaki = memberRepository.save(KAKI);

        reservationRepository.save(new Reservation(kaki, TODAY, horrorTheme, hour10, ReservationStatus.SUCCESS));
        reservationRepository.save(new Reservation(kaki, TODAY, horrorTheme, hour10, ReservationStatus.WAIT));

        List<ReservationStatus> reservationStatuses = reservationRepository.findStatusesByMemberIdAndDateAndTimeStartAt(
                kaki.getId(),
                TODAY,
                hour10.getStartAt()
        );

        assertThat(reservationStatuses).containsExactly(ReservationStatus.SUCCESS, ReservationStatus.WAIT);
    }

    @DisplayName("예약 상태 별로 동일한 예약이 있을 경우 true를 반환한다.")
    @Test
    void existsByDateAndReservationTimeStartAtAndStatus() {
        ReservationTime hour10 = reservationTimeRepository.save(RESERVATION_HOUR_10);

        Theme horrorTheme = themeRepository.save(HORROR_THEME);

        Member kaki = memberRepository.save(KAKI);

        Reservation savedReservation = reservationRepository.save(
                new Reservation(kaki, TODAY, horrorTheme, hour10, ReservationStatus.SUCCESS)
        );

        boolean success = reservationRepository.existsByDateAndTimeStartAtAndStatus(
                savedReservation.getDate(),
                savedReservation.getStartAt(),
                ReservationStatus.SUCCESS
        );

        boolean waiting = reservationRepository.existsByDateAndTimeStartAtAndStatus(
                savedReservation.getDate(),
                savedReservation.getStartAt(),
                ReservationStatus.WAIT
        );

        assertAll(
                () -> assertThat(success).isTrue(),
                () -> assertThat(waiting).isFalse()
        );
    }

    @DisplayName("회원 아이디, 테마 아이디와 기간이 일치하는 Reservation을 반환한다.")
    @Test
    void findAllByThemeIdAndMemberIdAndBetweenStartDateAndEndDate() {
        ReservationTime hour10 = reservationTimeRepository.save(RESERVATION_HOUR_10);

        Theme horrorTheme = themeRepository.save(HORROR_THEME);

        Member kaki = memberRepository.save(KAKI);

        LocalDate oneWeekLater = LocalDate.now().plusWeeks(1);
        reservationRepository.save(new Reservation(kaki, TOMORROW, horrorTheme, hour10, ReservationStatus.SUCCESS));
        reservationRepository.save(new Reservation(kaki, oneWeekLater, horrorTheme, hour10, ReservationStatus.SUCCESS));

        ReservationSearchConditionRequest request = new ReservationSearchConditionRequest(
                horrorTheme.getId(),
                kaki.getId(),
                TODAY,
                TOMORROW
        );

        List<Reservation> reservations = reservationRepository.findAllByThemeIdAndMemberIdAndDateBetweenOrderByDateAscTimeStartAtAscCreatedAtAsc(
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
        ReservationTime hour10 = reservationTimeRepository.save(RESERVATION_HOUR_10);

        Theme horrorTheme = themeRepository.save(HORROR_THEME);

        Member kaki = memberRepository.save(KAKI);

        Reservation savedReservation = reservationRepository.save(
                new Reservation(kaki, TODAY, horrorTheme, hour10, ReservationStatus.SUCCESS)
        );
        reservationRepository.deleteById(savedReservation.getId());

        List<Reservation> reservations = reservationRepository.findAll();

        assertThat(reservations.size()).isEqualTo(0);
    }
}
