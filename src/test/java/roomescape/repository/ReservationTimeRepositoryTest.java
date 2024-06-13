package roomescape.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static roomescape.TestFixture.RESERVATION_TIME_10AM;
import static roomescape.TestFixture.TIME;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import roomescape.domain.ReservationTime;
import roomescape.exception.NotFoundException;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ReservationTimeRepositoryTest {

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

    @DisplayName("모든 예약 시간을 보여준다")
    @Test
    void findAll() {
        assertThat(reservationTimeRepository.findAll()).isEmpty();
    }

    @DisplayName("예약 시간을 저장한다.")
    @Test
    void save() {
        // given & when
        reservationTimeRepository.save(RESERVATION_TIME_10AM);
        // then
        assertThat(reservationTimeRepository.findAll()).hasSize(1);
    }

    @DisplayName("해당 id의 예약 시간을 보여준다.")
    @Test
    void findById() {
        // given & when
        ReservationTime savedReservationTime = reservationTimeRepository.save(RESERVATION_TIME_10AM);
        ReservationTime reservationTime = reservationTimeRepository.findById(savedReservationTime.getId())
                .orElseThrow(() -> new NotFoundException("예약시간을 찾을 수 없습니다."));

        // then
        assertThat(reservationTime.getStartAt()).isEqualTo(TIME);
    }

    @DisplayName("해당 id의 예약 시간을 삭제한다.")
    @Test
    void deleteById() {
        // given
        ReservationTime reservationTime = reservationTimeRepository.save(RESERVATION_TIME_10AM);
        // when
        reservationTimeRepository.deleteById(reservationTime.getId());
        // then
        assertThat(reservationTimeRepository.findAll()).isEmpty();
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
