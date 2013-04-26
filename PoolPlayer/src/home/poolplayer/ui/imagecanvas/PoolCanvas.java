package home.poolplayer.ui.imagecanvas;

import home.poolplayer.messaging.Messages;
import home.poolplayer.messaging.Messenger;
import home.poolplayer.ui.utils.ConversionUtils;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.opencv.core.Mat;

public class PoolCanvas extends SWTImageCanvas implements
		PropertyChangeListener {
	public PoolCanvas(final Composite parent) {
		super(parent, SWT.NULL);
		Messenger.getInstance().addListener(this);
	}

	@Override
	public void propertyChange(final PropertyChangeEvent evt) {
		switch (Messages.MessageNames.valueOf(evt.getPropertyName())) {
		case FRAME_AVAILABLE:
			Display.getDefault().asyncExec(new Runnable() {
				
				@Override
				public void run() {
					Mat mat = (Mat) evt.getNewValue();
					ImageData idata = ConversionUtils.convertMat2ImageData(mat);
					setImageData(idata);
					showOriginal();					
				}
			});
//			loadImage("C:\\Users\\narsi_000\\Pictures\\Camera Roll\\picture000.jpg");
			break;
		default:
			break;
		}		
	}

}
