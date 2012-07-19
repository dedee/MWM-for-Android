package org.metawatch.manager.weather;

import android.content.Context;

public class DummyWeatherEngine extends AbstractWeatherEngine {

	@Override
	protected WeatherData update(Context context, WeatherData data)
			throws Exception {
		// Fill in some dummy values. They are not being used.
		data.ageOfMoon = 0;
		data.celsius = false;
		data.condition = "";
		data.forecast = new Forecast[0];
		data.forecastTimeStamp = System.currentTimeMillis();
		data.icon = "weather_cloudy.bmp";
		data.locationName = "NONE";
		data.moonPercentIlluminated = 0;
		data.received = true;
		data.sunriseH = 0;
		data.sunriseM = 0;
		data.sunsetH = 0;
		data.sunsetM = 0;
		data.temp = "0";
		data.timeStamp = System.currentTimeMillis();
		return data;
	}

}
