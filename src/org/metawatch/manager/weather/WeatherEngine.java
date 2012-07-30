package org.metawatch.manager.weather;

import android.content.Context;

/**
 * Weather provider interface for different weather APIs (e.g. Wunderweather,
 * Google, etc.)
 */
public interface WeatherEngine {

	WeatherData checkAndPerformUpdateIfRequired(Context context,
			WeatherData data);

}
