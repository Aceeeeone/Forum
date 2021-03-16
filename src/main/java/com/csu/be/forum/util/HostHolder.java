package com.csu.be.forum.util;

import com.csu.be.forum.entity.User;
import org.springframework.stereotype.Component;

/**
 * @author nql
 * @version 1.0
 * @date 2021/2/25 23:27
 */
@Component
public class HostHolder {

    private ThreadLocal<User> threadLocal = new ThreadLocal<>();


    public void setUser(User user) {
        threadLocal.set(user);
    }

    public User getUser() {
        return threadLocal.get();
    }

    public void clear(){
        threadLocal.remove();
    }
}
