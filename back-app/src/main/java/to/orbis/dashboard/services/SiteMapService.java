package to.orbis.dashboard.services;

import com.redfin.sitemapgenerator.WebSitemapGenerator;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import to.orbis.dashboard.models.dto.statistic.SiteMapType;
import to.orbis.dashboard.models.entity.types.PostType;
import to.orbis.dashboard.repositories.GroupRepository;
import to.orbis.dashboard.repositories.PlaceRepository;
import to.orbis.dashboard.repositories.PostRepository;
import to.orbis.dashboard.repositories.UserRepository;

import java.io.File;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class SiteMapService {
    private final GroupRepository groupRepository;
    private final UserRepository userRepository;
    private final PlaceRepository placeRepository;
    private final PostRepository postRepository;
    private final FireStorageService fireStorageService;
    private final static String FILE_FOLDER = "site-maps";

    private final static Integer MAX_LINKS_NUMBERS = 25_000;

    @SneakyThrows
    public void createSiteMap(SiteMapType type) {
        var maxNumber = getMaxEntities(type);
        var page = 0;
        var links = getLink(type, page, maxNumber);

        log.info("type: {} totalPages: {}. totalEntities: {}", type, links.allPages, links.allEntities);
        do {
            var file = new File(FILE_FOLDER);
            WebSitemapGenerator wsg = new WebSitemapGenerator(FIRST_LINK_PART, file);
            links.getLinks().forEach(link -> wsg.addUrl(link));
            wsg.write();

            var siteMapFile = new File(file.getPath() + "/sitemap.xml");

            var name = getFileName(type, page);
            Files.move(siteMapFile.toPath(), siteMapFile.toPath().resolveSibling(name));

            var chanel = fireStorageService.createWriteChanel(
                    name,
                    "sitemap",
                    "application/xml"
            );


            var newFile = new File(file.getPath() + "/" + name);
            chanel.write(ByteBuffer.wrap(Files.readAllBytes(newFile.toPath())));
            chanel.close();
            log.info("{} was created", page);
            page ++;
        } while (page != links.getAllPages());
        log.info("Finished");
    }

    private int getMaxEntities(SiteMapType type) {
        switch (type) {
            case GROUPS:
                return MAX_LINKS_NUMBERS / GROUPS_LINKS_LIST.size();
            case PLACES:
                return MAX_LINKS_NUMBERS / PLACES_LINKS_LIST.size();
            case POSTS:
                return MAX_LINKS_NUMBERS / POSTS_LINKS_LIST.size();
            case USERS:
                return MAX_LINKS_NUMBERS / USERS_LINKS_LIST.size();
            default:
                throw new IllegalStateException("Unexpected value: " + type);
        }
    }

    private String getFileName(SiteMapType type, int page) {
        var firstPart = type.name().toLowerCase();
        if (page != 0) {
            firstPart = firstPart + "-" + page;
        }
        return firstPart + "-sitemap.xml";
    }

    private PageResponse getLink(SiteMapType type, int page, int limit) {
        switch (type) {
            case GROUPS:
                return getGroupsLinks(page, limit);
            case PLACES:
                return getPlacesLinks(page, limit);
            case POSTS:
                return getPostsLinks(page, limit);
            case USERS:
                return getUsersLinks(page, limit);
            default:
                throw new IllegalStateException("Unexpected value: " + type);
        }
    }

    private PageResponse getGroupsLinks(int page, int limit) {
        var groups = groupRepository.findAll(PageRequest.of(page, limit));
        var links = groups.stream()
                .filter(group -> !group.isDeleted())
                .limit(limit)
                .map(group -> GROUPS_LINKS_LIST.stream()
                        .map(link -> String.format(link, group.getSlug()))
                        .collect(Collectors.toList())
                )
                .flatMap(List::stream)
                .collect(Collectors.toList());
        return PageResponse.builder()
                .pageNumber(page)
                .allPages(groups.getTotalPages())
                .links(links)
                .allEntities(groups.getTotalElements())
                .build();
    }


    private PageResponse getPlacesLinks(int page, int limit) {
        var places = placeRepository.findAllByDominantGroupKeyIsNotNull(PageRequest.of(page, limit));
        var links = places.stream()
                .filter(place -> !place.getDeleted())
                .limit(limit)
                .map(place -> PLACES_LINKS_LIST.stream()
                        .map(link -> String.format(link, place.getSlug()))
                        .collect(Collectors.toList())
                )
                .flatMap(List::stream)
                .collect(Collectors.toList());
        return PageResponse.builder()
                .pageNumber(page)
                .allPages(places.getTotalPages())
                .links(links)
                .allEntities(places.getTotalElements())
                .build();
    }

    private PageResponse getPostsLinks(int page, int limit) {
        var posts = postRepository.findAllByTypeNotIn(List.of(PostType.CHECK_IN, PostType.EVENT), PageRequest.of(page, limit));
        var links = posts.stream()
                .filter(post -> !post.isDeleted())
                .limit(limit)
                .map(post -> POSTS_LINKS_LIST.stream()
                        .map(link -> String.format(link, post.getPostKey()))
                        .collect(Collectors.toList())
                )
                .flatMap(List::stream)
                .collect(Collectors.toList());
        return PageResponse.builder()
                .pageNumber(page)
                .allPages(posts.getTotalPages())
                .links(links)
                .allEntities(posts.getTotalElements())
                .build();
    }

    private PageResponse getUsersLinks(int page, int limit) {
        var users = userRepository.findAll(PageRequest.of(page, limit));
        var links = users.stream()
                .filter(user -> !user.isDeleted())
                .limit(limit)
                .map(user -> USERS_LINKS_LIST.stream()
                        .map(link -> String.format(link, user.getSlug()))
                        .collect(Collectors.toList())
                )
                .flatMap(List::stream)
                .collect(Collectors.toList());
        return PageResponse.builder()
                .pageNumber(page)
                .allPages(users.getTotalPages())
                .links(links)
                .allEntities(users.getTotalElements())
                .build();
    }

    private final static String FIRST_LINK_PART = "https://orbis.social";

    private final static String GROUPS_COMMON_LINK = FIRST_LINK_PART + "/group/%s";
    private final static List<String> GROUPS_LINKS_LIST = List.of(
            GROUPS_COMMON_LINK,
            GROUPS_COMMON_LINK + "/events",
            GROUPS_COMMON_LINK + "/places",
            GROUPS_COMMON_LINK + "/members"
    );

    private final static String PLACES_COMMON_LINK = FIRST_LINK_PART + "/place/%s";
    private final static List<String> PLACES_LINKS_LIST = List.of(
            PLACES_COMMON_LINK,
            PLACES_COMMON_LINK + "/events"
    );

    private final static String POSTS_COMMON_LINK =  FIRST_LINK_PART + "/post/%s";
    private final static List<String> POSTS_LINKS_LIST  = List.of(
            POSTS_COMMON_LINK
    );

    private final static String USERS_COMMON_LINK = FIRST_LINK_PART + "/user/%s";
    private final static List<String> USERS_LINKS_LIST = List.of(
            USERS_COMMON_LINK,
            USERS_COMMON_LINK + "/feed",
            USERS_COMMON_LINK + "/groups",
            USERS_COMMON_LINK + "/followers",
            USERS_COMMON_LINK + "/followings"
    );

    @Builder
    @Getter
    private static class PageResponse {
        private List<String> links;
        private int pageNumber;
        private int allPages;
        private long allEntities;
    }
}
