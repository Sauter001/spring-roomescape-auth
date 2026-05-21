package roomescape.repository;

import roomescape.domain.Theme;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface ThemeRepository {
    List<Theme> findAll();

    List<Theme> findThemesToManage(Long managerId);

    Theme save(Theme theme);

    void delete(Long id);

    Optional<Theme> findById(Long id);

    List<Theme> findPopularThemes(LocalDate startInclusive, LocalDate endInclusive, int limit);
}
