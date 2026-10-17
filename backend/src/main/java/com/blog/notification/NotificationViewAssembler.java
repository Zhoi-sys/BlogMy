package com.blog.notification;

import com.blog.article.Article;
import com.blog.article.ArticleMapper;
import com.blog.common.api.PageResult;
import com.blog.common.util.CursorCodec;
import com.blog.common.util.PageResults;
import com.blog.interaction.dto.InteractionDtos;
import com.blog.user.UserMapper;
import com.blog.user.UserViewMapper;
import com.blog.user.dto.UserDtos;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 通知读模型组装器：批量加载操作者与文章标题，避免通知列表 N+1 查询。
 */
@Component
public class NotificationViewAssembler {

    private final UserMapper userMapper;
    private final UserViewMapper userViewMapper;
    private final ArticleMapper articleMapper;
    private final CursorCodec cursorCodec;

    public NotificationViewAssembler(UserMapper userMapper, UserViewMapper userViewMapper,
                                     ArticleMapper articleMapper, CursorCodec cursorCodec) {
        this.userMapper = userMapper;
        this.userViewMapper = userViewMapper;
        this.articleMapper = articleMapper;
        this.cursorCodec = cursorCodec;
    }

    public PageResult<InteractionDtos.NotificationView> toPage(List<Notification> fetchedRows, int pageSize) {
        return PageResults.slice(fetchedRows, pageSize,
                item -> cursorCodec.encode(item.getCreatedAt(), item.getId()),
                this::toViews);
    }

    private List<InteractionDtos.NotificationView> toViews(List<Notification> rows) {
        Map<Long, UserDtos.UserSummary> actors = loadActors(rows);
        Map<Long, String> articleTitles = loadArticleTitles(rows);
        return rows.stream().map(item -> new InteractionDtos.NotificationView(
                item.getId(), item.getType(), actors.get(item.getActorId()),
                item.getArticleId(), articleTitles.get(item.getArticleId()),
                item.getCommentId(), item.getContentPreview(),
                item.getReadFlag() != null && item.getReadFlag() == 1,
                item.getCreatedAt()
        )).toList();
    }

    private Map<Long, UserDtos.UserSummary> loadActors(List<Notification> rows) {
        Set<Long> ids = rows.stream().map(Notification::getActorId)
                .collect(Collectors.toCollection(LinkedHashSet::new));
        if (ids.isEmpty()) {
            return Map.of();
        }
        return userMapper.selectByIds(ids).stream()
                .map(userViewMapper::toSummary)
                .collect(Collectors.toMap(UserDtos.UserSummary::id, Function.identity(),
                        (a, b) -> a, HashMap::new));
    }

    private Map<Long, String> loadArticleTitles(List<Notification> rows) {
        Set<Long> ids = rows.stream().map(Notification::getArticleId)
                .collect(Collectors.toCollection(LinkedHashSet::new));
        if (ids.isEmpty()) {
            return Map.of();
        }
        return articleMapper.selectByIds(ids).stream()
                .collect(Collectors.toMap(Article::getId, Article::getTitle, (a, b) -> a, HashMap::new));
    }
}
