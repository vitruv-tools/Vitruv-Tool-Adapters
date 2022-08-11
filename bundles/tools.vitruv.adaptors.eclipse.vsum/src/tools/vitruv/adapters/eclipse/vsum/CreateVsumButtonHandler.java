package tools.vitruv.adapters.eclipse.vsum;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.jface.wizard.WizardDialog;
import tools.vitruv.adapters.eclipse.vsum.wizard.CreateVsumWizard;

public class CreateVsumButtonHandler extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		CreateVsumWizard wizard = new CreateVsumWizard();
		WizardDialog wd = new WizardDialog(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), wizard);
		wd.setTitle(wizard.getWindowTitle());
		return wd.open();
	}

}
