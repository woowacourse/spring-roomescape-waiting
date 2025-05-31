package roomescape.service;

import java.time.LocalDate;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.domain.Member;
import roomescape.domain.ReservationTime;
import roomescape.domain.Theme;
import roomescape.domain.Waiting;
import roomescape.domain.repository.MemberRepository;
import roomescape.domain.repository.ReservationTimeRepository;
import roomescape.domain.repository.ThemeRepository;
import roomescape.domain.repository.WaitingRepository;
import roomescape.dto.response.WaitingResponse;
import roomescape.exception.ExistedReservationException;
import roomescape.exception.MemberNotFoundException;
import roomescape.exception.ReservationTimeNotFoundException;
import roomescape.exception.ThemeNotFoundException;
import roomescape.exception.WaitingNotFoundException;

@Service
@Transactional
public class WaitingService {

    private final ReservationTimeRepository reservationTimeRepository;
    private final ThemeRepository themeRepository;
    private final MemberRepository memberRepository;
    private final WaitingRepository waitingRepository;

    public WaitingService(final ReservationTimeRepository reservationTimeRepository,
                          final ThemeRepository themeRepository,
                          final MemberRepository memberRepository,
                          final WaitingRepository waitingRepository) {
        this.reservationTimeRepository = reservationTimeRepository;
        this.themeRepository = themeRepository;
        this.memberRepository = memberRepository;
        this.waitingRepository = waitingRepository;
    }

    public WaitingResponse createWaiting(Long memberId, Long timeId, Long themeId, LocalDate date) {
        ReservationTime reservationTime = reservationTimeRepository.findById(timeId)
                .orElseThrow(ReservationTimeNotFoundException::new);
        Theme theme = themeRepository.findById(themeId).orElseThrow(ThemeNotFoundException::new);
        Member member = memberRepository.findById(memberId).orElseThrow(MemberNotFoundException::new);

        Waiting waiting = Waiting.createWithoutId(member, date, reservationTime, theme);

        waiting.validateDateTime();
        validateWaitingDuplicate(date, reservationTime, theme, member);

        Waiting savedWaiting = waitingRepository.save(waiting);
        return WaitingResponse.from(savedWaiting);
    }

    private void validateWaitingDuplicate(LocalDate date, ReservationTime time, Theme theme, Member member) {
        if (waitingRepository.findByDateAndReservationTimeAndThemeAndMember(date, time, theme, member).isPresent()) {
            throw new ExistedReservationException();
        }
    }

    @Transactional(readOnly = true)
    public List<WaitingResponse> findAll() {
        List<Waiting> waitings = waitingRepository.findAll();
        return waitings.stream()
                .map(WaitingResponse::from)
                .toList();
    }

    public void deleteWaitingById(Long id) {
        waitingRepository.findById(id).orElseThrow(WaitingNotFoundException::new);
        waitingRepository.deleteById(id);
    }
}
