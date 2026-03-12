package fr.openent.scratch.controllers;

import fr.openent.scratch.cron.ScratchCron;
import fr.wseduc.rs.Post;
import fr.wseduc.security.ActionType;
import fr.wseduc.security.SecuredAction;
import fr.wseduc.webutils.http.BaseController;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.impl.logging.Logger;
import io.vertx.core.impl.logging.LoggerFactory;

public class TaskController extends BaseController {
	protected static final Logger log = LoggerFactory.getLogger(TaskController.class);

	final ScratchCron scratchCron;

	public TaskController(ScratchCron scratchCron) {
		this.scratchCron = scratchCron;
	}

	@Post("api/internal/scratch-cron")
	@SecuredAction(value = "", type = ActionType.RESOURCE)
	public void runScratchCron(HttpServerRequest request) {
		log.info("Triggered scratch task");
		scratchCron.handle(0L);
		render(request, null, 202);
	}
}
