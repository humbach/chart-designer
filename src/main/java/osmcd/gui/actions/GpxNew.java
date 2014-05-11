/*******************************************************************************
 * Copyright (c) OSMCB developers
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package osmcd.gui.actions;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import osmcd.data.gpx.GPXUtils;
import osmcd.data.gpx.gpx11.Gpx;
import osmcd.gui.MainGUI;
import osmcd.gui.gpxtree.GpxRootEntry;
import osmcd.gui.mapview.layer.GpxLayer;
import osmcd.gui.panels.JGpxPanel;


public class GpxNew implements ActionListener {

	JGpxPanel panel;

	public GpxNew(JGpxPanel panel) {
		super();
		this.panel = panel;
	}

	public void actionPerformed(ActionEvent event) {
		if (!GPXUtils.checkJAXBVersion())
			return;
		newGpx();
		MainGUI.getMainGUI().previewMap.repaint();
	}

	public GpxRootEntry newGpx() {
		Gpx gpx = Gpx.createGpx();
		GpxLayer gpxLayer = new GpxLayer(gpx);
		return panel.addGpxLayer(gpxLayer);
	}
}