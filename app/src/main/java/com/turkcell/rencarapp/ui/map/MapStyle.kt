package com.turkcell.rencarapp.ui.map

/**
 * MapLibre raster stil — OpenStreetMap abc tile sunucuları.
 * Eğitmen referansı: MapStyle.kt (OSM çoklu tile URL).
 */
internal const val OSM_STYLE_JSON: String = """
{
  "version": 8,
  "sources": {
    "osm": {
      "type": "raster",
      "tiles": [
        "https://a.tile.openstreetmap.org/{z}/{x}/{y}.png",
        "https://b.tile.openstreetmap.org/{z}/{x}/{y}.png",
        "https://c.tile.openstreetmap.org/{z}/{x}/{y}.png"
      ],
      "tileSize": 256,
      "attribution": "© OpenStreetMap contributors"
    }
  },
  "layers": [
    { "id": "osm", "type": "raster", "source": "osm" }
  ]
}
"""

internal const val USER_LOCATION_SOURCE_ID = "me"
internal const val USER_LOCATION_LAYER_ID = "me-layer"
