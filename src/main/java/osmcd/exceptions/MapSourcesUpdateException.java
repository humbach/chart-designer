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
package osmcd.exceptions;

import osmcd.mapsources.MapSourcesManager;

/**
 * Encapsulates several other exceptions that may occur while performing an
 * mapsources online update.
 * 
 * @see MapSourcesManager#mapsourcesOnlineUpdate()
 */
public class MapSourcesUpdateException extends Exception {
	
	private static final long serialVersionUID = 1L;

	public MapSourcesUpdateException(String message) {
		super(message);
	}

	public MapSourcesUpdateException(Throwable cause) {
		super(cause);
	}

}