package org.metawatch.manager.weather;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import org.metawatch.manager.Idle;
import org.metawatch.manager.MetaWatch;
import org.metawatch.manager.MetaWatchService;
import org.metawatch.manager.MetaWatchService.GeolocationMode;
import org.metawatch.manager.MetaWatchService.Preferences;
import org.metawatch.manager.Monitors.LocationData;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.util.Log;

public abstract class AbstractWeatherEngine implements WeatherEngine {

	/**
	 * This is the real update method which needs to be filled with life in each
	 * engine provider implementation.
	 * 
	 * @param context
	 *            Context of the service
	 * @param data
	 *            Data object
	 * @return Data object
	 * @throws Exception
	 */
	protected abstract WeatherData update(Context context, WeatherData data)
			throws Exception;

	/**
	 * This method should be used to call from service. It will check for
	 * updates if required and update the WeatherData set.
	 * 
	 * @param context
	 *            Context of the service
	 * @param data
	 *            Data object
	 * @return Data object
	 */
	public WeatherData checkAndPerformUpdateIfRequired(Context context,
			WeatherData data) {
		// The atomic boolean is required because we are called from different
		// Threads and we don't want to request the same weather in parallel 5
		// times.
		if (data.updating.getAndSet(true) == false) {
			// We come here along just if no one else is checking and/or
			// updating right now.
			if (isUpdateRequired(data)) {
				try {
					if (Preferences.logging) {
						Log.d(MetaWatch.TAG, "Weather update start");
					}

					data = update(context, data);

					Idle.updateIdle(context, true);
					MetaWatchService.notifyClients();

					if (Preferences.logging) {
						Log.d(MetaWatch.TAG, "Weather update finished");
					}
				} catch (Exception e) {
					if (Preferences.logging) {
						Log.e(MetaWatch.TAG,
								"Weather update failed. Using old one.", e);
					}
				}
			}
			data.updating.set(false);
		}
		return data;
	}

	/**
	 * Weather update is done frequently. Update rate can be configured here.
	 * 
	 * @param data
	 *            Weather data
	 * @return True if weather data shall be updated
	 */
	private boolean isUpdateRequired(WeatherData data) {

		if (data.timeStamp > 0 && data.received) {
			long currentTime = System.currentTimeMillis();
			long diff = currentTime - data.timeStamp;
			long timeToWaitInMilliseconds = Preferences.weatherUpdateRate * 1000 * 60;

			if (diff < timeToWaitInMilliseconds) {
				if (Preferences.logging) {
					Log.d(MetaWatch.TAG,
							"Skipping weather update - updated less than "
									+ Preferences.weatherUpdateRate
									+ " minutes ago");
				}
				return false;
			}
		} else if (Preferences.weatherGeolocationMode != GeolocationMode.MANUAL && LocationData.received == false) {
			// Don't refresh the weather if the user has enabled geolocation, but we don't have a location yet
			return false;
		}

		return true;
	}
	
	protected boolean isGeolocationDataUsed() {
		return Preferences.weatherGeolocationMode != GeolocationMode.MANUAL && LocationData.received;
	}

	protected GoogleGeoCoderLocationData reverseLookupGeoLocation(Context context,
			double latitude, double longitude) throws IOException {
		GoogleGeoCoderLocationData locationData = new GoogleGeoCoderLocationData();
		try {
			Geocoder geocoder = new Geocoder(context, Locale.getDefault());
			List<Address> addresses = geocoder.getFromLocation(latitude,
					longitude, 1);

			for (Address address : addresses) {
				if (address.getPostalCode() != null) {
					String s = address.getPostalCode().trim();
					if (!s.equals(""))
						locationData.postalcode = s;
				}

				if (address.getLocality() != null) {
					String s = address.getLocality().trim();
					if (!s.equals(""))
						locationData.locality = s;
				}
			}

		} catch (IOException e) {
			if (Preferences.logging)
				Log.e(MetaWatch.TAG,
						"GeoCoder lookup failed. Using manually entered location from Preferences.",
						e);
		}
		return locationData;
	}

	class GoogleGeoCoderLocationData {
		String locality;
		String postalcode;

		public String getLocationName() {
			if (locality != null)
				return locality;
			if (postalcode != null)
				return postalcode;
			return Preferences.weatherCity;
		}
	}
}
