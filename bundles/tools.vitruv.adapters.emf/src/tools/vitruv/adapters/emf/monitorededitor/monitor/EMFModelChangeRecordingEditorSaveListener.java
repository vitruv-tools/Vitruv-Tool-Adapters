/*******************************************************************************
 * Copyright (c) 2014 Felix Kutzner.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Felix Kutzner - initial implementation.
 ******************************************************************************/

package tools.vitruv.adapters.emf.monitorededitor.monitor;

import org.apache.log4j.Logger;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.emf.ecore.change.ChangeDescription;
import org.eclipse.emf.ecore.resource.Resource;

import tools.vitruv.adapters.emf.monitorededitor.IEditorPartAdapterFactory.IEditorPartAdapter;
import tools.vitruv.adapters.emf.monitorededitor.tools.ISaveEventListener;
import tools.vitruv.adapters.emf.monitorededitor.tools.ResourceReloadListener;
import tools.vitruv.adapters.emf.monitorededitor.tools.SaveEventListenerMgr;
import tools.vitruv.change.composite.description.VitruviusChange;
import tools.vitruv.change.composite.recording.ChangeRecorder;

/**
 * <p>
 * A listener for EMF/GMF editors recording the changes in the editor's EMF model, calling the
 * method <code>onSavedResource()</code> whenever the user saves the edited file.
 * </p>
 * 
 * <p>
 * The listener is initially inactive and can be activated by calling <code>initialize()</code>. It
 * remains active until <code>dispose()</code> is called.
 * </p>
 * 
 * <p>
 * This listener class can be used by extending it and implementing the
 * <code>onSavedResource()</code> method.
 * </p>
 */
public abstract class EMFModelChangeRecordingEditorSaveListener {

    /** The logger for {@link EMFModelChangeRecordingEditorSaveListener} instances. */
    private static final Logger LOGGER = Logger.getLogger(EMFModelChangeRecordingEditorSaveListener.class);

    /**
     * <code>true</code> iff the listener has been initialized and is operational.
     */
    private boolean isInitialized = false;

    /**
     * The {@link ChangeRecorder} used to record changes to the edited model.
     */
    private ChangeRecorder changeRecorder;

    /** The monitored EMF model resource. */
    private final Resource targetResource;

    /** The listener getting fired when the user saves the edited file. */
    private final SaveEventListenerMgr saveActionListenerManager;

    /**
     * A constructor for {@link EMFModelChangeRecordingEditorSaveListener} instances. The listener
     * remains inactive until <code>initialize()</code> is called.
     * 
     * @param editorAdapter
     *            An {@link IEditorPartAdapter} instance adapting the EMF/GMF editor which needs to be
     *            monitored.
     */
    public EMFModelChangeRecordingEditorSaveListener(IEditorPartAdapter editorAdapter) {
        this.targetResource = editorAdapter.getEditedModelResource();
        this.saveActionListenerManager = new SaveEventListenerMgr();
        saveActionListenerManager.restrictSaveActionsToEditorPart(editorAdapter.getEditorPart());
        LOGGER.trace("Constructed a listener for an editor of type " + editorAdapter.getClass().getCanonicalName()
                + " for Res. " + targetResource);
    }

    /**
     * @return <code>true</code> iff the listener has been initialized.
     */
    public boolean isInitialized() {
        return isInitialized;
    }

    /**
     * Creates a {@link ISaveEventListener} instance reading out the <code>changeRecorder</code> and
     * calling <code>onSavedResource()</code> after the user has saved the edited file.
     * 
     * @return The newly created {@link SaveActionExecutionListener}.
     */
    protected ISaveEventListener createSaveActionExecutionListener() {
        return new ISaveEventListener() {
            @Override
            public void onPreSave() {
            }

            @Override
            public void onPostSave() {
                var change = readOutChangesAndEndRecording();
                LOGGER.trace("Detected a user save action, got change description: " + change);
                onSavedResource(change);
                resetChangeRecorder();
            }

            @Override
            public void postExecuteFailure(String commandId, ExecutionException exception) {
            }
        };
    }

    private void installResourceReloadListener() {
        ResourceReloadListener rrl = new ResourceReloadListener(targetResource) {
            @Override
            protected void onResourceUnloaded() {
                LOGGER.trace("Detected a resource unload event, deactivating the change recorder.");
                deactivateChangeRecorder();
            }

            @Override
            protected void onResourceLoaded() {
                LOGGER.trace("Detected a resource load event, resetting the change recorder.");
                resetChangeRecorder();
            }
        };

        targetResource.eAdapters().add(rrl);
    }

    private void deactivateChangeRecorder() {
        if (changeRecorder != null) {
            changeRecorder.close();
        }
        changeRecorder = null;
    }

    /**
     * Resets the change recorder by replacing it with a new one.
     */
    protected void resetChangeRecorder() {
        deactivateChangeRecorder();
        changeRecorder = new ChangeRecorder(targetResource.getResourceSet());
        changeRecorder.addToRecording(targetResource);
        changeRecorder.beginRecording();
    }

    /**
     * @return The changes recorded since last resetting the change recorder.
     */
    protected VitruviusChange readOutChangesAndEndRecording() {
        return changeRecorder.endRecording();
    }

    /**
     * Initializes the listener. After calling this method, the listener is active until
     * <code>dispose()</code> is called.
     */
    public void initialize() {
        if (!isInitialized) {
            resetChangeRecorder();
            installResourceReloadListener();
            saveActionListenerManager.install(createSaveActionExecutionListener());
            isInitialized = true;
        } else {
            LOGGER.warn("Called initialize() for an initialized instance," + " ignoring the call");
        }
    }

    /**
     * Disposes the listener. After calling this method, the listener is inactive until
     * <code>initialize()</code> is called.
     */
    public void dispose() {
        if (isInitialized) {
            saveActionListenerManager.dispose();
            if (changeRecorder != null) {
                changeRecorder.close();
            }

            isInitialized = false;
        } else {
            LOGGER.warn("Called dispose() for an uninitialized instance," + " ignoring the call");
        }
    }

    /**
     * @return The monitored EMF resource.
     */
    public Resource getMonitoredResource() {
        return targetResource;
    }

    /**
     * The "listener" method getting called when the user saves the edited file.
     * 
     * @param changeDescription
     *            The EMF {@link ChangeDescription} describing the changes to the EMF model since last
     *            saving it (rsp. since opening it, in case it has not been saved yet). This object is
     *            provided "as is" from a {@link ChangeRecorder} instance.
     */
    protected abstract void onSavedResource(VitruviusChange changeDescription);
}
