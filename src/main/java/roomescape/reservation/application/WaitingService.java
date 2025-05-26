package roomescape.reservation.application;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.exception.auth.AuthorizationException;
import roomescape.exception.resource.AlreadyExistException;
import roomescape.exception.resource.ResourceNotFoundException;
import roomescape.member.domain.Member;
import roomescape.member.domain.MemberRepository;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationSlot;
import roomescape.reservation.domain.ReservationTime;
import roomescape.reservation.domain.Waiting;
import roomescape.reservation.domain.repository.ReservationRepository;
import roomescape.reservation.domain.repository.ReservationTimeRepository;
import roomescape.reservation.domain.repository.WaitingRepository;
import roomescape.reservation.infrastructure.projection.WaitingWithRankProjection;
import roomescape.reservation.ui.dto.request.CreateWaitingRequest;
import roomescape.reservation.ui.dto.response.WaitingResponse;
import roomescape.reservation.ui.dto.response.WaitingWithRankResponse;
import roomescape.theme.domain.Theme;
import roomescape.theme.domain.ThemeRepository;

@Service
@RequiredArgsConstructor
public class WaitingService {

    private final ReservationTimeRepository reservationTimeRepository;
    private final ThemeRepository themeRepository;
    private final MemberRepository memberRepository;
    private final ReservationRepository reservationRepository;
    private final WaitingRepository waitingRepository;

    @Transactional
    public WaitingResponse create(
            final CreateWaitingRequest.ForMember request,
            final Long memberId
    ) {
        final ReservationTime time = getReservationTime(request.date(), request.timeId());
        final Theme theme = themeRepository.getById(request.themeId());
        final Member member = memberRepository.getById(memberId);

        return WaitingResponse.from(createWaiting(request.date(), time, theme, member));
    }

    private Waiting createWaiting(
            final LocalDate date,
            final ReservationTime time,
            final Theme theme,
            final Member member
    ) {
        final ReservationSlot reservationSlot = ReservationSlot.of(date, time, theme);
        final Reservation reservation = reservationRepository.findByReservationSlot(reservationSlot)
                .orElseThrow(() -> new ResourceNotFoundException("예약이 없는 상태에서 예약 대기를 추가할 수 없습니다."));

        if (Objects.equals(reservation.getMember(), member)) {
            throw new AlreadyExistException("해당 예약 슬롯에 본인 예약이 있습니다.");
        }

        if (waitingRepository.existsByReservationSlotAndMember(reservationSlot, member)) {
            throw new AlreadyExistException("신청한 예약 대기가 이미 존재합니다.");
        }

        final Waiting waiting = Waiting.of(reservationSlot, member, LocalDateTime.now());

        return waitingRepository.save(waiting);
    }

    private ReservationTime getReservationTime(final LocalDate date, final Long timeId) {
        final ReservationTime reservationTime = reservationTimeRepository.getById(timeId);
        final LocalDateTime now = LocalDateTime.now();
        final LocalDateTime reservationDateTime = LocalDateTime.of(date, reservationTime.getStartAt());
        if (reservationDateTime.isBefore(now)) {
            throw new IllegalArgumentException("예약 시간은 현재 시간보다 이후여야 합니다.");
        }

        return reservationTime;
    }

    @Transactional
    public void deleteIfOwner(final Long waitingId, final Long memberId) {
        final Waiting waiting = waitingRepository.getById(waitingId);
        final Member member = memberRepository.getById(memberId);

        if (!Objects.equals(waiting.getMember(), member)) {
            throw new AuthorizationException("본인이 아니면 삭제할 수 없습니다.");
        }

        waitingRepository.deleteById(waitingId);
    }

    @Transactional(readOnly = true)
    public List<WaitingWithRankResponse.ForMember> findAllWaitingWithRankByMemberId(final Long memberId) {
        final List<WaitingWithRankProjection> waitingWithRankProjections = waitingRepository.findAllWaitingWithRankProjectionByMemberId(
                memberId);

        return waitingWithRankProjections.stream()
                .map(WaitingWithRankResponse.ForMember::from)
                .toList();
    }
}
