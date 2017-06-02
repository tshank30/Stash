package com.fst.apps.ftelematics.entities;

import com.joanzapata.iconify.IconDrawable;

/**
 * Created by welcome on 5/26/2016.
 */
public class ExpandedMenuModel {
    String iconName = "";
    IconDrawable icon;

    public String getIconName() {
        return iconName;
    }
    public void setIconName(String iconName) {
        this.iconName = iconName;
    }
    public IconDrawable getIconImg() {
        return icon;
    }
    public void setIconImg(IconDrawable icon) {
        this.icon = icon;
    }
}
