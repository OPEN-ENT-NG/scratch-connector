package fr.openent.scratch.service;

import fr.wseduc.webutils.Either;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

public interface SessionService {

    /**
     * Add new session for user to open a file from Scratch
     *
     * @param documentId    document id to get
     * @param userId        id of the user
     * @param entId         id of the document in ENT
     * @param documentName  name of the document
     * @param canUpdate     ability to update the document
     * @param handler       Function handler returning data
     */
    void addSession(String documentId, String userId, String entId, String documentName, Boolean canUpdate,
                    Handler<Either<String, JsonObject>> handler);

    /**
     * Add new session for user to open a file from Scratch
     *
     * @param documentId    document id to get
     * @param userId        id of the user
     * @param entId         id of the document in ENT
     * @param documentName  name o the document
     * @param sessionId     id of the session
     * @param canUpdate     ability to update the document
     * @param handler       Function handler returning data
     */
    void addAllSessionInfos(String documentId, String userId, String entId, String documentName, String sessionId, Boolean canUpdate,
                            Handler<Either<String, JsonObject>> handler);

    /**
     * Add session_id to the specific session
     *
     * @param id            id of the session
     * @param sessionId     random sessionId pass by scratch
     * @param handler       Function handler returning data
     */
    void addSessionId(String id, String sessionId, Handler<Either<String, JsonObject>> handler);

    /**
     * get sessions infos to the specific session
     *
     * @param id        id of the session
     * @param handler   Function handler returning data
     */
    void getSessionInfos(String id, Handler<Either<String, JsonObject>> handler);

    /**
     * get sessions infos to the specific session
     *
     * @param id            id of the session
     * @param fileId        id of the file
     * @param documentName  name of the document
     * @param documentName  new Session id
     * @param handler       Function handler returning data
     */
    void updateFileInfos(String id, String fileId, String entId, String documentName, String newSessionId, Boolean canUpdate,
                         Handler<Either<String, JsonObject>> handler);


    /**
     * Delete old sessions (older than 24 hours)
     * @param handler function handler returning JsonObject data
     */
    void deleteOldSessions(Handler<Either<String, JsonArray>> handler);
}
