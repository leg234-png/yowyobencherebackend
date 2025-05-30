package com.yowyob.dev.dto.responseDTO;



import java.time.LocalDate;

import java.util.UUID;

public class AgenceDTO {

    private UUID id;

    private String shortName;

    private String longName;

    private String description;

    private String location;


    private boolean transferable;

//    @ElementCollection
//    private List<String> images = new ArrayList<>();

    private String greetingMessage;

    private LocalDate registrationDate;

    private double averageRevenue;

    private double capitalShare;

    private String registrationNumber;

    private String socialNetwork;

    private String taxNumber;
}
