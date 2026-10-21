package com.blog.interaction;

import org.apache.ibatis.annotations.Mapper;

/**
 * {@literal @} 提及关系数据访问接口，SQL 见 {@code resources/mapper/MentionMapper.xml}。
 *
 * @since 1.0.0
 */
@Mapper
public interface MentionMapper {

    /**
     * 新增 mention 关系。
     *
     * @param mention 提及关系实体
     * @return 受影响行数；重复关系会触发唯一索引冲突
     */
    int insert(Mention mention);
}
