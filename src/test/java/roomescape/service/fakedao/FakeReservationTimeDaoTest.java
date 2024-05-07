package roomescape.service.fakedao;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import roomescape.model.ReservationTime;

import java.time.LocalTime;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

class FakeReservationTimeDaoTest {

    private static final int INITIAL_TIME_COUNT = 3;
    private static final List<ReservationTime> INITIAL_TIMES = List.of(
            new ReservationTime(1, LocalTime.of(1, 0)),
            new ReservationTime(2, LocalTime.of(2, 0)),
            new ReservationTime(3, LocalTime.of(3, 0))
    );

    private FakeReservationTimeDao fakeReservationTimeDao;

    @BeforeEach
    void setUp() {
        fakeReservationTimeDao = new FakeReservationTimeDao(INITIAL_TIMES);
    }

    @DisplayName("예약 시간을 저장한 후 id를 반환한다.")
    @Test
    void should_save_reservation_time() {
        ReservationTime time = new ReservationTime(4, LocalTime.of(4, 0));
        long id = fakeReservationTimeDao.save(time);
        assertThat(id).isEqualTo(INITIAL_TIME_COUNT + 1);
        assertThat(fakeReservationTimeDao.findAll()).hasSize(INITIAL_TIME_COUNT + 1);
    }

    @DisplayName("전체 예약 시간을 조회한다.")
    @Test
    void should_find_all_reservation_times() {
        List<ReservationTime> allTimes = fakeReservationTimeDao.findAll();
        assertThat(allTimes).hasSize(INITIAL_TIME_COUNT);
        assertThat(allTimes).isEqualTo(INITIAL_TIMES);
    }

    @DisplayName("특정 id를 가진 예약 시간을 조회한다.")
    @Test
    void should_find_reservation_time_by_id() {
        Optional<ReservationTime> time = fakeReservationTimeDao.findById(1);
        assertThat(time).hasValue(new ReservationTime(1, LocalTime.of(1, 0)));
    }

    @DisplayName("유효하지 않은 id를 가진 예약 시간을 조회하려는 경우 빈 Optional 을 반환한다.")
    @Test
    void should_return_empty_optional_when_not_exist_id() {
        Optional<ReservationTime> time = fakeReservationTimeDao.findById(999);
        assertThat(time).isEmpty();
    }

    @DisplayName("예약 시간을 삭제한다.")
    @Test
    void should_delete_reservation_time() {
        fakeReservationTimeDao.deleteById(1);
        assertThat(fakeReservationTimeDao.findById(1)).isEmpty();
        assertThat(fakeReservationTimeDao.findAll()).hasSize(INITIAL_TIME_COUNT - 1);
    }

    @DisplayName("유효한 id를 가진 예약 시간을 삭제하려는 경우 예외가 발생하지 않는다.")
    @Test
    void should_not_throw_exception_when_exist_id() {
        assertThatCode(() -> fakeReservationTimeDao.deleteById(1))
                .doesNotThrowAnyException();
    }

    @DisplayName("유효하지 않은 id를 가진 예약 시간을 삭제하려는 경우 예외가 발생한다.")
    @Test
    void should_throw_exception_when_not_exist_id() {
        assertThatThrownBy(() -> fakeReservationTimeDao.deleteById(999))
                .isInstanceOf(NoSuchElementException.class)
                .hasMessage("존재하지 않는 예약 시간입니다.");
    }

    @DisplayName("해당 id를 가진 예약 시간이 존재하면 참을 반환한다.")
    @Test
    void should_return_true_when_exist_id() {
        Boolean isExist = fakeReservationTimeDao.isExistById(1);
        assertThat(isExist).isTrue();
    }

    @DisplayName("해당 id를 가진 예약 시간이 존재하지 않으면 거짓을 반환한다.")
    @Test
    void should_return_false_when_not_exist_id() {
        Boolean isExist = fakeReservationTimeDao.isExistById(999);
        assertThat(isExist).isFalse();
    }

    @DisplayName("해당 startAt을 가진 예약 시간이 존재하면 참을 반환한다.")
    @Test
    void should_return_true_when_exist_startAt() {
        Boolean isExist = fakeReservationTimeDao.isExistByStartAt(LocalTime.of(1, 0));
        assertThat(isExist).isTrue();
    }

    @DisplayName("해당 startAt을 가진 예약 시간이 존재하지 않으면 거짓을 반환한다.")
    @Test
    void should_return_false_when_not_exist_startAt() {
        Boolean isExist = fakeReservationTimeDao.isExistByStartAt(LocalTime.of(9, 0));
        assertThat(isExist).isFalse();
    }
}