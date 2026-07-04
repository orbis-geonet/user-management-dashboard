package to.orbis.dashboard.controllers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import to.orbis.dashboard.models.dto.AddUserDto;
import to.orbis.dashboard.services.admin.FollowService;

@RestController
@RequestMapping("/api/v1/follows")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(maxAge = 3600)
public class FollowController {
    private final FollowService followService;

    @DeleteMapping("/{id}")
    public void delete(@PathVariable String id) {
        log.info("delete: id={}", id);
        followService.delete(id);
    }

    @PostMapping("/users")
    public void addFollower(@RequestBody AddUserDto user) {
        log.info("addFollower: user={}", user.toString());
        followService.addFollower(user);
    }
}
