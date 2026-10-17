package com.blog.notification;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

@Mapper
public interface NotificationMapper {

    int insert(Notification notification);

    List<Notification> selectByUser(@Param("userId") Long userId,
                                    @Param("unreadOnly") boolean unreadOnly,
                                    @Param("cursorTime") LocalDateTime cursorTime,
                                    @Param("cursorId") Long cursorId,
                                    @Param("limit") int limit);

    long countUnread(@Param("userId") Long userId);

    int markAllRead(@Param("userId") Long userId);

    int markRead(@Param("userId") Long userId, @Param("ids") Collection<Long> ids);
}
