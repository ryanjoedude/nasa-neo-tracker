/**
 * NASA NEO Tracker and Risk Application
 * @Author Ryan J. Brady
 * @date 5/3/2026
 * 
 * This program requests JSON data form the NASA NEO (Near-Earth Object)
 * API and displays the 20 closest objects (comets and asteroids) within a user
 * defined date-range (cannot exceed 7 days due to API constraints) in a UI 
 * built with Java Swing. Objects are retrieved and stored in a min-heap, then 
 * top 20 closest objects are displayed with fields date, name, miss distance,
 * velocity, potentially hazardous, and a range for estimated diameter. A color
 * gradient is used to display objects' distances from earth, and objects classified
 * as potentially hazardous are also highlighted.
 * 
 */

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Comparator;
import java.util.PriorityQueue;

import org.json.JSONObject;
import org.json.JSONArray;

public class neoTrackerGUI {

    private static final String API_KEY = "goNwQcUrblUInl4ZIFiAYefNdTtE3ZZMJHFOHpqg";
    
    // Initialize variables for creating distance gradient
    private double minDistance = 0;
    private double maxDistance = 1;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new neoTrackerGUI().createUI());
    }
    
    /** createUI method creates the UI, including input fields for start and end dates,
     *  submit button to get API data, JTable for displaying data, renderer for color gradient
     *  and highlighting hazardous objects.
     */

    private void createUI() {
        JFrame frame = new JFrame("NASA NEO Viewer");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(1200, 800);

        JPanel panel = new JPanel();
        panel.setLayout(new GridBagLayout());
        
        panel.setBackground(new Color(220,235,250));
        
        // create and style text for UI
        
        JLabel projName = new JLabel("NASA NEO Tracker  ");
        projName.setFont(new Font("Helvetica", Font.BOLD, 20));
        projName.setHorizontalAlignment(SwingConstants.CENTER);

        JTextField startDate = new JTextField("YYYY-MM-DD", 10);
        startDate.setFont(new Font("Helvetica", Font.PLAIN, 14));
        
        JTextField endDate = new JTextField("YYYY-MM-DD", 10);
        endDate.setFont(new Font("Helvetica", Font.PLAIN, 14));
        
        JButton submit = new JButton("Submit");
        submit.setFont(new Font("Helvetica", Font.PLAIN, 14));
        submit.setBackground(Color.CYAN);
        
        JLabel range = new JLabel(" Please do not exceed a 7-day range");
        range.setFont(new Font("Helvetica", Font.PLAIN, 11));
        range.setForeground(Color.GRAY);
        
        
        
        panel.add(projName);
        panel.add(new JLabel(" Start Date "));
        panel.add(startDate);
        panel.add(new JLabel(" End Date: "));
        panel.add(endDate);
        panel.add(submit);
        panel.add(range);

        // table setup, create columns
        String[] columns = {"Date", "Object Name", "Miss Distance (miles)", "Velocity (mph)", "Potentially Hazardous?", "Estimated Diameter (ft)"};
        DefaultTableModel tableModel = new DefaultTableModel(columns, 0);
        JTable table = new JTable(tableModel);
        
        // highlight rows with attribute hazardous = true in red
        
        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
        	
        	@Override
        	public Component getTableCellRendererComponent(JTable table, Object value, boolean selected, boolean focus, int row, int column) {
        		
        		Component c = super.getTableCellRendererComponent(table, value, selected, focus, row, column);
        		
        		double distance = Double.parseDouble(table.getModel().getValueAt(row, 2).toString());
        		
        		// calculate range between furthest and closest objects
        		double range = maxDistance - minDistance;
        		if (range == 0) range = 1;
        		
        		double ratio = (distance - minDistance) / range;
        		ratio = Math.max(0, Math.min(1,  ratio));
        		
        		// use ratio to determine row color
        		int red = (int)(255 * (1-ratio));
        		int green = (int)(255 * ratio);
        		int blue = 150;
        		
        		Color gradientColor = new Color(red, green, blue);
        		c.setBackground(gradientColor);
        		
        		// use "hazardous" boolean value to highlight rows with value "true"
        		boolean hazardous = Boolean.parseBoolean(table.getModel().getValueAt(row, 4).toString());
        		
        		if (hazardous) {
        			c.setBackground(new Color(255,200,200));
        		}
        		if (selected) {
        			c.setBackground(new Color(184,207,229));
        		}
        		
        		return c;
        		
        	}
        });
        
        table.getTableHeader().setFont(new Font("Helvetica", Font.BOLD, 14));
        table.setFont(new Font("Helvetica", Font.BOLD, 13));

        JScrollPane scrollPane = new JScrollPane(table);

        frame.setLayout(new BorderLayout());
        frame.add(panel, BorderLayout.NORTH);
        frame.add(scrollPane, BorderLayout.CENTER);

        // action listener for "submit" JButton
        submit.addActionListener((ActionEvent e) -> {
            String start = startDate.getText();
            String end = endDate.getText();
            
            if (start.isEmpty() || end.isEmpty()) {
            	JOptionPane.showMessageDialog(null, "please enter both start and end dates.", "Input Error", JOptionPane.ERROR_MESSAGE);
            	return;
            }
            
            
            // clear table
            tableModel.setRowCount(0);
            getNEOData(start, end, tableModel);
        });

        frame.setVisible(true);
    }

    /**
     * getNEOData method uses and HTTP GET request to retrieve data
     * from NASA NEO API, parses JSON response, extracts the target data,
     * stores objects in a min-heap (Java PriorityQueue) sorted by miss_distance,
     * and displays the top 20 closest objects in the table.
     * 
     * @param start date YYYY-MM-DD
     * @param end date YYYY-MM-DD
     * @param tableModel to populate with data from the API
     */
    
    private void getNEOData(String start, String end, DefaultTableModel tableModel) {
        try {
            String url = "https://api.nasa.gov/neo/rest/v1/feed?start_date="
                    + start + "&end_date=" + end + "&api_key=" + API_KEY;

            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(new URI(url))
                    .GET()
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() >= 200 && response.statusCode() < 300) {

                JSONObject json = new JSONObject(response.body());
                JSONObject neoObjects = json.getJSONObject("near_earth_objects");
                
                // create min-heap comparing using missDistance
                PriorityQueue<neoObject> heap = new PriorityQueue<>(Comparator.comparingDouble(neoObject::getMissDistance));
                
                double minDist = Double.MAX_VALUE;
                double maxDist = Double.MIN_VALUE;

                for (String dateKey : neoObjects.keySet()) {
                    JSONArray neoArray = neoObjects.getJSONArray(dateKey);

                    for (int i = 0; i < neoArray.length(); i++) {
                        JSONObject neo = neoArray.getJSONObject(i);

                        String name = neo.getString("name");
                        
                        // handle edge case that data is empty
                        
                        JSONArray cadArray = neo.getJSONArray("close_approach_data");
                        if (cadArray.length() == 0) continue;
                        
                        JSONObject approachData = cadArray.getJSONObject(0);
                        
                        // extract relevant fields
                        
                        double distance = approachData.getJSONObject("miss_distance").getDouble("miles");
                        double velocity = approachData.getJSONObject("relative_velocity").getDouble("miles_per_hour");
                        JSONObject diameter = neo.getJSONObject("estimated_diameter").getJSONObject("feet");
                        double minDiameter = diameter.getDouble("estimated_diameter_min");
                        double maxDiameter = diameter.getDouble("estimated_diameter_max");
                        boolean hazardous = neo.getBoolean("is_potentially_hazardous_asteroid");
                        minDist = Math.min(minDist, distance);
                        maxDist = Math.max(maxDist, distance);
                        
                        // create neoObject with data fields and add to min-heap.
                        neoObject obj = new neoObject(name, distance, velocity, minDiameter, maxDiameter, dateKey, hazardous);
                        
                        heap.add(obj);
                    }
                }
                
                this.minDistance = minDist;
                this.maxDistance = maxDist;

                // print heap size in terminal for future debugging if necessary
                System.out.println("Heap size: " + heap.size());
                
                // add 20 objects with smallest missDistance value to table
                int cap = Math.min(20, heap.size());
                for (int i = 0; i < cap; i++) {
                	neoObject object = heap.poll();
                	
                	tableModel.addRow(new Object[] {
                			object.date, object.name, object.missDistance, object.velocity, object.hazardous, object.minDiam + " - " + object.maxDiam
                	});
                }

            } 
            
            // handle error 400, 403, and 404 for input errors, bad HTTP requests, API issues
            else if (response.statusCode() == 400 || response.statusCode() == 403 || response.statusCode() == 404) {
            	JOptionPane.showMessageDialog(null, "Error: Please check your date inputs or try different date range", "API Error", JOptionPane.ERROR_MESSAGE);
            }
            
            
            else {
                JOptionPane.showMessageDialog(null,
                        "Unexpected Error: " + response.statusCode(), "API Error: ", JOptionPane.ERROR_MESSAGE);
            }

        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(null,
                    "Exception: " + ex.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

	        		boolean hazardous = Boolean.parseBoolean(table.getModel().getValueAt(row, 4).toString());
}