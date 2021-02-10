package com.csu.be.forum.dao;

import com.csu.be.forum.entity.DiscussPost;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author nql
 * @version 1.0
 * @date 2021/1/20 16:49
 */
@Mapper
public interface DiscussPostMapper {

    List<DiscussPost> selectDiscussPosts(int userId, int offset, int limit);

    int selectDiscussRows(@Param("userId") int userId);
}
