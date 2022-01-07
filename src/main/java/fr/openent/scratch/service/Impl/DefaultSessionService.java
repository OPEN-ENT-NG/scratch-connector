package fr.openent.scratch.service.Impl;

import fr.openent.scratch.Scratch;
import fr.openent.scratch.service.SessionService;
import fr.wseduc.webutils.Either;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import org.entcore.common.sql.Sql;
import org.entcore.common.sql.SqlResult;

public class DefaultSessionService implements SessionService {

    public DefaultSessionService() {
    }

    @Override
    public void addSession(String documentId, String userId, String entId, String documentName, Boolean canUpdate,
                           Handler<Either<String, JsonObject>> handler) {
        JsonArray params = new fr.wseduc.webutils.collections.JsonArray();
        String query;

        if(documentId != null && entId != null && documentName != null) {

            query = "" +
                    "INSERT INTO " + Scratch.scratchSchema + ".sessions " +
                    "( fileid, userid, entid, documentname, canupdate ) VALUES " +
                    "( ?, ?, ?, ?, ? ) RETURNING id;";
            params.add(documentId).add(userId).add(entId).add(documentName).add(canUpdate);
        } else {
            query = "" +
                    "INSERT INTO " + Scratch.scratchSchema + ".sessions " +
                    "( userid ) VALUES " +
                    "( ? ) RETURNING id;";
            params.add(userId);
        }

        Sql.getInstance().prepared(query, params, SqlResult.validUniqueResultHandler(handler));
    }

    @Override
    public void addAllSessionInfos(String documentId, String userId, String entId, String documentName, String sessionId,
                                   Boolean canUpdate, Handler<Either<String, JsonObject>> handler) {
        JsonArray params;

        String query = "" +
                "INSERT INTO " + Scratch.scratchSchema + ".sessions " +
                "( fileid, userid, entid, documentname, sessionid, canupdate ) VALUES " +
                "( ?, ?, ?, ?, ?, ? ) RETURNING id;";
        params = new fr.wseduc.webutils.collections.JsonArray();

        params.add(documentId).add(userId).add(entId).add(documentName).add(sessionId).add(canUpdate);

        Sql.getInstance().prepared(query, params, SqlResult.validUniqueResultHandler(handler));
    }

    @Override
    public void getSessionInfos(String id, Handler<Either<String, JsonObject>> handler) {
        JsonArray params;

        String query = "" +
                "SELECT * FROM " + Scratch.scratchSchema + ".sessions " +
                "WHERE id = ?;";

        params = new fr.wseduc.webutils.collections.JsonArray();

        params.add(id);

        Sql.getInstance().prepared(query, params, SqlResult.validUniqueResultHandler(handler));
    }

    @Override
    public void addSessionId(String id, String sessionId, Handler<Either<String, JsonObject>> handler) {
        JsonArray params;

        String query = "" +
                "UPDATE " + Scratch.scratchSchema + ".sessions " +
                "SET sessionid = ? WHERE id = ? RETURNING fileid, documentname;";

        params = new fr.wseduc.webutils.collections.JsonArray();

        params.add(sessionId).add(id);

        Sql.getInstance().prepared(query, params, SqlResult.validUniqueResultHandler(handler));
    }

    @Override
    public void updateFileInfos(String id, String fileId, String entId, String documentName, String newSessionId, Boolean canUpdate,
                                Handler<Either<String, JsonObject>> handler) {
        JsonArray params;

        String query = "" +
                "UPDATE " + Scratch.scratchSchema + ".sessions " +
                "SET fileid = ?, documentname = ?, sessionid = ? ";
        if(entId != null && canUpdate != null){
            query += ", entid = ?, canupdate = ? ";
        }

        query += "WHERE id = ?;";

        params = new fr.wseduc.webutils.collections.JsonArray();

        params.add(fileId).add(documentName).add(newSessionId);

        if(entId != null && canUpdate != null){
            params.add(entId).add(canUpdate);
        }

        params.add(id);

        Sql.getInstance().prepared(query, params, SqlResult.validUniqueResultHandler(handler));
    }

    @Override
    public void deleteOldSessions(Handler<Either<String, JsonArray>> handler) {
        String query = "DELETE FROM " + Scratch.scratchSchema + ".sessions s WHERE s.created::date < (NOW() - INTERVAL '"
                + Scratch.scratchSessionDuration + "');";
        JsonArray params = new JsonArray();
        Sql.getInstance().prepared(query, params, SqlResult.validResultHandler(handler));
    }
}
