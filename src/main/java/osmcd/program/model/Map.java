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
package osmcd.program.model;

import java.awt.Dimension;
import java.awt.Point;
import java.io.StringWriter;
import java.util.Enumeration;

import javax.swing.tree.TreeNode;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlTransient;

import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.log4j.Logger;

import osmcd.exceptions.InvalidNameException;
import osmcd.program.interfaces.CapabilityDeletable;
import osmcd.program.interfaces.DownloadableElement;
import osmcd.program.interfaces.LayerInterface;
import osmcd.program.interfaces.MapInterface;
import osmcd.program.interfaces.MapSource;
import osmcd.program.interfaces.MapSpace;
import osmcd.program.interfaces.TileFilter;
import osmcd.program.interfaces.ToolTipProvider;
import osmcd.program.tilefilter.DummyTileFilter;
import osmcd.utilities.I18nUtils;

public class Map implements MapInterface, ToolTipProvider, CapabilityDeletable, TreeNode, DownloadableElement
{
	protected String name;
	protected Layer layer;
	protected TileImageParameters parameters = null;
	protected Point maxTileCoordinate = null;
	protected Point minTileCoordinate = null;
	@XmlAttribute
	protected MapSource mapSource = null;
	@XmlAttribute
	protected int zoom;
	protected Dimension tileDimension = null;
	private static Logger log = Logger.getLogger(Map.class);

	protected Map() {
	}

	protected Map(Layer layer, String name, MapSource mapSource, int zoom, Point minTileCoordinate, Point maxTileCoordinate, TileImageParameters parameters) {
		this.layer = layer;
		this.maxTileCoordinate = maxTileCoordinate;
		this.minTileCoordinate = minTileCoordinate;
		this.name = name;
		this.mapSource = mapSource;
		this.zoom = zoom;
		this.parameters = parameters;
		calculateRuntimeValues();
	}

	protected void calculateRuntimeValues()
	{
		if (mapSource == null)
			throw new RuntimeException("The map source of map " + name + " is unknown to OSMCB");
		if (parameters == null)
		{
			int tileSize = mapSource.getMapSpace().getTileSize();
			tileDimension = new Dimension(tileSize, tileSize);
		}
		else
			tileDimension = parameters.getDimension();
	}

	public LayerInterface getLayer()
	{
		return layer;
	}

	@XmlTransient
	public void setLayer(LayerInterface layer)
	{
		this.layer = (Layer) layer;
	}

	public MapSource getMapSource()
	{
		return this.mapSource;
	}

	@XmlAttribute
	public Point getMaxTileCoordinate()
	{
		return this.maxTileCoordinate;
	}

	@XmlAttribute
	public Point getMinTileCoordinate()
	{
		return this.minTileCoordinate;
	}

	public void setMaxTileCoordinate(Point MaxC)
	{
		this.maxTileCoordinate = MaxC;
	}

	public void setMinTileCoordinate(Point MinC)
	{
		this.minTileCoordinate = MinC;
	}

	@XmlAttribute
	public String getName()
	{
		return name;
	}

	public int getZoom()
	{
		return zoom;
	}

	@Override
	public String toString()
	{
		return getName();
	}

	public TileImageParameters getParameters()
	{
		return parameters;
	}

	public void setParameters(TileImageParameters parameters)
	{
		this.parameters = parameters;
	}

	public String getInfoText()
	{
		return "Map\n name=" + name + "\n mapSource=" + mapSource + "\n zoom=" + zoom + "\n maxTileCoordinate=" + maxTileCoordinate.x + "/" + maxTileCoordinate.y
				+ "\n minTileCoordinate=" + minTileCoordinate.x + "/" + minTileCoordinate.y + "\n parameters=" + parameters;
	}

	public String getToolTip()
	{
		MapSpace mapSpace = mapSource.getMapSpace();
		EastNorthCoordinate tl = new EastNorthCoordinate(mapSpace, zoom, minTileCoordinate.x, minTileCoordinate.y);
		EastNorthCoordinate br = new EastNorthCoordinate(mapSpace, zoom, maxTileCoordinate.x, maxTileCoordinate.y);

		StringWriter sw = new StringWriter(1024);
		sw.write("<html>");
		sw.write(I18nUtils.localizedStringForKey("lp_atlas_info_map_title"));
		sw.write(I18nUtils.localizedStringForKey("lp_atlas_info_map_source", StringEscapeUtils.escapeHtml4(mapSource.toString()),
				StringEscapeUtils.escapeHtml4(mapSource.getName())));
		sw.write(I18nUtils.localizedStringForKey("lp_atlas_info_map_zoom_lv", zoom));
		sw.write(I18nUtils.localizedStringForKey("lp_atlas_info_map_area_start", tl.toString(), minTileCoordinate.x, minTileCoordinate.y));
		sw.write(I18nUtils.localizedStringForKey("lp_atlas_info_map_area_end", br.toString(), maxTileCoordinate.x, maxTileCoordinate.y));
		sw.write(I18nUtils.localizedStringForKey("lp_atlas_info_map_size", (maxTileCoordinate.x - minTileCoordinate.x + 1), (maxTileCoordinate.y
				- minTileCoordinate.y + 1)));
		if (parameters != null)
		{
			sw.write(String.format(I18nUtils.localizedStringForKey("lp_atlas_info_tile_size"), parameters.getWidth(), parameters.getHeight()));
			sw.write(String.format(I18nUtils.localizedStringForKey("lp_atlas_info_tile_format"), parameters.getFormat().toString()));
		}
		else
		{
			sw.write(I18nUtils.localizedStringForKey("lp_atlas_info_tile_format_origin"));
		}

		sw.write(String.format(I18nUtils.localizedStringForKey("lp_atlas_info_max_tile"), calculateTilesToDownload()));
		sw.write("</html>");
		return sw.toString();
	}

