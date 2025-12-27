package slt.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Service;
import slt.connectivity.strava.StravaActivityService;
import slt.database.ActivityRepository;
import slt.dto.ActivityDto;
import slt.mapper.ActivityMapper;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@Service
@AllArgsConstructor
public class ActivityService {

    private ActivityRepository activityRepository;
    private StravaActivityService stravaActivityService;

    private final ActivityMapper activityMapper = ActivityMapper.INSTANCE;

    public List<ActivityDto> postActivities(final Long userId, final LocalDate date, final List<ActivityDto> activities) {
        deleteOldActivities(userId, date, activities);
        saveActivities(userId, activities);
        final var newActivities = activityRepository.getAllActivities(userId, date);
        return activityMapper.map(newActivities);
    }

    public List<ActivityDto> getActivitiesForDay(final Long userId, final LocalDate date, boolean forceSync) {
        final var allActivities = activityRepository.getAllActivities(userId, date);
        final var extraStravaActivities = stravaActivityService.getExtraStravaActivities(allActivities, userId, date, forceSync);
        allActivities.addAll(extraStravaActivities);
        return activityMapper.map(allActivities);
    }

    public void deleteActivity(final Long userId, final Long activityId) {
        final var optionalActivity = activityRepository.findById(activityId);
        if (optionalActivity.isPresent()) {
            final var activity = optionalActivity.get();
            if (activity.getUserId().equals(userId)) {
                if (StringUtils.isNotEmpty(activity.getSyncedWith())) {
                    activity.setStatus(StravaActivityService.DELETED);
                    activityRepository.saveActivity(activity);
                } else {
                    activityRepository.deleteActivity(userId, activityId);
                }
            }
        }
    }

    private void deleteOldActivities(final Long userId, final LocalDate date, final List<ActivityDto> activities) {
        final var existingActivities = activityRepository.getAllActivities(userId, date);
        final var activityIds = activities.stream().map(ActivityDto::getId).toList();
        for (final var activity : existingActivities) {
            if (!activityIds.contains(activity.getId())) {
                activityRepository.deleteActivity(userId, activity.getId());
            }
        }
    }

    private void saveActivities(final Long userId, final List<ActivityDto> activities) {
        for (final var activityDto : activities) {
            final var activity = activityMapper.map(activityDto, userId);
            activityRepository.saveActivity(activity);
        }
    }
}
