package roomescape.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static roomescape.TestFixture.DATE_AFTER_1DAY;
import static roomescape.TestFixture.MEMBER_BROWN;
import static roomescape.TestFixture.RESERVATION_TIME_10AM;
import static roomescape.TestFixture.ROOM_THEME1;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import roomescape.domain.Member;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationTime;
import roomescape.domain.RoomTheme;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ReservationRepositoryTest {

    @Autowired
    private ReservationRepository reservationRepository;
    @Autowired
    private ReservationTimeRepository reservationTimeRepository;
    @Autowired
    private RoomThemeRepository roomThemeRepository;
    @Autowired
    private MemberRepository memberRepository;

    @BeforeEach
    void setUp() {
        clearTable();
    }

    @DisplayName("존재하는 모든 예약을 보여준다.")
    @Test
    void findAll() {
        assertThat(reservationRepository.findAll()).isEmpty();
    }

    @DisplayName("예약을 저장한다.")
    @Test
    void save() {
        // given
        Member member = memberRepository.save(MEMBER_BROWN);
        ReservationTime reservationTime = reservationTimeRepository.save(RESERVATION_TIME_10AM);
        RoomTheme roomTheme = roomThemeRepository.save(ROOM_THEME1);
        // when
        reservationRepository.save(new Reservation(member, DATE_AFTER_1DAY, reservationTime, roomTheme));
        // then
        assertThat(reservationRepository.findAll()).hasSize(1);
    }

    @DisplayName("해당 id의 예약을 삭제한다.")
    @Test
    void deleteById() {
        // given
        Member member = memberRepository.save(MEMBER_BROWN);
        ReservationTime reservationTime = reservationTimeRepository.save(RESERVATION_TIME_10AM);
        RoomTheme roomTheme = roomThemeRepository.save(ROOM_THEME1);
        Reservation savedReservation = reservationRepository.save(
                new Reservation(member, DATE_AFTER_1DAY, reservationTime, roomTheme));
        // when
        reservationRepository.deleteById(savedReservation.getId());
        // then
        assertThat(reservationRepository.findAll()).isEmpty();
    }

    private void clearTable() {
        reservationRepository.findAll()
                .forEach(reservation -> reservationRepository.deleteById(reservation.getId()));
        reservationTimeRepository.findAll()
                .forEach(reservationTime -> reservationTimeRepository.deleteById(reservationTime.getId()));
        roomThemeRepository.findAll()
                .forEach(roomTheme -> roomThemeRepository.deleteById(roomTheme.getId()));
        memberRepository.findAll()
                .forEach(member -> memberRepository.deleteById(member.getId()));
    }
}
