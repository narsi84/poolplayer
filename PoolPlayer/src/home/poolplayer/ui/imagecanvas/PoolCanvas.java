package home.poolplayer.ui.imagecanvas;

import home.poolplayer.controller.Controller;
import home.poolplayer.messaging.Messages;
import home.poolplayer.messaging.Messenger;
import home.poolplayer.model.PoolBall;
import home.poolplayer.model.PoolCircle;
import home.poolplayer.model.PoolTable;
import home.poolplayer.ui.utils.ConversionUtils;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.opencv.core.Mat;

public class PoolCanvas extends Canvas implements PropertyChangeListener,
		PaintListener, MouseListener, MouseMoveListener {

	private static Color SOLID = new Color(Display.getDefault(), 255, 0, 0);
	private static Color STRIPE = new Color(Display.getDefault(), 0, 0, 255);
	private static Color CUE = new Color(Display.getDefault(), 0, 0, 0);
	private static Color BLACK = new Color(Display.getDefault(), 255, 255, 255);
	private static Color NONE = new Color(Display.getDefault(), 255, 0, 255);
	private static Color TABLE_COLOR = new Color(Display.getDefault(), 255,
			255, 255);

	private List<PoolBall> balls;
	private Image image;

	// aspect ratio
	private float arX, arY;

	private boolean mouseDown;
	private PoolCircle nearestPocket;

	public PoolCanvas(final Composite parent) {
		super(parent, SWT.NULL);
		Messenger.getInstance().addListener(this);

		this.addPaintListener(new PaintListener() { /* paint listener. */
			public void paintControl(final PaintEvent event) {
				paint(event.gc);
			}
		});

		this.addMouseListener(this);
		this.addMouseMoveListener(this);
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
				}
			});

			break;
		case BALLS_DETECTED:
			Display.getDefault().asyncExec(new Runnable() {

				@Override
				public void run() {
					balls = new ArrayList<PoolBall>();
					List<PoolBall> oldballs = (List<PoolBall>) evt
							.getNewValue();
					synchronized (oldballs) {
						for (int i = 0; i < oldballs.size(); i++) {
							balls.add(new PoolBall(oldballs.get(i)));
						}
					}
					redraw();
				}
			});
			break;
		default:
			break;
		}
	}

	private void setImageData(ImageData idata) {
		if (image != null)
			image.dispose();
		image = new Image(Display.getDefault(), idata);
		redraw();
	}

	@Override
	public void dispose() {
		super.dispose();
	}

	private void paint(GC gc) {
		if (image == null)
			return;

		Rectangle canvasRect = this.getClientArea();
		arX = (float) canvasRect.width / (float) image.getBounds().width;
		arY = (float) canvasRect.height / (float) image.getBounds().height;

		gc.drawImage(image, 0, 0, image.getBounds().width,
				image.getBounds().height, canvasRect.x, canvasRect.y,
				canvasRect.width, canvasRect.height);
		drawBalls(gc);
		drawTable(gc);
	}

	private void drawTable(GC gc) {
		PoolTable table = Controller.getInstance().getTable();

		gc.setLineStyle(SWT.LINE_SOLID);
		gc.setForeground(TABLE_COLOR);
		gc.setAlpha(100);

		int x = (int) (table.getX() * arX);
		int y = (int) (table.getY() * arY);
		int w = (int) (table.getWidth() * arX);
		int h = (int) (table.getHeight() * arY);

		int r = table.getPocketRadius();

		gc.drawLine(x, y, x + w, y);
		gc.drawLine(x, y, x, y + h);
		gc.drawLine(x + w, y, x + w, y + h);
		gc.drawLine(x, y + h, x + w, y + h);

		for (PoolCircle c : table.getPockets()) {
			gc.drawOval((int) ((c.getX() - r) * arX),
					(int) ((c.getY() - r) * arY), Math.round(r * arX * 2),
					Math.round(r * arY * 2));
		}
	}

	private void drawBalls(GC gc) {
		if (balls == null)
			return;

		gc.setLineWidth(2);
		for (PoolBall ball : balls) {
			switch (ball.getType()) {
			case BLACK:
				gc.setForeground(BLACK);
				gc.setLineStyle(SWT.LINE_SOLID);
				break;
			case CUE:
				gc.setForeground(CUE);
				gc.setLineStyle(SWT.LINE_SOLID);
				break;
			case SOLID:
				gc.setForeground(SOLID);
				gc.setLineStyle(SWT.LINE_SOLID);
				break;
			case STRIPE:
				gc.setForeground(STRIPE);
				gc.setLineStyle(SWT.LINE_DASH);
				break;
			default:
				gc.setForeground(NONE);
			}

			PoolTable table = Controller.getInstance().getTable();

			int x = (int) ((ball.getX() - ball.getR() + table.getX()) * arX);
			int y = (int) ((ball.getY() - ball.getR() + table.getY()) * arY);

			gc.drawOval(x, y, (int) (ball.getR() * arX * 2), (int) (ball.getR()
					* arY * 2));
		}
	}

	@Override
	public void paintControl(PaintEvent e) {
		paint(e.gc);
	}

	@Override
	public void mouseDoubleClick(MouseEvent evt) {
	}

	@Override
	public void mouseDown(MouseEvent evt) {
		mouseDown = true;
		nearestPocket = findNearestPocket(evt.x, evt.y);
	}

	@Override
	public void mouseUp(MouseEvent evt) {
		mouseDown = false;
	}

	@Override
	public void mouseMove(MouseEvent evt) {
		if (!mouseDown || nearestPocket == null)
			return;
		
		double x = evt.x / arX;
		double y = evt.y / arY;
		nearestPocket.setX(x);
		nearestPocket.setY(y);
		redraw();
	}

	private PoolCircle findNearestPocket(int x0, int y0) {
		double x = x0 / arX;
		double y = y0 / arY;

		for (PoolCircle poc : Controller.getInstance().getTable().getPockets()) {
			if (poc.isPointWithin(x, y))
				return poc;
		}
		
		return null;
	}

}
