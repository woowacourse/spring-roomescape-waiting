package roomescape.service;

import java.time.LocalDateTime;
import java.util.List;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.auth.service.ReservationAuthorizationService;
import roomescape.common.exception.DuplicateEntityException;
import roomescape.common.exception.EntityNotFoundException;
import roomescape.dao.ReservationDao;
import roomescape.dao.TimeDao;
import roomescape.domain.Member;
import roomescape.domain.Reservation;
import roomescape.domain.Time;
import roomescape.dto.request.ReservationPatchDto;
import roomescape.dto.request.ReservationRequestDto;

@Service
@Transactional
public class ReservationService {
    private final ReservationDao reservationDao;
    private final TimeDao timeDao;
    private final ReservationAuthorizationService authorizationService;
    private final ReservationCreator reservationCreator;

    public ReservationService(
            ReservationDao reservationDao,
            TimeDao timeDao,
            ReservationAuthorizationService authorizationService,
            ReservationCreator reservationCreator
    ) {
        this.reservationDao = reservationDao;
        this.timeDao = timeDao;
        this.authorizationService = authorizationService;
        this.reservationCreator = reservationCreator;
    }

    @Transactional(readOnly = true)
    public List<Reservation> findAllByMemberId(Long memberId) {
        return reservationDao.findAllByMemberId(memberId);
    }

    @Transactional(readOnly = true)
    public Reservation findActiveById(Long id) {
        Reservation reservation = reservationDao.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("존재하지 않는 예약입니다."));
        if (!reservation.isActive()) {
            throw new EntityNotFoundException("존재하지 않는 예약입니다.");
        }
        return reservation;
    }

    public Reservation create(Member member, ReservationRequestDto request) {
        Reservation reservation = reservationCreator.createByUser(member, request, LocalDateTime.now());
        try {
            return reservationDao.insert(reservation);
        } catch (DuplicateKeyException e) {
            throw new DuplicateEntityException("이미 존재하는 예약이 있습니다.");
        }
    }

    public Reservation updateByUser(Long id, Member member, ReservationPatchDto request) {
        authorizationService.validateMemberCanAccess(member, id);
        Reservation reservation = findActiveById(id);
        Time time = timeDao.findById(request.timeId())
                .orElseThrow(() -> new EntityNotFoundException("존재하지 않는 시간입니다."));
        reservation.update(request.date(), time, LocalDateTime.now());
        return reservationDao.update(reservation);
    }

    public void cancel(Long id, Member member) {
        authorizationService.validateMemberCanAccess(member, id);
        Reservation reservation = findActiveById(id);
        reservation.cancelByUser(LocalDateTime.now());
        reservationDao.update(reservation);
    }
}
