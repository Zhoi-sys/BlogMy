package com.blog.interaction;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Collection;
import java.util.List;

@Mapper
public interface LikeMapper {

    int insert(Like like);

    int delete(@Param("userId") Long userId,
               @Param("targetType") int targetType,
               @Param("targetId") Long targetId);

    long countByUserAndTarget(@Param("userId") Long userId,
                              @Param("targetType") int targetType,
                              @Param("targetId") Long targetId);

    List<Like> selectByUserAndTargets(@Param("userId") Long userId,
                                      @Param("targetType") int targetType,
                                      @Param("targetIds") Collection<Long> targetIds);
}
