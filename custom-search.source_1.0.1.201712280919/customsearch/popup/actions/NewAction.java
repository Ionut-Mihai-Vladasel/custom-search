package customsearch.popup.actions;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.search.IJavaSearchConstants;
import org.eclipse.jdt.core.search.IJavaSearchScope;
import org.eclipse.jdt.core.search.SearchEngine;
import org.eclipse.jdt.core.search.SearchMatch;
import org.eclipse.jdt.core.search.SearchParticipant;
import org.eclipse.jdt.core.search.SearchPattern;
import org.eclipse.jdt.core.search.SearchRequestor;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.texteditor.ITextEditor;

public class NewAction implements IObjectActionDelegate {

	private Shell shell;
	

	public NewAction() {
		super();
	}

	public void setActivePart(IAction action, IWorkbenchPart targetPart) {
		shell = targetPart.getSite().getShell();
	}

	public void run(IAction action) {

		String selectedText = getSelectedText();
		
		if(selectedText == null){
			MessageDialog.openInformation(shell, "IDIADA_SEARCH", "Error get text.");
		}else{
			if(selectedText.isEmpty()){
				MessageDialog.openInformation(shell, "IDIADA_SEARCH", "Text selected empty.");
			}else{
				try {
					
					searchTextLikeMethod(selectedText.replace("_", ""));
				} catch (CoreException e) {
					MessageDialog.openInformation(shell, "IDIADA_SEARCH", "Error search text.");
					e.printStackTrace();
				}
			}
		}
	}

	private void searchTextLikeMethod(String selectedText) throws CoreException {
		SearchPattern pattern = SearchPattern.createPattern(selectedText, IJavaSearchConstants.METHOD,
				IJavaSearchConstants.DECLARATIONS, SearchPattern.R_EXACT_MATCH);

		IJavaSearchScope scope = SearchEngine.createWorkspaceScope();

		SearchRequestor requestor = new SearchRequestor() {
			private String MANAGER_IMPL_TAG = "impl";

			@Override
			public void acceptSearchMatch(SearchMatch match) {

				if (match.getResource().getName().toLowerCase().indexOf(this.MANAGER_IMPL_TAG) >= 0) {
					IWorkbenchPage mActivePage = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();

					try {
						IContainer parent = match.getResource().getParent();
						IPath path = parent.getFullPath().addTrailingSeparator().append(match.getResource().getName());
						IFile miFichero = ResourcesPlugin.getWorkspace().getRoot().getFile(path);

						ITextEditor editor = (ITextEditor) IDE.openEditor(mActivePage, miFichero);
						editor.selectAndReveal(match.getOffset(), match.getLength());

					} catch (PartInitException e) {
						e.printStackTrace();
					}
				}
			}
		};

		// Search
		SearchEngine searchEngine = new SearchEngine();
		searchEngine.search(pattern, new SearchParticipant[] { SearchEngine.getDefaultSearchParticipant() }, scope,
				requestor, null);

	}

	private String getSelectedText() {
		ISelection mySelection = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getSelection();
		if (mySelection instanceof ITextSelection) {
			String myTextSelected = ((ITextSelection) mySelection).getText();
			return myTextSelected;
		}
		return null; 
	}

	public void selectionChanged(IAction action, ISelection selection) {
	}

}
