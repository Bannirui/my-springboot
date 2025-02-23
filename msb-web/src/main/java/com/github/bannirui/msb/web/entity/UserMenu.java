package com.github.bannirui.msb.web.entity;

import com.github.bannirui.msb.web.filter.User;
import java.io.Serializable;
import java.util.List;

public class UserMenu implements Serializable {
    private User user;
    private List<FullMenu> fullMenus;

    public UserMenu(User user, List<FullMenu> fullMenus) {
        this.user = user;
        this.fullMenus = fullMenus;
    }

    public User getUser() {
        return this.user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public List<FullMenu> getFullMenus() {
        return this.fullMenus;
    }

    public void setFullMenus(List<FullMenu> fullMenus) {
        this.fullMenus = fullMenus;
    }
}
