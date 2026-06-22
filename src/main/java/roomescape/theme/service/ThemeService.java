package roomescape.theme.service;

import java.time.Clock;
import java.time.LocalDate;
import java.util.List;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.common.exception.RoomEscapeException;
import roomescape.reservation.repository.ReservationRepository;
import roomescape.reservation.repository.SlotRepository;
import roomescape.theme.domain.Theme;
import roomescape.theme.dto.ThemeRequest;
import roomescape.theme.dto.ThemeResponse;
import roomescape.theme.exception.ThemeErrorCode;
import roomescape.theme.repository.ThemeRepository;

@Service
public class ThemeService {

    private final Clock clock;
    private final ThemeRepository themeRepository;
    private final ReservationRepository reservationRepository;
    private final SlotRepository slotRepository;


    public ThemeService(Clock clock, ThemeRepository themeRepository,
                        ReservationRepository reservationRepository,
                        SlotRepository slotRepository) {
        this.clock = clock;
        this.themeRepository = themeRepository;
        this.reservationRepository = reservationRepository;
        this.slotRepository = slotRepository;
    }

    @Transactional
    public ThemeResponse addTheme(ThemeRequest request) {
        Theme theme = Theme.create(request.name(), request.description(), request.imageUrl());
        validateDuplicateTheme(theme);
        try {
            Theme savedTheme = themeRepository.save(theme);
            return ThemeResponse.from(savedTheme);
        } catch (DuplicateKeyException duplicate) {
            throw new RoomEscapeException(ThemeErrorCode.THEME_DUPLICATE);
        }
    }

    private void validateDuplicateTheme(Theme theme) {
        if (themeRepository.existByThemeName(theme.getName())) {
            throw new RoomEscapeException(ThemeErrorCode.THEME_DUPLICATE);
        }
    }

    @Transactional(readOnly = true)
    public ThemeResponse findById(Long id) {
        Theme result = themeRepository.findById(id)
                .orElseThrow(() -> new RoomEscapeException(ThemeErrorCode.THEME_NOT_FOUND));
        return ThemeResponse.from(result);
    }

    @Transactional(readOnly = true)
    public List<ThemeResponse> findAllThemes() {
        return themeRepository.findAll().stream().map(ThemeResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ThemeResponse> getPopularThemes(Long weeks, Long limit) {
        LocalDate now = LocalDate.now(clock);
        return themeRepository.findPopularThemes(now.minusWeeks(weeks),
                now, limit).stream().map(ThemeResponse::from).toList();
    }

    @Transactional
    public void deleteTheme(Long id) {
        validateThemeExists(id);
        validateRemovableTheme(id);
        try {
            // 예약이 없는 테마의 슬롯은 전부 고아이므로 함께 정리해야 FK에 막히지 않는다
            slotRepository.deleteByThemeId(id);
            themeRepository.delete(id);
        } catch (DataIntegrityViolationException concurrentReservation) {
            throw new RoomEscapeException(ThemeErrorCode.RESERVATION_EXIST_ON_THEME);
        }
    }

    private void validateThemeExists(Long id) {
        themeRepository.findById(id).orElseThrow(
                () -> new RoomEscapeException(ThemeErrorCode.THEME_NOT_FOUND)
        );
    }

    private void validateRemovableTheme(Long id) {
        if (reservationRepository.existsByThemeId(id)) {
            throw new RoomEscapeException(ThemeErrorCode.RESERVATION_EXIST_ON_THEME);
        }
    }
}