	public Dimension getTileSize()
	{
		return tileDimension;
	}

	public double getMinLat()
	{
		return mapSource.getMapSpace().cYToLat(maxTileCoordinate.y, zoom);
	}

	public double getMaxLat()
	{
		return mapSource.getMapSpace().cYToLat(minTileCoordinate.y, zoom);
	}

	public double getMinLon()
	{
		return mapSource.getMapSpace().cXToLon(minTileCoordinate.x, zoom);
	}

	public double getMaxLon()
	{
		return mapSource.getMapSpace().cXToLon(maxTileCoordinate.x, zoom);
	}

	public void delete()
	{
		layer.deleteMap(this);
	}

	public void setName(String newName) throws InvalidNameException
	{
		if (layer != null)
		{
			for (MapInterface map: layer)
			{
				if ((map != this) && (newName.equals(map.getName())))
					throw new InvalidNameException("There is already a map named \"" + newName + "\" in this layer.\nMap names have to unique within an layer.");
			}
		}
		this.name = newName;
	}

	public Enumeration<?> children()
	{
		return null;
	}

	public boolean getAllowsChildren()
	{
		return false;
	}

	public TreeNode getChildAt(int childIndex)
	{
		return null;
	}

	public int getChildCount()
	{
		return 0;
	}

	public int getIndex(TreeNode node)
	{
		return 0;
	}

	public TreeNode getParent()
	{
		return (TreeNode) layer;
	}

	public boolean isLeaf()
	{
		return true;
	}

	public long calculateTilesToDownload()
	{
		int tileSize = mapSource.getMapSpace().getTileSize();
		// This algorithm has to be identically to those used in
		// @DownloadJobEnumerator
		int xMin = minTileCoordinate.x / tileSize;
		int xMax = maxTileCoordinate.x / tileSize;
		int yMin = minTileCoordinate.y / tileSize;
		int yMax = maxTileCoordinate.y / tileSize;
		int width = xMax - xMin + 1;
		int height = yMax - yMin + 1;
		int tileCount = width * height;
		// TODO correct tile count in case of multi-layer maps
		// if (mapSource instanceof MultiLayerMapSource) {
		// // We have a map with two layers and for each layer we have to
		// // download the tiles - therefore double the tileCount
		// tileCount *= 2;
		// }
		return tileCount;
	}

	public boolean checkData()
	{
		boolean result = false;
		boolean[] checks = {name == null, // 0
				layer == null, // 1
				maxTileCoordinate == null, // 2
				minTileCoordinate == null, // 3
				mapSource == null, // 4
				zoom < 0 // 5
		};

		for (int i = 0; i < checks.length; i++)
			if (checks[i])
			{
				log.error("Problem detectected with map \"" + name + "\" check: " + i);
				result = true;
			}
		// Automatically correct bad ordered min/max coordinates
		try
		{
			if (minTileCoordinate.x > maxTileCoordinate.x)
			{
				int tmp = maxTileCoordinate.x;
				maxTileCoordinate.x = minTileCoordinate.x;
				minTileCoordinate.x = tmp;
			}
			if (minTileCoordinate.y > maxTileCoordinate.y)
			{
				int tmp = maxTileCoordinate.y;
				maxTileCoordinate.y = minTileCoordinate.y;
				minTileCoordinate.y = tmp;
			}
		}
		catch (Exception e)
		{
		}

		return result;
	}

	public MapInterface deepClone(LayerInterface newLayer)
	{
		try
		{
			Map map = this.getClass().newInstance();
			map.layer = (Layer) newLayer;
			map.mapSource = mapSource;
			map.maxTileCoordinate = (Point) maxTileCoordinate.clone();
			map.minTileCoordinate = (Point) minTileCoordinate.clone();
			map.name = name;
			if (parameters != null)
				map.parameters = (TileImageParameters) parameters.clone();
			else
				map.parameters = null;
			map.tileDimension = (Dimension) tileDimension.clone();
			map.zoom = zoom;
			return map;
		}
		catch (Exception e)
		{
			throw new RuntimeException(e);
		}
	}

	/**
	 * Needs to be public - otherwise it will be kicked by ProGuard!
	 */
	public void afterUnmarshal(Unmarshaller u, Object parent)
	{
		this.layer = (Layer) parent;
		calculateRuntimeValues();
	}

	public TileFilter getTileFilter()
	{
		return new DummyTileFilter();
	}

}
