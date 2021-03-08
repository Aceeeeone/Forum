package com.csu.be.forum.dao;

import com.csu.be.forum.entity.Message;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * @author nql
 * @version 1.0
 * @date 2021/3/4 17:30
 */
@Mapper
public interface MessageMapper {

    List<com.csu.be.forum.entity.Message> selectConversations(int userId, int offset, int limit);

    int selectConversationCount(int userId);

    List<com.csu.be.forum.entity.Message> selectLetters(String conversationId, int offset, int limit);

    int selectLetterCount(String conversationId);

    int selectLetterUnreadCount(int userId, String conversationId);

    int insertMessage(Message message);

    int updateStatus(List<Integer> ids, int status);
}
