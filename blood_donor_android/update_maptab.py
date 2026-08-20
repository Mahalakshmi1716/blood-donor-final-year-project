import os
import re

path = r"d:\blood_donor\blood_donor_android\app\src\main\java\com\example\blood_donor\ui\screens\dashboard\DashboardScreen.kt"

with open(path, "r", encoding="utf-8") as f:
    content = f.read()

new_map_tab = """@Composable
fun MapTab() {
    val context = androidx.compose.ui.platform.LocalContext.current
    
    androidx.compose.runtime.LaunchedEffect(Unit) {
        org.osmdroid.config.Configuration.getInstance().load(
            context,
            context.getSharedPreferences("osmdroid", android.content.Context.MODE_PRIVATE)
        )
    }

    Box(modifier = Modifier.fillMaxSize()) {
        androidx.compose.ui.viewinterop.AndroidView(
            factory = { ctx ->
                org.osmdroid.views.MapView(ctx).apply {
                    setTileSource(
                        org.osmdroid.tileprovider.tilesource.XYTileSource(
                            "OpenStreetMap",
                            0,
                            19,
                            256,
                            ".png",
                            arrayOf("https://tile.openstreetmap.org/")
                        )
                    )
                    setMultiTouchControls(true)
                    controller.setZoom(13.0)
                    controller.setCenter(org.osmdroid.util.GeoPoint(28.6139, 77.2090)) // New Delhi center
                    
                    // Add a marker for example
                    val startMarker = org.osmdroid.views.overlay.Marker(this)
                    startMarker.position = org.osmdroid.util.GeoPoint(28.6139, 77.2090)
                    startMarker.setAnchor(org.osmdroid.views.overlay.Marker.ANCHOR_CENTER, org.osmdroid.views.overlay.Marker.ANCHOR_BOTTOM)
                    startMarker.title = "New Delhi"
                    overlays.add(startMarker)
                }
            },
            modifier = Modifier.fillMaxSize()
        )
    }
}"""

content = re.sub(
    r'@Composable\s*fun MapTab\(\)\s*\{\s*Box.*?Text\("Map Tab UI Pending"\)\s*\}\s*\}',
    new_map_tab,
    content,
    flags=re.DOTALL
)

with open(path, "w", encoding="utf-8") as f:
    f.write(content)
