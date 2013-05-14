package home.poolplayer.ui.imagecanvas;

import home.poolplayer.controller.Controller;
import home.poolplayer.messaging.Messages;
import home.poolplayer.messaging.Messenger;
import home.poolplayer.model.CueStick;
import home.poolplayer.model.PoolBall;
import home.poolplayer.model.PoolCircle;
import home.poolplayer.model.PoolTable;
import home.poolplayer.model.Robot;
import home.poolplayer.model.Shot;
import home.poolplayer.robot.Move;
import home.poolplayer.ui.actions.UIMessages;
import home.poolplayer.ui.controller.UIController;
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
import org.eclipse.swt.graphics.Font;
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
	private static Color CUESTICK_COLOR = new Color(Display.getDefault(), 255,
			0, 0);
	private static Color SHOT_COLOR = new Color(Display.getDefault(), 255, 255,
			255);
	private static Color PATH_COLOR = new Color(Display.getDefault(), 255, 255,
			0);

	private static Font FONT = new Font(Display.getDefault(), "Arial", 14,
			SWT.BOLD | SWT.ITALIC);

	private List<PoolBall> balls;
	private Image image;
	private Shot shot;
	private List<Move> moves;

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
		// Reset the shot object. It will get populated when there is a valid
		// shot. Otherwise we may end up showing the last valid shot
		shot = null;

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

		case CUESTICK_DETECTED:
			forceRedraw();
			break;

		case SHOT_FOUND:
			shot = (Shot) evt.getNewValue();
			forceRedraw();
			break;

		case ROBOT_DETECTED:
			forceRedraw();
			break;

		case PATH_FOUND:
			moves = (List<Move>) evt.getNewValue();
			forceRedraw();
			break;

		default:
			break;
		}
	}

	private void forceRedraw() {
		Display.getDefault().asyncExec(new Runnable() {

			@Override
			public void run() {
				redraw();
			}
		});
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
		drawCueStick(gc);
		drawShot(gc);
		drawRobot(gc);
		drawPath(gc);
	}

	private void drawPath(GC gc) {
		if (moves == null || moves.isEmpty())
			return;

		Robot bot = Controller.getInstance().getRobot();
		if (bot.getCenter() == null)
			return;

		int x1 = (int) (bot.getCenter().x * arX);
		int y1 = (int) (bot.getCenter().y * arY);

		int x2, y2;
		for (Move move : moves) {
			// Angle is wrt North
			x2 = (int) (x1 + move.dist * Math.sin(move.direction) * arX);
			y2 = (int) (y1 + move.dist * Math.cos(move.direction) * arY);

			gc.setAlpha(100);
			gc.setForeground(PATH_COLOR);
			gc.setLineStyle(SWT.LINE_DASHDOT);
			gc.drawLine(x1, y1, x2, y2);

			x1 = x2;
			y1 = y2;
		}
	}

	private void drawRobot(GC gc) {
		Robot bot = Controller.getInstance().getRobot();
		if (bot.getCenter() == null)
			return;

		gc.setForeground(CUESTICK_COLOR);
		gc.setLineStyle(SWT.LINE_SOLID);
		gc.setAlpha(255);
		
		int x = (int) (bot.getCenter().x * arX - 2);
		int y = (int) (bot.getCenter().y * arY - 10);

		gc.drawOval(x, y, 4, 20);
		
		x = (int) (bot.getCenter().x * arX - 10);
		y = (int) (bot.getCenter().y * arY - 2);
		gc.drawOval(x, y, 20, 4);
	}

	private void drawCueStick(GC gc) {
		CueStick stick = Controller.getInstance().getCueStick();
		if (stick == null)
			return;

		int x1 = (int) (stick.start.x * arX);
		int y1 = (int) (stick.start.y * arY);
		int x2 = (int) (stick.end.x * arX);
		int y2 = (int) (stick.end.y * arY);

		gc.setForeground(CUESTICK_COLOR);
		gc.setLineWidth(3);
		gc.setAlpha(255);
		gc.drawLine(x1, y1, x2, y2);
	}

	private void drawShot(GC gc) {
		if (shot == null)
			return;

		gc.setForeground(SHOT_COLOR);
		gc.setLineStyle(SWT.LINE_DASH);
		gc.setAlpha(255);

		double r = shot.ghost.getR();
		int x = (int) ((shot.ghost.getX() - r) * arX);
		int y = (int) ((shot.ghost.getY() - r) * arY);

		gc.drawOval(x, y, (int) (r * arX * 2), (int) (r * arY * 2));

		int textPosX = (int) (shot.pocket.getX() * arX);
		int textPosY = (int) (shot.pocket.getY() * arY);

		gc.setLineStyle(SWT.LINE_SOLID);
		gc.setForeground(BLACK);
		gc.setFont(FONT);
		gc.drawText(Integer.toString((int) (shot.velocity)), textPosX,
				textPosY, true);
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

			int x = (int) ((ball.getX() - ball.getR()) * arX);
			int y = (int) ((ball.getY() - ball.getR()) * arY);

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
		double x = evt.x / arX;
		double y = evt.y / arY;

		String coords = x + ", " + y;
		UIController.getInstance().firePropertyChangeEvent(
				UIMessages.SHOW_COORDS.name(), coords);

		if (!mouseDown || nearestPocket == null)
			return;

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
