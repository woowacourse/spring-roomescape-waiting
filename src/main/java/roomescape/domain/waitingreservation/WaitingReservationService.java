package roomescape.domain.waitingreservation;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import roomescape.domain.member.Member;
import roomescape.domain.member.MemberService;
import roomescape.domain.reservation.ReservationRepository;
import roomescape.domain.reservationdate.ReservationDate;
import roomescape.domain.reservationdate.ReservationDateService;
import roomescape.domain.reservationtime.ReservationTime;
import roomescape.domain.reservationtime.ReservationTimeService;
import roomescape.domain.theme.Theme;
import roomescape.domain.theme.ThemeService;
import roomescape.domain.waitingreservation.dto.RankProjection;
import roomescape.domain.waitingreservation.dto.WaitingReservationCreationRequest;
import roomescape.domain.waitingreservation.dto.WaitingReservationCreationResponse;
import roomescape.domain.waitingreservation.dto.WaitingReservationWithRank;
import roomescape.domain.waitingreservation.dto.WaitingReservationWithRankResponse;
import roomescape.support.exception.ReservationDateErrorCode;
import roomescape.support.exception.RoomescapeException;
import roomescape.support.exception.WaitingReservationErrorCode;

@Slf4j
@Service
@RequiredArgsConstructor
public class WaitingReservationService {

    private final WaitingReservationRepository waitingReservationRepository;
    private final ReservationRepository reservationRepository;
    private final MemberService memberService;
    private final ReservationDateService reservationDateService;
    private final ReservationTimeService reservationTimeService;
    private final ThemeService themeService;

    public WaitingReservationCreationResponse createWaitingReservation(WaitingReservationCreationRequest request) {
        Member member = memberService.findById(request.memberId());
        ReservationDate date = reservationDateService.findById(request.dateId());
        ReservationTime time = reservationTimeService.findById(request.timeId());
        Theme theme = themeService.findById(request.themeId());
        validateNotPast(date, time);
        validateSlotIsReserved(request);
        validateAlreadyReserved(request);
        validateDuplicationOfWaitingReservation(request);

        WaitingReservation waitingReservation = request.toEntity(member, date, time, theme, LocalDateTime.now());
        WaitingReservation savedWaitingReservation = waitingReservationRepository.save(waitingReservation);
        return WaitingReservationCreationResponse.from(savedWaitingReservation);
    }

    public void cancelWaitingReservation(Long id) {
        WaitingReservation waitingReservation = waitingReservationRepository
            .findById(id)
            .orElseThrow(() -> new RoomescapeException(WaitingReservationErrorCode.WAITING_RESERVATION_NOT_FOUND));

        if (reservationRepository.existsByMemberIdAndDateIdAndTimeIdAndThemeId(
            waitingReservation.getMember().getId(),
            waitingReservation.getDate().getId(),
            waitingReservation.getTime().getId(),
            waitingReservation.getTheme().getId())) {
            throw new RoomescapeException(WaitingReservationErrorCode.ALREADY_PROMOTED_TO_RESERVATION);
        }

        waitingReservationRepository.deleteById(id);
    }

    public List<WaitingReservationWithRankResponse> getWaitingReservationsWithRankByMemberId(Long memberId) {
        List<WaitingReservation> waitingReservations = waitingReservationRepository.findAllByMemberId(memberId);

        Map<Long, Long> rankMap = waitingReservationRepository.findRankByMemberId(memberId)
            .stream()
            .collect(Collectors.toMap(RankProjection::getId, RankProjection::getRank));

        return waitingReservations.stream()
            .map(wr -> new WaitingReservationWithRank(wr, rankMap.get(wr.getId())))
            .map(WaitingReservationWithRankResponse::from)
            .toList();
    }

    private void validateNotPast(ReservationDate reservationDate, ReservationTime reservationTime) {
        if (reservationDate.isPast(reservationTime)) {
            throw new RoomescapeException(ReservationDateErrorCode.PAST_DATE_NOT_ALLOWED);
        }
    }

    private void validateAlreadyReserved(WaitingReservationCreationRequest request) {
        if (reservationRepository.existsByMemberIdAndDateIdAndTimeIdAndThemeId(
            request.memberId(),
            request.dateId(),
            request.timeId(),
            request.themeId())) {
            throw new RoomescapeException(WaitingReservationErrorCode.ALREADY_RESERVED);
        }
    }

    private void validateDuplicationOfWaitingReservation(WaitingReservationCreationRequest request) {
        if (waitingReservationRepository.existsByMemberIdAndDateIdAndTimeIdAndThemeId(
            request.memberId(),
            request.dateId(),
            request.timeId(),
            request.themeId())) {
            throw new RoomescapeException(WaitingReservationErrorCode.DUPLICATE_WAITING_RESERVATION);
        }
    }

    private void validateSlotIsReserved(WaitingReservationCreationRequest request) {
        boolean reserved = reservationRepository.existsByDateIdAndTimeIdAndThemeId(
            request.dateId(),
            request.timeId(),
            request.themeId()
        );
        if (!reserved) {
            throw new RoomescapeException(WaitingReservationErrorCode.AVAILABLE_SLOT_NOT_WAITABLE);
        }
    }
}
