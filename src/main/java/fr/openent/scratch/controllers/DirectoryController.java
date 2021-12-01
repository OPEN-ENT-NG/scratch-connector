package fr.openent.scratch.controllers;

import fr.openent.scratch.helper.ParametersHelper;
import fr.openent.scratch.models.Directory;
import fr.openent.scratch.models.File;
import fr.openent.scratch.security.AccessRight;
import fr.openent.scratch.service.DocumentService;
import fr.openent.scratch.service.Impl.DefaultDocumentService;
import fr.openent.scratch.utils.WorkspaceType;
import fr.wseduc.rs.*;
import fr.wseduc.security.ActionType;
import fr.wseduc.security.SecuredAction;
import fr.wseduc.webutils.request.RequestUtils;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import org.entcore.common.bus.WorkspaceHelper;
import org.entcore.common.controller.ControllerHelper;
import org.entcore.common.http.filter.ResourceFilter;
import org.entcore.common.storage.Storage;

import static org.entcore.common.http.response.DefaultResponseHandler.defaultResponseHandler;

public class DirectoryController extends ControllerHelper {
    private static final Logger log = LoggerFactory.getLogger(DirectoryController.class);
    private final DocumentService documentService;
    private final WorkspaceHelper workspaceHelper;

    public DirectoryController(EventBus eb, Storage storage) {
        super();
        documentService = new DefaultDocumentService(eb, storage);
        workspaceHelper = new WorkspaceHelper(eb, storage);
    }

    @Get("/directory/base")
    @ApiDoc("Get src directory")
    @ResourceFilter(AccessRight.class)
    @SecuredAction(value = "", type = ActionType.RESOURCE)
    public void getBaseDirectory(HttpServerRequest request) {
        String entId = request.getParam("ent_id");
        String userId = request.headers().get("User-Id");

        ParametersHelper.hasMissingParameters(new String[] {entId, userId}, handler -> {
            if (handler.isRight()) {
                if (!entId.trim().isEmpty()) {
                    badRequest(request, "[Scratch@getDirectoryBase] Ent id must be empty, not '" + entId + "'");
                }

                // Get documents from src Workspace
                JsonObject baseDrc = new Directory().getSourceDirectoryInfos();
                renderMeAndMyChildren(request, baseDrc, userId);
            }
            else {
                badRequest(request, "[Scratch@getBaseDirectory] " + handler.left().getValue());
            }
        });
    }

    @Get("/directory")
    @ApiDoc("Get a specific directory")
    @ResourceFilter(AccessRight.class)
    @SecuredAction(value = "", type = ActionType.RESOURCE)
    public void getDirectory(HttpServerRequest request) {
        String entId = request.getParam("ent_id");
        String userId = request.headers().get("User-Id");

        ParametersHelper.hasMissingParameters(new String[] {entId, userId}, handler -> {
            if (handler.isRight()) {
                workspaceHelper.getDocument(entId, getFolder -> {
                    if (getFolder.succeeded()) {
                        JsonObject result = getFolder.result().body().getJsonObject("result");
                        if (result != null) {
                            JsonObject directory = new Directory(getFolder.result().body().getJsonObject("result")).toJson();
                            renderMeAndMyChildren(request, directory, userId);
                        }
                        else {
                            badRequest(request, "[Scratch@getDirectory] No document found for entId : " + entId);
                        }
                    }
                    else {
                        badRequest(request, getFolder.cause().getMessage());
                    }
                });
            }
            else {
                badRequest(request, "[Scratch@getDirectory] " + handler.left().getValue());
            }
        });
    }

    @Post("/directory")
    @ApiDoc("Create a new directory")
    @ResourceFilter(AccessRight.class)
    @SecuredAction(value = "", type = ActionType.RESOURCE)
    public void createDirectory(HttpServerRequest request) {
        String userId = request.headers().get("User-Id");
        String userName = request.headers().get("User-Name");

        ParametersHelper.hasMissingParameters(new String[] {userId, userName}, handler -> {
            if (handler.isRight()) {
                RequestUtils.bodyToJson(request, body -> createFolder(request, body, userId, userName));
            }
            else {
                badRequest(request, "[Scratch@createDirectory] " + handler.left().getValue());
            }
        });
    }

    @Delete("/directory")
    @ApiDoc("Delete a specific directory")
    @ResourceFilter(AccessRight.class)
    @SecuredAction(value = "", type = ActionType.RESOURCE)
    public void deleteDirectory(HttpServerRequest request) {
        String entId = request.getParam("ent_id");
        String userId = request.headers().get("User-Id");

        ParametersHelper.hasMissingParameters(new String[] {entId, userId}, handler -> {
            if (handler.isRight()) {
                documentService.delete(entId, userId, defaultResponseHandler(request));
            }
            else {
                badRequest(request, "[Scratch@deleteDirectory] " + handler.left().getValue());
            }
        });
    }

    private void renderMeAndMyChildren(HttpServerRequest request, JsonObject directory, String userId) {
        documentService.list(directory.getString("id"), userId, listChildren -> {
            if (listChildren.isRight()) {
                JsonArray children = listChildren.right().getValue();

                // Fill content of directory
                for (Object child : children) {
                    JsonObject doc = (JsonObject) child;
                    if (doc.getString("eType").equals(WorkspaceType.FOLDER.getName())) {
                        directory.getJsonArray("content").add(new Directory(doc).toJson());
                    }
                    else {
                        directory.getJsonArray("content").add(new File(doc).toJson().put("content", ""));
                    }
                }

                renderJson(request, directory, 200);
            }
            else {
                log.error(listChildren.left().getValue());
                badRequest(request);
            }
        });
    }

    private void createFolder(HttpServerRequest request, JsonObject directoryInfos, String userId, String userName) {
        documentService.createFolder(directoryInfos, userId, userName, createFolder -> {
            if (createFolder.isRight()) {
                JsonObject directory = new Directory(createFolder.right().getValue()).toJson();
                renderJson(request, directory, 200);
            }
            else {
                log.error(createFolder.left().getValue());
                badRequest(request);
            }
        });
    }
}