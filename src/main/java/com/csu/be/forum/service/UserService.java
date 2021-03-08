package com.csu.be.forum.service;

import com.csu.be.forum.dao.LoginTicketMapper;
import com.csu.be.forum.dao.UserMapper;
import com.csu.be.forum.entity.LoginTicket;
import com.csu.be.forum.entity.User;
import com.csu.be.forum.util.ForumConstant;
import com.csu.be.forum.util.ForumUtil;
import com.csu.be.forum.util.MailClient;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * @author nql
 * @version 1.0
 * @date 2021/1/31 23:15
 */
@Service
public class UserService implements ForumConstant {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private LoginTicketMapper loginTicketMapper;

    @Autowired
    private MailClient mailClient;

    @Autowired
    private TemplateEngine templateEngine;

    @Value("${server.servlet.context-path}")
    private String contextPath;

    @Value("${forum.path.domian}")
    private String domian;

    //查找用户
    public User findUserById(int id) {
        return userMapper.selectById(id);
    }

    //注册
    public Map<String, Object> register(User user) {
        Map<String, Object> map = new HashMap<>();

        // 空值处理
        if (user == null) {
            throw new IllegalArgumentException("参数不能为空！");
        }
        if (StringUtils.isBlank(user.getUsername())) {
            map.put("usernameMsg", "用户名不能为空");
            return map;
        }
        if (StringUtils.isBlank(user.getPassword())) {
            map.put("passwordMsg", "密码不能为空");
            return map;
        }
        if (StringUtils.isBlank(user.getEmail())) {
            map.put("emailMsg", "邮箱不能为空");
            return map;
        }

        // 验证重复
        User u = userMapper.selectByName(user.getUsername());
        if (u != null) {
            map.put("usernameMsg", "账号已存在！");
            return map;
        }

        u = userMapper.selectByEmail(user.getEmail());
        if (u != null) {
            map.put("emailMsg", "邮箱已注册！");
            return map;
        }

        // 注册用户
        user.setSalt(ForumUtil.generateUUID().substring(0, 5));
        user.setPassword(ForumUtil.MD5(user.getPassword() + user.getSalt()));
        user.setType(0);
        user.setStatus(0);
        user.setActivationCode(ForumUtil.generateUUID());
        user.setHeaderUrl(String.format("http://images.newcoder.com/head/%dt.png", new Random().nextInt(1000)));
        user.setCreateTime(new Date());
        userMapper.insertUser(user);

        // 激活邮件
        Context context = new Context();
        context.setVariable("email", user.getEmail());
        // http://localhost:8080/forum/activation/101/code
        String url = domian + contextPath + "/activation/" + user.getId() + "/" + user.getActivationCode();
        context.setVariable("url", url);
        String content = templateEngine.process("/mail/activation", context);
        mailClient.sendMail(user.getEmail(), "论坛账号激活", content);

        return map;
    }

    //激活
    public int activation(int userId, String code) {
        User user = userMapper.selectById(userId);
        if (user.getStatus() == 1) {
            return ForumConstant.ACTIVATION_REPEAT;
        } else if (user.getActivationCode().equals(code)) {
            userMapper.updateStatus(userId, 1);
            return ForumConstant.ACTIVATION_SUCCESS;
        } else {
            return ForumConstant.ACTIVATION_FAILURE;
        }
    }

    //登陆
    public Map<String, Object> login(String username, String password, int expiredSeconds) {
        Map<String, Object> map = new HashMap<>();

        //空值处理
        if (StringUtils.isBlank(username)) {
            map.put("usernameMsg", "账号不能为空！");
            return map;
        }
        if (StringUtils.isBlank(password)) {
            map.put("passwordMsg", "密码不能为空！");
            return map;
        }

        //验证账号
        User user = userMapper.selectByName(username);
        if (user == null) {
            map.put("usernameMsg", "账号不存在！");
            return map;
        }

        if (user.getStatus() == 0) {
            map.put("usernameMsg", "该账号未激活！");
            return map;
        }

        password = ForumUtil.MD5(password + user.getSalt());
        if (!user.getPassword().equals(password)) {
            map.put("passwordMsg", "密码错误！");
            return map;
        }

        //生成登陆凭证
        LoginTicket loginTicket = new LoginTicket();
        loginTicket.setUserId(user.getId());
        loginTicket.setTicket(ForumUtil.generateUUID());
        loginTicket.setStatus(0);
        loginTicket.setExpired(new Date(System.currentTimeMillis() + expiredSeconds * 1000));
        loginTicketMapper.insertLoginTicket(loginTicket);

        map.put("ticket", loginTicket.getTicket());
        return map;
    }

    //退出登陆
    public void logout(String ticket) {
        loginTicketMapper.updateStatus(ticket, 1);
    }

    //查找登陆凭证
    public LoginTicket findLoginTicket(String ticket){
        return loginTicketMapper.selectByTicket(ticket);
    }

    //更新头像
    public int updateHeader(int userId, String headerUrl){
        return userMapper.updateHeader(userId, headerUrl);
    }

    //更新密码
    public Map<String, Object> updatePassword(User user, String oldPassword, String newPassword){
        Map<String, Object> map = new HashMap<>();

        oldPassword = ForumUtil.MD5(oldPassword + user.getSalt());
        if (!user.getPassword().equals(oldPassword)) {
            map.put("oldPasError", "原密码不正确！");
            return map;
        }

        if (newPassword == null) {
            map.put("newPasError", "新密码不能为空！");
            return map;
        }

        int userId = user.getId();
        newPassword = ForumUtil.MD5(newPassword + user.getSalt());
        userMapper.updatePassword(userId, newPassword);

        return map;
    }


    public User findUserByName(String username) {
        return userMapper.selectByName(username);
    }
}
