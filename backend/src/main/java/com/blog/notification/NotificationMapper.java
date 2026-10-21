package com.blog.notification;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

/**
 * 通知数据访问接口，SQL 见 {@code resources/mapper/NotificationMapper.xml}。
 *
 * @since 1.0.0
 */
@Mapper
public interface NotificationMapper {

    /**
     * 新增通知并回填主键。
     *
     * @param notification 通知实体
     * @return 受影响行数
     */
    int insert(Notification notification);

    /**
     * 分页查询用户通知。
     *
     * @param userId     接收用户 ID
     * @param unreadOnly 是否只查询未读
     * @param cursorTime 上一页最后一条的创建时间
     * @param cursorId   上一页最后一条的通知 ID
     * @param limit      查询条数
     * @return 按创建时间倒序的通知列表
     */
    List<Notification> selectByUser(@Param("userId") Long userId,
                                    @Param("unreadOnly") boolean unreadOnly,
                                    @Param("cursorTime") LocalDateTime cursorTime,
                                    @Param("cursorId") Long cursorId,
                                    @Param("limit") int limit);

    /**
     * 统计用户未读通知数。
     *
     * @param userId 用户 ID
     * @return 未读数量
     */
    long countUnread(@Param("userId") Long userId);

    /**
     * 将用户全部未读通知标记为已读。
     *
     * @param userId 用户 ID
     * @return 实际更新行数
     */
    int markAllRead(@Param("userId") Long userId);

    /**
     * 将用户指定的通知标记为已读。
     *
     * @param userId 用户 ID
     * @param ids    通知 ID 集合
     * @return 实际更新行数
     */
    int markRead(@Param("userId") Long userId, @Param("ids") Collection<Long> ids);
}
