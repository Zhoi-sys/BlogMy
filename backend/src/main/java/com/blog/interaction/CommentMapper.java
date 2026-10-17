package com.blog.interaction;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

/**
 * 评论数据访问。一级评论倒序分页，回复按对话顺序升序分页；
 * 批量窗口函数用于每条一级评论只取前 N 条回复，避免 N+1 查询。
 */
@Mapper
public interface CommentMapper {

    Comment selectById(@Param("id") Long id);

    List<Comment> selectRoots(@Param("articleId") Long articleId,
                              @Param("cursorTime") LocalDateTime cursorTime,
                              @Param("cursorId") Long cursorId,
                              @Param("limit") int limit);

    List<Comment> selectReplies(@Param("rootId") Long rootId,
                                @Param("cursorTime") LocalDateTime cursorTime,
                                @Param("cursorId") Long cursorId,
                                @Param("limit") int limit);

    List<Comment> selectPreviewReplies(@Param("parentIds") Collection<Long> parentIds,
                                       @Param("limit") int limit);

    List<ReplyCount> countReplies(@Param("parentIds") Collection<Long> parentIds);

    int insert(Comment comment);

    int updateDeleted(@Param("id") Long id, @Param("updatedAt") LocalDateTime updatedAt);

    int incrementLikeCount(@Param("id") Long id);

    int decrementLikeCount(@Param("id") Long id);

    /**
     * 回复数量投影。字段使用下划线别名，便于 MyBatis 自动映射。
     */
    class ReplyCount {
        private Long parentId;
        private Long replyCount;

        public Long getParentId() {
            return parentId;
        }

        public void setParentId(Long parentId) {
            this.parentId = parentId;
        }

        public Long getReplyCount() {
            return replyCount;
        }

        public void setReplyCount(Long replyCount) {
            this.replyCount = replyCount;
        }
    }
}
