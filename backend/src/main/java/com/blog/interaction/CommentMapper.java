package com.blog.interaction;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

/**
 * 评论数据访问接口。
 *
 * <p>一级评论倒序分页，回复按对话顺序升序分页；批量窗口函数用于每条一级评论
 * 只取前 N 条回复，避免 N+1 查询。SQL 见 {@code resources/mapper/CommentMapper.xml}。</p>
 *
 * @since 1.0.0
 */
@Mapper
public interface CommentMapper {

    /**
     * 按主键查询评论，包含已软删除评论。
     *
     * @param id 评论 ID
     * @return 评论实体；不存在时返回 {@code null}
     */
    Comment selectById(@Param("id") Long id);

    /**
     * 查询一级评论。
     *
     * @param articleId  文章 ID
     * @param cursorTime 上一页最后一条的创建时间
     * @param cursorId   上一页最后一条的评论 ID
     * @param limit      查询条数
     * @return 按创建时间倒序的一级评论
     */
    List<Comment> selectRoots(@Param("articleId") Long articleId,
                              @Param("cursorTime") LocalDateTime cursorTime,
                              @Param("cursorId") Long cursorId,
                              @Param("limit") int limit);

    /**
     * 查询一级评论下的回复。
     *
     * @param rootId     一级评论 ID
     * @param cursorTime 上一页最后一条的创建时间
     * @param cursorId   上一页最后一条的评论 ID
     * @param limit      查询条数
     * @return 按创建时间升序的回复
     */
    List<Comment> selectReplies(@Param("rootId") Long rootId,
                                @Param("cursorTime") LocalDateTime cursorTime,
                                @Param("cursorId") Long cursorId,
                                @Param("limit") int limit);

    /**
     * 批量查询每条一级评论的前 N 条回复。
     *
     * @param parentIds 一级评论 ID 集合
     * @param limit     每条评论最多返回的回复数
     * @return 回复列表，包含已软删除回复以保留上下文
     */
    List<Comment> selectPreviewReplies(@Param("parentIds") Collection<Long> parentIds,
                                       @Param("limit") int limit);

    /**
     * 批量统计一级评论的回复总数。
     *
     * @param parentIds 一级评论 ID 集合
     * @return 每个一级评论对应的回复数
     */
    List<ReplyCount> countReplies(@Param("parentIds") Collection<Long> parentIds);

    /**
     * 新增评论并回填自增主键。
     *
     * @param comment 评论实体
     * @return 受影响行数
     */
    int insert(Comment comment);

    /**
     * 将评论标记为软删除。
     *
     * @param id        评论 ID
     * @param updatedAt 更新时间
     * @return 受影响行数
     */
    int updateDeleted(@Param("id") Long id, @Param("updatedAt") LocalDateTime updatedAt);

    /**
     * 评论点赞数原子加一。
     *
     * @param id 评论 ID
     * @return 受影响行数
     */
    int incrementLikeCount(@Param("id") Long id);

    /**
     * 评论点赞数原子减一，最低为 0。
     *
     * @param id 评论 ID
     * @return 受影响行数
     */
    int decrementLikeCount(@Param("id") Long id);

    /**
     * 回复数量投影，字段使用下划线别名便于 MyBatis 自动映射。
     *
     * @since 1.0.0
     */
    class ReplyCount {
        /** 一级评论 ID。 */
        private Long parentId;

        /** 该一级评论下的回复总数。 */
        private Long replyCount;

        /**
         * 获取一级评论 ID。
         *
         * @return 一级评论 ID
         */
        public Long getParentId() {
            return parentId;
        }

        /**
         * 设置一级评论 ID。
         *
         * @param parentId 一级评论 ID
         */
        public void setParentId(Long parentId) {
            this.parentId = parentId;
        }

        /**
         * 获取回复数量。
         *
         * @return 回复数量
         */
        public Long getReplyCount() {
            return replyCount;
        }

        /**
         * 设置回复数量。
         *
         * @param replyCount 回复数量
         */
        public void setReplyCount(Long replyCount) {
            this.replyCount = replyCount;
        }
    }
}
