package pl.backend.weddinggallery;

import static org.assertj.core.api.Assertions.assertThat;

import java.lang.reflect.Method;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;
import pl.backend.weddinggallery.event.model.*;
import pl.backend.weddinggallery.gallery.model.*;
import pl.backend.weddinggallery.media.model.*;
import pl.backend.weddinggallery.membership.model.*;
import pl.backend.weddinggallery.publicaccess.model.GalleryAccess;
import pl.backend.weddinggallery.upload.model.*;
import pl.backend.weddinggallery.user.model.*;

class ModelLifecycleBoundaryTest {

	@Test
	void persistenceCallbacksSupplyEveryDefaultForNewEntities() throws Exception {
		Event event = new Event();
		invoke(event, "onCreate");
		assertThat(event.getId()).isNotBlank();
		assertThat(event.getStatus()).isEqualTo(EventStatus.DRAFT);
		assertThat(event.getPrivacyMode()).isEqualTo(PrivacyMode.PRIVATE);
		assertThat(event.getCreatedAt()).isNotNull();

		Gallery gallery = new Gallery();
		invoke(gallery, "onCreate");
		assertThat(gallery.getId()).isNotBlank();
		assertThat(gallery.getStatus()).isEqualTo(GalleryStatus.ACTIVE);
		assertThat(gallery.getModerationMode()).isEqualTo(ModerationMode.REQUIRED);

		EventMembership membership = new EventMembership();
		invoke(membership, "onCreate");
		assertThat(membership.getId()).isNotBlank();
		assertThat(membership.getRole()).isEqualTo(EventRole.MANAGER);
		assertThat(membership.getJoinedAt()).isNotNull();

		MediaFile media = new MediaFile();
		invoke(media, "create");
		assertThat(media.getId()).isNotBlank();
		assertThat(media.getStatus()).isEqualTo(MediaStatus.PENDING);
		assertThat(media.getPublicationStatus()).isEqualTo(PublicationStatus.PENDING);
		Gallery noneGallery = Gallery.builder().moderationMode(ModerationMode.NONE).build();
		MediaFile autoApproved = MediaFile.builder().gallery(noneGallery).status(MediaStatus.PENDING).build();
		invoke(autoApproved, "create");
		assertThat(autoApproved.getPublicationStatus()).isEqualTo(PublicationStatus.APPROVED);

		UploadSession upload = new UploadSession();
		invoke(upload, "create");
		assertThat(upload.getId()).isNotBlank();
		assertThat(upload.getStatus()).isEqualTo(UploadSessionStatus.OPEN);

		GalleryAccess access = new GalleryAccess();
		invoke(access, "onCreate");
		assertThat(access.getId()).isNotBlank();
		assertThat(access.getCreatedAt()).isNotNull();

		User user = new User();
		invoke(user, "onCreate");
		assertThat(user.getId()).isNotBlank();
		assertThat(user.getSystemRole()).isEqualTo(SystemRole.USER);
	}

	@Test
	void persistenceCallbacksPreserveExplicitValuesAndRefreshOnlyUpdateTimestamps() throws Exception {
		LocalDateTime fixed = LocalDateTime.of(2020, 1, 2, 3, 4);
		Event event = Event.builder().id("event").status(EventStatus.PUBLISHED).privacyMode(PrivacyMode.PRIVATE)
				.build();
		invoke(event, "onCreate");
		assertThat(event.getId()).isEqualTo("event");
		assertThat(event.getStatus()).isEqualTo(EventStatus.PUBLISHED);
		assertThat(event.getPrivacyMode()).isEqualTo(PrivacyMode.PRIVATE);
		event.setUpdatedAt(fixed);
		invoke(event, "onUpdate");
		assertThat(event.getUpdatedAt()).isAfter(fixed);

		Gallery gallery = Gallery.builder().id("gallery").status(GalleryStatus.ARCHIVED)
				.moderationMode(ModerationMode.NONE).build();
		invoke(gallery, "onCreate");
		assertThat(gallery.getId()).isEqualTo("gallery");
		assertThat(gallery.getStatus()).isEqualTo(GalleryStatus.ARCHIVED);
		assertThat(gallery.getModerationMode()).isEqualTo(ModerationMode.NONE);
		gallery.setUpdatedAt(fixed);
		invoke(gallery, "onUpdate");
		assertThat(gallery.getUpdatedAt()).isAfter(fixed);

		EventMembership membership = EventMembership.builder().id("membership").role(EventRole.OWNER).joinedAt(fixed)
				.build();
		invoke(membership, "onCreate");
		assertThat(membership.getId()).isEqualTo("membership");
		assertThat(membership.getRole()).isEqualTo(EventRole.OWNER);
		assertThat(membership.getJoinedAt()).isEqualTo(fixed);
		assertThat(membership.isActive()).isTrue();
		membership.remove();
		assertThat(membership.isActive()).isFalse();
		membership.reactivate();
		assertThat(membership.isActive()).isTrue();
		assertThat(membership.getRole()).isEqualTo(EventRole.MANAGER);
		invoke(membership, "onUpdate");

		MediaFile media = MediaFile.builder().id("media").status(MediaStatus.STORED).build();
		invoke(media, "create");
		assertThat(media.getId()).isEqualTo("media");
		assertThat(media.getStatus()).isEqualTo(MediaStatus.STORED);
		assertThat(media.getPublicationStatus()).isEqualTo(PublicationStatus.PENDING);
		invoke(media, "update");

		UploadSession upload = UploadSession.builder().id("upload").status(UploadSessionStatus.CANCELLED).build();
		invoke(upload, "create");
		assertThat(upload.getId()).isEqualTo("upload");
		assertThat(upload.getStatus()).isEqualTo(UploadSessionStatus.CANCELLED);
		invoke(upload, "update");

		GalleryAccess access = GalleryAccess.builder().id("access").createdAt(fixed).build();
		invoke(access, "onCreate");
		assertThat(access.getId()).isEqualTo("access");
		assertThat(access.getCreatedAt()).isEqualTo(fixed);

		User user = User.builder().id("user").systemRole(SystemRole.ADMIN).build();
		invoke(user, "onCreate");
		assertThat(user.getId()).isEqualTo("user");
		assertThat(user.getSystemRole()).isEqualTo(SystemRole.ADMIN);
		user.setUpdatedAt(fixed);
		invoke(user, "onUpdate");
		assertThat(user.getUpdatedAt()).isAfter(fixed);
	}

	private void invoke(Object target, String methodName) throws Exception {
		Method method = target.getClass().getDeclaredMethod(methodName);
		method.setAccessible(true);
		method.invoke(target);
	}
}
