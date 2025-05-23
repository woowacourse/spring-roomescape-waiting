package roomescape.business.service;

import java.time.LocalDate;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.business.domain.Member;
import roomescape.business.domain.Reservation;
import roomescape.business.domain.ReservationTime;
import roomescape.business.domain.Theme;
import roomescape.business.domain.Waiting;
import roomescape.exception.BadRequestException;
import roomescape.exception.NotFoundException;
import roomescape.infrastructure.repository.ReservationRepository;
import roomescape.infrastructure.repository.WaitingRepository;
import roomescape.presentation.dto.LoginMember;
import roomescape.presentation.dto.WaitingRequest;
import roomescape.presentation.dto.WaitingResponse;

@Service
@Transactional(readOnly = true)
public class WaitingService {

    private final QueryService queryService;
    private final WaitingRepository waitingRepository;
    private final ReservationRepository reservationRepository;

    public WaitingService(final QueryService queryService,
                          final WaitingRepository waitingRepository,
                          final ReservationRepository reservationRepository) {

        this.queryService = queryService;
        this.waitingRepository = waitingRepository;
        this.reservationRepository = reservationRepository;
    }

    @Transactional
    public WaitingResponse insert(final LoginMember loginMember, final WaitingRequest waitingRequest) {
        final Theme theme = queryService.getThemeById(waitingRequest.themeId());
        final ReservationTime reservationTime = queryService.getReservationTimeById(waitingRequest.timeId());

        Reservation reservation = getReservationByDateAndTimeIdAndThemeId(
                waitingRequest.date(), waitingRequest.timeId(), waitingRequest.themeId());

        if(reservation.isSameMember(loginMember.id())) {
            throw new BadRequestException("사용자가 예약한 항목입니다. 예약 대기가 불가능합니다.");
        }

        boolean isAlreadyExisted = waitingRepository.existsByDateAndTimeIdAndThemeIdAndMemberId(waitingRequest.date(),
                waitingRequest.timeId(), waitingRequest.themeId(), loginMember.id());

        if(isAlreadyExisted) {
            throw new BadRequestException("이미 예약 대기를 하였습니다.");
        }

        final Member member = queryService.getMemberById(loginMember.id());
        final Waiting waiting = new Waiting(member, theme, reservationTime, waitingRequest.date());
        Waiting savedWaiting = waitingRepository.save(waiting);

        return WaitingResponse.from(savedWaiting);
    }

    private Reservation getReservationByDateAndTimeIdAndThemeId(final LocalDate date, final Long timeId, final Long themeId) {
        return reservationRepository.findByDateAndTimeIdAndThemeId(date, timeId, themeId)
                .orElseThrow(() -> new NotFoundException("예약이 존재하지 않아 예약 대기를 할 수 없습니다."));
    }

    @Transactional
    public void deleteById(final Long id) {
        Waiting waiting = getById(id);

        if(waiting.isPast()) {
            throw new BadRequestException("이전 예약 대기는 삭제할 수 없습니다.");
        }
        waitingRepository.deleteById(id);
    }

    private Waiting getById(final Long id) {
        return waitingRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("해당하는 예약 대기를 찾을 수 없습니다. 예약 대기 id: %d".formatted(id)));
    }

    public List<WaitingResponse> findAll() {
        return waitingRepository.findAll().stream()
                .map(WaitingResponse::from)
                .toList();
    }
}
