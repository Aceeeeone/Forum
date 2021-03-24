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
import com.qcloud.cos.COSClient;
import com.qcloud.cos.ClientConfig;
import com.qcloud.cos.auth.BasicCOSCredentials;
import com.qcloud.cos.auth.COSCredentials;
import com.qcloud.cos.model.PutObjectRequest;
import com.qcloud.cos.region.Region;
import org.apache.commons.lang3.StringUtils;
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

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
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

    @Value("${tencent.secret.id}")
    private String tencentSecretid;
    @Value("${tencent.secret.key}")
    private String tencentSecretKey;
    @Value("${tencent.region}")
    private String tencentRegion;
    @Value("${tencent.bucket}")
    private String tencentBucket;
    @Value("${tencent.community.url}")
    private String tencentUrl;

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

    private COSClient cosClient;

    @PostConstruct
    public void init() {
        // 1 初始化用户身份信息（secretId, secretKey）。
        COSCredentials cred = new BasicCOSCredentials(tencentSecretid, tencentSecretKey);
// 2 设置 bucket 的地域, COS 地域的简称请参照 https://cloud.tencent.com/document/product/436/6224
// clientConfig 中包含了设置 region, https(默认 http), 超时, 代理等 set 方法, 使用可参见源码或者常见问题 Java SDK 部分。
        Region region = new Region(tencentRegion);
        ClientConfig clientConfig = new ClientConfig(region);
// 3 生成 cos 客户端。
        cosClient = new COSClient(cred, clientConfig);
    }

    @RequestMapping(path = "/setting", method = RequestMethod.GET)
    @LoginRequired
    public String getSettingPage() {
        return "site/setting";
    }

    /**
     * 腾讯对象存储上传头像
     *
     * @param file
     * @return
     */
    @RequestMapping(value = "/upload", method = RequestMethod.POST)
    public String updateHeaderUrl(@RequestParam("file") MultipartFile file , Model model) throws IOException {
//        if (file == null || file.isEmpty() || "".equals(file.getOriginalFilename())) {
//            return ForumUtil.getJSONString(1, "文件不能为空！");
//        }
        if (file == null) {
            model.addAttribute("error", "您还没有选择图片！");
            return "/site/setting";
        }

        String fileName = file.getOriginalFilename();
        String suffix = fileName.substring(fileName.lastIndexOf("."));
        if (suffix == null) {
            model.addAttribute("error", "文件格式不正确！");
            return "site/setting";
        }

        //上传头像
        File toFile = null;

        InputStream ins = file.getInputStream();
        toFile = new File(file.getOriginalFilename());
        OutputStream os = new FileOutputStream(toFile);
        int bytesRead = 0;
        byte[] buffer = new byte[1024];
        while ((bytesRead = ins.read(buffer, 0, buffer.length)) != -1) {
            os.write(buffer, 0, bytesRead);
        }
        os.close();
        ins.close();
        String headerName = ForumUtil.generateUUID();
// 指定文件上传到 COS 上的路径，即对象键。例如对象键为folder/picture.jpg，则表示将文件 picture.jpg 上传到 folder 路径下
        PutObjectRequest putObjectRequest = new PutObjectRequest(tencentBucket, headerName, toFile);
        cosClient.putObject(putObjectRequest);
        toFile.delete();

        String newHeaderUrl = tencentUrl + "/" + headerName;
        userService.updateHeader(hostHolder.getUser().getId(), newHeaderUrl);
        return "redirect:/index";
    }

/*    @RequestMapping(path = "/upload", method = RequestMethod.POST)
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
            return "site/setting";
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
    }*/

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
            return "site/setting";
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

        return "site/profile";
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

        return "site/my-post";
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

        return "site/my-reply";
    }
}
