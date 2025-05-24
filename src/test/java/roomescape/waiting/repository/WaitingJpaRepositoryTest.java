package roomescape.waiting.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static roomescape.common.Constant.예약날짜_내일;
import static roomescape.member.role.Role.ADMIN;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import roomescape.member.domain.Email;
import roomescape.member.domain.Member;
import roomescape.member.domain.Name;
import roomescape.member.domain.Password;
import roomescape.member.repository.MemberRepository;
import roomescape.theme.domain.Theme;
import roomescape.theme.repository.ThemeRepository;
import roomescape.time.domain.ReservationTime;
import roomescape.time.repository.ReservationTimeRepository;
import roomescape.waiting.domain.Waiting;
import roomescape.waiting.domain.WaitingWithRank;

@DataJpaTest
public class WaitingJpaRepositoryTest {

    @Autowired
    WaitingRepository waitingRepository;

    @Autowired
    MemberRepository memberRepository;

    @Autowired
    ReservationTimeRepository reservationTimeRepository;

    @Autowired
    ThemeRepository themeRepository;

    private Member member;
    private ReservationTime reservationTime;
    private Theme theme;

    @Test
    void 랭크를_가진_대기를_불러온다() {
        // given
        member = memberRepository.save(
                new Member(new Name("매트"), new Email("matt@kakao.com"), new Password("1234"), ADMIN));
        reservationTime = reservationTimeRepository.save(
                ReservationTime.create(LocalTime.of(10, 0))
        );
        theme = themeRepository.save(new Theme("공포", "ss", "ss"));
        Waiting waiting1 = Waiting.create(예약날짜_내일, reservationTime, theme, member);
        Waiting waiting2 = Waiting.create(예약날짜_내일, reservationTime, theme, member);
        waitingRepository.save(waiting1);
        waitingRepository.save(waiting2);

        //when
        List<WaitingWithRank> allWaitingWithRankByMemberId = waitingRepository.findAllWaitingWithRankByMemberId(
                member.getId());

        //then
        assertThat(allWaitingWithRankByMemberId.getFirst().getRank()).isEqualTo(0);
        assertThat(allWaitingWithRankByMemberId.get(1).getRank()).isEqualTo(1);
    }

    @Test
    void 대기를_저장한다() {
        // given
        member = memberRepository.save(
                new Member(new Name("매트"), new Email("matt@kakao.com"), new Password("1234"), ADMIN));
        reservationTime = reservationTimeRepository.save(
                ReservationTime.create(LocalTime.of(10, 0))
        );
        theme = themeRepository.save(new Theme("공포", "ss", "ss"));
        Waiting waiting1 = Waiting.create(예약날짜_내일, reservationTime, theme, member);

        // when
        Waiting save = waitingRepository.save(waiting1);

        // then
        assertThat(save.getDate()).isEqualTo(LocalDate.of(2025, 5, 25));
        assertThat(save.getStartAt()).isEqualTo(LocalTime.of(10, 0));
        assertThat(save.getTheme()).isEqualTo(theme);
        assertThat(save.getMember()).isEqualTo(member);
    }

    @Test
    void 모든_랭크를_가진_대기를_불러온다() {
        // given
        member = memberRepository.save(
                new Member(new Name("매트"), new Email("matt@kakao.com"), new Password("1234"), ADMIN));
        reservationTime = reservationTimeRepository.save(
                ReservationTime.create(LocalTime.of(10, 0))
        );
        theme = themeRepository.save(new Theme("공포", "ss", "ss"));
        Waiting waiting1 = Waiting.create(예약날짜_내일, reservationTime, theme, member);
        Waiting waiting2 = Waiting.create(예약날짜_내일, reservationTime, theme, member);
        waitingRepository.save(waiting1);
        waitingRepository.save(waiting2);

        //when
        List<WaitingWithRank> allWaitingWithRankByMemberId = waitingRepository.findAllWithRank();

        //then
        assertThat(allWaitingWithRankByMemberId.getFirst().getRank()).isEqualTo(0);
        assertThat(allWaitingWithRankByMemberId.get(1).getRank()).isEqualTo(1);
    }

    @Test
    void 모든_대기를_불러온다() {
        // given
        member = memberRepository.save(
                new Member(new Name("매트"), new Email("matt@kakao.com"), new Password("1234"), ADMIN));
        reservationTime = reservationTimeRepository.save(
                ReservationTime.create(LocalTime.of(10, 0))
        );
        theme = themeRepository.save(new Theme("공포", "ss", "ss"));
        Waiting waiting1 = Waiting.create(예약날짜_내일, reservationTime, theme, member);
        Waiting waiting2 = Waiting.create(예약날짜_내일, reservationTime, theme, member);
        Waiting save1 = waitingRepository.save(waiting1);
        Waiting save2 = waitingRepository.save(waiting2);

        //when
        List<Waiting> allWaitings = waitingRepository.findAll();

        //then
        assertThat(allWaitings).containsExactly(
                save1, save2
        );
    }

    @Test
    void 특정_대기를_불러온다() {
        // given
        member = memberRepository.save(
                new Member(new Name("매트"), new Email("matt@kakao.com"), new Password("1234"), ADMIN));
        reservationTime = reservationTimeRepository.save(
                ReservationTime.create(LocalTime.of(10, 0))
        );
        theme = themeRepository.save(new Theme("공포", "ss", "ss"));
        Waiting request = Waiting.create(예약날짜_내일, reservationTime, theme, member);
        Waiting save = waitingRepository.save(request);

        //when
        Optional<Waiting> waiting = waitingRepository.findById(save.getId());

        //then
        assertThat(waiting.get().getMember().getName()).isEqualTo("매트");
        assertThat(waiting.get().getStartAt()).isEqualTo(LocalTime.of(10, 0));
        assertThat(waiting.get().getId()).isEqualTo(save.getId());
        assertThat(waiting.get().getDate()).isEqualTo(예약날짜_내일.getDate());
    }
}
