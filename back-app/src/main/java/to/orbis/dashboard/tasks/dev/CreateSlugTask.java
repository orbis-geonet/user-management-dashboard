package to.orbis.dashboard.tasks.dev;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import to.orbis.dashboard.services.admin.GroupService;
import to.orbis.dashboard.services.admin.PlaceService;
import to.orbis.dashboard.services.admin.UserService;

import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
//@ConditionalOnProperty(prefix = "app", name = "dev-task", havingValue = "true")
public class CreateSlugTask {

    private final GroupService groupService;
    private final UserService userService;
    private final PlaceService placeService;


//    @Scheduled(fixedDelay = 10, timeUnit = TimeUnit.SECONDS)
    public void generateSlugsGroupTask(){
        log.info("generateSlugsGroupTask: start....");
        groupService.generateSlugs();
        log.info("generateSlugsGroupTask: finish....");
    }

//    @Scheduled(fixedDelay = 10, timeUnit = TimeUnit.SECONDS)
    public void generateSlugsUserTask(){
        log.info("generateSlugsUserTask: start....");
        userService.generateSlugs();
        log.info("generateSlugsUserTask: finish....");
    }

    @Scheduled(fixedDelay = 10, timeUnit = TimeUnit.SECONDS)
    public void generateSlugsPlaceTask(){
        log.info("generateSlugsPlaceTask: start....");
        placeService.generateSlugs();
        log.info("generateSlugsPlaceTask: finish....");
    }
}
