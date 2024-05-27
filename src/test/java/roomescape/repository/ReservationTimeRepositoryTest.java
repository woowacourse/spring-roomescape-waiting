package roomescape.repository;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import roomescape.domain.ReservationTime;
import roomescape.domain.dto.ReservationTimeRequest;

import java.time.LocalTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class ReservationTimeRepositoryTest {
    @Autowired
    private ReservationTimeRepository reservationTimeRepository;

    private long getItemSize() {
        return reservationTimeRepository.findAll().size();
    }

    @DisplayName("Db에 등록된 시간 목록을 조회할 수 있다.")
    @Test
    void given_when_findAll_then_ReturnReservationTimes() {
        //when
        final List<ReservationTime> times = reservationTimeRepository.findAll();
        //then
        assertThat(times).hasSize(4);
    }

    @DisplayName("Db에 시간 정보를 저장한다.")
    @Test
    void given_reservationTimeRequest_when_create_then_returnCreatedTimeId() {
        //given
        ReservationTimeRequest reservationTimeRequest = new ReservationTimeRequest(LocalTime.parse("10:11"));
        ReservationTime expected = reservationTimeRequest.toEntity();
        //when
        ReservationTime savedReservationTime = reservationTimeRepository.save(expected);
        //then
        assertThat(savedReservationTime).isEqualTo(expected);
    }

    @DisplayName("시간 Id로 Db에서 시간 정보를 삭제한다.")
    @Test
    void given_when_delete_then_deletedFromDb() {
        //given
        long initialSize = getItemSize();
        //when
        reservationTimeRepository.deleteById(4L);
        long afterSize = getItemSize();
        //then
        assertThat(afterSize).isEqualTo(initialSize - 1);
    }

    @DisplayName("시간이 Db에 이미 등록되어 있는지 확인할 수 있다.")
    @Test
    void given_when_isExist_then_returnExistResult() {
        //when
        boolean actual = reservationTimeRepository.existsByStartAt(LocalTime.parse("10:00"));
        //then
        assertThat(actual).isTrue();
    }

    @DisplayName("Id를 통해 시간 정보를 반환할 수 있다.")
    @Test
    void given_when_findById_then_returnOptionalReservationTime() {
        final ReservationTime reservationTime = reservationTimeRepository.findById(1L).get();
        assertThat(reservationTime.getId()).isEqualTo(1);
    }
}
