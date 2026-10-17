-- Portable MySQL 8 / H2 (MODE=MySQL) schema.
CREATE TABLE users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(32) NOT NULL,
    password VARCHAR(100) NOT NULL,
    nickname VARCHAR(32) NOT NULL,
    avatar_url VARCHAR(500) NOT NULL DEFAULT '',
    bio VARCHAR(255) NOT NULL DEFAULT '',
    role TINYINT NOT NULL DEFAULT 1,
    status TINYINT NOT NULL DEFAULT 1,
    created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    CONSTRAINT uk_users_username UNIQUE (username)
);

CREATE TABLE articles (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    author_id BIGINT NOT NULL,
    title VARCHAR(80) NOT NULL,
    summary VARCHAR(255) NOT NULL DEFAULT '',
    content_md MEDIUMTEXT NOT NULL,
    content_html MEDIUMTEXT NOT NULL,
    status TINYINT NOT NULL DEFAULT 0,
    view_count INT NOT NULL DEFAULT 0,
    like_count INT NOT NULL DEFAULT 0,
    comment_count INT NOT NULL DEFAULT 0,
    reading_minutes INT NOT NULL DEFAULT 1,
    publish_time DATETIME(3) NULL,
    created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    deleted TINYINT NOT NULL DEFAULT 0
);
CREATE INDEX idx_articles_feed ON articles (status, deleted, publish_time, id);
CREATE INDEX idx_articles_author ON articles (author_id, deleted, status, created_at);

CREATE TABLE article_tags (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    article_id BIGINT NOT NULL,
    tag_name VARCHAR(32) NOT NULL,
    created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    CONSTRAINT uk_article_tag UNIQUE (article_id, tag_name)
);
CREATE INDEX idx_article_tags_name ON article_tags (tag_name);

CREATE TABLE likes (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    target_type TINYINT NOT NULL,
    target_id BIGINT NOT NULL,
    created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    CONSTRAINT uk_likes_user_target UNIQUE (user_id, target_type, target_id)
);
CREATE INDEX idx_likes_target ON likes (target_type, target_id);

CREATE TABLE comments (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    article_id BIGINT NOT NULL,
    parent_id BIGINT NOT NULL DEFAULT 0,
    reply_to_user_id BIGINT NULL,
    content VARCHAR(2000) NOT NULL,
    like_count INT NOT NULL DEFAULT 0,
    status TINYINT NOT NULL DEFAULT 1,
    deleted TINYINT NOT NULL DEFAULT 0,
    created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3)
);
CREATE INDEX idx_comments_article ON comments (article_id, parent_id, deleted, created_at, id);
CREATE INDEX idx_comments_parent ON comments (parent_id, deleted, created_at, id);

CREATE TABLE mentions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    comment_id BIGINT NOT NULL,
    from_user_id BIGINT NOT NULL,
    to_user_id BIGINT NOT NULL,
    is_read TINYINT NOT NULL DEFAULT 0,
    created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    CONSTRAINT uk_mentions_comment_user UNIQUE (comment_id, to_user_id)
);
CREATE INDEX idx_mentions_to_user ON mentions (to_user_id, is_read);

CREATE TABLE notifications (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    type TINYINT NOT NULL,
    actor_id BIGINT NOT NULL,
    article_id BIGINT NOT NULL,
    comment_id BIGINT NOT NULL,
    content_preview VARCHAR(255) NOT NULL DEFAULT '',
    is_read TINYINT NOT NULL DEFAULT 0,
    created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3)
);
CREATE INDEX idx_notifications_user ON notifications (user_id, is_read, created_at, id);
