package fr.openent.scratch.controllers;

import fr.openent.scratch.Scratch;
import fr.openent.scratch.helper.ParametersHelper;
import fr.openent.scratch.models.File;
import fr.openent.scratch.service.DocumentService;
import fr.openent.scratch.service.Impl.DefaultDocumentService;
import fr.openent.scratch.service.Impl.DefaultSessionService;
import fr.openent.scratch.service.Impl.DefaultStorageService;
import fr.openent.scratch.service.SessionService;
import fr.openent.scratch.service.StorageService;
import fr.wseduc.rs.ApiDoc;
import fr.wseduc.rs.Get;
import fr.wseduc.rs.Post;
import fr.wseduc.rs.Put;
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
import org.entcore.common.events.EventStore;
import org.entcore.common.storage.Storage;
import org.entcore.common.user.UserUtils;

import java.util.Arrays;
import java.util.Base64;

public class FileController extends ControllerHelper {
    private static final Logger log = LoggerFactory.getLogger(FileController.class);
    private final DocumentService documentService;
    private final StorageService storageService;
    private final WorkspaceHelper workspaceHelper;
    private final Storage storage;
    private final SessionService sessionService;
    private final EventStore eventStore;

    public FileController(EventBus eb, Storage storage, EventStore eventStore) {
        super();
        documentService = new DefaultDocumentService(eb);
        storageService = new DefaultStorageService(storage);
        workspaceHelper = new WorkspaceHelper(eb, storage);
        sessionService = new DefaultSessionService();
        this.eventStore = eventStore;
        this.storage = storage;
    }

    @Get("/open")
    @ApiDoc("Open a specific file")
    @SecuredAction(value = "", type = ActionType.AUTHENTICATED)
    public void openFile(HttpServerRequest request) {
        String entId = request.getParam("ent_id");

        UserUtils.getUserInfos(this.eb, request, user -> {
            if(user == null) {
                unauthorized(request, "[Scratch@openFile] Unauthorized access : authenticated users only");
                return;
            }
            final String userId = user.getUserId();
            if (entId == null) {
                sessionService.addSession(null, userId, null, null, null, event -> {
                    if (event.isRight()) {
                        redirect(request, Scratch.scratchURL, "#" + event.right().getValue().getInteger("id"));
                        eventStore.createAndStoreEvent(Scratch.ScratchEvent.ACCESS.name(), request);
                    } else {
                        badRequest(request, "[Scratch@openFile] problem to add user session in database : " + event.left().toString());
                    }
                });
            } else {
                ParametersHelper.hasMissingOrEmptyParameters(new String[]{entId}, handler -> {
                    if (handler.isRight()) {
                        workspaceHelper.getDocument(entId, getDocumentEvent -> {

                            if (getDocumentEvent.succeeded()) {
                                JsonObject document = getDocumentEvent.result().body().getJsonObject("result");
                                boolean canUpdate = true;
                                if (document != null) {
                                    if (!userId.equals(document.getString("owner"))) {
                                        boolean isShared = false;
                                        canUpdate = false;
                                        JsonArray shared = document.getJsonArray("shared", new JsonArray());
                                        for (Object userShared : shared) {
                                            JsonObject userJson = (JsonObject) userShared;
                                            if (userJson.getString("userId").equals(userId)) {
                                                isShared = true;
                                                canUpdate = userJson.containsKey("org-entcore-workspace-controllers-WorkspaceController|updateDocument") &&
                                                        userJson.getBoolean("org-entcore-workspace-controllers-WorkspaceController|updateDocument");
                                            }
                                        }
                                        if (!isShared) {
                                            unauthorized(request, "[Scratch@openFile] Unauthorized, the file is not shared to you");
                                            return;
                                        }
                                    }
                                    String fileid = document.getString("file");
                                    String documentName = document.getString("name");
                                    String fileExtension = documentName.substring(documentName.lastIndexOf("."));
                                    String[] validExtensions = {".sb3", ".sb2", ".sb"};
                                    boolean validExtension = Arrays.asList(validExtensions).contains(fileExtension);
                                    if (validExtension) {
                                        sessionService.addSession(fileid, userId, entId, documentName, canUpdate, event -> {
                                            if (event.isRight()) {
                                                redirect(request, Scratch.scratchURL, "#" + event.right().getValue().getInteger("id"));
                                                eventStore.createAndStoreEvent(Scratch.ScratchEvent.ACCESS.name(), request);
                                            } else {
                                                badRequest(request, "[Scratch@openFile] problem to add user session in database : " + event.left().toString());
                                            }
                                        });
                                    } else {
                                        badRequest(request, "[Scratch@openFile] The document doesn't have the valid extension for Scratch project : " + entId);
                                    }
                                } else {
                                    badRequest(request, "[Scratch@openFile] No document found for entId : " + entId);
                                }
                            } else {
                                badRequest(request, "[Scratch@openFile] Fail to get document from workspace : " + getDocumentEvent.cause().getMessage());
                            }
                        });
                    } else {
                        badRequest(request, "[Scratch@openFile] " + handler.left().getValue());
                    }
                });
            }
        });

    }

