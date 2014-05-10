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
package osmcbdef.gui.actions;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import osmcbdef.utilities.GUIExceptionHandler;

public class DebugShowReport implements ActionListener {

	public void actionPerformed(ActionEvent e) {
		GUIExceptionHandler.processException(null, null);
		// throw new RuntimeException("Test");
	}

}