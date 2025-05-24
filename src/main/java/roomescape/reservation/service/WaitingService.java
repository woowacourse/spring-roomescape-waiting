package roomescape.reservation.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import roomescape.common.exception.AlreadyInUseException;
import roomescape.common.exception.EntityNotFoundException;
import roomescape.member.domain.Member;
import roomescape.member.repository.MemberRepository;
import roomescape.reservation.domain.ReservationTime;
import roomescape.reservation.domain.Theme;
import roomescape.reservation.domain.Waiting;
import roomescape.reservation.dto.request.WaitingCreateRequest;
import roomescape.reservation.dto.response.WaitingResponse;
import roomescape.reservation.repository.ReservationTimeRepository;
import roomescape.reservation.repository.ThemeRepository;
import roomescape.reservation.repository.WaitingRepository;

@Service
public class WaitingService {

    private final WaitingRepository waitingRepository;
    private final ReservationTimeRepository reservationTimeRepository;
    private final ThemeRepository themeRepository;
    private final MemberRepository memberRepository;

    public WaitingService(
            final WaitingRepository waitingRepository,
            final ReservationTimeRepository reservationTimeRepository,
            final ThemeRepository themeRepository,
            final MemberRepository memberRepository
    ) {
        this.waitingRepository = waitingRepository;
        this.reservationTimeRepository = reservationTimeRepository;
        this.themeRepository = themeRepository;
        this.memberRepository = memberRepository;
    }

    public List<WaitingResponse> getAll() {
        return waitingRepository.findAll()
                .stream()
                .map(WaitingResponse::from)
                .toList();
    }

    @Transactional
    public WaitingResponse createWaiting(final WaitingCreateRequest request) {
        if (hasAlreadyWaiting(request)) {
            throw new AlreadyInUseException("이미 예약 대기가 존재합니다.");
        }

        Waiting waiting = getWaiting(request);
        LocalDateTime now = LocalDateTime.now();
        validateDateTime(now, waiting.getDate(), waiting.getTime().getStartAt());

        Waiting savedWaiting = waitingRepository.save(waiting);
        return WaitingResponse.from(savedWaiting);
    }

    private boolean hasAlreadyWaiting(final WaitingCreateRequest request) {
        return waitingRepository.existsByDateAndTimeIdAndThemeIdAndMemberId(
                request.date(), request.timeId(), request.themeId(), request.loginMember().id()
        );
    }

    private Waiting getWaiting(final WaitingCreateRequest request) {
        Member member = getMember(request);
        ReservationTime time = gerReservationTime(request);
        Theme theme = getTheme(request);

        return new Waiting(request.date(), member, time, theme);
    }

    private ReservationTime gerReservationTime(final WaitingCreateRequest request) {
        Long timeId = request.timeId();
        return reservationTimeRepository.findById(timeId)
                .orElseThrow(() -> new EntityNotFoundException("존재하지 않는 예약시간 입니다."));
    }

    private Theme getTheme(final WaitingCreateRequest request) {
        Long themeId = request.themeId();
        return themeRepository.findById(themeId)
                .orElseThrow(() -> new EntityNotFoundException("존재하지 않는 테마입니다."));
    }

    private Member getMember(final WaitingCreateRequest request) {
        return memberRepository.findById(request.loginMember().id())
                .orElseThrow(() -> new EntityNotFoundException("등록되지 않은 회원입니다."));
    }

    private void validateDateTime(final LocalDateTime now, final LocalDate date, final LocalTime time) {
        LocalDateTime reservationDateTime = LocalDateTime.of(date, time);

        if (now.isAfter(reservationDateTime)) {
            throw new IllegalArgumentException("이미 지난 예약 시간입니다.");
        }
    }

    @Transactional
    public void deleteWaiting(final Long id) {
        if (!waitingRepository.existsById(id)) {
            throw new EntityNotFoundException("존재하지 않는 예약 대기입니다.");
        }
        waitingRepository.deleteById(id);
    }
}
