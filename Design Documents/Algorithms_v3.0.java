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
        Address departure;
        Address destination;
        String name;
        Date date;
        Time beginTime;
        Time departureTime;
        Time duration;
        Time arrivalTime;
        Travel travel;
*/

public class Appointment {
    
    String name;
    Date date;
    Time beginTime;
    Address destination;
    Address departure;
    Time departureTime;
    float arrivalTime;
    float duration;
    Travel travel;
}

public Appointment createAppointment() {
    
    Appointment appointment;

    String name = readText("name");
    Date date = formatDate.parse(readText("date"));
    Time beginTime = Time.valueOf(readText("beginTime"));
    Address destination = new Address(readText("destination"));
    Address departure = new Address(readText("departure"));
    Time departureTime = Time.valueOf(readText("departureTime"));
    float arrivalTime = Float.parseFloat(readText("arrivalTime"));
    float duration = Float.parseFloat(readText("duration"));

    appointment = new Appointment(name,date,beginTime,destination,departure,departureTime,arrivalTime,duration);
    
    return appointment;
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

/* Travel modes:
		driving
		walking
		bicycling
		transit
*/

public class Travel {

	Appointment appointment;
    Address departure;
    Address destination;
    TravelType travelType;
    Weather weatherCondition;
    ArrayList<Movement> movements;

    public float computeTravelTime();
    public float computeTravelCost();
}

public ArrayList<JSONObject> mapsQuery (String start, String destination, TravelMode[] travelMode);
public ArrayList<Travel> travelParser (ArrayList<JSONObject> travelResponseJSON);


/*
PROBLEMA COMBINAZIONI DI TRAVEL TYPE
GREEN is bike assumption (se dico sia walk che bike maps mi da due percorsi diversi, quale faccio vedere all'utente?)
*/

public Travel checkTravelAlternative (Travel travel, TravelType travelType) {
	
	Travel travelAlternative;
	
	switch(travelType) {
		case ONLYOWNCAR:
			travelAlternative = travelParser(mapsQuery(travel.departure.toString(), travel.destination.toString(), TravelMode.DRIVING, false));
			break;
		case ONLYPUBLICTRANSPORT:
			travelAlternative = travelParser(mapsQuery(travel.departure.toString(), travel.destination.toString(), TravelMode.TRANSIT, false));
			break;
		case GREEN:
			travelAlternative = travelParser(mapsQuery(travel.departure.toString(), travel.destination.toString(), TravelMode.BICYCLING, false));
			break;
		case FASTER:
		case CHEAPER:
			ArrayList<Travel> travelAlternatives;
			for(TravelMode travelMode: TravelMode.values())
				travelAlternatives.add(
			(mapsQuery(travel.departure.toString(), travel.destination.toString(), travelMode, true)));
		}

		if(travelType.equals(TravelType.FASTER)) {
			Travel fasterTravel = travelAlternatives.get(0);
			float fasterTime = fasterTravel.computeTravelTime();
			for(Travel travel: travelAlternatives)
				if(travel.computeTravelTime() < fasterTime)
					fasterTravel = travel;
			return fasterTravel;
		}
		else 
			if(travelType.equals(TravelType.CHEAPER)) {
				Travel cheaperTravel = travelAlternatives.get(0);
				float cheaperCost = cheaperTravel.computeTravelCost();
				for(Travel travel: travelAlternatives)
					if(travel.computeTravelCost()) < cheaperCost)
						cheaperTravel = travel;
				return cheaperTravel;
			}
			else
				return travelAlternative;
}

//--------------------------------------------------------//
// COMPUTE DAILY SCHEDULE
//--------------------------------------------------------//

public void viewDailySchedule(Calendar calendar, DateTime day) {
        
	ArrayList<Appointment> appointmentList = new ArrayList<Appointment>();
	Appointment appPrevious;
	Appointment appNext;
	
	appointmentList = calendarQuery(calendar, day);
	
	for(int i=0; i< appointmentList.size()-1; i++) {

		appPrevious = appointmentList.get(i);
		appNext = appointmentList.get(i+1); 

		//needed for first cycle
		if(checkUnreachability(appPrevious)) {// if unreachble
			appPrevious = manageUnreachability(appPrevious); //change parameters (and is reachable) or delete appointment
			if(!appPrevious.equals(null))
				appPrevious.travel = new Travel (appPrevious.departure, appPrevious.destination, preferences.travelMode);
		}
		else { // if reachable

			if(checkUnreachability(appNext))
				appNext = manageUnreachability(appNext); //change parameters (and is reachable) or delete appointment
			if(appNext.equals(null)) { //appNext is unreachable and is deleted
				do {
					i++;
					appNext = (Appointment) calendarData.getEntries().get(i);
					if(checkUnreachability(appNext))
						appNext = manageUnreachability(appNext);
				}while(appNext.equals(null))
			}
			
			//surely appNext is not null
			if(checkOverlap(appPrevious, appNext))
				appPrevious = manageOverlap(appPrevious,appNext); //choose which is the appointment to keep 
			else 
				appPrevious = appNext; //Save previous appointment and go on

			appPrevious.travel = new Travel (appPrevious.departure, appPrevious.destination, preferences.travelMode);
			
		}
	}
}

//--------------------------------------------------------//
// CHECK OVERLAP
//--------------------------------------------------------//

public boolean checkOverlap(Appointment app1, Appointment app2) {
    if(app1.beginTime.before(app2.beginTime))  //appointment 1 is before 2
        if(app2.beginTime.after(app1.beginTime.add(app1.duration))
            return false;   // no overlap
    else
        if(app1.beginTime.after(app2.beginTime(app2.duration))
            return false;   // no overlap
    return true;
}

//--------------------------------------------------------//
// CHECK UNREACHABLE
//--------------------------------------------------------//

public boolean checkUnreachability(Appointment appointment){
    if(appointment.arrivalTime.after(appointment.departureTime.add(appointment.travel.computeTravelTime())))
        return false;
    return true;
}