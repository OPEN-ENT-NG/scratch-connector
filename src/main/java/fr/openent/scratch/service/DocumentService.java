package fr.openent.scratch.service;

import fr.wseduc.webutils.Either;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;

public interface DocumentService {

    /**
     * Update file in workspace
     *
     * @param documentId    document id to update
     * @param uploaded      new file uploaded
     * @param handler       Function handler returning data
     */
    void update(String documentId, JsonObject uploaded, Handler<Either<String, JsonObject>> handler);

}
