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
package osmcd.mapsources.mappacks.region_america_north;

import osmcd.mapsources.AbstractHttpMapSource;
import osmcd.program.model.TileImageType;

public class USNationalMapVS extends AbstractHttpMapSource {

	public USNationalMapVS() {
		super("USGS National Map Vector Layer", 0, 15, TileImageType.PNG, TileUpdate.IfNoneMatch);
	}

	private static final String BASE_URL = "http://basemap.nationalmap.gov/ArcGIS/rest/services/TNM_Vector_Small/MapServer/tile/";

	public String getTileUrl(int zoom, int x, int y) {
		return BASE_URL + zoom + "/" + y + "/" + x;
	}

}