package pl.backend.weddinggallery.gallery.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.*;

@Entity
@Table(name = "gallery_customizations", uniqueConstraints = @UniqueConstraint(name = "uk_gallery_customization_gallery", columnNames = "gallery_id"))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GalleryCustomization {
	@Id
	@Column(length = 36, nullable = false, updatable = false)
	private String id;

	@OneToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "gallery_id", nullable = false, unique = true)
	private Gallery gallery;

	@Enumerated(EnumType.STRING)
	@Column(name = "theme_key", nullable = false, length = 20)
	private ThemeKey themeKey;

	@Enumerated(EnumType.STRING)
	@Column(name = "layout_mode", nullable = false, length = 20)
	private LayoutMode layoutMode;

	@Column(name = "primary_color", nullable = false, length = 7)
	private String primaryColor;

	@Column(name = "accent_color", nullable = false, length = 7)
	private String accentColor;

	@Column(name = "welcome_text", length = 1000)
	private String welcomeText;

	@Column(name = "cover_media_id", length = 36)
	private String coverMediaId;

	@Column(nullable = false)
	private long version;

	@Column(name = "created_at", nullable = false, updatable = false)
	private LocalDateTime createdAt;

	@Column(name = "updated_at", nullable = false)
	private LocalDateTime updatedAt;

	@Version
	@Column(name = "lock_version", nullable = false)
	private long lockVersion;

	@PrePersist
	void onCreate() {
		LocalDateTime now = LocalDateTime.now();
		if (id == null) {
			id = UUID.randomUUID().toString();
		}
		createdAt = now;
		updatedAt = now;
	}

	@PreUpdate
	void onUpdate() {
		updatedAt = LocalDateTime.now();
	}
}
