package edu.cornell.csi;

import ij.IJ;
import ij.ImageListener;
import ij.ImagePlus;
import ij.Prefs;
import ij.WindowManager;
import ij.gui.ImageCanvas;
import ij.gui.ImageWindow;
import ij.gui.Line;
import ij.gui.Plot;
import ij.gui.PlotWindow;
import ij.gui.ProfilePlot;
import ij.gui.Roi;
import ij.measure.Calibration;
import ij.plugin.PlugIn;
import ij.process.ImageProcessor;
import ij.util.Tools;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

/**
 * This plugin continuously plots the profile along a line scan or a rectangle. The profile is updated if the image
 * changes, thus it can be used to monitor the effect of a filter during preview. Plot size etc. are set by
 * Edit>Options>Profile Plot Options
 *
 * Restrictions: - The plot window is not calibrated. Use Analyze>Plot Profile to get a spatially calibrated plot window
 * where you can do measurements.
 *
 * By Wayne Rasband and Michael Schmid Version 2009-Jun-09: obeys 'fixed y axis scale' in Edit>Options>Profile Plot
 * Options
 */
public class Dynamic_Profiler implements PlugIn, MouseListener, MouseMotionListener, KeyListener, ImageListener,
	Runnable {
    // MouseListener, MouseMotionListener, KeyListener: to detect changes to the selection of an ImagePlus
    // ImageListener: listens to changes (updateAndDraw) and closing of an image
    // Runnable: for background thread
    private ImagePlus imp; // the ImagePlus that we listen to and the last one
    private ImagePlus plotImage; // where we plot the profile
    private Thread bgThread; // thread for plotting (in the background)
    private boolean doUpdate; // tells the background thread to update

    /* Initialization and plot for the first time. Later on, updates are triggered by the listeners * */
    @Override
    public void run(final String arg) {
	imp = WindowManager.getCurrentImage();
	if (imp == null) {
	    IJ.noImage();
	    return;
	}
	if (!isSelection()) {
	    IJ.error("Dynamic Profiler", "Line or Rectangular Selection Required");
	    return;
	}
	final ImageProcessor ip = getProfilePlot(); // get a profile
	if (ip == null) { // no profile?
	    IJ.error("Dynamic Profiler", "No Profile Obtained");
	    return;
	}
	// new plot window
	plotImage = new ImagePlus("Profile of " + imp.getShortTitle(), ip);
	plotImage.show();
	IJ.wait(50);
	positionPlotWindow();
	// thread for plotting in the background
	bgThread = new Thread(this, "Dynamic Profiler Plot");
	bgThread.setPriority(Math.max(bgThread.getPriority() - 3, Thread.MIN_PRIORITY));
	bgThread.start();
	createListeners();
    }

    // these listeners are activated if the selection is changed in the corresponding ImagePlus
    @Override
    public synchronized void mousePressed(final MouseEvent e) {
	doUpdate = true;
	notify();
    }

    @Override
    public synchronized void mouseDragged(final MouseEvent e) {
	doUpdate = true;
	notify();
    }

    @Override
    public synchronized void mouseClicked(final MouseEvent e) {
	doUpdate = true;
	notify();
    }

    @Override
    public synchronized void keyPressed(final KeyEvent e) {
	doUpdate = true;
	notify();
    }

    // unused listeners concering actions in the corresponding ImagePlus
    @Override
    public void mouseReleased(final MouseEvent e) {
	// not used
    }

    @Override
    public void mouseExited(final MouseEvent e) {
	// not used
    }

    @Override
    public void mouseEntered(final MouseEvent e) {
	// not used
    }

    @Override
    public void mouseMoved(final MouseEvent e) {
	// not used
    }

    @Override
    public void keyTyped(final KeyEvent e) {
	// not used
    }

    @Override
    public void keyReleased(final KeyEvent e) {
	// not used
    }

    @Override
    public void imageOpened(final ImagePlus imp1) {
	// not used
    }

    // this listener is activated if the image content is changed (by imp.updateAndDraw)
    @Override
    public synchronized void imageUpdated(final ImagePlus imp1) {
	if (imp1 == this.imp) {
	    if (!isSelection())
		IJ.run(imp1, "Restore Selection", "");
	    doUpdate = true;
	    notify();
	}
    }

    // if either the plot image or the image we are listening to is closed, exit
    @Override
    public void imageClosed(final ImagePlus imp1) {
	if (imp1 == this.imp || imp1 == plotImage) {
	    removeListeners();
	    closePlotImage(); // also terminates the background thread
	}
    }

    // the background thread for plotting.
    @Override
    public void run() {
	while (true) {
	    IJ.wait(50); // delay to make sure the roi has been updated
	    final ImageProcessor ip = getProfilePlot();
	    if (ip != null)
		plotImage.setProcessor(null, ip);
	    synchronized (this) {
		if (doUpdate) {
		    doUpdate = false; // and loop again
		} else {
		    try {
			wait();
		    } // notify wakes up the thread
		    catch (final InterruptedException e) { // interrupted tells the thread to exit
			return;
		    }
		}
	    }
	}
    }

    private synchronized void closePlotImage() { // close the plot window and terminate the background thread
	bgThread.interrupt();
	plotImage.getWindow().close();
    }

    private void createListeners() {
	final ImageWindow win = imp.getWindow();
	final ImageCanvas canvas = win.getCanvas();
	canvas.addMouseListener(this);
	canvas.addMouseMotionListener(this);
	canvas.addKeyListener(this);
	ImagePlus.addImageListener(this);
	ImagePlus.addImageListener(this);
    }

    private void removeListeners() {
	final ImageWindow win = imp.getWindow();
	final ImageCanvas canvas = win.getCanvas();
	canvas.removeMouseListener(this);
	canvas.removeMouseMotionListener(this);
	canvas.removeKeyListener(this);
	ImagePlus.removeImageListener(this);
	ImagePlus.removeImageListener(this);
    }

    /** Place the plot window to the right of the image window */
    void positionPlotWindow() {
	IJ.wait(500);
	if (plotImage == null || imp == null)
	    return;
	final ImageWindow pwin = plotImage.getWindow();
	final ImageWindow iwin = imp.getWindow();
	if (pwin == null || iwin == null)
	    return;
	final Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
	final Dimension plotSize = pwin.getSize();
	final Dimension imageSize = iwin.getSize();
	if (plotSize.width == 0 || imageSize.width == 0)
	    return;
	final Point imageLoc = iwin.getLocation();
	int x = imageLoc.x + imageSize.width + 10;
	if (x + plotSize.width > screen.width)
	    x = screen.width - plotSize.width;
	pwin.setLocation(x, imageLoc.y);
	final ImageCanvas canvas = iwin.getCanvas();
	canvas.requestFocus();
    }

    /** get a profile, analyze it and return a plot (or null if not possible) */
    ImageProcessor getProfilePlot() {
	if (!isSelection())
	    return null;
	final ImageProcessor ip = imp.getProcessor();
	final Roi roi = imp.getRoi();
	if (ip == null || roi == null)
	    return null; // these may change asynchronously
	if (roi.getType() == Roi.LINE)
	    ip.setInterpolate(PlotWindow.interpolate);
	else
	    ip.setInterpolate(false);
	final ProfilePlot profileP = new ProfilePlot(imp, Prefs.verticalProfile);// get the profile
	final double[] profile = profileP.getProfile();
	if (profile == null || profile.length < 2)
	    return null;
	String xUnit = "pixels"; // the following code is mainly for x calibration
	double xInc = 1;
	final Calibration cal = imp.getCalibration();
	if (roi.getType() == Roi.LINE) {
	    final Line line = (Line) roi;
	    if (cal != null) {
		final double dx = cal.pixelWidth * (line.x2 - line.x1);
		final double dy = cal.pixelHeight * (line.y2 - line.y1);
		final double length = Math.sqrt(dx * dx + dy * dy);
		xInc = length / (profile.length - 1);
		xUnit = cal.getUnits();
	    }
	} else if (roi.getType() == Roi.RECTANGLE) {
	    if (cal != null) {
		xInc = roi.getBounds().getWidth() * cal.pixelWidth / (profile.length - 1);
		xUnit = cal.getUnits();
	    }
	} else
	    return null;
	final String xLabel = "Distance (" + xUnit + ")";
	final String yLabel = (cal != null && cal.getValueUnit() != null && !cal.getValueUnit().equals("Gray Value")) ? "Value ("
		+ cal.getValueUnit() + ")"
		: "Value";

	final int n = profile.length; // create the x axis
	final double[] x = new double[n];
	for (int i = 0; i < n; i++)
	    x[i] = i * xInc;

	final Plot plot = new Plot("profile", xLabel, yLabel, x, profile);
	final double fixedMin = ProfilePlot.getFixedMin();
	final double fixedMax = ProfilePlot.getFixedMax();
	if (fixedMin != 0 || fixedMax != 0) {
	    final double[] a = Tools.getMinMax(x);
	    plot.setLimits(a[0], a[1], fixedMin, fixedMax);
	}
	return plot.getProcessor();
    }

    /** returns true if there is a simple line selection or rectangular selection */
    boolean isSelection() {
	if (imp == null)
	    return false;
	final Roi roi = imp.getRoi();
	if (roi == null)
	    return false;
	return roi.getType() == Roi.LINE || roi.getType() == Roi.RECTANGLE;
    }
}
