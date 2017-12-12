/*
 * DeconvolutionLab2
 * 
 * Conditions of use: You are free to use this software for research or
 * educational purposes. In addition, we expect you to include adequate
 * citations and acknowledgments whenever you present or publish results that
 * are based on it.
 * 
 * Reference: DeconvolutionLab2: An Open-Source Software for Deconvolution
 * Microscopy D. Sage, L. Donati, F. Soulez, D. Fortun, G. Schmit, A. Seitz,
 * R. Guiet, C. Vonesch, M Unser, Methods of Elsevier, 2017.
 */

/*
 * Copyright 2010-2017 Biomedical Imaging Group at the EPFL.
 * 
 * This file is part of DeconvolutionLab2 (DL2).
 * 
 * DL2 is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 * 
 * DL2 is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * DL2. If not, see <http://www.gnu.org/licenses/>.
 */

package DL2.deconvolution.capsule;

import DL2.bilib.component.HTMLPane;
import DL2.bilib.table.CustomizedTable;
import DL2.bilib.tools.NumFormat;
import DL2.deconvolution.Deconvolution;
import DL2.deconvolution.Features;
import DL2.deconvolution.algorithm.Algorithm;
import DL2.deconvolution.algorithm.AlgorithmList;
import DL2.deconvolution.algorithm.AlgorithmPanel;
import DL2.deconvolutionlab.monitor.Monitors;
import DL2.fft.AbstractFFT;
import DL2.fft.FFT;
import DL2.signal.ComplexSignal;
import DL2.signal.RealSignal;
import DL2.signal.SignalCollector;

import javax.swing.*;
import java.awt.*;

/**
 * This class is a information module for the algorithm.
 * 
 * @author Daniel Sage
 *
 */
public class AlgorithmCapsule extends AbstractCapsule implements Runnable {

	private CustomizedTable	table;
	private HTMLPane		doc;

	public AlgorithmCapsule(Deconvolution deconvolution) {
		super(deconvolution);
		doc = new HTMLPane(100, 100);
		table = new CustomizedTable(new String[] { "Features", "Values" }, false);
		split = new JSplitPane(JSplitPane.VERTICAL_SPLIT, table.getPane(100, 100), doc.getPane());
		split.setDividerLocation(0.5);
	}

	@Override
	public void update() {
		if (doc == null)
			return;
		if (table == null)
			return;
		
		table.removeRows();
		table.append(new String[] { "PSF", "Waiting for loading ..." });
		Thread thread = new Thread(this);
		thread.setPriority(Thread.MIN_PRIORITY);
		thread.start();
	}

	@Override
	public String getID() {
		return "Algorithm";
	}
	
	@Override
	public void run() {
		Features features = new Features();
		
		if (deconvolution.getAlgorithm() == null) {
			features.add("Algorithm", "No valid algorithm");
			return;
		}
		Algorithm algo = deconvolution.getAlgorithm();
		doc.clear();
		String name = algo.getShortnames()[0];
		AlgorithmPanel algoPanel = AlgorithmList.getPanel(name);
		if (algoPanel != null)
			doc.append(algoPanel.getDocumentation());
		
		String p = algo.getParametersAsString();
		features.add("<html><b>Parameters</b></html>", p.equals("") ? "Parameters free" : p);
		features.add("Iteration", algo.isIterative()  ? "" + algo.getIterationsMax() : "Direct");
		
		if (deconvolution.image == null) {
			startAsynchronousTimer("Open image", 200);
			deconvolution.image = deconvolution.openImage();
			stopAsynchronousTimer();
		}
		
		if (deconvolution.image == null) {
			features.add("Image", "No valid input image");
			return;
		}
		if (deconvolution.getController().getPadding() == null) {
			features.add("Padding", "No valid padding");
			return;
		}
		if (deconvolution.getController().getApodization() == null) {
			features.add("Apodization", "No valid apodization");
			return;
		}
		
		if (deconvolution.psf == null) {
			startAsynchronousTimer("Open PSF", 200);
			deconvolution.psf = deconvolution.openPSF();
			stopAsynchronousTimer();
		}
		
		if (deconvolution.psf == null) {
			features.add("Image", "No valid PSF");
			return;
		}

		startAsynchronousTimer("Check Algorithm", 200);
		
		AbstractFFT f = FFT.getFastestFFT().getDefaultFFT();
		double Q = Math.sqrt(2);
		if (deconvolution.image != null) {
			int mx = deconvolution.image.nx;
			int my = deconvolution.image.ny;
			int mz = deconvolution.image.nz;
			
			while (mx * my * mz > Math.pow(2, 15)) {
				mx = (int)(mx / Q);
				my = (int)(my / Q);
				mz = (int)(mz / Q);
			}
			double N = deconvolution.image.nx * deconvolution.image.ny * deconvolution.image.nz;
			double M = mx * my * mz;
			double ratio = 1;
			if (M != 0)
				ratio = (N * Math.log(N)) / (M * Math.log(M));
			
			double chrono = System.nanoTime(); 
			RealSignal x = new RealSignal("test", mx, my, mz);
			ComplexSignal c = new ComplexSignal("test", mx, my, mz);
			f.init(Monitors.createDefaultMonitor(), mx, my, mz);
			f.transform(x, c);
			SignalCollector.free(x);
			SignalCollector.free(c);
			
			chrono = (System.nanoTime() - chrono);
			
			double chronoI = chrono * ratio * algo.getComplexityNumberofFFT();
			
			features.add("<html><b>Estimation</b></html>", "");
			features.add("Estimated time", NumFormat.time(chronoI) );
			features.add("Estimated number of FFTs", "" + algo.getComplexityNumberofFFT());
			
			features.add("<html><b>Run on tiny dataset</b></html>", mx + "x" + my + "x" + mz);
			features.add("Elapsed time", NumFormat.time(chrono) );
		}
		else 
			features.add("Estimated time", "Error" );
		double mem = (algo.getMemoryFootprintRatio() * deconvolution.image.nx * deconvolution.image.ny * deconvolution.image.nz * 4);
		features.add("Required memory", NumFormat.bytes(mem));
		
		table.removeRows();
		for (String[] feature : features)
			table.append(feature);
		
		Rectangle rect = table.getCellRect(0, 0, true);
		Point pt = ((JViewport) table.getParent()).getViewPosition();
		rect.setLocation(rect.x - pt.x, rect.y - pt.y);

		stopAsynchronousTimer();
	}

}