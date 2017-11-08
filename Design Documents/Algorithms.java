
/* Position class
    double x;
    double y;
*/

void computeTravel(Appointment appointment) {
    Position destination = appointment.destination;
    Position origin = appointment.origin;
    
    string URL = "http://maps.googleapis.com/maps/api/directions/json?origin=origin.x,origin.y&destination=destination.x,destination.y&key=API_KEY";
}

void Deserialize


public boolean googleServiceAvailable() {
    GoogleApiAvailability googleApiAvailability = GoogleApiAvailability.getInstance();
    int availabilty = googleApiAvailability.isGooglePlayServicesAvailable(this);
    if(availability == ConnectionResult.SUCCESS) 
        return TRUE;
    else
        if(googleApiAvailability.isUserResovableError(availability)){
            Dialog dialog = googleApiAvailability.getErrorDialog(this, availability, 0);
            dialog.show();
        }
        else
            Toast.makeText(this, "Can't connect to play services", Toast.LENGTH_LONG),show();
    return FALSE;
}

private void initMap() {
    MapFragment mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.map);
    mapFragment.getMapAsync(this);
}

private onMapReady(GoogleMap map) {
    GoogleMap googleMap = map;
}

private void goToLocation(double lat, double lng, flaot zoom) {
    LatLng latLng = new LatLng(lat,lng);
    CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng,zoom);
    googleMap.moveCamera(cameraUpdate);
}

private String readLocation(id ) {
    
    EditText editText = (EditText) findViewById(R.id.where);
    String location = editText.getText().toString();
    
    Geocoder geocoder = new Geocoder(this);
    List<Address> list = geocoder.getFromLocationName(location,1);
    Address address = list.get(0);
    String locality = address.getLocality();
    
    double lat = address.getLatitude();
    double lng = address.getLongitude();
    
   
}

private void setMarker(String locality, double lat, double lng) {
    MarkerOptions markerOptions = new MarkerOptions().title(locality).position(new LatLng(lat,lng));
    googleMap.addMarker(markerOptions);
}


//Location user
googleApiClient = new GoogleApiClient.Builder(this).addApi(LocationServices.API).addConnectionCallbacks(this).build();
googleApiClient.connect();




//--------------------------------------------------------//
// PER DISEGNARE STRADA SULLA MAPPA
//--------------------------------------------------------//
public void displayDirection(String[] directionList) {
    int cont = directionList.length;
    for(int i=0; i<cont; i++) {
        PolylineOptions options = new PolylineOptions();
        options.color(Color.RED);
        options.width(10);
        options.addAll(PolyUtil.decode(directionsList[i]));
        
        map.addPolyline(options);
    }
}

//--------------------------------------------------------//
// CALCOLARE TEMPO TRA DUE PUNTI
//--------------------------------------------------------//
public float ComputeTime(float estimatedSpeed) {
    
    Address origin = readLocation("starting");
    Address destination = readLocation("where");
    
    Location originLoc = new Location("origin");
    originLoc.setLatitude(origin.getLatitude());
    originLoc.setLongitude(origin.getLongitude());
    
    Location destinationLoc = new Location("destination");
    destinationLoc.setLatitude(destination.getLatitude());
    destinationLoc.setLongitude(destination.getLongitude());
    
    float distance = originLoc.distanceTo(destinationLoc)/1000;
    
    return distance/estimatedSpeed;
}

private Address readLocation(String editTextId) {
    
    EditText editText = (EditText) findViewById(R.id.editTextId);
    String location = editText.getText().toString();
    
    Geocoder geocoder = new Geocoder(this);
    List<Address> list = geocoder.getFromLocationName(location,1);
    Address address = list.get(0);
    
    return address; 
}

//--------------------------------------------------------//
// CREATE APPOINTMENT
//--------------------------------------------------------//
/* Appointment class:
        Location departure;
        Location destination;
        String name;
        Date date;
        Time beginTime;
        Time departureTime;
        Time duration;
        Time arrivalTime;
        Travel travel;
*/

public Appointment(String name, Date date, Time beginTime, Location departure, Location destination, Time departureTime, Float duration,
                  Time arrivalTime, Preferences preferences) {
    this.name = name;
    this.date = date;
    this.beginTime = beginTime;
    this.departure = departure;
    this.destination = destination;
    this.departureTime = departureTime;
    this.duration = duration;
    this.arrivalTime = arrivalTime; 
    this.travel = new Travel (departure, destination, preferences.travelMode) // <-- pay attention
}

//--------------------------------------------------------//
// COMPUTE TRAVEL
//--------------------------------------------------------//
/* Travel class:
        Location start;
        Location destination;
        String travelMode;
        Float speed;
        Float distance; kilometers
        Float estimatedTime;
        Weather weatherCondition;
*/

public Travel(Location start, Location destination, Time startAt, String travelMode){
    this.start = start;
    this.destination = destination;
    this.startAt = startAt;
    this.travelMode = travelMode;
    this.speed = computeSpeed(travelMode); // <-- pay attention
    this.distance = computeDistance(start,destination);
    this.estimatedTime = (this.distance/1000)/this.speed;
    this.weatherCondition = new Weather(start,startAt);
}

public float computeDistance(Location start, Location destination) { 
    return start.distanceTo(destination);
}

//--------------------------------------------------------//
// COMPUTE DAILY SCHEDULE
//--------------------------------------------------------//

public void viewDailySchedule(Calendar calendar, DateTime day) {
    
    URL feedURL = new URL("https://www.google.com/calendar/feeds/default/private/full");
    
    CalendarService myService = new CalendarService("travlendarService");
    CalendarQuery myQuery = new CalendarQuery(feedUrl);
    myService.setUserCredentials(p.getgUser(), p.getgPassword());
    
    myQuery.setMinimumStartTime(day);
    myQuery.setMaximumStartTime(day);

    CalendarEventFeed resultFeed = myService.query(myQuery, CalendarEventFeed.class);
    
    for(int i=0; i<resultFeed.getEntries().size(); i++){
        Appointment appointment = (Appointment) calendarData.getEntries().get(i); //<-- suppose that appointment extends CalendarEventEntry 
        appointment.travel = new Travel (appointment.departure, appointment.destination, preferences.travelMode);
    }
}

//--------------------------------------------------------//
// CHECK OVERLAP
//--------------------------------------------------------//

public boolean checkOverlap(Appointment app1, Appointment app2) {
    if(app1.beginTime < app2.beginTime)  //appointment 1 is before 2
        if(app2.beginTime > app1.beginTime+app1.duration)
            return false;   // no overlap
    else
        if(app1.beginTime > app2.beginTime+app2.duration)
            return false;   // no overlap
    return true;
}

//--------------------------------------------------------//
// CHECK UNREACHABLE
//--------------------------------------------------------//

public boolean checkUnreachability(Appointment appointment){
    if(appointment.arrivalTime > appointment.departureTime+appointment.travel.estimatedTime)
        return false;
    return true;
}