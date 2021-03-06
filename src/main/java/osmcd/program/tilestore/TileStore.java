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
package osmcd.program.tilestore;

import java.awt.Point;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.swing.JOptionPane;

import org.apache.log4j.Logger;

import osmcd.exceptions.TileStoreException;
import osmcd.program.DirectoryManager;
import osmcd.program.interfaces.MapSource;
import osmcd.program.model.Settings;
import osmcd.program.tilestore.berkeleydb.BerkeleyDbTileStore;
import osmcd.utilities.I18nUtils;

public abstract class TileStore
{
	protected static TileStore INSTANCE = null;
	protected Logger log;
	protected File tileStoreDir;

	public static synchronized void initialize()
	{
		if (INSTANCE != null)
			return;
		try
		{
			INSTANCE = new BerkeleyDbTileStore();
		}
		catch (TileStoreException e)
		{
			String errMsg = I18nUtils.localizedStringForKey("msg_tile_store_access_conflict");
			JOptionPane.showMessageDialog(null, errMsg, I18nUtils.localizedStringForKey("msg_tile_store_access_conflict_title"), JOptionPane.ERROR_MESSAGE);
			System.exit(1);
		}
	}

	public static TileStore getInstance()
	{
		return INSTANCE;
	}

	protected TileStore() {
		log = Logger.getLogger(this.getClass());
		String tileStorePath = Settings.getInstance().directories.tileStoreDirectory;
		if (tileStorePath != null)
			tileStoreDir = new File(tileStorePath);
		else
			tileStoreDir = DirectoryManager.tileStoreDir;
		log.debug("Tile store path: " + tileStoreDir);
	}

	/**
	 * 
	 * @param tileData
	 * @param x
	 * @param y
	 * @param zoom
	 * @param mapSource
	 * @throws IOException
	 */
	public abstract void putTileData(byte[] tileData, int x, int y, int zoom, MapSource mapSource) throws IOException;

	/**
	 * 
	 * @param tileData
	 * @param x
	 * @param y
	 * @param zoom
	 * @param mapSource
	 * @param timeLastModified
	 * @param timeExpires
	 * @param eTag
	 * @throws IOException
	 */
	public abstract void putTileData(byte[] tileData, int x, int y, int zoom, MapSource mapSource, long timeLastModified, long timeExpires, String eTag)
			throws IOException;

	/**
	 * 
	 * @param x
	 * @param y
	 * @param zoom
	 * @param mapSource
	 * @return
	 */
	public abstract TileStoreEntry getTile(int x, int y, int zoom, MapSource mapSource);

	/**
	 * 
	 * @param x
	 * @param y
	 * @param zoom
	 * @param mapSource
	 * @return
	 */
	public abstract boolean contains(int x, int y, int zoom, MapSource mapSource);

	/**
	 * 
	 * @param mapSource
	 */
	public abstract void prepareTileStore(MapSource mapSource);

	/**
	 * 
	 * @param storeName
	 */
	public abstract void clearStore(String storeName);

	/**
	 * 
	 * @return
	 */
	public abstract String[] getAllStoreNames();

	/**
	 * Returns <code>true</code> if the tile store directory of the specified {@link MapSource} exists.
	 * 
	 * @param mapSource
	 * @return
	 */
	public abstract boolean storeExists(MapSource mapSource);

	/**
	 * 
	 * @param mapSourceName
	 * @return
	 * @throws InterruptedException
	 */
	public abstract TileStoreInfo getStoreInfo(String mapSourceName) throws InterruptedException;

	/**
	 * 
	 * @param mapSource
	 * @param zoom
	 * @param tileNumMin
	 * @param tileNumMax
	 * @return
	 * @throws InterruptedException
	 */
	public abstract BufferedImage getCacheCoverage(MapSource mapSource, int zoom, Point tileNumMin, Point tileNumMax) throws InterruptedException;

	public abstract void closeAll();

	public abstract void putTile(TileStoreEntry tile, MapSource mapSource);

	public abstract TileStoreEntry createNewEntry(int x, int y, int zoom, byte[] data, long timeLastModified, long timeExpires, String eTag);

	/**
	 * Creates a new {@link TileStoreEntry} that represents a missing tile in a sparse map source
	 * 
	 * @param x
	 * @param y
	 * @param zoom
	 * @return
	 */
	public abstract TileStoreEntry createNewEmptyEntry(int x, int y, int zoom);
}
