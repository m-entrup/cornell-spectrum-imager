package edu.cornell.csi;

import ij.IJ;
import ij.ImagePlus;
import ij.plugin.filter.PlugInFilter;
import ij.process.ImageProcessor;
// for the Vector and Hashtable classes

//import ij.IJ.*;

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
 * The Original Code is CSI DM3 Reader.
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
public class CSI_Map_to_Line implements PlugInFilter {
    @Override
    public int setup(final String arg, final ImagePlus img) {
	IJ.run(img, "Reslice [/]...", "slice_count=1 rotate avoid");
	final ImagePlus resliced = IJ.getImage();
	IJ.run(resliced, "Z Project...", "start=1 stop=" + resliced.getStackSize() + " projection=[Sum Slices]");
	final ImagePlus binned = IJ.getImage();
	resliced.close();
	binned.setTitle("Binned " + img.getTitle());
	binned.setCalibration(img.getCalibration());
	return DOES_ALL;
    }

    @Override
    public void run(final ImageProcessor ip) {
	return;
    }

}
