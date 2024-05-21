package roomescape.service;

import static roomescape.exception.ExceptionType.DUPLICATE_RESERVATION;
import static roomescape.exception.ExceptionType.NOT_FOUND_MEMBER;
import static roomescape.exception.ExceptionType.NOT_FOUND_RESERVATION_TIME;
import static roomescape.exception.ExceptionType.NOT_FOUND_THEME;
import static roomescape.exception.ExceptionType.PAST_TIME_RESERVATION;
import static roomescape.service.mapper.ReservationResponseMapper.toResponse;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.domain.Member;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationSearchCondition;
import roomescape.domain.ReservationTime;
import roomescape.domain.Theme;
import roomescape.dto.LoginMemberReservationResponse;
import roomescape.dto.ReservationRequest;
import roomescape.dto.ReservationResponse;
import roomescape.exception.RoomescapeException;
import roomescape.repository.MemberRepository;
import roomescape.repository.ReservationRepository;
import roomescape.repository.ReservationTimeRepository;
import roomescape.repository.ThemeRepository;
import roomescape.service.mapper.LoginMemberReservationResponseMapper;
import roomescape.service.mapper.ReservationResponseMapper;

@Service
@Transactional
public class ReservationService {
    private final ReservationRepository reservationRepository;
    private final ReservationTimeRepository reservationTimeRepository;
    private final ThemeRepository themeRepository;
    private final MemberRepository memberRepository;

    public ReservationService(
            ReservationRepository reservationRepository,
            ReservationTimeRepository reservationTimeRepository,
            ThemeRepository themeRepository,
            MemberRepository memberRepository
    ) {
        this.reservationRepository = reservationRepository;
        this.reservationTimeRepository = reservationTimeRepository;
        this.themeRepository = themeRepository;
        this.memberRepository = memberRepository;
    }

    public ReservationResponse save(ReservationRequest reservationRequest) {
        ReservationTime time = reservationTimeRepository.findById(reservationRequest.timeId())
                .orElseThrow(() -> new RoomescapeException(NOT_FOUND_RESERVATION_TIME));

        Theme theme = themeRepository.findById(reservationRequest.themeId())
                .orElseThrow(() -> new RoomescapeException(NOT_FOUND_THEME));

        Member member = memberRepository.findById(reservationRequest.memberId())
                .orElseThrow(() -> new RoomescapeException(NOT_FOUND_MEMBER));

        Reservation beforeSave = new Reservation(member, reservationRequest.date(), time, theme);

        validateDuplicateReservation(time, theme, beforeSave.getDate());
        validatePastTimeReservation(beforeSave);

        Reservation saved = reservationRepository.save(beforeSave);
        return toResponse(saved);
    }

    private void validateDuplicateReservation(ReservationTime requestedTime, Theme requestedTheme, LocalDate date) {
        boolean isDuplicate = reservationRepository.existsByThemeAndDateAndTime(requestedTheme, date, requestedTime);
        if (isDuplicate) {
            throw new RoomescapeException(DUPLICATE_RESERVATION);
        }
    }

    private void validatePastTimeReservation(Reservation beforeSave) {
        if (beforeSave.isBefore(LocalDateTime.now())) {
            throw new RoomescapeException(PAST_TIME_RESERVATION);
        }
    }

    public List<ReservationResponse> findAll() {
        return reservationRepository.findAll().stream()
                .map(ReservationResponseMapper::toResponse)
                .toList();
    }

    public List<ReservationResponse> findByMemberAndThemeBetweenDates(ReservationSearchCondition condition) {
        List<Reservation> reservations = reservationRepository.findByReservationMember_IdAndTheme_IdAndDateBetween(
                condition.memberId(), condition.themeId(), condition.start(), condition.end());
        
        return reservations
                .stream()
                .map(ReservationResponseMapper::toResponse)
                .toList();
    }

    public List<LoginMemberReservationResponse> findByMemberId(long memberId) {
        return reservationRepository.findAllByReservationMember_Id(memberId)
                .stream()
                .map(LoginMemberReservationResponseMapper::toResponse)
                .toList();
    }

    public void delete(long id) {
        reservationRepository.deleteById(id);
    }
}
