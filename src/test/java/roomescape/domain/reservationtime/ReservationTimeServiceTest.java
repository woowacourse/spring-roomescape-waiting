package roomescape.domain.reservationtime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.BDDAssertions.tuple;
import static org.assertj.core.api.SoftAssertions.assertSoftly;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import roomescape.domain.reservationdate.ReservationDate;
import roomescape.domain.reservationslot.ReservationSlot;
import roomescape.domain.reservationtime.admin.dto.CreateTimeRequest;
import roomescape.domain.reservationtime.admin.dto.CreateTimeResponse;
import roomescape.domain.reservationtime.admin.dto.ReservationTimeResponse;
import roomescape.domain.theme.Theme;
import roomescape.support.exception.RoomescapeException;
import roomescape.support.fake.FakeReservationSlotRepository;
import roomescape.support.fake.FakeReservationTimeRepository;

class ReservationTimeServiceTest {

    private FakeReservationSlotRepository reservationRepository;
    private FakeReservationTimeRepository reservationTimeRepository;

    @BeforeEach
    void setUp() {
        reservationRepository = new FakeReservationSlotRepository();
        reservationTimeRepository = new FakeReservationTimeRepository();
    }

    @Test
    @DisplayName("예약 시간을 생성한다.")
    void createReservationTime() {
        // given
        ReservationTimeService reservationTimeService = createReservationTimeService();

        // when
        CreateTimeResponse response = reservationTimeService.createReservationTime(
            new CreateTimeRequest(LocalTime.of(10, 0))
        );
        ReservationTime savedReservationTime = reservationTimeRepository.findById(response.id()).orElseThrow();

        // then
        assertSoftly(softly -> {
            assertThat(response.id()).isEqualTo(savedReservationTime.getId());
            assertThat(response.startAt()).isEqualTo(LocalTime.of(10, 0));
            assertThat(savedReservationTime.getStartAt()).isEqualTo(LocalTime.of(10, 0));
        });
    }

    @Test
    @DisplayName("예약 시간 목록을 조회한다.")
    void getReservationTimeList() {
        // given
        reservationTimeRepository.save(ReservationTime.createWithoutId(LocalTime.of(10, 0)));
        reservationTimeRepository.save(ReservationTime.createWithoutId(LocalTime.of(11, 0)));
        ReservationTimeService reservationTimeService = createReservationTimeService();

        // when
        List<ReservationTimeResponse> responses = reservationTimeService.getAllReservationTime();

        // then
        assertSoftly(softly -> {
            assertThat(responses).hasSize(2);
            assertThat(responses)
                .extracting(ReservationTimeResponse::id, ReservationTimeResponse::startAt)
                .containsExactly(
                    tuple(1L, LocalTime.of(10, 0)),
                    tuple(2L, LocalTime.of(11, 0))
                );
        });
    }

    @Test
    @DisplayName("이미 예약이 존재하는 시간은 삭제할 수 없다.")
    void throwExceptionWhenDeletingTimeInUse() {
        // given
        ReservationTime reservationTime = reservationTimeRepository.save(
            ReservationTime.createWithoutId(LocalTime.of(10, 0))
        );
        reservationRepository.save(
            ReservationSlot.createWithoutId(ReservationDate.of(1L, LocalDate.of(2026, 5, 12)),
                reservationTime,
                Theme.of(1L, "공포", "무서운 테마", "theme-url")
            )
        );
        ReservationTimeService reservationTimeService = createReservationTimeService();

        // when & then
        assertThatThrownBy(() -> reservationTimeService.deleteReservationTime(reservationTime.getId()))
            .isInstanceOf(RoomescapeException.class)
            .hasMessage("이미 예약이 존재하는 시간대는 삭제할 수 없습니다.");
    }

    @Test
    @DisplayName("예약이 없는 시간은 삭제한다.")
    void deleteTimeWhenNoReservationExists() {
        // given
        ReservationTime reservationTime = reservationTimeRepository.save(
            ReservationTime.createWithoutId(LocalTime.of(10, 0))
        );
        ReservationTimeService reservationTimeService = createReservationTimeService();

        // when
        reservationTimeService.deleteReservationTime(reservationTime.getId());

        // then
        assertThat(reservationTimeRepository.findById(reservationTime.getId())).isEmpty();
    }

    private ReservationTimeService createReservationTimeService() {
        return new ReservationTimeService(
            reservationTimeRepository,
            reservationRepository
        );
    }
}
