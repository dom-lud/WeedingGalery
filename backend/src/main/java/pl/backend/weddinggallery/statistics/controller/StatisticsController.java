package pl.backend.weddinggallery.statistics.controller;

import java.security.Principal;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pl.backend.weddinggallery.statistics.dto.StatisticsResponse;
import pl.backend.weddinggallery.statistics.service.StatisticsService;

@RestController
@RequestMapping("/api/events/{eventId}")
@RequiredArgsConstructor
public class StatisticsController {
	private final StatisticsService statisticsService;

	@GetMapping("/statistics")
	public StatisticsResponse event(@PathVariable String eventId, Principal principal) {
		return statisticsService.forEvent(eventId, principal.getName());
	}

	@GetMapping("/galleries/{galleryId}/statistics")
	public StatisticsResponse gallery(@PathVariable String eventId, @PathVariable String galleryId,
			Principal principal) {
		return statisticsService.forGallery(eventId, galleryId, principal.getName());
	}
}
