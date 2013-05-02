/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package home.poolplayer.ui;

import home.poolplayer.controller.Controller;
import home.poolplayer.messaging.Messages;
import home.poolplayer.messaging.Messenger;
import home.poolplayer.ui.imagecanvas.PoolCanvas;
import home.poolplayer.ui.imagecanvas.SWTImageCanvas;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.part.ViewPart;

/**
 * This ImageView class shows how to use SWTImageCanvas to manipulate images.
 * <p>
 * To facilitate the usage, you should setFocus to the canvas at the beginning,
 * and call the dispose at the end.
 * <p>
 * 
 * @author Chengdong Li: cli4@uky.edu
 * @see uky.article.imageviewer.SWTImageCanvas
 */

public class ImageView extends ViewPart implements SelectionListener {
	public static final String ID = "PoolPlayer.view";

	public SWTImageCanvas imageCanvas;

	private Text configFileT;
	private Button loadB;

	private Button startB;
	private Composite parent;
	private Composite configC;

	/**
	 * The constructor.
	 */
	public ImageView() {
	}

	/**
	 * Create the GUI.
	 * 
	 * @param frame
	 *            The Composite handle of parent
	 */
	public void createPartControl(Composite frame) {
		this.parent = frame;
		setupComponents(parent);
		layoutComponents();
		addActionListeners();
	}

	private void setupComponents(Composite parent) {
		configC = new Composite(parent, parent.getStyle());

		configFileT = new Text(configC, SWT.BORDER);
		loadB = new Button(configC, SWT.PUSH);
		loadB.setText("Load");

		startB = new Button(parent, SWT.PUSH);
		startB.setText("Start");
		startB.setBackground(new Color(Display.getCurrent(), 0, 128, 0));

		imageCanvas = new PoolCanvas(parent);
	}

	private void layoutComponents() {
		GridLayout configL = new GridLayout(2, false);
		configC.setLayout(configL);
		
		GridData configCd = new GridData(SWT.FILL, SWT.FILL, true, false);
		configC.setLayoutData(configCd);
		
		GridData cTd = new GridData(SWT.FILL, SWT.CENTER, true, false);
		configFileT.setLayoutData(cTd);

		GridData lbd = new GridData(SWT.CENTER, SWT.CENTER, false, false);
		loadB.setLayoutData(lbd);

		GridLayout gl = new GridLayout(1, false);
		parent.setLayout(gl);

		GridData bd = new GridData(SWT.CENTER, SWT.CENTER, false,
				false);
		bd.widthHint = 100;
		bd.heightHint = 50;
		startB.setLayoutData(bd);

		GridData d = new GridData(GridData.FILL, GridData.FILL, true, true);
		imageCanvas.setLayoutData(d);
	}

	private void addActionListeners() {
		startB.addSelectionListener(this);
		loadB.addSelectionListener(this);
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
		if (e.getSource() == startB) {
			Messenger.getInstance().broadcastMessage(
					Messages.MessageNames.START.name());
		}
		
		if (e.getSource() == loadB){
			String fname = configFileT.getText();
			if (fname == null || fname.length() == 0){
				System.out.println("No file specified");
				return;
			}
			Controller.getInstance().loadSettings(configFileT.getText());
		}
			

	}

	@Override
	public void widgetDefaultSelected(SelectionEvent e) {
	}
}