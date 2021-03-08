package com.csu.be.forum.dao;

import com.csu.be.forum.entity.LoginTicket;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

/**
 * @author nql
 * @version 1.0
 * @date 2021/2/24 20:56
 */
@Mapper
public interface LoginTicketMapper {

    int insertLoginTicket(LoginTicket loginTicket);

    LoginTicket selectByTicket(String ticket);

    int updateStatus(String ticket, int status);
}
