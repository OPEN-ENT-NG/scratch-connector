package fr.openent.scratch.service;

import fr.wseduc.webutils.Either;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;

public interface StorageService {
    /**
     * Add file in file system
     *
     * @param body      Body of the file to upload
     * @param handler   Function handler returning data
     */
    void add(JsonObject body, Handler<Either<String, JsonObject>> handler);

    /**
     * Remove file in file system
     *
     * @param storageId Id of the file to remove
     * @param handler   Function handler returning data
     */
    void remove(String storageId, Handler<Either<String, JsonObject>> handler);
}
