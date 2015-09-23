package edu.cornell.csi;

import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.WindowManager;
import ij.gui.GenericDialog;
import ij.gui.Roi;
import ij.measure.Calibration;
import ij.measure.Measurements;
import ij.plugin.PlugIn;
import ij.process.ImageProcessor;
import ij.process.ImageStatistics;

/*
 * CSI_Darkref_Subtractor is a plugin for ImageJ to subtract dark reference spectrum.
 * Developed at Cornell University by Paul Cueva
 *   with support from Robert Hovden and the Muller Group
 *
 *   v1.5 CSI 211212
 */

/* ***** BEGIN LICENSE BLOCK *****
 * Version: MPL 2.0
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 *
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the
 * License.
 *
 * The Original Code is CSI Darkref Subtractor.
 *
 * The Initial Developer of the Original Code is
 * Paul Cueva <pdc23@cornell.edu>, Cornell University.
 * Portions created by the Initial Developer are Copyright (C) 2011
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 *   Paul Cueva <pdc23@cornell.edu>
 *   Robert Hovden <rmh244@cornell.edu>
 *   David A. Muller <david.a.muller@cornell.edu>
 *
 * ***** END LICENSE BLOCK ***** */
public class CSI_Darkref_Subtractor implements PlugIn {
    private ImagePlus imp;
    private ImagePlus drimp;

    @Override
    public void run(final String arg) {
	imp = WindowManager.getCurrentImage();
	final ImagePlus impsubbed = chooseSpectra();
	if (impsubbed == null)
	    return;
	impsubbed.updateChannelAndDraw();
	impsubbed.resetDisplayRange();
	impsubbed.show();
    }

    public ImagePlus chooseSpectra() {
	final int[] winIDs = WindowManager.getIDList();
	if (winIDs == null) {
	    IJ.error("No spectra are open.");
	    return null;
	}

	final String[] winNames = new String[winIDs.length];
	for (int i = 0; i < winIDs.length; i++) {
	    final ImagePlus impi = WindowManager.getImage(winIDs[i]);
	    if (impi != null)
		winNames[i] = impi.getTitle();
	    else
		winNames[i] = "";
	}

	final GenericDialog gd = new GenericDialog("Subtract Dark Reference");
	gd.addChoice("Spectrum:", winNames, winNames[0]);
	gd.addChoice("Dark Reference:", winNames, winNames[0]);
	gd.showDialog();

	if (gd.wasCanceled())
	    return null;

	imp = WindowManager.getImage(winIDs[gd.getNextChoiceIndex()]);
	drimp = WindowManager.getImage(winIDs[gd.getNextChoiceIndex()]);
	return subtractDR();
    }

    public ImagePlus subtractDR() {
	final double[] dr = getDR();
	final int width = imp.getWidth();
	final int height = imp.getHeight();
	final int size = imp.getStackSize();
	Roi roi = drimp.getRoi();
	if (roi == null) {
	    drimp.setRoi(0, 0, width, height);
	    roi = drimp.getRoi();
	}
	IJ.run(drimp, "Median...", "radius=1 stack");

	if (size < 2) {
	    if (width != dr.length)
		IJ.error("Different number of spectra.");

	    final ImageProcessor ip = imp.getProcessor().duplicate();
	    for (int i = 0; i < width; i++)
		for (int j = 0; j < height; j++)
		    ip.putPixelValue(i, j, ip.getf(i, j) - dr[i]);
	    return new ImagePlus("darkref_" + imp.getTitle(), ip);
	}
	if (size != dr.length)
	    IJ.error("Different number of spectra.");

	final ImageStack stack = imp.getStack();
	final ImageStack stacksub = new ImageStack(width, height);
	ImageProcessor ip;
	for (int k = 0; k < size; k++) {
	    ip = stack.getProcessor(k + 1).duplicate();
	    ip.setRoi(roi);
	    for (int i = 0; i < width; i++)
		for (int j = 0; j < height; j++)
		    ip.putPixelValue(i, j, ip.getf(i, j) - dr[k]);
	    stacksub.addSlice(stack.getSliceLabel(k + 1), ip);
	}
	final ImagePlus subbed = new ImagePlus("darkref_" + imp.getTitle(), stacksub);
	subbed.setCalibration(imp.getCalibration());
	return subbed;

    }

    public double[] getDR() {
	final int width = drimp.getWidth();
	final int height = drimp.getHeight();
	final int size = drimp.getStackSize();
	Roi roi = drimp.getRoi();
	if (roi == null) {
	    drimp.setRoi(0, 0, width, height);
	    roi = drimp.getRoi();
	}

	if (size < 2) {
	    final int ystart = (int) roi.getBounds().getY();
	    final int yend = ystart + (int) roi.getBounds().getHeight();
	    final double[] values = new double[width];
	    final ImageProcessor ip = drimp.getProcessor();

	    for (int i = 0; i < width; i++) {
		values[i] = 0;
		for (int j = ystart; j < yend; j++) {
		    values[i] += ip.getf(i, j);
		}
		values[i] /= (yend - ystart);
	    }
	    return values;
	}
	final ImageStack stack = drimp.getStack();
	final double[] values = new double[size];
	final Calibration cal = drimp.getCalibration();
	ImageProcessor ip;
	ImageStatistics stats;
	for (int k = 0; k < size; k++) {
	    ip = stack.getProcessor(k + 1);
	    ip.setRoi(roi);
	    stats = ImageStatistics.getStatistics(ip, Measurements.MEAN, cal);
	    values[k] = stats.mean;
	}
	return values;

    }

}