    @Get("/file")
    @ApiDoc("Get a specific file")
    @SecuredAction(value = "", type = ActionType.AUTHENTICATED)
    public void getFile(HttpServerRequest request) {
        String session_id = request.getParam("session_id");
        String id = request.getParam("id");
        sessionService.getSessionInfos(id, event -> {
            if (event.isRight()) {
                JsonObject res = event.right().getValue();
                //if sessionid not defined already
                if(!res.isEmpty() &&
                        (res.getValue("sessionid") == null || res.getString("sessionid","").equals(session_id))){
                    sessionService.addSessionId(id, session_id, addingEvent -> {
                        if (addingEvent.isRight()) {
                            String fileid = addingEvent.right().getValue().getString("fileid");
                            String documentname = addingEvent.right().getValue().getString("documentname");
                            if(fileid == null && documentname == null){
                                renderJson(request, new JsonObject().put("newFile",true));
                            } else {
                                workspaceHelper.readFile(fileid, content -> {
                                    if (content != null) {
                                        String base64File = Base64.getEncoder().encodeToString(content.getBytes());
                                        renderJson(request, new JsonObject().put("base64File", base64File).put("title", documentname));
                                    } else {
                                        badRequest(request, "[Scratch@openFile] No file found in storage for id : " + id);
                                    }
                                });
                            }
                        } else {
                            badRequest(request, "[Scratch@getFile] problem to add sessionid in database : " + event.left().toString());
                        }
                    });
                }else{
                    unauthorized(request, "[Scratch@getFile] Unauthorized access : a session is already set");
                }
            }
            else {
                badRequest(request, "[Scratch@getFile] problem to get infos of the session in database : " + event.left().toString());
            }
        });
    }

    @Post("/file")
    @ApiDoc("Create a new file")
    @SecuredAction(value = "", type = ActionType.AUTHENTICATED)
    public void createFile(HttpServerRequest request) {
        RequestUtils.bodyToJson(request, body -> {
            String old_session_id = body.getString("old_session_id");
            String new_session_id = body.getString("session_id");
            String id = body.getString("id");
            sessionService.getSessionInfos(id, event -> {
                if (event.isRight()) {
                    JsonObject res = event.right().getValue();
                    String sessionId = res.getString("sessionid","");
                    final String userId = res.getString("userid","");
                    //if session defined
                    if (sessionId.equals(old_session_id)) {
                        UserUtils.getUserInfos(eb, userId, user -> {
                            if(user == null){
                                unauthorized(request, "[Scratch@createFile] Unauthorized access : cannot fin the user");
                                return;
                            }
                            storageService.add(body, addFileEvent -> {
                                if (addFileEvent.isRight()) {
                                    JsonObject storageEntries = addFileEvent.right().getValue();
                                    String name = body.getString("name");
                                    String application = config.getString("app-name");
                                    workspaceHelper.addDocument(storageEntries, user, name, application, false, null, createEvent -> {
                                        if (createEvent.succeeded()) {
                                            JsonObject doc = createEvent.result().body();
                                            JsonObject file = new File(doc).toJson();

                                            sessionService.addAllSessionInfos(doc.getString("file"), user.getUserId(),
                                                    doc.getString("_id"), name, new_session_id, true, eventAdding -> {
                                                if(eventAdding.isRight()){
                                                    Integer newId = eventAdding.right().getValue().getInteger("id");
                                                    String parentId = body.getString("parent_id");
                                                    // If parent is base directory there's no need to move the document
                                                    if (parentId == null || parentId.isEmpty()) {
                                                        renderJson(request, new JsonObject().put("newId",newId), 200);
                                                        eventStore.createAndStoreEvent(Scratch.ScratchEvent.CREATE.name(), request);
                                                    }
                                                    else {
                                                        workspaceHelper.moveDocument(file.getString("id"), parentId, user, moveEvent -> {
                                                            if (moveEvent.succeeded()) {
                                                                renderJson(request, new JsonObject().put("newId",newId), 200);
                                                                eventStore.createAndStoreEvent(Scratch.ScratchEvent.CREATE.name(), request);
                                                            }
                                                            else {
                                                                badRequest(request, "[Scratch@createFile] Failed to move a workspace document : " + moveEvent.cause().getMessage());
                                                            }
                                                        });
                                                    }
                                                }else{
                                                    badRequest(request, "[Scratch@createFile] Failed to adding all session Infos : " + eventAdding.left());
                                                }
                                            });
                                        }
                                        else {
                                            badRequest(request, "[Scratch@createFile] Failed to create a workspace document : " + createEvent.cause().getMessage());
                                        }
                                    });
                                }
                                else {
                                    badRequest(request, "[Scratch@createFile] Failed to create a new entry in the storage");
                                }
                            });
                        });
                    } else {
                        unauthorized(request, "[Scratch@updateFile] Unauthorized updating : sessionid not match");
                    }
                } else {
                    badRequest(request, "[Scratch@getFile] problem to get infos of the session in database : " + event.left().toString());
                }
            });
        });
    }

