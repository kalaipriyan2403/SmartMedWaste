package com.smartmedwaste.backend.entity.enums;

public enum WasteStatus {
    REGISTERED,           // logged at EOD by Attender; tracking ID + QR generated
    READY_FOR_COLLECTION, // registered and awaiting Waste Collector pickup
    COLLECTED_STORED,     // picked up by Collector, held in on-site BMW storage awaiting transport
    IN_TRANSIT,           // loaded onto vehicle, Driver transporting to disposal facility
    DISPOSED              // received & disposed at facility — terminal state
}