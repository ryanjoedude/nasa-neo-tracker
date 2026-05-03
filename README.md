# NASA NEO REST API Tracker for Risk and Proximity
Application using NASA NEO REST API to display Risk and Proximity - Author: Ryan J. Brady

## Overview
The NASA NEO Tracker is a Java Swing application that retrieves and displays Near-Earth Object (NEO) data from NASA’s public API. NEOs are comets and asteroids that come within 1.3 astronomical units of the sun. The program visualizes the 20 closest objects to Earth within a user-defined date range (maximum 7 days due to API constraints).

## How It Works
- User enters a start and end date (YYYY-MM-DD format)
- Program sends an HTTP GET request to NASA’s NEO API
- JSON response is parsed into neoObject instances
- Objects are stored in a PriorityQueue sorted by miss distance
- Top 20 closest objects are displayed in a JTable
- Table visually highlights:
  - Distance (color gradient)
  - Hazardous asteroids (red highlight)

## Features
- Fetches real-time NEO data from NASA Open API
- Displays results in a Java Swing GUI (JTable)
- Uses a PriorityQueue (min-heap) to sort objects by miss distance
- Shows the 20 closest objects to Earth
- Highlights potentially hazardous asteroids
- Applies a color gradient based on distance (red = close, green = far)
- Handles API and input errors (400, 403, 404)

## Technologies Used
- Java 11+
- Java Swing (GUI)
- Java HttpClient
- JSON parsing (org.json)
- PriorityQueue (min-heap)

## API Reference

NASA Near-Earth Object REST API:
https://api.nasa.gov/
An active API key is included in the source code.

## How to Run
- Download files to a folder
- Open files in and IDE of your choice (I used eclipse). Or navigate to folder in command line.
- For command line: javac -cp .;json.jar neoObject.java neoTrackerGUI.java
- Make sure to inlcude json.jar in your build path. I have included this for download, but it can also be downloaded here: https://github.com/stleary/JSON-java
- Run from the command line or in the IDE.
- Input valid dates in format: YYYY-MM-DD  (Do not exceed a range of 7 days due to NEO API constraints).
- Click "Submit"
  - NOTE: I recommend running with date range: 2015-09-07 to 2015-09-08 in order to see full capabilities.

