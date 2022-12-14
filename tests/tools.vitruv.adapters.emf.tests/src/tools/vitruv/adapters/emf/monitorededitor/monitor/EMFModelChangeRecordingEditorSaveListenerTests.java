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

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EcoreFactory;
import org.eclipse.ui.IEditorPart;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import tools.vitruv.adapters.emf.monitorededitor.IEditorPartAdapterFactory.IEditorPartAdapter;
import tools.vitruv.adapters.emf.monitorededitor.test.mocking.EclipseMock;
import tools.vitruv.adapters.emf.monitorededitor.test.mocking.EclipseMock.SaveEventKind;
import tools.vitruv.adapters.emf.monitorededitor.test.testmodels.Files;
import tools.vitruv.adapters.emf.monitorededitor.test.utils.BasicTestCase;
import tools.vitruv.adapters.emf.monitorededitor.test.utils.EnsureExecuted;
import tools.vitruv.adapters.emf.monitorededitor.test.utils.EnsureNotExecuted;
import tools.vitruv.adapters.emf.monitorededitor.tools.EclipseAdapterProvider;
import tools.vitruv.adapters.emf.monitorededitor.tools.IEclipseAdapter;
import tools.vitruv.change.composite.description.VitruviusChange;

public class EMFModelChangeRecordingEditorSaveListenerTests extends BasicTestCase {
    private EclipseMock eclipseCtrl;
    private IEclipseAdapter mockedEclipseUtils;

    private IEditorPart editorPart;
    private IEditorPartAdapter editorPartAdapter;

    private final EObject DUMMY_EOBJECT = EcoreFactory.eINSTANCE.createEClass();
    
    @BeforeEach
    public void setUp() {
        this.eclipseCtrl = new EclipseMock();
        this.mockedEclipseUtils = eclipseCtrl.getEclipseUtils();
        EclipseAdapterProvider.getInstance().setProvidedEclipseAdapter(mockedEclipseUtils);
        DefaultEditorPartAdapterFactoryImpl adapterFactory = new DefaultEditorPartAdapterFactoryImpl(
                Files.ECORE_FILE_EXTENSION);
        editorPart = eclipseCtrl.openNewEMFTreeEditorPart(Files.EXAMPLEMODEL_ECORE);
        editorPartAdapter = adapterFactory.createAdapter(editorPart);
    }

    @AfterEach
    public void tearDown() {
        assert !eclipseCtrl.hasListeners() : "Listeners were not fully removed from Eclipse";
    }

    @Test
    public void savesAreDetected() {
        final EnsureExecuted ensureExecuted = new EnsureExecuted();

        EMFModelChangeRecordingEditorSaveListener listener = new EMFModelChangeRecordingEditorSaveListener(
                editorPartAdapter) {
            @Override
            protected void onSavedResource(VitruviusChange changeDescription) {
                assert changeDescription != null;
                assertTrue(changeDescription.getEChanges().isEmpty());
                ensureExecuted.markExecuted();
            }
        };
        listener.initialize();

        eclipseCtrl.issueSaveEvent(SaveEventKind.SAVE);

        assert !ensureExecuted.isIndicatingFail() : "The save listenener was not executed.";
        listener.dispose();
    }

    @Test
    public void changesArePassedToTheListener() {
        final EnsureExecuted ensureExecuted = new EnsureExecuted();
        final String newRootObjName = "Foobar";
        final EPackage rootObj = (EPackage) editorPartAdapter.getEditingDomain().getRoot(DUMMY_EOBJECT);

        // Set up the listener.
        EMFModelChangeRecordingEditorSaveListener listener = new EMFModelChangeRecordingEditorSaveListener(
                editorPartAdapter) {
            @Override
            protected void onSavedResource(VitruviusChange changeDescription) {
                assert changeDescription != null;
                assert changeDescription.getEChanges().size() == 1;

                // assert
                // changeDescriptions.get(0).getChangeDescription().getObjectChanges().containsKey(rootObj);

                ensureExecuted.markExecuted();
            }
        };
        listener.initialize();

        // Change the model.
        rootObj.setName(newRootObjName);

        // Simulate a save event.
        eclipseCtrl.issueSaveEvent(SaveEventKind.SAVE);

        assert !ensureExecuted.isIndicatingFail() : "The save listenener was not executed.";
        listener.dispose();
    }

    @Test
    public void savesForOtherEditorsAreIgnored() {
        eclipseCtrl.openNewEMFTreeEditorPart(Files.DATATYPE_ECORE);
        // Now, the currently active editor part in the mocked eclipse environment is ep

        final EnsureNotExecuted ensureNotExecuted = new EnsureNotExecuted();

        EMFModelChangeRecordingEditorSaveListener listener = new EMFModelChangeRecordingEditorSaveListener(
                editorPartAdapter) {
            @Override
            protected void onSavedResource(VitruviusChange changeDescription) {
                ensureNotExecuted.markExecuted();
            }
        };
        listener.initialize();

        eclipseCtrl.issueSaveEvent(SaveEventKind.SAVE);

        assert !ensureNotExecuted
                .isIndicatingFail() : "The save listener was executed though another editor was active.";
        listener.dispose();
    }
}
