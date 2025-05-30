package roomescape.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import roomescape.domain.Member;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationTime;
import roomescape.domain.Role;
import roomescape.domain.Theme;
import roomescape.domain.Waiting;

@DataJpaTest
class WaitingRepositoryTest {

    @Autowired
    ReservationRepository reservationRepository;

    @Autowired
    ReservationTimeRepository reservationTimeRepository;

    @Autowired
    MemberRepository memberRepository;

    @Autowired
    WaitingRepository waitingRepository;

    @Autowired
    ThemeRepository themeRepository;

    @AfterEach
    void tearDown() {
        reservationRepository.deleteAllInBatch();
        waitingRepository.deleteAllInBatch();
        reservationTimeRepository.deleteAllInBatch();
        memberRepository.deleteAllInBatch();
        themeRepository.deleteAllInBatch();
    }

    @DisplayName("회원 id로 대기 목록을 조회할 수 있다.")
    @Test
    void findAllMemberById() {
        //given
        Reservation saveReservation = createSaveReservation();
        Waiting saveWaiting = createSaveWaiting(saveReservation);

        //when
        List<Waiting> actual = waitingRepository.findAllByMemberId(saveWaiting.getMember().getId());

        //then
        assertThat(actual).hasSize(1);
    }

    @DisplayName("날짜, 테마, 예약시간, 회원으로 대기가 존재하지 않는다면 false를 반환한다.")
    @Test
    void nonExistsByDateAndThemeIdAndReservationTimeIdAndMemberId() {
        //given
        LocalDate date = LocalDate.now();
        Long themeId = 1L;
        Long reservationTimeId = 1L;
        Long memberId = 1L;

        Reservation saveReservation = createSaveReservation();
        createSaveWaiting(saveReservation);

        //when
        boolean actual = waitingRepository.existsByDateAndThemeIdAndReservationTimeIdAndMemberId(
                date,
                themeId,
                reservationTimeId,
                memberId
        );

        //then
        assertThat(actual).isFalse();
    }

    @DisplayName("날짜, 테마, 예약시간, 회원으로 대기가 존재한다면 true를 반환한다.")
    @Test
    void existsByDateAndThemeIdAndReservationTimeIdAndMemberId() {
        //given
        Reservation saveReservation = createSaveReservation();
        Waiting saveWaiting = createSaveWaiting(saveReservation);

        //when
        boolean actual = waitingRepository.existsByDateAndThemeIdAndReservationTimeIdAndMemberId(
                saveWaiting.getDate(),
                saveWaiting.getTheme().getId(),
                saveWaiting.getReservationTime().getId(),
                saveWaiting.getMember().getId()
        );

        //then
        assertThat(actual).isTrue();
    }

    private Waiting createSaveWaiting(final Reservation savedReservation) {
        Waiting waiting = Waiting.of(
                savedReservation.getDate(),
                savedReservation.getTheme(),
                savedReservation.getReservationTime(),
                savedReservation.getMember(),
                LocalDateTime.of(2023, 1, 1, 1, 1, 1)
        );
        return waitingRepository.save(waiting);
    }

    private Reservation createSaveReservation() {
        ReservationTime reservationTime = new ReservationTime(LocalTime.of(12, 30));
        reservationTimeRepository.save(reservationTime);

        Theme theme = new Theme("테마", "공포", "image");
        themeRepository.save(theme);

        Member member = new Member("도기", "email@gamil.com", "password", Role.ADMIN);
        memberRepository.save(member);

        Reservation reservation = new Reservation(
                LocalDate.now().plusDays(1),
                reservationTime,
                theme,
                member,
                LocalDate.now()
        );

        return reservationRepository.save(reservation);
    }

}
