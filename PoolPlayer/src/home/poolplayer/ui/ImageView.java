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

import home.poolplayer.messaging.Messages;
import home.poolplayer.ui.controller.Controller;
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
import org.eclipse.ui.part.ViewPart;


/**
 * This ImageView class shows how to use SWTImageCanvas to 
 * manipulate images. 
 * <p>
 * To facilitate the usage, you should setFocus to the canvas
 * at the beginning, and call the dispose at the end.
 * <p>
 * @author Chengdong Li: cli4@uky.edu
 * @see uky.article.imageviewer.SWTImageCanvas
 */

public class ImageView extends ViewPart implements SelectionListener  {
	public static final String ID = "PoolPlayer.view";

	public SWTImageCanvas imageCanvas;
	
	private Button startB;
	private Composite parent;
	
	/**
	 * The constructor.
	 */
	public ImageView() {
	}
	
	/**
	 * Create the GUI.
	 * @param frame The Composite handle of parent
	 */
	public void createPartControl(Composite frame) {
		this.parent = frame;
		setupComponents(parent);
		layoutComponents();
		addActionListeners();
	}

	private void setupComponents(Composite parent){
		startB = new Button(parent, SWT.PUSH);
		startB.setText("Start");
		startB.setBackground(new Color(Display.getCurrent(), 0, 128, 0));

		imageCanvas=new PoolCanvas(parent);
	}
	
	private void layoutComponents(){
		GridLayout gl = new GridLayout(1, false); 
		parent.setLayout(gl);
		
		GridData bd = new GridData(GridData.CENTER, GridData.CENTER, false, false);
		bd.widthHint = 100;
		bd.heightHint = 50;
		startB.setLayoutData(bd);
		
		GridData d = new GridData(GridData.FILL, GridData.FILL, true, true);
		imageCanvas.setLayoutData(d);	
	}
	
	private void addActionListeners(){
		startB.addSelectionListener(this);
	}
	
	/**
	 * Called when we must grab focus.
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
		if (e.getSource() == startB){
			Controller.getInstance().firePropertyChangeEvent(Messages.START.name());
		}
			
	}

	@Override
	public void widgetDefaultSelected(SelectionEvent e) {
	}
}