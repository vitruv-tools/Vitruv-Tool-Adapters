package tools.vitruv.adapters.eclipse.vsum.util;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;

import com.google.common.collect.FluentIterable;

import tools.vitruv.adapters.eclipse.builder.VitruvEclipseProjectBuilderApplicator;
import tools.vitruv.change.interaction.UserInteractionFactory;
import tools.vitruv.change.propagation.ChangePropagationSpecification;
import tools.vitruv.framework.applications.VitruvApplication;
import tools.vitruv.framework.vsum.VirtualModel;
import tools.vitruv.framework.vsum.VirtualModelBuilder;

public class VitruvInstanceCreator {
	private static final Logger LOGGER = Logger.getLogger(VitruvInstanceCreator.class);

	private final Map<IProject, ? extends Set<VitruvEclipseProjectBuilderApplicator>> projectToApplicators;

	private final Iterable<VitruvApplication> applications;

	private final String name;

	public VitruvInstanceCreator(final String name, final Map<IProject, Set<VitruvEclipseProjectBuilderApplicator>> projectToApplicators,
			final Iterable<VitruvApplication> applications) {
		this.projectToApplicators = projectToApplicators;
		this.applications = applications;
		this.name = name;
	}

	public boolean createVsumProject() {
		final VirtualModel virtualModel = this.createVirtualModel(this.name);
		for (final IProject project : projectToApplicators.keySet()) {
			for (final VitruvEclipseProjectBuilderApplicator applicator : projectToApplicators.get(project)) {
				try {
					// TODO HK Provide dialog option for enabling automatic propagation
					applicator.setPropagateAfterBuild(true).addBuilder(project, virtualModel.getFolder(), Collections.emptySet());
				} catch (IllegalStateException e) {
					LOGGER.error("Could not initialize V-SUM project for project: " + project.getName());
					return false;
				}
			}
		}
		return true;
	}

	private VirtualModel createVirtualModel(final String vsumName) {
		final IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(vsumName);
		try {
			project.create(null);
			project.open(null);
			return new VirtualModelBuilder().withStorageFolder(project.getLocation().toFile())
					.withUserInteractor(UserInteractionFactory.instance.createDialogUserInteractor())
					.withChangePropagationSpecifications(this.createChangePropagationSpecifications()).buildAndInitialize();
		} catch (CoreException e) {
			throw new IllegalStateException(e);
		}
	}

	private Iterable<ChangePropagationSpecification> createChangePropagationSpecifications() {
		return FluentIterable.from(applications).transformAndConcat((application) -> application.getChangePropagationSpecifications());
	}
}
