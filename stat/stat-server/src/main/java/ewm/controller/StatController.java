package ewm.controller;

import ewm.ParamDto;
import ewm.ParamHitDto;
import ewm.ViewStats;
import ewm.service.StatService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequiredArgsConstructor
@Slf4j
@Validated
public class StatController {
    private final StatService statService;
    private static final String FORMAT_DATETIME = "yyyy-MM-dd HH:mm:ss";

    @PostMapping("/hit")
    @ResponseStatus(HttpStatus.CREATED)
    public void hit(@RequestBody ParamHitDto paramHitDto) {
        log.info("Поступил запрос POST /hit на создание hit {}", paramHitDto);
        statService.hit(paramHitDto);
        log.info("Запрос POST /hit успешно обработан");
    }

    @GetMapping("/stats")
    public List<ViewStats> getStat(@RequestParam @DateTimeFormat(pattern = FORMAT_DATETIME) LocalDateTime start,
                                   @RequestParam @DateTimeFormat(pattern = FORMAT_DATETIME) LocalDateTime end,
                                   @RequestParam(required = false) List<String> uris,
                                   @RequestParam(defaultValue = "false") Boolean unique) {
        log.info("Поступил запрос GET /stats на получение статистики {}", new ParamDto(start, end, uris, unique));
        List<ViewStats> viewStats = statService.getStat(new ParamDto(start, end, uris, unique));
        log.info("Запрос GET /stats успешно обработан {}", viewStats);
        return viewStats;
    }
}