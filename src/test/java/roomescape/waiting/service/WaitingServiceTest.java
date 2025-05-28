package roomescape.waiting.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDateTime;
import java.util.List;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import roomescape.common.CleanUp;
import roomescape.fixture.MemberDbFixture;
import roomescape.fixture.ReservationDateTimeDbFixture;
import roomescape.fixture.ThemeDbFixture;
import roomescape.global.exception.InvalidArgumentException;
import roomescape.member.controller.response.MemberResponse;
import roomescape.member.domain.Member;
import roomescape.reservation.domain.ReservationDateTime;
import roomescape.theme.controller.response.ThemeResponse;
import roomescape.theme.domain.Theme;
import roomescape.time.controller.response.ReservationTimeResponse;
import roomescape.waiting.controller.request.WaitingRequest;
import roomescape.waiting.controller.response.WaitingResponse;
import roomescape.waiting.domain.Waiting;
import roomescape.waiting.repository.WaitingRepository;

@SpringBootTest(webEnvironment = WebEnvironment.NONE)
class WaitingServiceTest {

    @Autowired
    private MemberDbFixture memberDbFixture;
    @Autowired
    private ReservationDateTimeDbFixture reservationDateTimeDbFixture;
    @Autowired
    private ThemeDbFixture themeDbFixture;
    @Autowired
    private WaitingService waitingService;
    @Autowired
    private WaitingRepository waitingRepository;
    @Autowired
    private CleanUp cleanUp;

    @BeforeEach
    void setUp() {
        cleanUp.all();
    }

    @Nested
    class WaitContext {

        Theme theme;
        Member waiter;
        ReservationDateTime slot;
        WaitingRequest request;

        @BeforeEach
        void init() {
            theme = themeDbFixture.공포();
            waiter = memberDbFixture.유저1_생성();
            slot = reservationDateTimeDbFixture.내일_열시();

            request = new WaitingRequest(
                    slot.reservationDate().date(),
                    slot.reservationTime().getId(),
                    theme.getId()
            );
        }

        @Test
        void 예약을_대기한다() {
            WaitingResponse response = waitingService.wait(request, waiter.getId());

            SoftAssertions.assertSoftly(softly -> {
                softly.assertThat(response.id()).isNotNull();
                softly.assertThat(response.member()).isEqualTo(MemberResponse.from(waiter));
                softly.assertThat(response.theme()).isEqualTo(ThemeResponse.from(theme));
                softly.assertThat(response.date()).isEqualTo(slot.reservationDate().date());
                softly.assertThat(response.time())
                        .isEqualTo(ReservationTimeResponse.from(slot.reservationTime()));
            });
        }

        @Test
        void 같은_유저가_대기를_중복해서_할_수_없다() {
            waitingRepository.save(Waiting.wait(waiter, slot, theme, LocalDateTime.now()));

            assertThatThrownBy(() -> waitingService.wait(request, waiter.getId()))
                    .isInstanceOf(InvalidArgumentException.class)
                    .hasMessage("이미 대기 중인 예약입니다.");
        }
    }

    @Nested
    class QueryContext {

        Member waiter;
        ReservationDateTime slot;
        Theme theme;

        @BeforeEach
        void init() {
            waiter = memberDbFixture.유저1_생성();
            slot = reservationDateTimeDbFixture.내일_열시();
            theme = themeDbFixture.공포();

            waitingRepository.save(Waiting.wait(waiter, slot, theme, LocalDateTime.now()));
        }

        @Test
        void 대기중인_예약을_조회한다() {
            List<WaitingResponse> responses = waitingService.getWaitings();

            SoftAssertions.assertSoftly(softly -> {
                softly.assertThat(responses).hasSize(1);
                softly.assertThat(responses.getFirst().id()).isNotNull();
                softly.assertThat(responses.getFirst().member()).isEqualTo(MemberResponse.from(waiter));
                softly.assertThat(responses.getFirst().theme()).isEqualTo(ThemeResponse.from(theme));
                softly.assertThat(responses.getFirst().date()).isEqualTo(slot.reservationDate().date());
                softly.assertThat(responses.getFirst().time())
                        .isEqualTo(ReservationTimeResponse.from(slot.reservationTime()));
            });
        }
    }

    @Nested
    class CancelContext {

        Waiting savedWaiting;

        @BeforeEach
        void init() {
            Member waiter = memberDbFixture.유저1_생성();
            ReservationDateTime slot = reservationDateTimeDbFixture.내일_열시();
            Theme theme = themeDbFixture.공포();
            savedWaiting = waitingRepository.save(
                    Waiting.wait(waiter, slot, theme, LocalDateTime.now()));
        }

        @Test
        void 사용자가_예약_대기를_취소한다() {
            waitingService.cancel(savedWaiting.getId());

            SoftAssertions.assertSoftly(softly ->
                    softly.assertThat(waitingRepository.existsById(savedWaiting.getId())).isFalse()
            );
        }
    }
}
