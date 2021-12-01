package fr.openent.scratch.service.Impl;

import fr.openent.scratch.service.StorageService;
import fr.wseduc.webutils.Either;
import io.vertx.core.Handler;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonObject;
import org.entcore.common.storage.Storage;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.UUID;

public class DefaultStorageService implements StorageService {

    private final Storage storage;

    public DefaultStorageService(Storage storage) {
        this.storage = storage;
    }

    @Override
    public void add(JsonObject body, Handler<Either<String, JsonObject>> handler) {
        byte[] byteContent;
        String contentType;

        if ("base64".equals(body.getString("format"))) { // Case file imported in Scratch from local computer
            String docName = body.getString("name");
            if (docName.contains(".")) {
                byteContent = Base64.getDecoder().decode(body.getString("content").getBytes(StandardCharsets.UTF_8));
                contentType = body.getString("content-type");
            } else {
                handler.handle(new Either.Left<>("[DefaultFileService@getFile] Filename does not contains extension : " + docName));
                return;
            }
        } else {
            handler.handle(new Either.Left<>("[DefaultFileService@getFile] File type unknown : " + body.getString("format")));
            return;
        }

        Buffer buffer = Buffer.buffer(byteContent);
        storage.writeBuffer(UUID.randomUUID().toString(), buffer, contentType, body.getString("name"), file -> {
            if ("ok".equals(file.getString("status"))) {
                handler.handle(new Either.Right<>(file));
            }
            else {
                handler.handle(new Either.Left<>("[DefaultFileService@add] Failed to upload file from http request"));
            }
        });
    }
}
