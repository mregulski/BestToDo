package ppt.reshi.besttodo.model;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Marcin Regulski on 25.04.2017.
 */

public class Task implements Serializable {
    private Integer id;
    private String title;
    private String description;
    private DateTime deadline;
    private Boolean done;
    private List<Tag> tags;

    public Integer id() {
        return id;
    }

    public Task id(Integer id) {
        this.id = id;
        return this;
    }

    public String title() {
        return title;
    }

    public Task title(String title) {
        this.title = title;
        return this;
    }

    public String description() {
        return description;
    }

    public Task description(String description) {
        this.description = description;
        return this;
    }

    public DateTime deadline() {
        return deadline;
    }
    public String formattedDeadline() {
        return DateTimeFormat.longDateTime().print(deadline.toLocalDateTime());
    }
    public Task deadline(DateTime deadline) {
        this.deadline = deadline;
        return this;
    }

    public Boolean done() { return done; }

    public Task done(Boolean isDone) {
        done = isDone;
        return this;
    }

    public List<Tag> tags() {
        return tags;
    }

    public Task tags(List<Tag> tags) {
        this.tags = tags;
        return this;
    }

    public Task tag(Tag tag) {
        if (this.tags == null) {
            this.tags = new ArrayList<>();
        }
        this.tags.add(tag);
        return this;
    }

    @Override
    public String toString() {
        return "Task{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", description='" + description + '\'' +
                ", deadline=" + deadline +
                ", tags=" + tags +
                '}';
    }
}
