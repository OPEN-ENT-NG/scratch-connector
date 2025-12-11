package fr.openent.scratch;

import fr.openent.scratch.controllers.FileController;
import fr.openent.scratch.cron.ScratchCron;
import fr.wseduc.cron.CronTrigger;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.eventbus.EventBus;
import org.entcore.common.events.EventStore;
import org.entcore.common.events.EventStoreFactory;
import org.entcore.common.http.BaseServer;
import org.entcore.common.storage.Storage;
import org.entcore.common.storage.StorageFactory;

import java.text.ParseException;

import static io.vertx.core.Future.failedFuture;
import static io.vertx.core.Future.succeededFuture;

public class Scratch extends BaseServer {

	public static final String WORKSPACE_BUS_ADDRESS = "org.entcore.workspace";

	public static final String ACCESS_RIGHT = "scratch.access";
	public static String scratchSchema;
	public static String scratchURL;
	public static String scratchSessionDuration;

	public enum ScratchEvent { ACCESS, CREATE }

	@Override
	public void start(Promise<Void> startPromise) throws Exception {
		final Promise<Void> promise = Promise.promise();
		super.start(promise);
		promise.future()
			.compose(e -> initScratch())
			.onComplete(startPromise)
			.onFailure(th -> log.error("[Geogebra@Geogebra::start] Fail to start Geogebra", th))
			.onSuccess(e -> log.info("[Geogebra@Geogebra::start] Fail to start Geogebra"));
	}

	public Future<Void> initScratch() {
		scratchSchema = config.getString("db-schema");
		scratchURL = config.getString("scratch-url");
		scratchSessionDuration = config.getString("scratch-session-duration", "1 DAY");
		final EventBus eb = getEventBus(vertx);
		return StorageFactory.build(vertx, config)
		.compose(storageFactory -> {
			final Storage storage = storageFactory.getStorage();

			final EventStore eventStore = EventStoreFactory.getFactory().getEventStore(Scratch.class.getSimpleName());

			addController(new FileController(eb, storage, eventStore));

			// cron
			ScratchCron scratchCron = new ScratchCron();
            try {
                new CronTrigger(vertx, config.getString("scratch-cron", "0 */10 * * * ? *")).schedule(scratchCron);
            } catch (ParseException e) {
                return failedFuture(e);
            }
            return succeededFuture();
		});
	}
}
