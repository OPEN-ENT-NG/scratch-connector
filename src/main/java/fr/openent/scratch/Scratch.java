package fr.openent.scratch;

import fr.openent.scratch.controllers.DirectoryController;
import fr.openent.scratch.controllers.FileController;
import fr.openent.scratch.controllers.ManagerController;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.json.JsonArray;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import org.entcore.common.http.BaseServer;
import org.entcore.common.storage.Storage;
import org.entcore.common.storage.StorageFactory;

public class Scratch extends BaseServer {
	private static final Logger log = LoggerFactory.getLogger(Scratch.class);

	public static final String WORKSPACE_BUS_ADDRESS = "org.entcore.workspace";
	public static final String WORKSPACE_SRC_DIRECTORY_NAME = "Base";

	public static final String ACCESS_RIGHT = "scratch.access";

	@Override
	public void start() throws Exception {
		super.start();

		final EventBus eb = getEventBus(vertx);
		final Storage storage = new StorageFactory(vertx, config).getStorage();

		addController(new DirectoryController(eb, storage));
		addController(new FileController(eb, storage));
		addController(new ManagerController(eb, storage));
	}

}
