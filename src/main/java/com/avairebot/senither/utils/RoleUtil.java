package com.avairebot.senither.utils;

import net.dv8tion.jda.core.entities.Role;

import java.util.List;

public class RoleUtil {

    public static boolean hasRole(List<Role> roles, String name) {
        for (Role role : roles) {
            if (role.getName().equalsIgnoreCase(name)) {
                return true;
            }
        }
        return false;
    }
}
