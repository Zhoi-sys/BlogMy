package com.blog.interaction;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Collection;
import java.util.List;

/**
 * 点赞数据访问接口，SQL 见 {@code resources/mapper/LikeMapper.xml}。
 *
 * @since 1.0.0
 */
@Mapper
public interface LikeMapper {

    /**
     * 新增点赞记录并回填主键。
     *
     * @param like 点赞实体
     * @return 受影响行数；唯一索引冲突时抛出 DuplicateKeyException
     */
    int insert(Like like);

    /**
     * 删除点赞记录。
     *
     * @param userId     用户 ID
     * @param targetType 目标类型
     * @param targetId   目标 ID
     * @return 实际删除行数
     */
    int delete(@Param("userId") Long userId,
               @Param("targetType") int targetType,
               @Param("targetId") Long targetId);

    /**
     * 统计用户对指定目标的点赞记录数。
     *
     * @param userId     用户 ID
     * @param targetType 目标类型
     * @param targetId   目标 ID
     * @return 记录数，0 或 1
     */
    long countByUserAndTarget(@Param("userId") Long userId,
                              @Param("targetType") int targetType,
                              @Param("targetId") Long targetId);

    /**
     * 批量查询用户对一组目标的点赞记录。
     *
     * @param userId     用户 ID
     * @param targetType 目标类型
     * @param targetIds  目标 ID 集合
     * @return 点赞记录列表
     */
    List<Like> selectByUserAndTargets(@Param("userId") Long userId,
                                      @Param("targetType") int targetType,
                                      @Param("targetIds") Collection<Long> targetIds);
}
