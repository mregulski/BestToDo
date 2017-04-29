package ppt.reshi.besttodo.model;

import java.io.Serializable;

/**
 * Created by Marcin Regulski on 25.04.2017.
 */

public class Tag implements Serializable {
    private Integer id;
    private String title;
    private String color;
    private Boolean isDefault;

    public Integer getId() {
        return id;
    }

    public Tag id(Integer id) {
        this.id = id;
        return this;
    }

    public String getTitle() {
        return title;
    }

    public Tag title(String title) {
        this.title = title;
        return this;
    }

    public String getColor() {
        return color;
    }

    public Tag color(String color) {
        this.color = color;
        return this;
    }

    public Boolean getDefault() {
        return isDefault;
    }

    public Tag setDefault(Boolean isDefault) {
        this.isDefault = isDefault;
        return this;
    }

    @Override
    public String toString() {
        return "Tag{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", color='" + color + '\'' +
                ", isDefault=" + isDefault +
                '}';
    }
}
