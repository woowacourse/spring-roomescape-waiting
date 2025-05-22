package roomescape.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static roomescape.TestFixture.DEFAULT_DATE;
import static roomescape.TestFixture.createDefaultMember;
import static roomescape.TestFixture.createDefaultReservationTime;
import static roomescape.TestFixture.createDefaultTheme;
import static roomescape.TestFixture.createMemberByName;
import static roomescape.TestFixture.createNewReservation;
import static roomescape.TestFixture.createWaiting;

import jakarta.transaction.Transactional;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import roomescape.DatabaseCleaner;
import roomescape.controller.dto.request.ReservationSearchCondition;
import roomescape.domain.Member;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationTime;
import roomescape.domain.Theme;
import roomescape.domain.repository.MemberRepository;
import roomescape.domain.repository.ReservationRepository;
import roomescape.domain.repository.ReservationTimeRepository;
import roomescape.domain.repository.ThemeRepository;
import roomescape.domain.repository.WaitingRepository;
import roomescape.service.dto.result.ReservationResult;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class ReservationServiceTest {

    @Autowired
    private ReservationService reservationService;

    @Autowired
    private ReservationTimeRepository reservationTimeRepository;

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private WaitingRepository waitingRepository;

    @Autowired
    private ThemeRepository themeRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private DatabaseCleaner databaseCleaner;

    @BeforeEach
    void clean() {
        databaseCleaner.clean();
    }

    @DisplayName("조건이 없다면 전체 예약을 조회한다.")
    @Test
    void getAllReservations() {
        //given
        Theme theme = themeRepository.save(createDefaultTheme());
        ReservationTime reservationTime = reservationTimeRepository.save(createDefaultReservationTime());
        Member member = memberRepository.save(createDefaultMember());
        Reservation reservation1 = reservationRepository.save(
                createNewReservation(member, DEFAULT_DATE, reservationTime, theme));
        Reservation reservation2 = reservationRepository.save(
                createNewReservation(member, DEFAULT_DATE.plusDays(1), reservationTime, theme));

        //when
        ReservationSearchCondition condition = new ReservationSearchCondition(null, null, null, null);
        List<ReservationResult> reservationResults = reservationService.getReservationsInConditions(condition);

        //then
        assertAll(
                () -> assertThat(reservationResults).hasSize(2),
                () -> assertThat(reservationResults)
                        .isEqualTo(List.of(
                                ReservationResult.from(reservation1),
                                ReservationResult.from(reservation2)
                        ))
        );
    }

    @DisplayName("조건이 있다면 필터링하여 조회한다.")
    @Test
    void getReservationsInConditions() {
        //given
        Theme theme = themeRepository.save(createDefaultTheme());
        ReservationTime reservationTime = reservationTimeRepository.save(createDefaultReservationTime());
        Member member1 = memberRepository.save(createMemberByName("member1"));
        Member member2 = memberRepository.save(createMemberByName("member2"));
        Reservation reservation1 = reservationRepository.save(
                createNewReservation(member1, DEFAULT_DATE, reservationTime, theme));
        Reservation reservation2 = reservationRepository.save(
                createNewReservation(member2, DEFAULT_DATE, reservationTime, theme));

        //when
        ReservationSearchCondition condition = new ReservationSearchCondition(member1.getId(), null, null, null);
        List<ReservationResult> reservationResults = reservationService.getReservationsInConditions(condition);

        //then
        assertAll(
                () -> assertThat(reservationResults).hasSize(1),
                () -> assertThat(reservationResults)
                        .isEqualTo(List.of(ReservationResult.from(reservation1)))
        );
    }

    @DisplayName("예약자 ID로 예약을 삭제한다.")
    @Test
    void deleteById() {
        //given
        Theme theme = themeRepository.save(createDefaultTheme());
        ReservationTime reservationTime = reservationTimeRepository.save(createDefaultReservationTime());
        Member member = memberRepository.save(createDefaultMember());
        Reservation reservation = reservationRepository.save(
                createNewReservation(member, DEFAULT_DATE, reservationTime, theme));

        //when
        reservationService.deleteByIdAndReserveNextWaiting(reservation.getId());

        //then
        assertThat(reservationRepository.findById(reservation.getId())).isEmpty();
    }

    @DisplayName("예약 삭제 후 다음 대기자가 있다면 자동으로 예약한다")
    @Test
    void deleteByIdWithAutoReservation() {
        //given
        Theme theme = themeRepository.save(createDefaultTheme());
        ReservationTime reservationTime = reservationTimeRepository.save(createDefaultReservationTime());
        Member member1 = memberRepository.save(createMemberByName("member1"));
        Member member2 = memberRepository.save(createMemberByName("member2"));

        Reservation reservation = reservationRepository.save(
                createNewReservation(member1, DEFAULT_DATE, reservationTime, theme));
        waitingRepository.save(createWaiting(member2, DEFAULT_DATE, reservationTime, theme));

        //when
        reservationService.deleteByIdAndReserveNextWaiting(reservation.getId());

        //then
        assertAll(
                () -> assertThat(waitingRepository.findAll()).hasSize(0),
                () -> assertThat(reservationRepository.findByMemberId(member2.getId())).isNotNull()
        );
    }
}
