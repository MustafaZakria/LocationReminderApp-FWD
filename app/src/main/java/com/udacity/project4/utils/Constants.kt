package com.udacity.project4.utils

import java.util.concurrent.TimeUnit

object Constants {

    const val ACTION_GEOFENCE_EVENT = "locationReminders.action.ACTION_GEOFENCE_EVENT"

    const val REQUEST_FOREGROUND_AND_BACKGROUND_PERMISSION_RESULT_CODE = 33
    const val REQUEST_FOREGROUND_ONLY_PERMISSIONS_REQUEST_CODE = 34
    const val TAG = "***"
    const val LOCATION_PERMISSION_INDEX = 0

     const val REQUEST_TURN_DEVICE_LOCATION_ON = 29
     const val BACKGROUND_LOCATION_PERMISSION_INDEX = 1

    const val GEOFENCE_RADIUS_IN_METERS = 100f
    const val EXTRA_GEOFENCE_INDEX = "GEOFENCE_INDEX"
    val GEOFENCE_EXPIRATION_IN_MILLISECONDS: Long = TimeUnit.HOURS.toMillis(1)

}