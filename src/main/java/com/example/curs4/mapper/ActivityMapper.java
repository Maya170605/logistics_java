package com.example.curs4.mapper;

import com.example.curs4.dto.ActivityDTO;
import com.example.curs4.entity.Activity;
import org.springframework.stereotype.Component;

@Component
public class ActivityMapper {

    public ActivityDTO toDto(Activity activity) {
        if (activity == null) {
            return null;
        }

        String userName = null;
        try {
            if (activity.getUser() != null) {
                userName = activity.getUser().getName();
                if (userName == null || userName.trim().isEmpty()) {
                    userName = activity.getUser().getUsername();
                }
                if (userName == null) {
                    userName = "Unknown User";
                }
            }
        } catch (Exception e) {
            // Если произошла ошибка при доступе к user, используем значение по умолчанию
            userName = "Unknown User";
        }

        return ActivityDTO.builder()
                .id(activity.getId())
                .userId(activity.getUser() != null ? activity.getUser().getId() : null)
                .userName(userName)
                .description(activity.getDescription())
                .activityDate(activity.getActivityDate())
                //.createdAt(activity.getCreatedAt())
                //.updatedAt(activity.getUpdatedAt())
                .build();
    }

    public Activity toEntity(ActivityDTO dto) {
        if (dto == null) {
            return null;
        }

        return Activity.builder()
                .id(dto.getId())
                .description(dto.getDescription())
                .activityDate(dto.getActivityDate() != null ? dto.getActivityDate() : java.time.LocalDateTime.now())
                .build();
    }
}