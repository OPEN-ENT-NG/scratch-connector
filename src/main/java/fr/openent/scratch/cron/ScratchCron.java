package fr.openent.scratch.cron;

import fr.openent.scratch.service.Impl.DefaultSessionService;
import fr.openent.scratch.service.SessionService;
import fr.wseduc.webutils.Either;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import org.entcore.common.controller.ControllerHelper;


public class ScratchCron extends ControllerHelper implements Handler<Long> {
    private static final Logger log = LoggerFactory.getLogger(ScratchCron.class);
    private final SessionService sessionService;

    public ScratchCron() {
        this.sessionService = new DefaultSessionService();
    }

    @Override
    public void handle(Long event) {
        log.info("[ScratchConnector@ScratchCron] ScratchCron started");

        deleteOldSessions(deleteEvent -> {
            if (deleteEvent.isLeft()) {
                log.info("[ScratchConnector@ScratchCron] ScratchCron failed");
            } else {
                log.info("[ScratchConnector@ScratchCron] ScratchCron launch successful");
            }
        });
    }

    public void deleteOldSessions(Handler<Either<String, JsonObject>> handler) {
        this.sessionService.deleteOldSessions(deleteSessionEvent -> {
            if(deleteSessionEvent.isLeft()) {
                log.error("[ScratchConnector@ScratchCron] Failed to delete old sessions for Scratch");
                handler.handle(new Either.Left<>(deleteSessionEvent.left().getValue()));
            } else {
                handler.handle(new Either.Right<>(new JsonObject()));
            }
        });
    }
}
