package roomescape.unit.fake;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import roomescape.member.domain.Member;
import roomescape.reservation.domain.Theme;
import roomescape.reservation.domain.TimeSlot;
import roomescape.reservation.domain.Waiting;
import roomescape.reservation.dto.response.WaitingWithRankResponse;

class FakeWaitingRepositoryTest {

    private final FakeWaitingRepository waitingRepository = new FakeWaitingRepository();

    @Test
    void 대기를_저장한다() {
        // given
        Theme theme = Theme.builder()
                .name("theme1")
                .description("desc1")
                .thumbnail("thumb1").build();
        TimeSlot timeSlot = TimeSlot.builder()
                .startAt(LocalTime.of(9, 0)).build();
        Member member = Member.builder()
                .name("name1")
                .email("email1@domain.com")
                .password("password1")
                .build();
        Waiting waiting = Waiting.builder()
                .theme(theme)
                .timeSlot(timeSlot)
                .member(member)
                .date(LocalDate.of(2025, 1, 1)).build();

        // when
        Assertions.assertThatCode(() -> waitingRepository.save(waiting))
                .doesNotThrowAnyException();
    }

    @Test
    void 날짜_회원_시간_테마으로_대기가_존재하는지_조회한다() {
        // given
        Theme theme = Theme.builder()
                .name("theme1")
                .description("desc1")
                .thumbnail("thumb1").build();
        TimeSlot timeSlot = TimeSlot.builder()
                .startAt(LocalTime.of(9, 0)).build();
        Member member = Member.builder()
                .name("name1")
                .email("email1@domain.com")
                .password("password1")
                .build();
        LocalDate date = LocalDate.of(2025, 1, 1);
        Waiting waiting = Waiting.builder()
                .date(date)
                .member(member)
                .theme(theme)
                .timeSlot(timeSlot)
                .build();
        waitingRepository.save(waiting);

        // when
        boolean exist = waitingRepository.existsByDateAndMemberAndThemeAndTimeSlot(date, member, theme, timeSlot);

        //then
        assertThat(exist).isTrue();
    }

    @Test
    void 회원의_대기를_대기순서와_함께_조회한다() {
        // given
        Theme theme = Theme.builder()
                .name("theme1")
                .description("desc1")
                .thumbnail("thumb1").build();
        TimeSlot timeSlot = TimeSlot.builder()
                .startAt(LocalTime.of(9, 0)).build();
        Member member1 = Member.builder()
                .id(1L)
                .name("name1")
                .email("email1@domain.com")
                .password("password1")
                .build();
        Member member2 = Member.builder()
                .id(2L)
                .name("name2")
                .email("email2@domain.com")
                .password("password2")
                .build();
        LocalDate date = LocalDate.of(2025, 1, 1);
        waitingRepository.save(
                Waiting.builder()
                        .date(date)
                        .member(member2)
                        .theme(theme)
                        .timeSlot(timeSlot)
                        .build()
        );
        waitingRepository.save(
                Waiting.builder()
                        .date(date)
                        .member(member1)
                        .theme(theme)
                        .timeSlot(timeSlot)
                        .build()
        );
        // when
        List<WaitingWithRankResponse> waitingWithRank = waitingRepository.findByMemberIdWithRank(member1.getId());
        //then
        assertThat(waitingWithRank.get(0).getRank()).isEqualTo(2);
    }

    @Test
    void id로_대기를_조회한다() {
        // given
        Theme theme = Theme.builder()
                .name("theme1")
                .description("desc1")
                .thumbnail("thumb1").build();
        TimeSlot timeSlot = TimeSlot.builder()
                .startAt(LocalTime.of(9, 0)).build();
        Member member = Member.builder()
                .name("name1")
                .email("email1@domain.com")
                .password("password1")
                .build();
        Waiting waiting = Waiting.builder()
                .date(LocalDate.of(2025, 1, 1))
                .member(member)
                .theme(theme)
                .timeSlot(timeSlot)
                .build();
        Waiting savedWaiting = waitingRepository.save(waiting);

        // when
        Optional<Waiting> optionalWaiting = waitingRepository.findById(savedWaiting.getId());

        //then
        assertThat(optionalWaiting).isPresent();
    }

    @Test
    void 대기를_삭제한다() {
        // given
        Theme theme = Theme.builder()
                .name("theme1")
                .description("desc1")
                .thumbnail("thumb1").build();
        TimeSlot timeSlot = TimeSlot.builder()
                .startAt(LocalTime.of(9, 0)).build();
        Member member = Member.builder()
                .name("name1")
                .email("email1@domain.com")
                .password("password1")
                .build();
        Waiting waiting = Waiting.builder()
                .date(LocalDate.of(2025, 1, 1))
                .member(member)
                .theme(theme)
                .timeSlot(timeSlot)
                .build();
        Waiting savedWaiting = waitingRepository.save(waiting);

        // when
        waitingRepository.delete(savedWaiting);

        //then
        Optional<Waiting> optionalWaiting = waitingRepository.findById(savedWaiting.getId());
        assertThat(optionalWaiting).isEmpty();
    }
}