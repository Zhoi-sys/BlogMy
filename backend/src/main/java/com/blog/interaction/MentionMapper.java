package com.blog.interaction;

import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface MentionMapper {

    int insert(Mention mention);
}
