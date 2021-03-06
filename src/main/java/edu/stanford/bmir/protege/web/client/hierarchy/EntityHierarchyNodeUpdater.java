package edu.stanford.bmir.protege.web.client.hierarchy;

import com.google.gwt.core.client.GWT;
import edu.stanford.bmir.protege.web.shared.event.BrowserTextChangedEvent;
import edu.stanford.bmir.protege.web.shared.event.EntityDeprecatedChangedEvent;
import edu.stanford.bmir.protege.web.shared.event.WebProtegeEventBus;
import edu.stanford.bmir.protege.web.shared.hierarchy.EntityHierarchyNode;
import edu.stanford.bmir.protege.web.shared.issues.CommentPostedEvent;
import edu.stanford.bmir.protege.web.shared.issues.DiscussionThreadStatusChangedEvent;
import edu.stanford.bmir.protege.web.shared.project.ProjectId;
import edu.stanford.bmir.protege.web.shared.watches.Watch;
import edu.stanford.bmir.protege.web.shared.watches.WatchAddedEvent;
import edu.stanford.bmir.protege.web.shared.watches.WatchRemovedEvent;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.inject.Inject;
import java.util.HashSet;
import java.util.Set;

import static com.google.common.base.Preconditions.checkNotNull;
import static edu.stanford.bmir.protege.web.shared.event.BrowserTextChangedEvent.ON_BROWSER_TEXT_CHANGED;
import static edu.stanford.bmir.protege.web.shared.event.EntityDeprecatedChangedEvent.ON_ENTITY_DEPRECATED;
import static edu.stanford.bmir.protege.web.shared.issues.CommentPostedEvent.ON_COMMENT_POSTED;
import static edu.stanford.bmir.protege.web.shared.issues.DiscussionThreadStatusChangedEvent.ON_STATUS_CHANGED;
import static edu.stanford.bmir.protege.web.shared.watches.WatchAddedEvent.ON_WATCH_ADDED;
import static edu.stanford.bmir.protege.web.shared.watches.WatchRemovedEvent.ON_WATCH_REMOVED;

/**
 * Matthew Horridge Stanford Center for Biomedical Informatics Research 30 Nov 2017
 */
public class EntityHierarchyNodeUpdater {

    @Nonnull
    private final ProjectId projectId;

    @Nullable
    private EntityHierarchyModel model;

    @Inject
    public EntityHierarchyNodeUpdater(@Nonnull ProjectId projectId) {
        this.projectId = checkNotNull(projectId);
    }

    /**
     * Start listening for events on the specified event bus in order to keep the specified hierarchy
     * up to date.
     * @param eventBus The event bus on which project changes are broadcast.
     * @param model The hierarchy model that will be kept up to date.
     */
    public void start(@Nonnull WebProtegeEventBus eventBus,
                      @Nonnull EntityHierarchyModel model) {
        GWT.log("[EntityHierarchyNodeUpdater] Starting to listen for events");
        this.model = checkNotNull(model);
        eventBus.addProjectEventHandler(projectId, ON_WATCH_ADDED, this::handleWatchAdded);
        eventBus.addProjectEventHandler(projectId, ON_WATCH_REMOVED, this::handleWatchRemoved);
        eventBus.addProjectEventHandler(projectId, ON_BROWSER_TEXT_CHANGED, this::handleBrowserTextChanged);
        eventBus.addProjectEventHandler(projectId, ON_COMMENT_POSTED, this::handleCommentPosted);
        eventBus.addProjectEventHandler(projectId, ON_STATUS_CHANGED, this::handleDiscussionThreadStatusChanged);
        eventBus.addProjectEventHandler(projectId, ON_ENTITY_DEPRECATED, this::handleEntityDeprecatedChanged);
    }

    private void handleBrowserTextChanged(BrowserTextChangedEvent event) {
        if (model == null) {
            throw createHierarchyModelIsNullException();
        }
        model.getHierarchyNode(event.getEntity()).ifPresent(node -> {
            EntityHierarchyNode updatedNode = new EntityHierarchyNode(
                    node.getEntity(),
                    event.getNewBrowserText(),
                    node.isDeprecated(),
                    node.getWatches(),
                    node.getOpenCommentCount());
            model.updateNode(updatedNode);
        });
    }

    private void handleWatchAdded(WatchAddedEvent event) {
        if (model == null) {
            throw createHierarchyModelIsNullException();
        }
        model.getHierarchyNode(event.getWatch().getEntity()).ifPresent(node -> {
            Set<Watch> updatedWatches = new HashSet<>(node.getWatches());
            updatedWatches.add(event.getWatch());
            updateWatches(node, updatedWatches);
        });
    }

    private void handleWatchRemoved(WatchRemovedEvent event) {
        if (model == null) {
            throw createHierarchyModelIsNullException();
        }
        model.getHierarchyNode(event.getWatch().getEntity()).ifPresent(node -> {
            Set<Watch> updatedWatches = new HashSet<>(node.getWatches());
            updatedWatches.remove(event.getWatch());
            updateWatches(node, updatedWatches);
        });
    }

    private void updateWatches(EntityHierarchyNode node, Set<Watch> updatedWatches) {
        if (model == null) {
            throw createHierarchyModelIsNullException();
        }
        EntityHierarchyNode updatedNode = new EntityHierarchyNode(
                node.getEntity(),
                node.getBrowserText(),
                node.isDeprecated(),
                updatedWatches,
                node.getOpenCommentCount());
        model.updateNode(updatedNode);
    }

    private void handleCommentPosted(CommentPostedEvent event) {
        if (model == null) {
            throw createHierarchyModelIsNullException();
        }
        event.getEntity().ifPresent(entity -> {
            model.getHierarchyNode(entity.getEntity()).ifPresent(node -> {
                EntityHierarchyNode updatedNode = new EntityHierarchyNode(
                        node.getEntity(),
                        node.getBrowserText(),
                        node.isDeprecated(),
                        node.getWatches(),
                        event.getOpenCommentCountForEntity());
                model.updateNode(updatedNode);
            });
        });
    }

    private void handleDiscussionThreadStatusChanged(DiscussionThreadStatusChangedEvent event) {
        if (model == null) {
            throw createHierarchyModelIsNullException();
        }
        event.getEntity().ifPresent(entity -> {
            model.getHierarchyNode(entity).ifPresent(node -> {
                EntityHierarchyNode updatedNode = new EntityHierarchyNode(
                        node.getEntity(),
                        node.getBrowserText(),
                        node.isDeprecated(),
                        node.getWatches(),
                        event.getOpenCommentsCountForEntity());
                model.updateNode(updatedNode);
            });
        });
    }

    private void handleEntityDeprecatedChanged(EntityDeprecatedChangedEvent event) {
        if (model == null) {
            throw createHierarchyModelIsNullException();
        }
        model.getHierarchyNode(event.getEntity()).ifPresent(node -> {
            EntityHierarchyNode updatedNode = new EntityHierarchyNode(
                    node.getEntity(),
                    node.getBrowserText(),
                    event.isDeprecated(),
                    node.getWatches(),
                    node.getOpenCommentCount());
            model.updateNode(updatedNode);
        });
    }

    private static RuntimeException createHierarchyModelIsNullException() {
        return new NullPointerException("Hierarchy Model is null");
    }

}
