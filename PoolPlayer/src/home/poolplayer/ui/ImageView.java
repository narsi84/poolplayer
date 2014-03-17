package home.poolplayer.ui;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import home.poolplayer.controller.Controller;
import home.poolplayer.messaging.Messenger;
import home.poolplayer.messaging.Messages.MessageNames;
import home.poolplayer.ui.actions.UIMessages;
import home.poolplayer.ui.controller.UIController;
import home.poolplayer.ui.imagecanvas.PoolCanvas;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.part.ViewPart;

public class ImageView extends ViewPart implements SelectionListener,
		PropertyChangeListener {
	public static final String ID = "PoolPlayer.view";

	public PoolCanvas imageCanvas;

	private Text configFileT;
	private Button loadB;
	private Button pauseB;

	private Composite parent;
	private Composite configC;

	private Text pixelCoordsT;

	public ImageView() {
	}

	public void createPartControl(Composite frame) {
		this.parent = frame;
		setupComponents(parent);
		layoutComponents();
		addActionListeners();
	}

	private void setupComponents(Composite parent) {
		configC = new Composite(parent, parent.getStyle());

		configFileT = new Text(configC, SWT.BORDER);
		configFileT
				.setText("C:\\Users\\narasimhan.rajagopal\\Documents\\GitHub\\poolplayer\\Settings.txt");

		loadB = new Button(configC, SWT.PUSH);
		loadB.setText("Load/Start");

		pauseB = new Button(configC, SWT.TOGGLE);
		pauseB.setText("Pause");
		pauseB.setSelection(false);
		
		imageCanvas = new PoolCanvas(parent);

		pixelCoordsT = new Text(parent, SWT.BORDER);
	}

	private void layoutComponents() {
		GridLayout configL = new GridLayout(3, false);
		configC.setLayout(configL);

		GridData configCd = new GridData(SWT.FILL, SWT.FILL, true, false);
		configC.setLayoutData(configCd);

		GridData cTd = new GridData(SWT.FILL, SWT.CENTER, true, false);
		configFileT.setLayoutData(cTd);

		GridData lbd = new GridData(SWT.CENTER, SWT.CENTER, false, false);
		lbd.widthHint = 100;
		lbd.heightHint = 50;
		loadB.setLayoutData(lbd);

		GridData pbd = new GridData(SWT.CENTER, SWT.CENTER, false, false);
		pbd.widthHint = 100;
		pbd.heightHint = 50;
		pauseB.setLayoutData(pbd);

		GridLayout gl = new GridLayout(1, false);
		parent.setLayout(gl);

		GridData d = new GridData(GridData.FILL, GridData.FILL, true, true);
		imageCanvas.setLayoutData(d);

		GridData pxd = new GridData(GridData.FILL, GridData.CENTER, true, false);
		pixelCoordsT.setLayoutData(pxd);
	}

	private void addActionListeners() {
		loadB.addSelectionListener(this);
		pauseB.addSelectionListener(this);
		UIController.getInstance().addListener(this);
	}

	/**
	 * Called when we must grab focus.
	 * 
	 * @see org.eclipse.ui.part.ViewPart#setFocus
	 */
	public void setFocus() {
		imageCanvas.setFocus();
	}

	/**
	 * Called when the View is to be disposed
	 */
	public void dispose() {
		imageCanvas.dispose();
		super.dispose();
	}

	@Override
	public void widgetSelected(SelectionEvent e) {
		if (e.getSource() == loadB) {
			String fname = configFileT.getText();
			if (fname == null || fname.length() == 0) {
				System.out.println("No file specified");
				return;
			}
			Controller.getInstance().loadSettings(configFileT.getText());
		}
		
		if (e.getSource() == pauseB){
			Messenger.getInstance().broadcastMessage(MessageNames.PAUSE.name(), pauseB.getSelection());
		}

	}

	@Override
	public void widgetDefaultSelected(SelectionEvent e) {
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		UIMessages msg = UIMessages.valueOf(evt.getPropertyName());
		switch (msg) {
		case SHOW_COORDS:
			pixelCoordsT.setText((String) evt.getNewValue());
			break;
		}
	}
}