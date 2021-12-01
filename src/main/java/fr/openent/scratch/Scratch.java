package fr.openent.scratch;

import fr.openent.scratch.controllers.FileController;
import fr.openent.scratch.cron.ScratchCron;
import fr.wseduc.cron.CronTrigger;
import io.vertx.core.eventbus.EventBus;
import org.entcore.common.events.EventStore;
import org.entcore.common.events.EventStoreFactory;
import org.entcore.common.http.BaseServer;
import org.entcore.common.storage.Storage;
import org.entcore.common.storage.StorageFactory;

public class Scratch extends BaseServer {

	public static final String WORKSPACE_BUS_ADDRESS = "org.entcore.workspace";

	public static final String ACCESS_RIGHT = "scratch.access";
	public static String scratchSchema;
	public static String scratchURL;
	public static String scratchSessionDuration;

	public enum ScratchEvent { ACCESS, CREATE }

	@Override
	public void start() throws Exception {
		super.start();

		scratchSchema = config.getString("db-schema");
		scratchURL = config.getString("scratch-url");
		scratchSessionDuration = config.getString("scratch-session-duration", "1 DAY");
		final EventBus eb = getEventBus(vertx);
		final Storage storage = new StorageFactory(vertx, config).getStorage();

		EventStore eventStore = EventStoreFactory.getFactory().getEventStore(Scratch.class.getSimpleName());

		addController(new FileController(eb, storage, eventStore));

		// cron
		ScratchCron scratchCron = new ScratchCron();
		new CronTrigger(vertx, config.getString("scratch-cron", "0 */10 * * * ? *")).schedule(scratchCron);
	}
}
