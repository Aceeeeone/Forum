package com.csu.be.forum.controller;

import com.csu.be.forum.annotation.LoginRequired;
import com.csu.be.forum.entity.Comment;
import com.csu.be.forum.entity.DiscussPost;
import com.csu.be.forum.entity.Page;
import com.csu.be.forum.entity.User;
import com.csu.be.forum.service.*;
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
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author nql
 * @version 1.0
 * @date 2021/2/26 2:39
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
    private DiscussPostService discussPostService;

    @Autowired
    private CommentService commentService;

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

    @GetMapping("/profilepost/{userId}")
    public String getProfilePost(@PathVariable("userId") int userId, Model model, Page page) {
        User user = userService.findUserById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        model.addAttribute("user", user);

        int discussPostRows = discussPostService.findDiscussPostRows(userId);
        page.setRows(discussPostRows);
        page.setPath("/user/profilepost/" + userId);
        page.setLimit(5);

        List<DiscussPost> list = discussPostService.findDiscussPosts(userId, page.getOffset(), page.getLimit());
        List<Map<String, Object>> discussPosts = new ArrayList<>();
        if (list != null) {
            for (DiscussPost post : list) {
                Map<String, Object> map = new HashMap<>();
                map.put("post", post);
                long likeCount = likeService.findEntityLikeCount(ENTITY_TYPE_POST, post.getId());
                map.put("likeCount", likeCount);

                discussPosts.add(map);
            }
        }
        model.addAttribute("discussCount", discussPostRows);
        model.addAttribute("discussPosts", discussPosts);

        return "/site/my-post";
    }

    @GetMapping("/profilereply/{userId}")
    public String getProfileReply(@PathVariable("userId") int userId, Model model, Page page) {
        User user = userService.findUserById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        model.addAttribute("user", user);


        int commentCount = commentService.findUserCommentCount(userId);
        page.setRows(commentCount);
        page.setPath("/user/profilereply/" + userId);
        page.setLimit(5);

        List<Comment> list = commentService.findUserComments(userId, page.getOffset(), page.getLimit());
        List<Map<String, Object>> comments = new ArrayList<>();
        if (list != null) {
            for (Comment comment : list) {
                Map<String, Object> map = new HashMap<>();
                map.put("comment", comment);

                DiscussPost post;
                if (comment.getEntityType() == ENTITY_TYPE_COMMENT) {
                    int postId = commentService.findCommentById(comment.getEntityId()).getEntityId();
                    post = discussPostService.findDiscussPostById(postId);
                } else {
                    post = discussPostService.findDiscussPostById(comment.getEntityId());
                }

                map.put("post", post);

                comments.add(map);
            }
        }
        model.addAttribute("commentCount", commentCount);
        model.addAttribute("comments", comments);

        return "/site/my-reply";
    }
}
