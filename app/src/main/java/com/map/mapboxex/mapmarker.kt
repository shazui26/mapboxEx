package com.map.mapboxex

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import androidx.appcompat.content.res.AppCompatResources
import com.mapbox.android.gestures.Utils.dpToPx
import com.mapbox.geojson.Point
import com.mapbox.maps.CameraOptions
import com.mapbox.maps.EdgeInsets
import com.mapbox.maps.MapView
import com.mapbox.maps.MapboxMap
import com.mapbox.maps.plugin.annotation.annotations
import com.mapbox.maps.plugin.annotation.generated.PointAnnotationOptions
import com.mapbox.maps.plugin.annotation.generated.createPointAnnotationManager

class MapMarkersManager(mapView: MapView) {

    private companion  object MainActivity  {

        private val MARKERS_EDGE_OFFSET = dpToPx(64F).toDouble()
        private val PLACE_CARD_HEIGHT = dpToPx(300F).toDouble()

        val MARKERS_INSETS = EdgeInsets(
            MARKERS_EDGE_OFFSET, MARKERS_EDGE_OFFSET, MARKERS_EDGE_OFFSET, MARKERS_EDGE_OFFSET
        )

        val MARKERS_INSETS_OPEN_CARD = EdgeInsets(
            MARKERS_EDGE_OFFSET, MARKERS_EDGE_OFFSET, PLACE_CARD_HEIGHT, MARKERS_EDGE_OFFSET
        )


    }

    private val context: Context = mapView.context
    private val mapboxMap: MapboxMap = mapView.getMapboxMap()
    private val pointAnnotationManager = mapView.annotations.createPointAnnotationManager(
        annotationConfig = null)

    //mutable map of markers
    private val markers = mutableMapOf<Long, Point>()

    var onMarkersChangeListener: (() -> Unit)? = null

    val hasMarkers: Boolean
        get() = markers.isNotEmpty()





    fun clearMarkers() {
        markers.clear()
        pointAnnotationManager.deleteAll()
    }

    fun showMarker(coordinate: Point) {
        showMarkers(listOf(coordinate))
    }

    fun showMarkers(coordinates: List<Point>) {
        clearMarkers()
        if (coordinates.isEmpty()) {
            onMarkersChangeListener?.invoke()
            return
        }

        coordinates.forEach { coordinate ->

            val pointAnnotationOptions: PointAnnotationOptions? = convertDrawableToBitmap(
                AppCompatResources.getDrawable(
                    this.context,
                    R.drawable.red_pin
                )
            )?.let {
                PointAnnotationOptions()

                    .withPoint(coordinate)
                    .withIconImage(it)
                    .withIconSize(0.1)
            }


            val annotation = pointAnnotationOptions?.let { pointAnnotationManager.create(it) }
            if (annotation != null) {
                markers[annotation.id] = coordinate
            }
        }

        if (coordinates.size == 1) {
            CameraOptions.Builder()
                .center(coordinates.first( ))
                .padding(MARKERS_INSETS_OPEN_CARD)
                .zoom(13.0)
                .build()
        } else {
            mapboxMap.cameraForCoordinates(
                coordinates, MARKERS_INSETS, bearing = null, pitch = null
            )
        }.also {

            mapboxMap.cameraAnimationsPlugin{
                easeTo(it)

            }
        }
        onMarkersChangeListener?.invoke()
    }


    private fun convertDrawableToBitmap(sourceDrawable: Drawable?): Bitmap? {
        if (sourceDrawable == null) {
            return null
        }
        return if (sourceDrawable is BitmapDrawable) {
            sourceDrawable.bitmap
        } else {
// copying drawable object to not manipulate on the same reference
            val constantState = sourceDrawable.constantState ?: return null
            val drawable = constantState.newDrawable().mutate()
            val bitmap: Bitmap = Bitmap.createBitmap(
                drawable.intrinsicWidth, drawable.intrinsicHeight,
                Bitmap.Config.ARGB_8888
            )
            val canvas = Canvas(bitmap)
            drawable.setBounds(0, 0, canvas.width, canvas.height)
            drawable.draw(canvas)
            bitmap
        }
    }
}