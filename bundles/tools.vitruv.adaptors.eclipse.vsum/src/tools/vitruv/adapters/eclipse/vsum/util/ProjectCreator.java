package tools.vitruv.adapters.eclipse.vsum.util;

import edu.kit.ipd.sdq.commons.util.org.eclipse.core.resources.IProjectUtil;

public class ProjectCreator {
	  private final String projectName;

	  public ProjectCreator(final String projectName) {
	    this.projectName = projectName;
	  }

	  public void createProject() {
	    IProjectUtil.createJavaProject(this.projectName);
	  }
	}