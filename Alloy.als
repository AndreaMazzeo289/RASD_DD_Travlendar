open util/integer

//--------MODEL:-------//

sig Name {}

sig Place {
	latitude: one Int,
	longitude: one Int,
	name: one Name,
	address: one Name
} {
	latitude >0
	longitude >0
}

sig Appointment {
	place: one Place,
	date: one Int,
	time: Int,
	duration: lone Int,
	alert: lone Alert,
	travel: one Travel
} {
	date >0
	time >0
	alert.appointment = this
	travel.placeOfArrival = place
}

sig Alert {
	appointment: one Appointment,
	date: one Int,
	time: one Int
} {
	date >0
	time >0
	appointment.alert = this
}

sig Calendar {
	appointments: some Appointment
}

sig User {
	calendar: one Calendar,
	email: one Name,
	password: one Name
}

sig Movement {
	placeOfDeparture: one Place,
	placeOfArrival: one Place,
	extimatedTime: one Int,
} {
	placeOfDeparture != placeOfArrival
	extimatedTime > 0
}

sig Travel {
	placeOfDeparture: one Place,
	placeOfArrival: one Place,
	extimatedTime: one Int,
	movements: some Movement,
	alternatives: set Travel
} {
	placeOfDeparture != placeOfArrival
	extimatedTime > 0
}



//--------FACTS:-------//

//different users  have different email addresses
fact mailUnique {
	no disjoint u1, u2: User | u1.email = u2.email
}


//different users must have different calendars
fact calendarUnique {
	no disjoint u1, u2: User | u1.calendar = u2.calendar
}


//no different places with same name, address or coordinates
fact placesAreDifferent {
	no disjoint p1, p2: Place | p1.name = p2.name or
											  p1.address = p2.address or
											  (p1.latitude = p2.latitude and p1.longitude = p2.longitude)
}

//a movement cannot exist without its travel
fact noMovementWithoutTravel {
	Travel.movements = Movement
}

/*
//every travel is related to an appointment or is an alternative to a travel related to an appointment
fact noTravelWithoutAppointment {
	Travel = Travel.alternatives + Appointment.travel
}
*/


//an appointment cannot exist without its calendar
fact noAppointmentWithoutCalendar {
	Calendar.appointments = Appointment
}


//a calendar cannot exist without its user
fact noCalendarWithoutUser {
	User.calendar = Calendar
}


//the alert of an appointment cannot be scheduled after the beginning of the appointment
fact alertBeforeAppointment {
	all a: Appointment | a.date<a.alert.date or a.time < a.alert.time
}

//travel alternatives depart from and lead to the same place
fact alternativesAreEquivalent {
	all t: Travel | (all t1 : t.alternatives | t1.placeOfDeparture = t.placeOfDeparture and t1.placeOfArrival = t.placeOfArrival)
}


//a travel cant be an alternative of itself
fact alternativesDontContainThemselves {
	all t: Travel | (t not in t.alternatives)
}


//alternatives of a travel are also alternatives of the alternatives of the travel
fact alternativesAreSymmetrical {
	all t: Travel | (all t1 : t.alternatives | t1.alternatives = t.alternatives +t - t1)
}


//every travel is composed by a sequence of connected movements
fact travelIsMadeByMovements {

		//every travel starts with a movement
		all t: Travel | (one m: t.movements | m.placeOfDeparture = t.placeOfDeparture)
		//every travel ends with a movement
		all t: Travel | (one m: t.movements | m.placeOfArrival = t.placeOfArrival)
		//the ending of a movement is the beginning of a new one or the end of the travel
		all t: Travel | (all m: t.movements | m.placeOfArrival = t.placeOfArrival 
										                         or
							                                     (one m1: t.movements | m1!=m and m.placeOfArrival = m1.placeOfDeparture))
		//the beginning of a movement is the ending of an old one or the beginning of the travel
		all t: Travel | (all m: t.movements | m.placeOfDeparture = t.placeOfDeparture 
										                         or
									                             (one m1: t.movements | m1!=m and m.placeOfDeparture = m1.placeOfArrival ))
		//different movements cannot start or end at the same position
		all t: Travel | (no disjoint m1, m2: t.movements | m1.placeOfDeparture = m2.placeOfDeparture
																					    or
																					    m1.placeOfArrival = m2.placeOfArrival )
		//no close path
		all t: Travel | (no m: t.movements | m.placeOfArrival = t.placeOfDeparture)
	
}

fact travelTimeSumOfMovementsTime {
	all t: Travel | t.extimatedTime = sum (t.movements.extimatedTime)
													
}




//--------ASSERTIONS:-------//

assert numberOfMovementsPerTravel {
	all t: Travel | #t.movements = #t.movements.placeOfDeparture
}	


run {} for 4 but exactly 2 Travel, exactly 0 Appointment, exactly 2 Movement