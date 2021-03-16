package com.csu.be.forum.controller;

import com.csu.be.forum.annotation.LoginRequired;
import com.csu.be.forum.entity.User;
import com.csu.be.forum.service.FollowService;
import com.csu.be.forum.service.LikeService;
import com.csu.be.forum.service.UserService;
import com.csu.be.forum.util.ForumConstant;
import com.csu.be.forum.util.ForumUtil;
import com.csu.be.forum.util.HostHolder;
import org.apache.ibatis.annotations.Update;
import org.apache.tomcat.jni.Multicast;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Controller;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;

/**
 * @author nql
 * @version 1.0
 * @date 2020/2/26 2:39
 */
@Controller
@RequestMapping("/user")
public class UserController implements ForumConstant {

    public static final Logger logger = LoggerFactory.getLogger(UserController.class);

    @Value("${forum.path.upload}")
    private String uploadPath;

    @Value("${forum.path.domian}")
    private String domian;

    @Value("${server.servlet.context-path}")
    private String contextPath;

    @Autowired
    private UserService userService;

    @Autowired
    private LikeService likeService;

    @Autowired
    private FollowService followService;

    @Autowired
    private HostHolder hostHolder;

    @RequestMapping(path = "/setting", method = RequestMethod.GET)
    @LoginRequired
    public String getSettingPage() {
        return "/site/setting";
    }

    @RequestMapping(path = "/upload", method = RequestMethod.POST)
    @LoginRequired
    private String uploadHeader(MultipartFile headerImage, Model model) {
        if (headerImage == null) {
            model.addAttribute("error", "您还没有选择图片！");
            return "/site/setting";
        }

        String fileName = headerImage.getOriginalFilename();
        String suffix = fileName.substring(fileName.lastIndexOf("."));
        if (suffix == null) {
            model.addAttribute("error", "文件格式不正确！");
            return "/site/setting";
        }

        // 文件存放
        fileName = ForumUtil.generateUUID() + suffix;
        File dest = new File(uploadPath + "/" + fileName);
        try {
            headerImage.transferTo(dest);
        } catch (IOException e) {
            logger.error("上传文件失败" + e.getMessage());
            throw new RuntimeException("上传文件失败，服务器发展异常" + e.getMessage());
        }

        // 更新用户头像路径
        // http://localhost:8080/forum/user/header/xxx.png
        User user = hostHolder.getUser();
        String headUrl = domian + contextPath + "/user/header/" + fileName;
        userService.updateHeader(user.getId(), headUrl);

        return "redirect:/index";
    }

    @RequestMapping(path = "/header/{fileName}", method = RequestMethod.GET)
    public void getHeader(@PathVariable("fileName") String fileName, HttpServletResponse response) {
        // 服务器存放路径
        fileName = uploadPath + "/" + fileName;
        // 后缀处理
        String suffix = fileName.substring(fileName.indexOf("."));
        response.setContentType("image/" + suffix);
        try (
                OutputStream os = response.getOutputStream();
                FileInputStream fis = new FileInputStream(fileName);
        ) {
            byte[] buffer = new byte[1024];
            int b = 0;
            while ((b = fis.read(buffer)) != -1) {
                os.write(buffer, 0, b);
            }

        } catch (IOException e) {
            logger.error("读取图像失败" + e.getMessage());
        }
    }

    @RequestMapping(path = "/uploadpassword", method = RequestMethod.POST)
    @LoginRequired
    private String uploadHeader(String oldPassword, String newPassword, @CookieValue("ticket") String ticket, Model model) {

        User user = hostHolder.getUser();
        Map<String, Object> map = userService.updatePassword(user, oldPassword, newPassword);

        if (map == null || map.isEmpty()) {
            userService.logout(ticket);
            return "redirect:/login";
        } else {
            model.addAttribute("oldPasError", map.get("oldPasError"));
            model.addAttribute("newPasError", map.get("newPasError"));
            return "/site/setting";
        }
    }

    @RequestMapping(path = "/profile/{userId}", method = RequestMethod.GET)
    public String getProfilePage(@PathVariable("userId") int userId, Model model) {
        User user = userService.findUserById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }

        model.addAttribute("user", user);
        int likeCount = likeService.findUserLikeCount(userId);
        model.addAttribute("likeCount", likeCount);

        // 关注
        long followeeCount = followService.findFolloweeCount(userId, ENTITY_TYPE_User);
        model.addAttribute("followeeCount", followeeCount);
        // 粉丝
        long followerCount = followService.findFollowerCount(ENTITY_TYPE_User, userId);
        model.addAttribute("followerCount", followerCount);
        // 关注情况
        boolean hasFollowed = false;
        if (hostHolder.getUser() != null) {
            hasFollowed = followService.findUserHasFollowed(hostHolder.getUser().getId(), ENTITY_TYPE_User, userId);
        }
        model.addAttribute("hasFollowed", hasFollowed);

        return "/site/profile";
    }
}
