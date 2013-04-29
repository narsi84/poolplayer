package home.poolplayer.ui.imagecanvas;

import home.poolplayer.messaging.Messages;
import home.poolplayer.messaging.Messenger;
import home.poolplayer.model.PoolBall;
import home.poolplayer.ui.utils.ConversionUtils;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.opencv.core.Mat;

public class PoolCanvas extends SWTImageCanvas implements
		PropertyChangeListener {

	private List<PoolBall> balls;

	private static Color SOLID = new Color(Display.getDefault(), 255, 0, 0);
	private static Color STRIPE = new Color(Display.getDefault(), 0, 255, 0);
	private static Color CUE = new Color(Display.getDefault(), 0, 0, 0);
	private static Color BLACK = new Color(Display.getDefault(), 255, 255, 255);
	private static Color NONE = new Color(Display.getDefault(), 255, 0, 255);

	public PoolCanvas(final Composite parent) {
		super(parent, SWT.NULL);
		Messenger.getInstance().addListener(this);

		addPaintListener(new PaintListener() { /* paint listener. */
			public void paintControl(final PaintEvent event) {
				paint(event.gc);
			}
		});
	}

	@SuppressWarnings("unchecked")
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

			// loadImage("C:\\Users\\narsi_000\\Pictures\\Camera Roll\\picture000.jpg");
			break;
		case BALLS_DETECTED:
			Display.getDefault().asyncExec(new Runnable() {

				@Override
				public void run() {
					balls = new ArrayList<PoolBall>();
					List<PoolBall> oldballs = (List<PoolBall>) evt.getNewValue();
					synchronized (oldballs) {
						for(PoolBall b : oldballs){
							balls.add(new PoolBall(b));						
						}						
					}
				}
			});
			break;
		default:
			break;
		}
	}

	@Override
	public void dispose() {
		super.dispose();
	}
	
	private void paint(GC gc) {				
		if (balls == null)
			return;
		
		gc.setLineWidth(2);
		for (PoolBall ball : balls) {
			switch (ball.getType()) {
			case BLACK:
				gc.setForeground(BLACK);
				break;
			case CUE:
				gc.setForeground(CUE);
				break;
			case SOLID:
				gc.setForeground(SOLID);
				break;
			case STRIPE:
				gc.setForeground(STRIPE);
				break;
			default:
				gc.setForeground(NONE);
			}
			gc.drawOval((int) ball.getX(), (int) ball.getY(),
					(int) ball.getR() * 2, (int) ball.getR() * 2);

		}
	}

}
