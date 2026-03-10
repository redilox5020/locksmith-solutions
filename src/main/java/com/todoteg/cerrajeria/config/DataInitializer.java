package com.todoteg.cerrajeria.config;

import com.todoteg.cerrajeria.model.*;
import com.todoteg.cerrajeria.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final UserProfileRepository userRepository;
    private final PublicationRepository publicationRepository;
    private final PublicationImageRepository imageRepository;
    private final TagRepository tagRepository;
    private final CommentRepository commentRepository;
    private final VideoReelRepository videoRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        if (userRepository.count() > 0) {
            log.info("Base de datos ya inicializada. Saltando seed.");
            return;
        }

        log.info("Inicializando datos de prueba...");

        // === Admin User ===
        UserProfile admin = new UserProfile();
        admin.setEmail("admin@autokeys.com");
        admin.setName("Admin");
        admin.setPassword(passwordEncoder.encode("admin123"));
        admin.setIsActive(true);
        admin.setIsStaff(true);
        admin.setIsSuperuser(true);
        admin.setCreatedAt(LocalDateTime.now().toString());
        admin = userRepository.save(admin);
        log.info("Admin creado: admin@autokeys.com / admin123");

        // === Tags ===
        Tag tagPromo = createTag("promoción");
        Tag tagLlave = createTag("llave con chip");
        Tag tagGarantia = createTag("garantía");
        Tag tagEmergencia = createTag("emergencia");
        Tag tag247 = createTag("24/7");
        Tag tagApertura = createTag("apertura");
        Tag tagCombo = createTag("combo");
        Tag tagSeguridad = createTag("seguridad");
        Tag tagOferta = createTag("oferta especial");
        Tag tagProgramacion = createTag("programación");
        Tag tagMarcas = createTag("todas las marcas");
        Tag tagDomicilio = createTag("domicilio");
        Tag tagControl = createTag("control remoto");
        Tag tagOriginal = createTag("original");
        Tag tagReparacion = createTag("reparación");
        Tag tagChapas = createTag("chapas");
        Tag tagUrgente = createTag("urgente");

        // === Publications ===
        Publication p1 = createPublication(
            "🔥 Duplicado de Llave con Chip",
            "¡Promoción del mes! Duplicamos tu llave con chip para cualquier marca de vehículo. Incluye programación y garantía de 6 meses.",
            "$105.000", "$150.000", "30% OFF",
            "Hola! Me interesa la promoción de duplicado de llave con chip. ¿Podrían darme más información?",
            true, 234,
            List.of("https://images.unsplash.com/photo-1558618666-fcd25c85cd64?w=600&h=600&fit=crop",
                     "https://images.unsplash.com/photo-1503376780353-7e6692767b70?w=600&h=600&fit=crop"),
            Set.of(tagPromo, tagLlave, tagGarantia)
        );
        createComment(p1, "Juan P.", "¡Excelente servicio! Me salvaron cuando perdí mis llaves.", "2024-01-15");
        createComment(p1, "María G.", "Muy rápidos y profesionales. Recomendados 100%", "2024-01-14");

        Publication p2 = createPublication(
            "🚗 Apertura Express",
            "¿Te quedaste fuera de tu carro? Llegamos en menos de 30 minutos a cualquier punto de la ciudad. Sin daños, rápido y seguro.",
            "$50.000", null, null,
            "Hola! Necesito servicio de apertura de vehículo urgente. ¿Están disponibles?",
            false, 189,
            List.of("https://images.unsplash.com/photo-1449965408869-eaa3f722e40d?w=600&h=600&fit=crop",
                     "https://images.unsplash.com/photo-1552519507-da3b142c6e3d?w=600&h=600&fit=crop",
                     "https://images.unsplash.com/photo-1494976388531-d1058494cdd8?w=600&h=600&fit=crop"),
            Set.of(tagEmergencia, tag247, tagApertura)
        );

        Publication p3 = createPublication(
            "🔑 Combo Seguridad Total",
            "Incluye: Duplicado de llave con chip + Cambio de guardas + Control remoto nuevo. ¡La protección completa para tu vehículo!",
            "$262.500", "$350.000", "25% OFF",
            "Hola! Me interesa el Combo Seguridad Total. ¿Cuáles son los detalles?",
            true, 312,
            List.of("https://images.unsplash.com/photo-1489824904134-891ab64532f1?w=600&h=600&fit=crop",
                     "https://images.unsplash.com/photo-1502877338535-766e1452684a?w=600&h=600&fit=crop"),
            Set.of(tagCombo, tagSeguridad, tagOferta)
        );

        Publication p4 = createPublication(
            "⚡ Programación Rápida",
            "Programamos llaves codificadas para Toyota, Chevrolet, Mazda, Kia, Hyundai y más. Servicio express.",
            "$120.000", null, null,
            "Hola! Necesito programar una llave para mi vehículo. ¿Qué marcas manejan?",
            false, 156,
            List.of("https://images.unsplash.com/photo-1580273916550-e323be2ae537?w=600&h=600&fit=crop"),
            Set.of(tagProgramacion, tagMarcas, tagDomicilio)
        );

        Publication p5 = createPublication(
            "🛡️ Control Remoto Original",
            "Controles remotos originales y compatibles para todas las marcas. Programación incluida. Garantía de 1 año.",
            "$72.250", "$85.000", "15% OFF",
            "Hola! Necesito un control remoto para mi carro. ¿Tienen disponible?",
            false, 98,
            List.of("https://images.unsplash.com/photo-1549317661-bd32c8ce0db2?w=600&h=600&fit=crop",
                     "https://images.unsplash.com/photo-1542362567-b07e54358753?w=600&h=600&fit=crop"),
            Set.of(tagControl, tagOriginal, tagGarantia)
        );

        Publication p6 = createPublication(
            "🔧 Reparación de Chapas",
            "¿Tu chapa no funciona bien? La reparamos o reemplazamos en el momento. Servicio para puertas, maletero y encendido.",
            "$60.000", null, null,
            "Hola! Tengo problemas con la chapa de mi vehículo. ¿Pueden ayudarme?",
            false, 67,
            List.of("https://images.unsplash.com/photo-1486262715619-67b85e0b08d3?w=600&h=600&fit=crop"),
            Set.of(tagReparacion, tagChapas, tagUrgente)
        );

        // === Video Reels ===
        createVideoReel("https://www.youtube.com/shorts/dQw4w9WgXcQ",
                "https://images.unsplash.com/photo-1558618666-fcd25c85cd64?w=400&h=700&fit=crop",
                "@cerrajero_pro", p1);
        createVideoReel("https://www.youtube.com/shorts/2lAe1cqCOXo",
                "https://images.unsplash.com/photo-1449965408869-eaa3f722e40d?w=400&h=700&fit=crop",
                "@autokeys_oficial", p2);
        createVideoReel("https://www.youtube.com/shorts/rokGy0huYEA",
                "https://images.unsplash.com/photo-1489824904134-891ab64532f1?w=400&h=700&fit=crop",
                "@llaves_express", p3);
        createVideoReel("https://www.youtube.com/shorts/qQhdkCHPfkM",
                "https://images.unsplash.com/photo-1580273916550-e323be2ae537?w=400&h=700&fit=crop",
                "@cerrajeria24h", p4);

        log.info("Datos de prueba cargados: 6 promociones, 4 videos, 17 tags");
    }

    private Tag createTag(String name) {
        Tag tag = new Tag();
        tag.setName(name);
        return tagRepository.save(tag);
    }

    private Publication createPublication(String title, String desc, String price, String originalPrice,
                                       String discount, String whatsappMsg, boolean isNew, int likes,
                                       List<String> imageUrls, Set<Tag> tags) {
        Publication p = new Publication();
        p.setTitle(title);
        p.setDescription(desc);
        p.setPrice(price);
        p.setOriginalPrice(originalPrice);
        p.setDiscount(discount);
        p.setWhatsappMessage(whatsappMsg);
        p.setIsNew(isNew);
        p.setLikes(likes);
        p.setTags(tags);
        p = publicationRepository.save(p);

        for (String url : imageUrls) {
            PublicationImage img = new PublicationImage();
            img.setImageUrl(url);
            img.setPublication(p);
            imageRepository.save(img);
        }

        return p;
    }

    private void createComment(Publication publication, String author, String text, String date) {
        Comment c = new Comment();
        c.setAuthor(author);
        c.setText(text);
        c.setDate(date);
        c.setPublication(publication);
        commentRepository.save(c);
    }

    private void createVideoReel(String videoUrl, String thumbnailUrl, String username, Publication publication) {
        VideoReel v = new VideoReel();
        v.setVideoUrl(videoUrl);
        v.setThumbnailUrl(thumbnailUrl);
        v.setUsername(username);
        v.setPublication(publication);
        videoRepository.save(v);
    }
}

