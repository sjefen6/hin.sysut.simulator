package org.hikst.Crawler;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.hikst.Commons.Datatypes.Forecast;
import org.hikst.Commons.Datatypes.WeatherData;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class WeatherParser {
	
	public static WeatherData getWeatherData(String country,String county,String municipiality, String city)
	{
		WeatherData data = null;
		
		try {
			SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
			
			String urlName = "http://www.yr.no/place/"+country+"/"+county+"/"+municipiality+"/"+city+"/forecast.xml";
			System.out.println(urlName);
			URL url = new URL(urlName);
			URLConnection connection = url.openConnection();
			
			DocumentBuilderFactory xmlFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder xmlBuilder = xmlFactory.newDocumentBuilder();
			System.out.println("Opening up a stream...");
			Document document = xmlBuilder.parse(connection.getInputStream());
			document.getDocumentElement().normalize();
			
			System.out.println("Begins to collect weather-data");
			Element root = document.getDocumentElement();
			
			System.out.println("-------------------------");
			//System.out.print(document.getDocumentElement().getTagName());
			Element locationNode = (Element)root.getElementsByTagName("location").item(0);
			
			String name = getTagValue("name",locationNode);
			String type = getTagValue("type",locationNode);
			String countryName = getTagValue("country",locationNode);
			
			Element timezoneElement = (Element)locationNode.getElementsByTagName("timezone").item(0);
			
			String timezone = timezoneElement.getAttribute("id");
			
			String utc_offset_in_min = timezoneElement.getAttribute("utcoffsetMinutes");
			
			int utc_offset = Integer.parseInt(utc_offset_in_min);
			
			Element locationElement =(Element)locationNode.getElementsByTagName("location").item(0);
			
			double altitude = Double.parseDouble(locationElement.getAttribute("altitude"));
			double latitude = Double.parseDouble(locationElement.getAttribute("latitude"));
			double longitude = Double.parseDouble(locationElement.getAttribute("longitude"));
			
			String geobase = locationElement.getAttribute("geobase");
			int geobaseid = Integer.parseInt(locationElement.getAttribute("geobaseid"));
			
			Element metaElement = (Element)root.getElementsByTagName("meta").item(0);
			
			String lastUpdate = getTagValue("lastupdate",metaElement);
			lastUpdate = lastUpdate.replace("T", " ");
			
			Date lastWeatherUpdate = dateFormat.parse(lastUpdate);
			
			String nextUpdate = getTagValue("nextupdate",metaElement);
			nextUpdate = nextUpdate.replace("T"," ");
			
			Date nextWeatherUpdate = dateFormat.parse(nextUpdate);
			
			Element sunElement = (Element)root.getElementsByTagName("sun").item(0);
			
			String timeSunrise = sunElement.getAttribute("rise");
			
			timeSunrise = timeSunrise.replace("T", " ");
			
			Date sunrise = dateFormat.parse(timeSunrise);
			
			String timeSunset = sunElement.getAttribute("set");
			
			timeSunset = timeSunset.replace("T", " ");
			
			Date sunset = dateFormat.parse(timeSunset);
			
			
			Element forecastElement = (Element)root.getElementsByTagName("forecast").item(0);
			Element tabularElement = (Element)forecastElement.getElementsByTagName("tabular").item(0);
			
			NodeList tabulars = tabularElement.getChildNodes();

			data = new WeatherData(name,type,countryName,timezone,utc_offset, altitude,latitude,longitude,
					geobase, geobaseid, lastWeatherUpdate, nextWeatherUpdate, sunrise,sunset);
			
			
			for(int index = 0; index < tabulars.getLength(); index++)
			{
				Node node = tabulars.item(index);
				
				if(node.getNodeType() == Node.ELEMENT_NODE)
				{
					Element timeElement = (Element)node;
					
					String timeFrom = timeElement.getAttribute("from");
					timeFrom = timeFrom.replace("T", " ");
					
					Date forecastFrom = dateFormat.parse(timeFrom);
					
					String timeTo = timeElement.getAttribute("to");
					timeTo = timeTo.replace("T", " ");
					
					Date forecastTo = dateFormat.parse(timeTo);
					
					String period = timeElement.getAttribute("period");
					
					int forecastPeriod = Integer.parseInt(period);
					
					Element windDirectionElement = (Element)timeElement.getElementsByTagName("windDirection").item(0);
					
					String windDirectionDegree = windDirectionElement.getAttribute("deg");
					
					double forecastWindDirectionDegree = Double.parseDouble(windDirectionDegree);
					
					String windDirectionCode = windDirectionElement.getAttribute("code");
					
					Element windSpeedElement = (Element)timeElement.getElementsByTagName("windSpeed").item(0);
					
					String windSpeedMps = windSpeedElement.getAttribute("mps");
					
					double forecastWindSpeed = Double.parseDouble(windSpeedMps);
					
					String windSpeedUnit = windSpeedElement.getAttribute("name");
					
					Element temperatureElement = (Element)timeElement.getElementsByTagName("temperature").item(0);
					
					String temperatureValue = temperatureElement.getAttribute("value");
					double forecastTemperature = Double.parseDouble(temperatureValue);
					
					String temperatureUnit = temperatureElement.getAttribute("unit");
					
					Element pressureElement = (Element)timeElement.getElementsByTagName("pressure").item(0);
					
					String pressureValue = pressureElement.getAttribute("value");
					
					double forecastPressure = Double.parseDouble(pressureValue);
					
					String pressureUnit = pressureElement.getAttribute("unit");
					
					
					data.addForecast(new Forecast(forecastFrom, forecastTo, forecastPeriod,
							forecastWindDirectionDegree, windDirectionCode, forecastWindSpeed,windSpeedUnit
							,forecastTemperature,temperatureUnit,forecastPressure, pressureUnit ));
				}
			}
			
			
			System.out.println(data);
			System.out.println(data.toJSONObject());
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return data;
	}
	
	
	
	
	private static String getTagValue(String tag, Element element)
	{
		NodeList list = element.getElementsByTagName(tag).item(0).getChildNodes();
	
		Node value = (Node)list.item(0);
		return value.getNodeValue();
	}
}
