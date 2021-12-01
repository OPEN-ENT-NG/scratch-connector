package fr.openent.scratch.models;

import fr.openent.scratch.helper.DateHelper;
import io.vertx.core.json.JsonObject;

public class File {

    public String id;
    public String name;
    public String extension;
    public String last_modified;
    public String created;
    public String type;
    public String format;
    public String mimetype;
    public boolean writable;

    public File(JsonObject document) {
        this.id = document.getString("_id");
        this.name = document.getString("name");
        this.type = "file";
        this.format = "base64";
        this.extension = "sb3";
        this.mimetype = "application/octet-stream";
        this.writable = true;
        this.last_modified = DateHelper.tryFormat(document.getString("modified"));
        this.created = DateHelper.tryFormat(document.getString("created"));
    }

    public File() {}

    public JsonObject toJson() {
        return new JsonObject()
                .put("id", this.id)
                .put("name", this.name)
                .put("last_modified", this.last_modified)
                .put("created", this.created)
                .put("type", this.type)
                .put("format", this.format)
                .put("mimetype", this.mimetype)
                .put("writable", this.writable);
    }
}
