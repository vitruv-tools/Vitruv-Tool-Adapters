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

import java.util.Set;

import org.eclipse.emf.common.util.URI;
import org.eclipse.ui.IEditorPart;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import tools.vitruv.adapters.emf.monitorededitor.IEditorPartAdapterFactory;
import tools.vitruv.adapters.emf.monitorededitor.test.mocking.EclipseMock;
import tools.vitruv.adapters.emf.monitorededitor.test.testmodels.Files;
import tools.vitruv.adapters.emf.monitorededitor.test.utils.BasicTestCase;
import tools.vitruv.adapters.emf.monitorededitor.test.utils.DefaultImplementations;
import tools.vitruv.adapters.emf.monitorededitor.test.utils.DefaultImplementations.TestVirtualModel;
import tools.vitruv.adapters.emf.monitorededitor.tools.EclipseAdapterProvider;
import tools.vitruv.adapters.emf.monitorededitor.tools.IEclipseAdapter;

public class VitruviusEMFEditorMonitorImplInternalTests extends BasicTestCase {
    private EclipseMock eclipseMockCtrl;
    private IEclipseAdapter eclipseUtils;
    private IEditorPartAdapterFactory factory;
    private TestVirtualModel virtualModel;
    
    @BeforeEach
    public void setUp() {
        this.eclipseMockCtrl = new EclipseMock();
        this.eclipseUtils = eclipseMockCtrl.getEclipseUtils();
        EclipseAdapterProvider.getInstance().setProvidedEclipseAdapter(eclipseUtils);
        this.factory = new DefaultEditorPartAdapterFactoryImpl(Files.ECORE_FILE_EXTENSION);
        this.virtualModel = TestVirtualModel.createInstance();
    }

    @Test
    public void EMFEditorsCanBeFoundByURI() {
        VitruviusEMFEditorMonitorImpl syncMgr = new VitruviusEMFEditorMonitorImpl(factory,
                virtualModel, DefaultImplementations.ALL_ACCEPTING_VITRUV_ACCESSOR);
        syncMgr.initialize();

        IEditorPart exampleEditor = eclipseMockCtrl.openNewEMFDiagramEditorPart(Files.EXAMPLEMODEL_ECORE,
                Files.EXAMPLEMODEL_ECOREDIAG);
        IEditorPart datatypeEditor = eclipseMockCtrl.openNewEMFTreeEditorPart(Files.DATATYPE_ECORE);
        eclipseMockCtrl.openNewNonEMFEditorPart();

        URI exampleURI = getURI(Files.EXAMPLEMODEL_ECORE);
        URI datatypeURI = getURI(Files.DATATYPE_ECORE);

        Set<IEditorPart> foundExampleEditors = syncMgr.findEditorsForModel(exampleURI);
        Set<IEditorPart> foundDatatypeEditors = syncMgr.findEditorsForModel(datatypeURI);

        assert foundExampleEditors != null;
        assert foundDatatypeEditors != null;

        assert foundExampleEditors.size() == 1;
        assert foundExampleEditors.contains(exampleEditor);

        assert foundDatatypeEditors.size() == 1;
        assert foundDatatypeEditors.contains(datatypeEditor);

        syncMgr.dispose();
    }
}
