package com.blog.interaction;

import com.blog.common.api.PageResult;
import com.blog.common.util.CursorCodec;
import com.blog.common.util.PageResults;
import com.blog.interaction.dto.InteractionDtos;
import com.blog.security.UserPrincipal;
import com.blog.user.UserMapper;
import com.blog.user.UserViewMapper;
import com.blog.user.dto.UserDtos;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 评论读模型组装器。
 *
 * <p>集中处理评论树、回复预览、作者批量加载和点赞状态，让
 * {@link CommentService} 的实现只负责写入流程与权限判断。</p>
 *
 * @since 1.0.0
 */
@Component
public class CommentViewAssembler {

    /** 每条一级评论默认携带的回复预览数。 */
    private static final int PREVIEW_REPLY_COUNT = 3;

    private final CommentMapper commentMapper;
    private final UserMapper userMapper;
    private final UserViewMapper userViewMapper;
    private final LikeMapper likeMapper;
    private final CommentContentRenderer contentRenderer;
    private final CursorCodec cursorCodec;

    /**
     * 创建 {@code CommentViewAssembler} 实例，依赖由 Spring 注入。
     * @param commentMapper 依赖组件
     * @param userMapper 依赖组件
     * @param userViewMapper 依赖组件
     * @param likeMapper 依赖组件
     * @param contentRenderer 依赖组件
     * @param cursorCodec 依赖组件
     */
    public CommentViewAssembler(CommentMapper commentMapper, UserMapper userMapper, UserViewMapper userViewMapper, LikeMapper likeMapper, CommentContentRenderer contentRenderer, CursorCodec cursorCodec) {
        this.commentMapper = commentMapper;
        this.userMapper = userMapper;
        this.userViewMapper = userViewMapper;
        this.likeMapper = likeMapper;
        this.contentRenderer = contentRenderer;
        this.cursorCodec = cursorCodec;
    }

    /**
     * 组装一级评论分页，并为每条评论附加前三条回复。
     *
     * @param fetchedRows 查询结果，通常为 pageSize + 1 条
     * @param pageSize    每页条数
     * @param principal   当前用户，用于点赞状态，可为空
     * @return 一级评论分页
     */
    public PageResult<InteractionDtos.CommentView> assembleRoots(List<Comment> fetchedRows, int pageSize,
                                                                  UserPrincipal principal) {
        return PageResults.slice(fetchedRows, pageSize,
                root -> cursorCodec.encode(root.getCreatedAt(), root.getId()),
                roots -> buildRoots(roots, principal));
    }

    /**
     * 组装回复分页。
     *
     * @param fetchedRows 查询结果，通常为 pageSize + 1 条
     * @param pageSize    每页条数
     * @param principal   当前用户，用于点赞状态，可为空
     * @return 回复分页
     */
    public PageResult<InteractionDtos.CommentView> assembleReplies(List<Comment> fetchedRows, int pageSize,
                                                                    UserPrincipal principal) {
        return PageResults.slice(fetchedRows, pageSize,
                reply -> cursorCodec.encode(reply.getCreatedAt(), reply.getId()),
                replies -> buildSimple(replies, principal));
    }

    /**
     * 组装单条新评论视图。
     *
     * @param comment   评论实体
     * @param principal 当前用户
     * @return 评论视图
     */
    public InteractionDtos.CommentView assembleSingle(Comment comment, UserPrincipal principal) {
        ViewContext context = loadContext(List.of(comment), principal);
        return toView(comment, context, 0L, List.of());
    }

