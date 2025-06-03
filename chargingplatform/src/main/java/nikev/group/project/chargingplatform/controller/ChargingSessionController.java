package nikev.group.project.chargingplatform.controller;

public class ChargingSessionController {

    // this charging session controller is used to handle the charging session
    // live charging session but it can maybe also be used in booking to process the session when its happening
    // due to payment classes we'll have to implement further on

    // start charging session
    // stop charging session
    // get charging session
    // get all charging sessions
    // get charging session by id
    // get charging session by user id
    // get charging session by station id
    // could add other ones

    // in the session we will simulate the charging process, by randomizing an initial battery level
    // and then we'll update the battery level every second
    // we'll also update the charging speed every second
    // we'll also update the cost every second
    // we'll also update the time remaining every second
    // we'll also update the status every second
    // meanwhile the cost and money being extracted from the user wont be simulated and we need to add
    // a wallet class to simulate the wallet balance of the user, and implement it
    // with payment services

    // the operator should have access to the history of charging sessions and the context
    // the session itself should know if its being used or not
    // we should have the station controller session and the charging session controller session


    // we have booking and we need to make the live charging session in booking as well
    // when we start a live session, to not be creating new classes what we're gonna do is, simulate an amount of battery randomly in our car
    // this can be done in the frontend and then after this when we start a live session
    // we're actually creating a booking that in theory starts when its created, and ends based on the time
    // it would take to charge thebattery percentage thats left in our car by taking into account
    // the chargin sdpeed of the session
    // also it should always give the possibility to cance th elive session, which wouldnt cancel the bookiing
    // ut would just change the end time of the booking to the current time
    // this should be able to still save an history of bookings, and lvie sessions, we just wont be able
    // to make a dinstinguishment in between them

    // problems will be deducing money
    // andlso canceliling
    // and hjistory disinguishment

    // idk maybe way too many ideas become unnecessary
    
}
