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

https://maps.googleapis.com/maps/api/directions/json?origin=Brooklyn&destination=Queens&mode=transit

enum TravelMode { DRIVING, WALKING, BICYCLING, TRANSIT; }

enum TravelAlternatives { FASTER, CHEAPER, GREEN, ONLYOWNCAR, ONLYPUBLICTRANSPORT; }
	
public Travel checkTravelAlternative (Travel travel, TravelAlternatives travelAlternatives) {
	
	TravelMode[] travelMode;
	
	if(travelAlternative.equals(TravelAlternatives.ONLYOWNCAR))
		travelMode = new TravelMode[]{TravelMode.DRIVING};
	
	if(travelAlternative.equals(TravelAlternatives.ONLYPUBLICTRANSPORT))
		travelMode = new TravelMode[]{TravelMode.TRANSIT};
	
	if(travelAlternative.equals(TravelAlternatives.GREEN))
		travelMode = new TravelMode[]{TravelMode.WALKING, TravelMode.BICYCLING};
	
	if(travelAlternative.equals(TravelAlternatives.FASTER) || travelAlternative.equals(TravelAlternatives.CHEAPER))
		travelMode = new TravelMode[]{TravelMode.DRIVING, TravelMode.WALKING, TravelMode.BICYCLING, TravelMode.TRANSIT};
	
	ArrayList<JSONObject> travelResponseJSON = mapsQuery(travel.start.toString(), travel.destination, travelMode);
	ArrayList<Travel> computedTravels = travelParser(travelResponseJSON);
	
	if(!computedTravels.isEmpty())
		switch(travelAlternatives) {
			case FASTER: 
				Travel fasterTravel = computedTravels.get(0);
				float fasterTime = fasterTravel.estimatedTime;
				for(Travel travel: computedTravels)
					if(travel.estimatedTime < fasterTime)
						fasterTravel = travel;
				return fasterTravel;
			case CHEAPER: 
				Travel cheaperTravel = computedTravels.get(0);
				float cheaperCost = computeTravelCost(cheaperTravel);
				for(Travel travel: computedTravels)
					if(computeTravelCost(travel) < cheaperCost)
						cheaperTravel = travel;
				return cheaperTravel;
			// only one travel alternative
			case GREEN:
			case ONLYOWNCAR:
			case ONLYPUBLICTRANSPORT:
				return computedTravels.get(0);
		}
}
	
enum MovementMean { CAR, WALK, PUBLICTRANSPORT, BIKE, CARSHARING, BIKESHARING; }
	
public Movement checkMovementAlternative (Movement movement, MovementMean movementMean) {
	
	
	
	
	
	
	
	
	
	
	
}	
	
checkMovement