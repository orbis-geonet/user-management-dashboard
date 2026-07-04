package to.orbis.dashboard.tasks.dev;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.geo.Point;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import to.orbis.dashboard.models.entity.Post;
import to.orbis.dashboard.models.entity.Story;
import to.orbis.dashboard.repositories.PostRepository;
import to.orbis.dashboard.repositories.StoryRepository;
import to.orbis.dashboard.services.PlacesInfoService;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "app", name = "dev-task", havingValue = "true")
public class AddCityTask {

    private final PostRepository postRepository;
    private final StoryRepository storyRepository;
    private final PlacesInfoService placesInfoService;


//    @Scheduled(fixedDelay = 1, timeUnit = TimeUnit.SECONDS)
    public void addCityToPosts(){
        log.info("addCityToPosts: start....");
        List.of(0, 1, 2, 3, 4, 5, 6, 7, 8)
                .parallelStream()
                .forEach(it -> {
                    PageRequest pageRequest = PageRequest.of(it, 500);
                    addCityToPostsByPage(pageRequest);
                });
        log.info("addCityToPosts: finishing....");
    }

//    @Scheduled(fixedDelay = 10, timeUnit = TimeUnit.SECONDS)
    public void addCityToStory(){
        log.info("addCityToStory: start....");
        List.of(0, 1, 2, 3, 4, 5, 6, 7, 8)
                .parallelStream()
                .forEach(it -> {
                    PageRequest pageRequest = PageRequest.of(it, 50);
                    addCityToStoriesByPage(pageRequest);
                });
        log.info("addCityToStory: finishing....");
    }

    private void addCityToPostsByPage(PageRequest pageRequest) {
        Page<Post> postPage = postRepository.findWhereCityNull(pageRequest);
        postPage.forEach(post -> {
            try {
                if (post.getCoordinates() != null) {
                    String city = placesInfoService.findCityByCoordinates(post.getCoordinates());
                    if (city == null) {
                        post.setCity("null_city");
                        log.info("Post: {} added city NULL. Coordinates: {}", post.getId(), post.getCoordinates());
                    } else {
                        post.setCity(city);
                        log.info("Post: {} added city {}. Coordinates: {}", post.getId(), city, post.getCoordinates());
                    }
                } else {
                    post.setCity("no_point");
                }
            } catch (Exception e) {
                post.setCity("ERROR");
            }

            postRepository.save(post);
        });
    }

    private void addCityToStoriesByPage(PageRequest pageRequest) {
        Page<Story> storyPage = storyRepository.findWhereCitiesNull(pageRequest);
        storyPage.forEach(story -> {
            try {
                if (story.getCoordinates() != null && story.getCoordinates().getCoordinates() != null && !story.getCoordinates().getCoordinates().isEmpty()) {
                    LinkedList<String> cities = new LinkedList<>();
                    for (Point point : story.getCoordinates().getCoordinates()) {
                        String city = placesInfoService.findCityByCoordinates(point);
                        if (city == null) {
                            log.info("Story: {} added city NULL. Coordinates: {}", story.getId(), point);
                        } else {
                            cities.add(city);
                            log.info("Story: {} added city {}. Coordinates: {}", story.getId(), city, point);
                        }
                    }
                    story.setCities(cities);

                } else {
                    story.setCity("no_point");
                }
            } catch (Exception e) {
                story.setCity("error");
            }

            storyRepository.save(story);
        });
    }
}
