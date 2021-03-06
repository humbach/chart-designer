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

import javax.swing.JOptionPane;

import osmcd.gui.MainGUI;
import osmcd.mapsources.MapSourcesManager;
import osmcd.program.interfaces.FileBasedMapSource;
import osmcd.program.interfaces.MapSource;
import osmcd.utilities.I18nUtils;

public class RefreshCustomMapsources implements ActionListener
{

	public void actionPerformed(ActionEvent e)
	{
		MapSourcesManager manager = MapSourcesManager.getInstance();
		MainGUI gui = MainGUI.getMainGUI();
		MapSource selectedMapSource = gui.getSelectedMapSource();
		boolean updateGui = false;
		int count = 0;
		for (MapSource mapSource: manager.getAllAvailableMapSources())
		{
			if (mapSource instanceof FileBasedMapSource)
			{
				FileBasedMapSource fbms = (FileBasedMapSource) mapSource;
				fbms.reinitialize();
				count++;
				if (mapSource.equals(selectedMapSource))
					updateGui = true;
			}
		}
		if (updateGui)
		{
			/*
			 * The currently selected map source was updated - we have to force an GUI update in case the available zoom levels has been changed
			 */
			gui.mapSourceChanged(selectedMapSource);
		}
		JOptionPane.showMessageDialog(gui, String.format(I18nUtils.localizedStringForKey("msg_refresh_all_map_source_done"), count));
	}

}
