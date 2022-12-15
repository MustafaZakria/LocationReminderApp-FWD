package com.udacity.project4.locationreminders.savereminder

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.app.PendingIntent.FLAG_MUTABLE
import android.content.Context
import android.content.Intent
import android.content.IntentSender
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.material.snackbar.Snackbar
import com.udacity.project4.R
import com.udacity.project4.base.BaseFragment
import com.udacity.project4.base.NavigationCommand
import com.udacity.project4.databinding.FragmentSaveReminderBinding
import com.udacity.project4.locationreminders.geofence.GeofenceBroadcastReceiver
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem
import com.udacity.project4.utils.Constants.ACTION_GEOFENCE_EVENT
import com.udacity.project4.utils.Constants.GEOFENCE_EXPIRATION_IN_MILLISECONDS
import com.udacity.project4.utils.Constants.GEOFENCE_RADIUS_IN_METERS
import com.udacity.project4.utils.Constants.REQUEST_TURN_DEVICE_LOCATION_ON
import com.udacity.project4.utils.Constants.TAG
import com.udacity.project4.utils.Utils.foregroundAndBackgroundLocationPermissionApproved
import com.udacity.project4.utils.setDisplayHomeAsUpEnabled
import org.koin.android.ext.android.inject

@SuppressLint("UnspecifiedImmutableFlag")
class SaveReminderFragment : BaseFragment() {
    //Get the view model this time as a single to be shared with the another fragment
    override val _viewModel: SaveReminderViewModel by inject()
    private lateinit var binding: FragmentSaveReminderBinding
    private lateinit var geofencingClient: GeofencingClient

    private val geofencePendingIntent: PendingIntent by lazy {
        val intent = Intent(activity, GeofenceBroadcastReceiver::class.java)
        intent.action = ACTION_GEOFENCE_EVENT

        PendingIntent.getBroadcast(activity, 0, intent, FLAG_MUTABLE)
    }

    private lateinit var currentReminder: ReminderDataItem

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_save_reminder, container, false)

        setDisplayHomeAsUpEnabled(true)

        binding.viewModel = _viewModel

        geofencingClient = LocationServices.getGeofencingClient(requireActivity())

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.lifecycleOwner = this
        binding.selectLocation.setOnClickListener {
            //            Navigate to another fragment to get the user location
            _viewModel.navigationCommand.value =
                NavigationCommand.To(SaveReminderFragmentDirections.actionSaveReminderFragmentToSelectLocationFragment())
        }

        binding.saveReminder.setOnClickListener {
            val title = _viewModel.reminderTitle.value
            val description = _viewModel.reminderDescription.value
            val location = _viewModel.reminderSelectedLocationStr.value
            val latitude = _viewModel.latitude.value
            val longitude = _viewModel.longitude.value


            val reminder = ReminderDataItem(title, description, location, latitude, longitude)
//            : use the user entered reminder details to:
//             1) add a geofencing request
            currentReminder = reminder
            checkPermissionsAndStartGeofencing()
//             2) save the reminder to the local db
            _viewModel.validateAndSaveReminder(reminder)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _viewModel.onClear()
        removeGeofences()
    }


    //------------------------------------ENABLE DEVICE LOCATION-------------------------------------

    private fun checkPermissionsAndStartGeofencing() {
        if (foregroundAndBackgroundLocationPermissionApproved(requireContext())) {
            checkDeviceLocationSettingsAndStartGeofence()
        } else {
//            requestForegroundAndBackgroundLocationPermissions()
        }
    }


    private fun checkDeviceLocationSettingsAndStartGeofence(resolve: Boolean = true) {
        val locationRequest = LocationRequest.create().apply {
            priority = LocationRequest.PRIORITY_LOW_POWER
        }
        val builder = LocationSettingsRequest.Builder().addLocationRequest(locationRequest)

        val settingsClient = LocationServices.getSettingsClient(requireActivity())
        val locationSettingsResponseTask =
            settingsClient.checkLocationSettings(builder.build())

        locationSettingsResponseTask.addOnFailureListener { exception ->
            if (exception is ResolvableApiException && resolve) {
                try {
                    exception.startResolutionForResult(
                        requireActivity(),
                        REQUEST_TURN_DEVICE_LOCATION_ON
                    )
                } catch (sendEx: IntentSender.SendIntentException) {
                    Log.d(TAG, "Error geting location settings resolution: " + sendEx.message)
                }
            } else {
                Snackbar.make(
                    requireActivity().window.decorView.rootView,
                    R.string.location_required_error, Snackbar.LENGTH_INDEFINITE
                ).setAction(android.R.string.ok) {
                    checkDeviceLocationSettingsAndStartGeofence()
                }.show()
            }
        }
        locationSettingsResponseTask.addOnCompleteListener {
            if (it.isSuccessful) {
                addGeofenceForClue()
            }
        }
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_TURN_DEVICE_LOCATION_ON) {
            checkDeviceLocationSettingsAndStartGeofence(false)
        }
    }
    //--------------------------------------------ADD GEOFENCE--------------------------------------------

    @SuppressLint("MissingPermission")
    private fun addGeofenceForClue() {

        val geofence = Geofence.Builder()
            .setRequestId(currentReminder.id)
            .setCircularRegion(
                currentReminder.latitude!!,
                currentReminder.longitude!!,
                GEOFENCE_RADIUS_IN_METERS
            )
            .setExpirationDuration(GEOFENCE_EXPIRATION_IN_MILLISECONDS)
            .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER)
            .build()

        val geofencingRequest = GeofencingRequest.Builder()
            .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
            .addGeofence(geofence)
            .build()

        geofencingClient.removeGeofences(geofencePendingIntent).run {
            addOnCompleteListener {
                geofencingClient.addGeofences(geofencingRequest, geofencePendingIntent).run {
                    addOnSuccessListener {
                        context?.let {
                            Toast.makeText(
                                it, R.string.geofences_added,
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                        Log.d(TAG, geofence.requestId)
                    }
                    addOnFailureListener {
                        context?.let {
                            Toast.makeText(
                                it, R.string.geofences_not_added,
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                        if ((it.message != null)) {
                            Log.w(TAG, it.message.toString())
                        }
                    }
                }
            }
        }
    }


    //----------------------------------REMOVING GEOFENCE ONDESTROY ACTIVITY------------------


    private fun removeGeofences() {
        if (!foregroundAndBackgroundLocationPermissionApproved(requireContext())) {
            return
        }
        geofencingClient.removeGeofences(geofencePendingIntent).run {
            addOnSuccessListener {
                // Geofences removed
                context?.let {
                    Log.d(TAG, it.getString(R.string.geofences_removed))
                    Toast.makeText(it, R.string.geofences_removed, Toast.LENGTH_SHORT)
                        .show()
                }
            }
            addOnFailureListener {
                // Failed to remove geofences
                Log.d(TAG, getString(R.string.geofences_not_removed))
            }
        }
    }


}


