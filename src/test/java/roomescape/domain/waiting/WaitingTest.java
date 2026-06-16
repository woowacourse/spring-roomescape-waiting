package roomescape.domain.waiting;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import roomescape.domain.reservation.Reservation;
import roomescape.domain.reservationtime.ReservationTime;
import roomescape.domain.theme.Theme;
import roomescape.exception.ErrorCode;
import roomescape.exception.RoomescapeException;

class WaitingTest {

    private ReservationTime time;
    private Theme theme;
    private Waiting waiting1;
    private Waiting waiting2;
    private Waiting waiting3;

    @BeforeEach
    void setUp() {
        time = ReservationTime.of(1L, LocalTime.of(10, 0), LocalTime.of(11, 0));
        theme = Theme.of(1L, "테마1", "설명", "https://example.com/image.jpg", 50_000L);
        waiting1 = Waiting.of(1L, "유저1", LocalDate.of(2099, 12, 31), time, theme);
        waiting2 = Waiting.of(2L, "유저2", LocalDate.of(2099, 12, 31), time, theme);
        waiting3 = Waiting.of(3L, "유저3", LocalDate.of(2099, 12, 31), time, theme);
    }

    @Nested
    @DisplayName("first() - 첫 번째 대기자 반환")
    class First {

        @Test
        void 대기가_있으면_첫_번째를_반환한다() {
            Waitings waitings = Waitings.of(List.of(waiting1, waiting2, waiting3));

            Optional<Waiting> first = waitings.first();

            assertThat(first).isPresent();
            assertThat(first.get().getName()).isEqualTo("유저1");
        }

        @Test
        void 대기가_없으면_빈_Optional을_반환한다() {
            Waitings waitings = Waitings.of(List.of());

            assertThat(waitings.first()).isEmpty();
        }
    }

    @Nested
    @DisplayName("positionOf() - 대기 순번 반환")
    class PositionOf {

        @Test
        void 첫_번째_대기자의_순번은_1이다() {
            Waitings waitings = Waitings.of(List.of(waiting1, waiting2, waiting3));

            assertThat(waitings.positionOf("유저1")).isEqualTo(1);
        }

        @Test
        void 세_번째_대기자의_순번은_3이다() {
            Waitings waitings = Waitings.of(List.of(waiting1, waiting2, waiting3));

            assertThat(waitings.positionOf("유저3")).isEqualTo(3);
        }

        @Test
        void 목록에_없는_이름이면_예외() {
            Waitings waitings = Waitings.of(List.of(waiting1, waiting2));

            assertThatThrownBy(() -> waitings.positionOf("없는유저"))
                    .isInstanceOf(RoomescapeException.class)
                    .extracting("errorCode").isEqualTo(ErrorCode.WAITING_ID_NOT_FOUND);
        }
    }

    @Nested
    @DisplayName("validateCanEnqueue() - 대기 등록 가능 여부 검증")
    class ValidateCanEnqueue {

        private final Reservation reservation = Reservation.of(1L, "예약자", LocalDate.of(2099, 12, 31), time, theme);

        @Test
        void 중복도_아니고_예약자도_아니면_정상_처리() {
            Waitings waitings = Waitings.of(List.of(waiting1));

            assertThatCode(() -> waitings.validateCanEnqueue("유저2", reservation))
                    .doesNotThrowAnyException();
        }

        @Test
        void 이미_대기중인_이름이면_DUPLICATE_WAITING_NAME_예외() {
            Waitings waitings = Waitings.of(List.of(waiting1));

            assertThatThrownBy(() -> waitings.validateCanEnqueue("유저1", reservation))
                    .isInstanceOf(RoomescapeException.class)
                    .extracting("errorCode").isEqualTo(ErrorCode.DUPLICATE_WAITING_NAME);
        }

        @Test
        void 예약자_본인이면_WAITING_NOT_AVAILABLE_예외() {
            Waitings waitings = Waitings.of(List.of());

            assertThatThrownBy(() -> waitings.validateCanEnqueue("예약자", reservation))
                    .isInstanceOf(RoomescapeException.class)
                    .extracting("errorCode").isEqualTo(ErrorCode.WAITING_NOT_AVAILABLE);
        }
    }
}