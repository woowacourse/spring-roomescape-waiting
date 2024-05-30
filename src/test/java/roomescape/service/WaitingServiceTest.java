package roomescape.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import roomescape.domain.Member;
import roomescape.domain.MemberRepository;
import roomescape.exception.reservation.DuplicatedReservationException;
import roomescape.helper.fixture.DateFixture;
import roomescape.service.dto.request.WaitingRequest;
import roomescape.service.dto.response.WaitingResponse;

class WaitingServiceTest extends ServiceTest {

    @Autowired
    private WaitingService waitingService;

    @Autowired
    private MemberRepository memberRepository;

    @Test
    void 모든_예약_대기를_조회한다() {
        List<WaitingResponse> responses = waitingService.findAllWaitings();

        assertThat(responses.size()).isEqualTo(1);
    }

    @Test
    void 예약_대기를_저장한다() {
        WaitingRequest request = new WaitingRequest(LocalDate.now().plusYears(1), 1L, 1L);
        Member member = memberRepository.findById(1L).get();

        WaitingResponse response = waitingService.save(request, member);
        Integer count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM waiting", Integer.class);

        assertThat(count).isEqualTo(2);
    }

    @Test
    void 같은_사람이_중복된_예약_대기는_할_수_없다() {
        WaitingRequest request = new WaitingRequest(DateFixture.dayAfterTomorrow(), 1L, 1L);
        Member user = memberRepository.findById(2L).get();
        assertThatThrownBy(() -> waitingService.save(request, user))
                .isInstanceOf(DuplicatedReservationException.class);
    }

    @Test
    void 다른_사람은_이미_대기가_있는_예약에_대기를_걸_수_있다() {
        WaitingRequest request = new WaitingRequest(DateFixture.dayAfterTomorrow(), 1L, 1L);
        Member admin = memberRepository.findById(1L).get();
        assertThatThrownBy(() -> waitingService.save(request, admin))
                .isInstanceOf(DuplicatedReservationException.class);
    }

    @Test
    void 예약_대기를_삭제할_수_있다() {
        waitingService.delete(1L);
        Integer count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM waiting", Integer.class);
        assertThat(count).isEqualTo(0);
    }
}