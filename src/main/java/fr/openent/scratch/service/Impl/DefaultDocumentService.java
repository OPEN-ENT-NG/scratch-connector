package fr.openent.scratch.service.Impl;

import fr.openent.scratch.Scratch;
import fr.openent.scratch.service.DocumentService;
import fr.wseduc.webutils.Either;
import io.vertx.core.Handler;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.json.JsonObject;

import static fr.wseduc.webutils.Utils.handlerToAsyncHandler;

public class DefaultDocumentService implements DocumentService {
    private final EventBus eb;

    public DefaultDocumentService(EventBus eb) {
        this.eb = eb;
    }

    @Override
    public void update(String documentId, JsonObject uploaded, Handler<Either<String, JsonObject>> handler) {
        JsonObject action = new JsonObject()
                .put("action", "updateDocument")
                .put("id", documentId)
                .put("uploaded", uploaded);
        ebHandling(action, handler);
    }

    private void ebHandling(JsonObject action, Handler<Either<String, JsonObject>> handler) {
        String WORKSPACE_BUS_ADDRESS = Scratch.WORKSPACE_BUS_ADDRESS;
        eb.request(WORKSPACE_BUS_ADDRESS, action, handlerToAsyncHandler(message -> {
            JsonObject body = message.body();
            if (!"ok".equals(body.getString("status"))) {
                String errorMessage = "[DefaultDocumentService@" + "update" + "] An error occurred when calling document by event bus : ";
                String errorEb = message.body().getString("message");
                handler.handle(new Either.Left<>(errorMessage + errorEb));
            } else {
                handler.handle(new Either.Right<>(message.body()));
            }
        }));
    }
}
