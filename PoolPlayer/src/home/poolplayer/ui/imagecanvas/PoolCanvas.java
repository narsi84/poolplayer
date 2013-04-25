package home.poolplayer.ui.imagecanvas;

import home.poolplayer.messaging.Messages;
import home.poolplayer.ui.controller.Controller;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

public class PoolCanvas extends SWTImageCanvas implements
		PropertyChangeListener {
	public PoolCanvas(final Composite parent) {
		super(parent, SWT.NULL);
		Controller.getInstance().addListener(this);
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		switch (Messages.valueOf(evt.getPropertyName())) {
		case FRAME_AVAILABLE:
			loadImage("C:\\Users\\narsi_000\\Pictures\\Camera Roll\\picture000.jpg");
			break;
		default:
			break;
		}		
	}

}
