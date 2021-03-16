package com.csu.be.forum.util;

/**
 * @author nql
 * @version 1.0
 * @date 2020/2/23 0:11
 */
public interface ForumConstant {
    //激活成功
    int ACTIVATION_SUCCESS = 0;

    //激活重复
    int ACTIVATION_REPEAT = 1;

    //激活失败
    int ACTIVATION_FAILURE = 2;

    //默认状态登陆凭证超时时间
    int DEFAULT_EXPIRED_SECONDS = 3600 * 12;

    //记录下登陆凭证超时时间
    int REMEMBER_EXPIRED_SECONDS = 3600 * 24 * 30;

    //类型：帖子
    int ENTITY_TYPE_POST = 1;

    //类型：评论
    int ENTITY_TYPE_COMMENT = 2;

    //类型：用户
    int ENTITY_TYPE_User = 3;

    // 主题: 评论
    String TOPIC_COMMENT = "comment";

    //  主题: 点赞
    String TOPIC_LIKE = "like";

    // 主题: 关注
    String TOPIC_FOLLOW = "follow";

    // 系统用户ID
    int SYSTEM_USER_ID = 1;
}