    @Put("/file")
    @ApiDoc("Update a specific file")
    @SecuredAction(value = "", type = ActionType.AUTHENTICATED)
    public void updateFile(HttpServerRequest request) {
        RequestUtils.bodyToJson(request, body -> {
            String old_session_id = body.getString("old_session_id");
            String new_session_id = body.getString("session_id");
            String id = body.getString("id");
            sessionService.getSessionInfos(id, event -> {
                if (event.isRight()) {
                    JsonObject res = event.right().getValue();
                    String sessionId = res.getString("sessionid","");
                    boolean canUpdate = res.getBoolean("canupdate",false);
                    //if sessionid not defined already
                    if (sessionId.equals(old_session_id)) {
                        if(canUpdate) {
                            storageService.add(body, addFileEvent -> {
                                if (addFileEvent.isRight()) {
                                    JsonObject uploaded = addFileEvent.right().getValue();
                                    String entId = res.getString("entid", "");
                                    documentService.update(entId, uploaded, updateEvent -> {
                                        if (updateEvent.isRight()) {
                                            workspaceHelper.getDocument(entId, getDocumentEvent -> {
                                                if (getDocumentEvent.succeeded()) {
                                                    JsonObject document = getDocumentEvent.result().body().getJsonObject("result");
                                                    if (document != null) {
                                                        JsonObject file = new File(document).toJson();
                                                        String fileid = document.getString("file");
                                                        String documentName = document.getString("name");
                                                        sessionService.updateFileInfos(id, fileid, documentName, new_session_id, update -> {
                                                            renderJson(request, file);
                                                        });
                                                    } else {
                                                        badRequest(request, "[Scratch@getFile] No document found for entId : " + entId);
                                                    }
                                                } else {
                                                    badRequest(request, "[Scratch@updateFile] Fail to get document from workspace : " + getDocumentEvent.cause().getMessage());
                                                }
                                            });
                                        } else {
                                            log.error("[Scratch@updateFile] Failed to update document with new storage id : " + updateEvent.left().getValue());
                                            storage.removeFile(uploaded.getString("_id"), eventDelete -> {
                                                if (!"ok".equals(eventDelete.getString("status"))) {
                                                    log.error("[Scratch@updateFile] Error removing file " + uploaded.getString("_id") + " : " + eventDelete.getString("message"));
                                                }
                                                badRequest(request);
                                            });
                                        }
                                    });
                                } else {
                                    badRequest(request, addFileEvent.left().getValue());
                                }
                            });
                        } else {
                            unauthorized(request, "[Scratch@updateFile] Unauthorized updating : the user doesn't have the right to update the file");
                        }
                    } else {
                        unauthorized(request, "[Scratch@updateFile] Unauthorized updating : sessionid not match");
                    }
                } else {
                    badRequest(request, "[Scratch@getFile] problem to get infos of the session in database : " + event.left().toString());
                }
            });
        });
    }
}