    /**
     * 构建一级评论视图，附加回复预览与回复总数。
     *
     * @param roots     一级评论列表
     * @param principal 当前用户
     * @return 评论视图列表
     */
    private List<InteractionDtos.CommentView> buildRoots(List<Comment> roots, UserPrincipal principal) {
        List<Long> rootIds = roots.stream().map(Comment::getId).toList();
        List<Comment> previewReplies = commentMapper.selectPreviewReplies(rootIds, PREVIEW_REPLY_COUNT);
        Map<Long, Long> replyCounts = commentMapper.countReplies(rootIds).stream()
                .collect(Collectors.toMap(CommentMapper.ReplyCount::getParentId,
                        reply -> reply.getReplyCount() == null ? 0L : reply.getReplyCount(),
                        (a, b) -> a));
        Map<Long, List<Comment>> previewsByRoot = previewReplies.stream()
                .collect(Collectors.groupingBy(Comment::getParentId,
                        LinkedHashMap::new, Collectors.toList()));

        List<Comment> all = new ArrayList<>(roots);
        all.addAll(previewReplies);
        ViewContext context = loadContext(all, principal);
        return roots.stream().map(root -> toView(
                root, context, replyCounts.getOrDefault(root.getId(), 0L),
                previewsByRoot.getOrDefault(root.getId(), List.of()).stream()
                        .map(reply -> toView(reply, context, 0L, List.of()))
                        .toList()
        )).toList();
    }

    /**
     * 构建不带回复预览的评论视图。
     *
     * @param comments  评论列表
     * @param principal 当前用户
     * @return 评论视图列表
     */
    private List<InteractionDtos.CommentView> buildSimple(List<Comment> comments, UserPrincipal principal) {
        ViewContext context = loadContext(comments, principal);
        return comments.stream()
                .map(comment -> toView(comment, context, 0L, List.of()))
                .toList();
    }

    /**
     * 批量加载评论作者、被回复人与当前用户点赞状态。
     *
     * @param comments  评论列表
     * @param principal 当前用户，可为空
     * @return 视图上下文
     */
    private ViewContext loadContext(List<Comment> comments, UserPrincipal principal) {
        if (comments.isEmpty()) {
            return new ViewContext(Map.of(), Set.of());
        }
        Set<Long> userIds = new LinkedHashSet<>();
        for (Comment comment : comments) {
            userIds.add(comment.getUserId());
            if (comment.getReplyToUserId() != null) {
                userIds.add(comment.getReplyToUserId());
            }
        }
        Map<Long, UserDtos.UserSummary> authors = userMapper.selectByIds(userIds).stream()
                .map(userViewMapper::toSummary)
                .collect(Collectors.toMap(UserDtos.UserSummary::id, Function.identity(),
                        (a, b) -> a, HashMap::new));

        Set<Long> likedIds = new HashSet<>();
        if (principal != null) {
            List<Long> commentIds = comments.stream().map(Comment::getId).toList();
            likedIds = likeMapper.selectByUserAndTargets(
                            principal.id(), LikeService.TARGET_COMMENT, commentIds)
                    .stream().map(Like::getTargetId).collect(Collectors.toSet());
        }
        return new ViewContext(authors, likedIds);
    }

    /**
     * 将评论实体转换为视图，隐藏已删除评论内容并保留占位。
     *
     * @param comment    评论实体
     * @param context    批量加载上下文
     * @param replyCount 回复总数
     * @param previews   回复预览
     * @return 评论视图
     */
    private InteractionDtos.CommentView toView(Comment comment, ViewContext context, long replyCount,
                                                List<InteractionDtos.CommentView> previews) {
        boolean deleted = comment.getDeleted() != null && comment.getDeleted() == 1;
        UserDtos.UserSummary author = deleted ? null : context.authors().get(comment.getUserId());
        UserDtos.UserSummary replyTo = comment.getReplyToUserId() == null
                ? null
                : context.authors().get(comment.getReplyToUserId());
        return new InteractionDtos.CommentView(
                comment.getId(),
                comment.getArticleId(),
                comment.getParentId(),
                author,
                deleted ? "" : contentRenderer.render(comment.getContent()),
                deleted,
                comment.getLikeCount() == null ? 0 : comment.getLikeCount(),
                !deleted && context.likedIds().contains(comment.getId()),
                comment.getReplyToUserId(),
                replyTo == null ? null : replyTo.nickname(),
                replyCount,
                previews,
                comment.getCreatedAt()
        );
    }

    /**
     * 评论视图的批量加载上下文。
     *
     * @param authors  用户 ID 到摘要的映射
     * @param likedIds 当前用户已点赞的评论 ID
     */
    private record ViewContext(
            Map<Long, UserDtos.UserSummary> authors,
            Set<Long> likedIds
    ) {
    }
}
