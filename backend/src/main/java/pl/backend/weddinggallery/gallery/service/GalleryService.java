package pl.backend.weddinggallery.gallery.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.backend.weddinggallery.common.exception.AppException;
import pl.backend.weddinggallery.gallery.exception.GalleryErrorCode;
import pl.backend.weddinggallery.gallery.model.Gallery;
import pl.backend.weddinggallery.gallery.repository.GalleryRepository;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class GalleryService {

    private final GalleryRepository galleryRepository;

    @Transactional(readOnly = true)
    public List<Gallery> getGalleriesForEvent(String eventId) {
        log.debug("Fetching galleries for event {}", eventId);
        return galleryRepository.findByEventId(eventId);
    }

    @Transactional(readOnly = true)
    public Gallery getGalleryBySlug(String slug) {
        log.debug("Fetching gallery by slug {}", slug);
        return galleryRepository.findBySlug(slug)
                .orElseThrow(() -> new AppException(GalleryErrorCode.GALLERY_NOT_FOUND));
    }

    @Transactional
    public Gallery createGallery(Gallery gallery) {
        log.info("Creating new gallery with slug: {}", gallery.getSlug());
        
        Optional<Gallery> existing = galleryRepository.findBySlug(gallery.getSlug());
        if (existing.isPresent()) {
            log.warn("Attempt to create gallery with existing slug: {}", gallery.getSlug());
            throw new AppException(GalleryErrorCode.GALLERY_SLUG_ALREADY_EXISTS);
        }

        return galleryRepository.save(gallery);
    }
}
